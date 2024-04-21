package com.yupi.springbootinit.service.impl;

import static com.anyan.apicommon.utils.JwtUtils.SECRET_KEY;
import static com.yupi.springbootinit.constant.UserConstant.USER_LOGIN_STATE;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.util.RandomUtil;
import cn.hutool.crypto.digest.DigestUtil;
import cn.hutool.json.JSONUtil;
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
import com.yupi.springbootinit.model.dto.user.UserAddRequest;
import com.yupi.springbootinit.model.dto.user.UserQueryRequest;
import com.anyan.apicommon.model.entity.User;
import com.yupi.springbootinit.model.dto.user.UserUpdateMyRequest;
import com.yupi.springbootinit.model.dto.user.UserUpdateRequest;
import com.yupi.springbootinit.model.enums.UserRoleEnum;
import com.yupi.springbootinit.model.vo.LoginUserVO;
import com.yupi.springbootinit.model.vo.UserDevKeyVO;
import com.yupi.springbootinit.model.vo.UserVO;
import com.yupi.springbootinit.service.UserService;
import com.yupi.springbootinit.utils.SqlUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;
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

    @Resource
    private StringRedisTemplate stringRedisTemplate;

    @Resource
    private Gson gson;

    @Override
    public long userRegister(String userAccount, String userPassword, String checkPassword) {
        // 1. 校验
        if (StringUtils.isAnyBlank(userAccount, userPassword, checkPassword)) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "参数为空");
        }
        if (userAccount.length() < 4) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "用户账号过短");
        }
        if (userPassword.length() < 8 || checkPassword.length() < 8) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "用户密码过短");
        }
        // 密码和校验密码相同
        if (!userPassword.equals(checkPassword)) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "两次输入的密码不一致");
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
        if (userAccount.length() < 4) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "账号错误");
        }
        if (userPassword.length() < 8) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "密码错误");
        }
        // 2. 加密
        String encryptPassword = DigestUtils.md5DigestAsHex((SALT + userPassword).getBytes());
        // 查询用户是否存在
        QueryWrapper<User> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("userAccount", userAccount);
        queryWrapper.eq("userPassword", encryptPassword);
        User user = this.baseMapper.selectOne(queryWrapper);
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

    private UserDevKeyVO genKey(String userAccount) {
        String accessKey = DigestUtil.md5Hex(SALT + userAccount + RandomUtil.randomNumbers(5));
        String secretKey = DigestUtil.md5Hex(SALT + userAccount + RandomUtil.randomNumbers(8));
        UserDevKeyVO userDevKeyVO = new UserDevKeyVO();
        userDevKeyVO.setAccessKey(accessKey);
        userDevKeyVO.setSecretKey(secretKey);
        return userDevKeyVO;
    }
}
