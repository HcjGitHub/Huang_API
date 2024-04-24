package com.yupi.springbootinit.service.impl;

import static com.anyan.apicommon.common.LeakyBucket.loginLeakyBucket;
import static com.anyan.apicommon.common.LeakyBucket.registerLeakyBucket;
import static com.anyan.apicommon.constant.RabbitmqConstant.EXCHANGE_SMS_INFORM;
import static com.anyan.apicommon.constant.RabbitmqConstant.ROUTING_KEY_SMS;
import static com.anyan.apicommon.constant.RedisConstant.CODE_LOGIN_PRE;
import static com.anyan.apicommon.constant.RedisConstant.CODE_REGISTER_PRE;
import static com.anyan.apicommon.utils.JwtUtils.SECRET_KEY;
import static com.yupi.springbootinit.constant.UserConstant.USER_LOGIN_STATE;

import cn.hutool.captcha.CaptchaUtil;
import cn.hutool.captcha.LineCaptcha;
import cn.hutool.captcha.ShearCaptcha;
import cn.hutool.captcha.generator.MathGenerator;
import cn.hutool.captcha.generator.RandomGenerator;
import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.util.RandomUtil;
import cn.hutool.crypto.digest.DigestUtil;
import cn.hutool.json.JSONUtil;
import com.anyan.apicommon.common.LeakyBucket;
import com.anyan.apicommon.common.SmsMessage;
import com.anyan.apicommon.utils.JwtUtils;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.UpdateWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.google.gson.Gson;
import com.yupi.springbootinit.common.BaseResponse;
import com.yupi.springbootinit.common.ErrorCode;
import com.yupi.springbootinit.common.ResultUtils;
import com.yupi.springbootinit.constant.CommonConstant;
import com.yupi.springbootinit.exception.BusinessException;
import com.yupi.springbootinit.exception.ThrowUtils;
import com.yupi.springbootinit.mapper.UserMapper;
import com.yupi.springbootinit.model.dto.user.*;
import com.anyan.apicommon.model.entity.User;
import com.yupi.springbootinit.model.enums.UserRoleEnum;
import com.yupi.springbootinit.model.vo.LoginUserVO;
import com.yupi.springbootinit.model.vo.UserDevKeyVO;
import com.yupi.springbootinit.model.vo.UserVO;
import com.yupi.springbootinit.service.UserService;
import com.yupi.springbootinit.utils.SqlUtils;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import javax.annotation.Resource;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jws;
import io.jsonwebtoken.Jwts;
import lombok.extern.slf4j.Slf4j;
import me.chanjar.weixin.common.bean.WxOAuth2UserInfo;
import org.apache.commons.lang3.StringUtils;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.BeanUtils;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.util.DigestUtils;

/**
 * 用户服务实现
 *
 * @author <a href="https://github.com/liyupi">程序员鱼皮</a>
 * @from <a href="https://yupi.icu">编程导航知识星球</a>
 */
@Service
@Slf4j
public class UserServiceImpl extends ServiceImpl<UserMapper, User> implements UserService {

    /**
     * 盐值，混淆密码
     */
    public static final String SALT = "huang";

    /**
     * 验证码存储到redis前缀 存储两分钟
     */
    public static final String CAPTCHA_PREFIX = "captcha:prefix:";

    //登录和注册的标识，方便切换不同的令牌桶来限制验证码发送
    private static final String LOGIN_SIGN = "login";
    private static final String REGISTER_SIGN = "register";

    //redis中存储发code的时间的key
    public static final String USER_EMAIL_CODE_LOGIN = "user:email:code:login:";
    public static final String USER_EMAIL_CODE_REGISTER = "user:email:code:register:";

    @Resource
    private StringRedisTemplate stringRedisTemplate;

    @Resource
    private RabbitTemplate rabbitTemplate;

    @Resource
    private Gson gson;

    @Override
    public long userRegister(UserRegisterRequest userRegisterRequest, String signature) {
        String userAccount = userRegisterRequest.getUserAccount();
        String userPassword = userRegisterRequest.getUserPassword();
        String checkPassword = userRegisterRequest.getCheckPassword();
        String captcha = userRegisterRequest.getCaptcha();

        // 1. 校验
        if (StringUtils.isAnyBlank(userAccount, userPassword, checkPassword, captcha, signature)) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "参数为空");
        }
        if (userAccount.length() < 3) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "用户账号过短");
        }
        if (userPassword.length() < 3 || checkPassword.length() < 3) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "用户密码过短");
        }
        // 密码和校验密码相同
        if (!userPassword.equals(checkPassword)) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "两次输入的密码不一致");
        }

        // 验证码校验
        if (captcha.length() < 4) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "验证码输入错误");
        }
        //获取redis中保存的验证码
        String code = stringRedisTemplate.opsForValue().get(CAPTCHA_PREFIX + signature);
        if (!captcha.equals(code)) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "验证码输入错误");
        }
        synchronized (userAccount.intern()) {
            // 账户不能重复
            QueryWrapper<User> queryWrapper = new QueryWrapper<>();
            queryWrapper.eq("userAccount", userAccount);
            long count = this.baseMapper.selectCount(queryWrapper);
            if (count > 0) {
                throw new BusinessException(ErrorCode.PARAMS_ERROR, "账号重复");
            }
            // 2. 加密
            String encryptPassword = DigestUtils.md5DigestAsHex((SALT + userPassword).getBytes());

            //给用户分配调用接口的公钥和私钥ak,sk，保证复杂的同时要保证唯一
            String accessKey = DigestUtil.md5Hex(SALT + userAccount + RandomUtil.randomNumbers(5));
            String secretKey = DigestUtil.md5Hex(SALT + userAccount + RandomUtil.randomNumbers(8));
            // 3. 插入数据
            User user = new User();
            user.setUserAccount(userAccount);
            //注册用户 账号与昵称一样
            user.setUserName(userAccount);
            user.setUserPassword(encryptPassword);
            user.setAccessKey(accessKey);
            user.setSecretKey(secretKey);
            boolean saveResult = this.save(user);
            if (!saveResult) {
                throw new BusinessException(ErrorCode.SYSTEM_ERROR, "注册失败，数据库错误");
            }
            return user.getId();
        }
    }

    @Override
    public LoginUserVO userLogin(String userAccount, String userPassword, HttpServletRequest request
            , HttpServletResponse response) {
        // 1. 校验
        if (StringUtils.isAnyBlank(userAccount, userPassword)) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "参数为空");
        }
        if (userAccount.length() < 3) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "账号错误");
        }
        if (userPassword.length() < 3) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "密码错误");
        }
        // 2. 加密
        String encryptPassword = DigestUtils.md5DigestAsHex((SALT + userPassword).getBytes());
        // 查询用户是否存在
        QueryWrapper<User> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("userAccount", userAccount);
        queryWrapper.eq("userPassword", encryptPassword);
        User user = this.baseMapper.selectOne(queryWrapper);
        return setLoginUser(response, user);
    }

    private LoginUserVO setLoginUser(HttpServletResponse response, User user) {
        // 用户不存在
        if (user == null) {
            log.info("user login failed, userAccount cannot match userPassword");
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "用户不存在或密码错误");
        }
        // 3. 记录用户的登录态
        //request.getSession().setAttribute(USER_LOGIN_STATE, user);
        //通过JWT+Redis实现分布式登录
        //利用自定义jwt生成token
        String token = JwtUtils.createJwtToken(user.getId(), user.getUserName());
        //将 token 保存到前端cookie
        Cookie cookie = new Cookie("token", token);
        //任何路径可访问
        cookie.setPath("/");
        response.addCookie(cookie);
        //json化user保存到redis
        String userToJson = gson.toJson(user);
        stringRedisTemplate.opsForValue().set(USER_LOGIN_STATE + user.getId(), userToJson
                , JwtUtils.EXPIRE, TimeUnit.MILLISECONDS);
        return this.getLoginUserVO(user);
    }

    @Override
    public LoginUserVO userLoginByMpOpen(WxOAuth2UserInfo wxOAuth2UserInfo, HttpServletRequest request) {
        String unionId = wxOAuth2UserInfo.getUnionId();
        String mpOpenId = wxOAuth2UserInfo.getOpenid();
        // 单机锁
        synchronized (unionId.intern()) {
            // 查询用户是否已存在
            QueryWrapper<User> queryWrapper = new QueryWrapper<>();
            queryWrapper.eq("unionId", unionId);
            User user = this.getOne(queryWrapper);
            // 被封号，禁止登录
            if (user != null && UserRoleEnum.BAN.getValue().equals(user.getUserRole())) {
                throw new BusinessException(ErrorCode.FORBIDDEN_ERROR, "该用户已被封，禁止登录");
            }
            // 用户不存在则创建
            if (user == null) {
                user = new User();
                user.setUnionId(unionId);
                user.setMpOpenId(mpOpenId);
                user.setUserAvatar(wxOAuth2UserInfo.getHeadImgUrl());
                user.setUserName(wxOAuth2UserInfo.getNickname());
                boolean result = this.save(user);
                if (!result) {
                    throw new BusinessException(ErrorCode.SYSTEM_ERROR, "登录失败");
                }
            }
            // 记录用户的登录态
            request.getSession().setAttribute(USER_LOGIN_STATE, user);
            return getLoginUserVO(user);
        }
    }

    /**
     * 获取当前登录用户
     *
     * @param request
     * @return
     */
    @Override
    public User getLoginUser(HttpServletRequest request) {
        // 先判断是否已登录
//        Object userObj = request.getSession().getAttribute(USER_LOGIN_STATE);
        Long userId = JwtUtils.parserUserIdByToken(request);
        if (userId == null) {
            throw new BusinessException(ErrorCode.NOT_LOGIN_ERROR);
        }

        //获取redis中User,防止该用户被删除
        String userJson = stringRedisTemplate.opsForValue().get(USER_LOGIN_STATE + userId);
        User currentUser = gson.fromJson(userJson, User.class);
        if (currentUser == null) {
            throw new BusinessException(ErrorCode.NOT_LOGIN_ERROR);
        }
        return currentUser;
    }

    /**
     * 是否为管理员
     *
     * @param request
     * @return
     */
    @Override
    public boolean isAdmin(HttpServletRequest request) {
        // 仅管理员可查询
        Object userObj = request.getSession().getAttribute(USER_LOGIN_STATE);
        User user = (User) userObj;
        return isAdmin(user);
    }

    @Override
    public boolean isAdmin(User user) {
        return user != null && UserRoleEnum.ADMIN.getValue().equals(user.getUserRole());
    }

    /**
     * 用户注销
     *
     * @param request
     * @param response
     */
    @Override
    public boolean userLogout(HttpServletRequest request, HttpServletResponse response) {
        Cookie[] cookies = request.getCookies();
        if (cookies.length == 0) {
            throw new BusinessException(ErrorCode.OPERATION_ERROR, "未登录");
        }
        for (Cookie cookie : cookies) {
            if (cookie.getName().equals("token")) {
                String token = cookie.getValue();
                if (!StringUtils.isEmpty(token)) {
                    try {
                        Jws<Claims> claimsJws = Jwts.parser().setSigningKey(SECRET_KEY).parseClaimsJws(token);
                        Claims claims = claimsJws.getBody();
                        long userId = Long.parseLong(claims.get("id").toString());
                        // 移除登录态
                        stringRedisTemplate.delete(USER_LOGIN_STATE + userId);
                        Cookie coo = new Cookie(cookie.getName(), token);
                        coo.setMaxAge(0);
                        response.addCookie(coo);
                        return true;
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            }
        }
        throw new BusinessException(ErrorCode.OPERATION_ERROR, "未登录");
    }

    @Override
    public LoginUserVO getLoginUserVO(User user) {
        if (user == null) {
            return null;
        }
        LoginUserVO loginUserVO = new LoginUserVO();
        BeanUtils.copyProperties(user, loginUserVO);
        return loginUserVO;
    }

    @Override
    public UserVO getUserVO(User user) {
        if (user == null) {
            return null;
        }
        UserVO userVO = new UserVO();
        BeanUtils.copyProperties(user, userVO);
        return userVO;
    }

    @Override
    public List<UserVO> getUserVO(List<User> userList) {
        if (CollUtil.isEmpty(userList)) {
            return new ArrayList<>();
        }
        return userList.stream().map(this::getUserVO).collect(Collectors.toList());
    }

    @Override
    public QueryWrapper<User> getQueryWrapper(UserQueryRequest userQueryRequest) {
        if (userQueryRequest == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "请求参数为空");
        }
        Long id = userQueryRequest.getId();
        String unionId = userQueryRequest.getUnionId();
        String mpOpenId = userQueryRequest.getMpOpenId();
        String userName = userQueryRequest.getUserName();
        Integer gender = userQueryRequest.getGender();
        String userProfile = userQueryRequest.getUserProfile();
        String userRole = userQueryRequest.getUserRole();
        String sortField = userQueryRequest.getSortField();
        String sortOrder = userQueryRequest.getSortOrder();
        QueryWrapper<User> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq(id != null, "id", id);
        queryWrapper.eq(StringUtils.isNotBlank(unionId), "unionId", unionId);
        queryWrapper.eq(StringUtils.isNotBlank(mpOpenId), "mpOpenId", mpOpenId);
        queryWrapper.eq(StringUtils.isNotBlank(userRole), "userRole", userRole);
        queryWrapper.eq(StringUtils.isNotBlank(userRole), "gender", gender);
        queryWrapper.like(StringUtils.isNotBlank(userProfile), "userProfile", userProfile);
        queryWrapper.like(StringUtils.isNotBlank(userName), "userName", userName);
        queryWrapper.orderBy(SqlUtils.validSortField(sortField), sortOrder.equals(CommonConstant.SORT_ORDER_ASC),
                sortField);
        return queryWrapper;
    }

    @Override
    public UserDevKeyVO genKey(HttpServletRequest request) {
        User loginUser = getLoginUser(request);
        if (loginUser == null) {
            throw new BusinessException(ErrorCode.NOT_LOGIN_ERROR);
        }
        UserDevKeyVO userDevKeyVO = genKey(loginUser.getUserAccount());
        //更新ak/sk
        UpdateWrapper<User> updateWrapper = new UpdateWrapper<>();
        updateWrapper.eq("id", loginUser.getId());
        updateWrapper.eq("userAccount", loginUser.getUserAccount());
        updateWrapper.set("accessKey", userDevKeyVO.getAccessKey());
        updateWrapper.set("secretKey", userDevKeyVO.getSecretKey());
        this.update(updateWrapper);

        //更新缓存信息
        loginUser.setAccessKey(userDevKeyVO.getAccessKey());
        loginUser.setSecretKey(userDevKeyVO.getSecretKey());
        String userToJson = gson.toJson(loginUser);
        stringRedisTemplate.opsForValue().set(USER_LOGIN_STATE + loginUser.getId(), userToJson
                , JwtUtils.EXPIRE, TimeUnit.MILLISECONDS);
        return userDevKeyVO;
    }

    @Override
    public BaseResponse<Boolean> updateMyUser(UserUpdateMyRequest userUpdateMyRequest, HttpServletRequest request) {
        if (userUpdateMyRequest == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        User loginUser = getLoginUser(request);
        if (loginUser == null) {
            throw new BusinessException(ErrorCode.NOT_LOGIN_ERROR);
        }
        User user = new User();
        BeanUtils.copyProperties(userUpdateMyRequest, user);
        user.setId(loginUser.getId());
        boolean result = this.updateById(user);
        ThrowUtils.throwIf(!result, ErrorCode.OPERATION_ERROR);

        //更新缓存
        loginUser.setUserName(userUpdateMyRequest.getUserName());
        loginUser.setGender(userUpdateMyRequest.getGender());
        String toJson = gson.toJson(loginUser);
        stringRedisTemplate.opsForValue().set(USER_LOGIN_STATE + loginUser.getId(), toJson
                , JwtUtils.EXPIRE, TimeUnit.MILLISECONDS);
        return ResultUtils.success(true);
    }

    @Override
    public BaseResponse<Boolean> updateUser(UserUpdateRequest userUpdateRequest) {
        if (userUpdateRequest == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        User user = new User();
        BeanUtils.copyProperties(userUpdateRequest, user);
        boolean result = this.updateById(user);
        ThrowUtils.throwIf(!result, ErrorCode.OPERATION_ERROR);

        User newUser = this.getById(user.getId());
        String toJson = gson.toJson(newUser);
        String redisKey = USER_LOGIN_STATE + newUser.getId();
        String userFromUser = stringRedisTemplate.opsForValue().get(redisKey);
        if (!StringUtils.isBlank(userFromUser)) {
            stringRedisTemplate.opsForValue().set(redisKey, toJson
                    , JwtUtils.EXPIRE, TimeUnit.MILLISECONDS);
        }
        return ResultUtils.success(true);
    }

    @Override
    public BaseResponse<Boolean> removeUser(Long userId) {
        if (userId == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        boolean result = this.removeById(userId);

        //清除缓存数据
        stringRedisTemplate.delete(USER_LOGIN_STATE + userId);
        return ResultUtils.success(result);
    }

    @Override
    public Long addUser(UserAddRequest userAddRequest) {
        User user = new User();
        BeanUtils.copyProperties(userAddRequest, user);
        // 默认密码 12345678
        String defaultPassword = "12345678";
        String encryptPassword = DigestUtils.md5DigestAsHex((SALT + defaultPassword).getBytes());
        user.setUserPassword(encryptPassword);
        UserDevKeyVO userDevKeyVO = genKey(user.getUserAccount());
        user.setAccessKey(userDevKeyVO.getAccessKey());
        user.setSecretKey(userDevKeyVO.getSecretKey());
        boolean result = this.save(user);
        if (result) {
            return user.getId();
        }
        return null;
    }

    @Override
    public void getCaptcha(HttpServletRequest request, HttpServletResponse response) {
        String signature = request.getHeader("signature");
        if (StringUtils.isBlank(signature)) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }

        //纯数字四位验证码
        RandomGenerator randomGenerator = new RandomGenerator("0123456789", 4);
        LineCaptcha lineCaptcha = CaptchaUtil.createLineCaptcha(100, 30);
        lineCaptcha.setGenerator(randomGenerator);
        ShearCaptcha captcha = CaptchaUtil.createShearCaptcha(200, 45, 4, 4);

        try {
            //设置响应头
            response.setContentType("image/jpeg");
            response.setHeader("Pragma", "No-cache");

            //写回前端页面
            lineCaptcha.write(response.getOutputStream());

            log.info("captchaId:{}，code:{}", signature, lineCaptcha.getCode());
            // 将验证码设置到Redis中,2分钟过期
            stringRedisTemplate.opsForValue().set(CAPTCHA_PREFIX + signature, lineCaptcha.getCode(), 2, TimeUnit.MINUTES);
            response.getOutputStream().close();
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
        }
    }

    @Override
    public void sendSMSCode(String emailNum, String captchaType) {
        if (StringUtils.isBlank(captchaType)) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "验证码类型错误");
        }

        //令牌桶算法实现短信接口的限流，因为手机号码重复发送短信，要进行流量控制
        //解决同一个手机号的并发问题，锁的粒度非常小，不影响性能。只是为了防止用户第一次发送短信时的恶意调用
        synchronized (emailNum.intern()) {
            Boolean exist = stringRedisTemplate.hasKey(USER_EMAIL_CODE_REGISTER + emailNum);
            if (exist != null && exist) {
                //1.令牌桶算法对手机短信接口进行限流 具体限流规则为同一个手机号，60s只能发送一次
                Long finalTime = 0l;
                LeakyBucket leakyBucket = null;
                if (REGISTER_SIGN.equals(captchaType)) {
                    String strFinalTime = stringRedisTemplate.opsForValue().get(USER_EMAIL_CODE_REGISTER + emailNum);
                    finalTime = Long.parseLong(strFinalTime);
                    leakyBucket = registerLeakyBucket;
                }
                if (LOGIN_SIGN.equals(captchaType)) {
                    String strFinalTime = stringRedisTemplate.opsForValue().get(USER_EMAIL_CODE_LOGIN + emailNum);
                    finalTime = Long.parseLong(strFinalTime);
                    leakyBucket = loginLeakyBucket;
                }

                if (!leakyBucket.control(finalTime)) {
                    log.info("邮箱{}请求验证码太频繁了", emailNum);
                    throw new BusinessException(ErrorCode.PARAMS_ERROR, "请求邮箱太频繁了");
                }
            }

            //2.符合限流规则则生成验证码
            String code = RandomUtil.randomNumbers(4);
            //将code存储到redis
            String captchaRedisKey = CODE_REGISTER_PRE;
            if (LOGIN_SIGN.equals(captchaType)) {
                captchaRedisKey = CODE_LOGIN_PRE;
            }
            stringRedisTemplate.opsForValue().set(captchaRedisKey + emailNum, code, 5, TimeUnit.MINUTES);

            //3.通过消息队列发送，提供并发量
            SmsMessage smsMessage = new SmsMessage(emailNum, code);
            rabbitTemplate.convertAndSend(EXCHANGE_SMS_INFORM, ROUTING_KEY_SMS, smsMessage);

            //4.更新发送短信时间
            if (REGISTER_SIGN.equals(captchaType)) {
                stringRedisTemplate.opsForValue().set(USER_EMAIL_CODE_REGISTER + emailNum,
                        "" + System.currentTimeMillis() / 1000, 5, TimeUnit.MINUTES);
            }
            if (LOGIN_SIGN.equals(captchaType)) {
                stringRedisTemplate.opsForValue().set(USER_EMAIL_CODE_LOGIN + emailNum,
                        "" + System.currentTimeMillis() / 1000, 5, TimeUnit.MINUTES);
            }
        }
    }

    @Override
    public long userEmailRegister(String email, String captcha) {
        this.verifyCaptcha(captcha, email, CODE_REGISTER_PRE);
        synchronized (email.intern()) {
            // 账户不能重复
            QueryWrapper<User> queryWrapper = new QueryWrapper<>();
            queryWrapper.eq("email", email);
            long count = this.baseMapper.selectCount(queryWrapper);
            if (count > 0) {
                throw new BusinessException(ErrorCode.PARAMS_ERROR, "账号重复");
            }

            //给用户分配调用接口的公钥和私钥ak,sk，保证复杂的同时要保证唯一
            String accessKey = DigestUtil.md5Hex(SALT + email + RandomUtil.randomNumbers(5));
            String secretKey = DigestUtil.md5Hex(SALT + email + RandomUtil.randomNumbers(8));
            // 3. 插入数据
            User user = new User();
            user.setEmail(email);
            //注册用户 账号与昵称一样
            user.setUserName(email);
            user.setAccessKey(accessKey);
            user.setSecretKey(secretKey);
            boolean saveResult = this.save(user);
            if (!saveResult) {
                throw new BusinessException(ErrorCode.SYSTEM_ERROR, "注册失败，数据库错误");
            }
            stringRedisTemplate.delete(CODE_REGISTER_PRE + email);
            return user.getId();
        }
    }

    private void verifyCaptcha(String captcha, String email, String captchaRedisKey) {
        // 1. 校验
        if (StringUtils.isAnyBlank(email, captcha)) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "参数为空");
        }
        //邮箱格式
        if (!Pattern.matches("^[a-zA-Z0-9_-]+@[a-zA-Z0-9_-]+(\\.[a-zA-Z0-9_-]+)+$", email)) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "邮箱格式错误");
        }

        // 验证码校验
        if (captcha.length() < 4) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "验证码输入错误");
        }
        //获取redis中保存的验证码
        String code = stringRedisTemplate.opsForValue().get(captchaRedisKey + email);
        if (!captcha.equals(code)) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "验证码输入错误");
        }
    }

    @Override
    public LoginUserVO userEmailLogin(String email, String captcha, HttpServletRequest request, HttpServletResponse response) {
        this.verifyCaptcha(captcha, email, CODE_LOGIN_PRE);
        // 查询用户是否存在
        QueryWrapper<User> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("email", email);
        User user = this.baseMapper.selectOne(queryWrapper);
        return setLoginUser(response, user);
    }

    private UserDevKeyVO genKey(String userAccount) {
        String accessKey = DigestUtil.md5Hex(SALT + userAccount + RandomUtil.randomNumbers(5));
        String secretKey = DigestUtil.md5Hex(SALT + userAccount + RandomUtil.randomNumbers(8));
        UserDevKeyVO userDevKeyVO = new UserDevKeyVO();
        userDevKeyVO.setAccessKey(accessKey);
        userDevKeyVO.setSecretKey(secretKey);
        return userDevKeyVO;
    }
}
