package com.gwq.cloudpicturebackend.service;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.gwq.cloudpicturebackend.model.dto.space.SpaceAddRequest;
import com.gwq.cloudpicturebackend.model.dto.space.SpaceQueryRequest;
import com.gwq.cloudpicturebackend.model.dto.spaceuser.SpaceUserAddRequest;
import com.gwq.cloudpicturebackend.model.dto.spaceuser.SpaceUserQueryRequest;
import com.gwq.cloudpicturebackend.model.entity.Space;
import com.gwq.cloudpicturebackend.model.entity.SpaceUser;
import com.baomidou.mybatisplus.extension.service.IService;
import com.gwq.cloudpicturebackend.model.entity.User;
import com.gwq.cloudpicturebackend.model.vo.SpaceUserVO;
import com.gwq.cloudpicturebackend.model.vo.SpaceVO;

import javax.servlet.http.HttpServletRequest;
import java.util.List;

/**
 * @author gong
 * @description 针对表【space_user(空间用户关联)】的数据库操作Service
 * @createDate 2026-05-08 15:45:01
 */
public interface SpaceUserService extends IService<SpaceUser> {

    //获取空间成员
    long addSpaceUser(SpaceUserAddRequest spaceUserAddRequest);

    //效应空间成员
    void validSpaceUser(SpaceUser spaceUser, boolean add);

    //获取空间成员单条
    SpaceUserVO getSpaceUserVO(SpaceUser spaceUser, HttpServletRequest request);

    //获取空间成员多条
    List<SpaceUserVO> getSpaceUserVOList(List<SpaceUser> spaceUserList);

    //获取查询对象
    QueryWrapper<SpaceUser> getQueryWrapper(SpaceUserQueryRequest spaceUserQueryRequest);

}
