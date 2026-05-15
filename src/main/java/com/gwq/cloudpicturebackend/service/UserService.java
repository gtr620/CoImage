package com.gwq.cloudpicturebackend.service;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.gwq.cloudpicturebackend.model.dto.user.UserQueryRequest;
import com.gwq.cloudpicturebackend.model.entity.User;
import com.baomidou.mybatisplus.extension.service.IService;
import com.gwq.cloudpicturebackend.model.vo.LoginUserVO;
import com.gwq.cloudpicturebackend.model.vo.UserVO;

import javax.servlet.http.HttpServletRequest;
import java.util.List;


public interface UserService extends IService<User> {
    long userRegister(String username, String password, String checkPassword);

    String getEncryptPassword(String userPassword);

    LoginUserVO userLogin(String username, String password, HttpServletRequest request);

    LoginUserVO getLoginUserVO(User user);

    UserVO getUserVO(User user);

    List<UserVO> getUserVOList(List<User> userList);

    User getLoginUser(HttpServletRequest request);

    boolean userLogout(HttpServletRequest request);

    QueryWrapper<User> getUserQueryWrapper(UserQueryRequest userQueryRequest);

    boolean isAdmin(User user);

}
