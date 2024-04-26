package com.yupi.springbootinit.service;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.IService;
import com.yupi.springbootinit.common.BaseResponse;
import com.yupi.springbootinit.model.dto.user.*;
import com.anyan.apicommon.model.entity.User;
import com.yupi.springbootinit.model.vo.LoginUserVO;
import com.yupi.springbootinit.model.vo.UserDevKeyVO;
import com.yupi.springbootinit.model.vo.UserVO;

import java.util.List;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import me.chanjar.weixin.common.bean.WxOAuth2UserInfo;

/**
 * 用户服务
 *
 * @author <a href="https://github.com/liyupi">程序员鱼皮</a>
 * @from <a href="https://yupi.icu">编程导航知识星球</a>
 */
public interface UserService extends IService<User> {

    /**
     * 用户注册
     *
     * @param userRegisterRequest 用户注册请求体
     * @param signature           验证码唯一标识
     * @return 新用户 id
     */
    long userRegister(UserRegisterRequest userRegisterRequest, String signature);

    /**
     * 用户登录
     *
     * @param userAccount  用户账户
     * @param userPassword 用户密码
     * @param request
     * @param response
     * @return 脱敏后的用户信息
     */
    LoginUserVO userLogin(String userAccount, String userPassword, HttpServletRequest request, HttpServletResponse response);

    /**
     * 用户登录（微信开放平台）
     *
     * @param wxOAuth2UserInfo 从微信获取的用户信息
     * @param request
     * @return 脱敏后的用户信息
     */
    LoginUserVO userLoginByMpOpen(WxOAuth2UserInfo wxOAuth2UserInfo, HttpServletRequest request);

    /**
     * 获取当前登录用户
     *
     * @param request
     * @return
     */
    User getLoginUser(HttpServletRequest request);

    /**
     * 是否为管理员
     *
     * @param request
     * @return
     */
    boolean isAdmin(HttpServletRequest request);

    /**
     * 是否为管理员
     *
     * @param user
     * @return
     */
    boolean isAdmin(User user);

    /**
     * 用户注销
     *
     * @param request
     * @param response
     * @return
     */
    boolean userLogout(HttpServletRequest request, HttpServletResponse response);

    /**
     * 获取脱敏的已登录用户信息
     *
     * @return
     */
    LoginUserVO getLoginUserVO(User user);

    /**
     * 获取脱敏的用户信息
     *
     * @param user
     * @return
     */
    UserVO getUserVO(User user);

    /**
     * 获取脱敏的用户信息
     *
     * @param userList
     * @return
     */
    List<UserVO> getUserVO(List<User> userList);

    /**
     * 获取查询条件
     *
     * @param userQueryRequest
     * @return
     */
    QueryWrapper<User> getQueryWrapper(UserQueryRequest userQueryRequest);

    /**
     * 重新生成ak/sk
     *
     * @param request
     * @return
     */
    UserDevKeyVO genKey(HttpServletRequest request);

    /**
     * 更新当前登录用户信息
     *
     * @param userUpdateMyRequest
     * @param request
     * @return
     */
    BaseResponse<Boolean> updateMyUser(UserUpdateMyRequest userUpdateMyRequest, HttpServletRequest request);

    /**
     * 更新用户数据
     *
     * @param userUpdateRequest
     * @return
     */
    BaseResponse<Boolean> updateUser(UserUpdateRequest userUpdateRequest);

    /**
     * 删除用户
     *
     * @param userId 删除用户id
     * @return
     */
    BaseResponse<Boolean> removeUser(Long userId);

    /**
     * 添加用户
     *
     * @param userAddRequest
     * @return 用户id
     */
    Long addUser(UserAddRequest userAddRequest);

    /**
     * 获取验证码
     *
     * @param request
     * @param response
     */
    void getCaptcha(HttpServletRequest request, HttpServletResponse response);

    /**
     * 发送邮箱注册登录验证码
     *
     * @param emailNum    邮箱号码
     * @param captchaType 验证码类型 login:登录 register:注册
     */
    void sendSMSCode(String emailNum, String captchaType);

    /**
     * 用户邮箱注册
     *
     * @param email   邮箱
     * @param captcha 验证码
     * @return
     */
    long userEmailRegister(String email, String captcha);

    /**
     * 使用邮箱方式进行登录
     *
     * @param email    邮箱
     * @param captcha  验证码
     * @param request
     * @param response
     * @return
     */
    LoginUserVO userEmailLogin(String email, String captcha, HttpServletRequest request, HttpServletResponse response);

    /**
     * 更新用户头像
     *
     * @param url
     * @param request
     * @return
     */
    Boolean updateAvatar(String url, HttpServletRequest request);
}
