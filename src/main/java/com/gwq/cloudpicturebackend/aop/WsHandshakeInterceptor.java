package com.gwq.cloudpicturebackend.aop;

import cn.hutool.core.util.StrUtil;
import com.gwq.cloudpicturebackend.manager.auth.SpaceUserAuthManager;
import com.gwq.cloudpicturebackend.manager.auth.model.SpaceUserPermission;
import com.gwq.cloudpicturebackend.manager.auth.model.SpaceUserPermissionConstant;
import com.gwq.cloudpicturebackend.model.entity.Picture;
import com.gwq.cloudpicturebackend.model.entity.Space;
import com.gwq.cloudpicturebackend.model.entity.User;
import com.gwq.cloudpicturebackend.model.enums.SpaceRoleEnum;
import com.gwq.cloudpicturebackend.model.enums.SpaceTypeEnum;
import com.gwq.cloudpicturebackend.service.PictureService;
import com.gwq.cloudpicturebackend.service.SpaceService;
import com.gwq.cloudpicturebackend.service.SpaceUserService;
import com.gwq.cloudpicturebackend.service.UserService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.server.ServerHttpRequest;
import org.springframework.http.server.ServerHttpResponse;
import org.springframework.http.server.ServletServerHttpRequest;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.WebSocketHandler;
import org.springframework.web.socket.server.HandshakeInterceptor;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import java.util.List;
import java.util.Map;
import java.util.Objects;

@Slf4j
@Component
public class WsHandshakeInterceptor implements HandshakeInterceptor {

    @Resource
    private UserService userService;

    @Resource
    private PictureService pictureService;

    @Resource
    private SpaceService spaceService;

    @Resource
    private SpaceUserAuthManager spaceUserAuthManager;

    @Override
    public boolean beforeHandshake(ServerHttpRequest request, ServerHttpResponse response, WebSocketHandler wsHandler, Map<String, Object> attributes) throws Exception {
        if (request instanceof ServletServerHttpRequest) {
            HttpServletRequest servletRequest = ((ServletServerHttpRequest) request).getServletRequest();
            String pictureId = servletRequest.getParameter("pictureId");
            if (StrUtil.isBlank(pictureId)) {
                log.error("picture id is null");
                return false;
            }
            User loginUser = userService.getLoginUser(servletRequest);
            if (Objects.isNull(loginUser)) {
                log.error("loginUser is null");
                return false;
            }
            Picture picture = pictureService.getById(pictureId);
            if (Objects.isNull(picture)) {
                log.error("picture is null");
                return false;
            }
            Long spaceId = picture.getSpaceId();
            Space space = null;
            if (spaceId != null) {
                space = spaceService.getById(spaceId);
                if (Objects.isNull(space)) {
                    log.error("space is null");
                    return false;
                }
                if (SpaceTypeEnum.TEAM.getValue() != space.getSpaceType()) {
                    log.error("space type is null");
                    return false;
                }
            }
            List<String> permissionList = spaceUserAuthManager.getPermissionList(space, loginUser);
            if (!permissionList.contains(SpaceUserPermissionConstant.PICTURE_EDIT)) {
                return false;
            }
            attributes.put("user", loginUser);
            attributes.put("userId", loginUser.getId());
            attributes.put("pictureId", Long.valueOf(pictureId));
            return true;

        }


        return false;
    }

    @Override
    public void afterHandshake(ServerHttpRequest request, ServerHttpResponse response, WebSocketHandler wsHandler, Exception exception) {

    }
}
