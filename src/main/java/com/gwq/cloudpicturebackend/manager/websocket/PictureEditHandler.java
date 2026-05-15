package com.gwq.cloudpicturebackend.manager.websocket;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.json.JSONUtil;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.module.SimpleModule;
import com.fasterxml.jackson.databind.ser.std.ToStringSerializer;
import com.gwq.cloudpicturebackend.manager.websocket.disruptor.PictureEditEventProducer;
import com.gwq.cloudpicturebackend.manager.websocket.model.PictureEditMessageTypeEnum;
import com.gwq.cloudpicturebackend.manager.websocket.model.PictureEditRequestMessage;
import com.gwq.cloudpicturebackend.manager.websocket.model.PictureEditResponseMessage;
import com.gwq.cloudpicturebackend.model.entity.User;
import com.gwq.cloudpicturebackend.service.PictureService;
import com.gwq.cloudpicturebackend.service.UserService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.*;
import org.springframework.web.socket.handler.TextWebSocketHandler;

import javax.annotation.Resource;
import java.io.IOException;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

@Slf4j
@Component
public class PictureEditHandler extends TextWebSocketHandler {

    @Resource
    private UserService userService;

    @Resource
    private PictureEditEventProducer pictureEditEventProducer;



    // 每张图片的编辑状态，key: pictureId, value: 当前正在编辑的用户 ID
    private final Map<Long, Long> pictureEditingUsers = new ConcurrentHashMap<>();

    // 保存所有连接的会话，key: pictureId, value: 用户会话集合
    private final Map<Long, Set<WebSocketSession>> pictureSessions = new ConcurrentHashMap<>();


    @Override
    public void afterConnectionEstablished(WebSocketSession session) throws Exception {
        super.afterConnectionEstablished(session);
        //保存会话到map
        User user = (User) session.getAttributes().get("user");
        Long pictureId = (Long) session.getAttributes().get("pictureId");
        pictureEditingUsers.put(pictureId, user.getId());
        pictureSessions.putIfAbsent(pictureId, ConcurrentHashMap.newKeySet());
        pictureSessions.get(pictureId).add(session);
        //通知其他用户
        PictureEditResponseMessage responseMessage = new PictureEditResponseMessage();
        responseMessage.setType(PictureEditMessageTypeEnum.INFO.getText());
        String message = String.format("%s加入编辑",user.getUserName());
        responseMessage.setMessage(message);
        responseMessage.setUser(userService.getUserVO(user));
        broadcastToPicture(pictureId, responseMessage);


    }

    @Override
    protected void handleTextMessage(WebSocketSession session, TextMessage message) throws Exception {
        // 将消息解析为 PictureEditMessage
        PictureEditRequestMessage pictureEditRequestMessage = JSONUtil.toBean(message.getPayload(), PictureEditRequestMessage.class);
        String type = pictureEditRequestMessage.getType();
        PictureEditMessageTypeEnum pictureEditMessageTypeEnum = PictureEditMessageTypeEnum.valueOf(type);

        // 从 Session 属性中获取公共参数
        Map<String, Object> attributes = session.getAttributes();
        User user = (User) attributes.get("user");
        Long pictureId = (Long) attributes.get("pictureId");

        pictureEditEventProducer.publishEvent(pictureEditRequestMessage,session,user,pictureId);

    }

    public void handleEnterEditMessage(PictureEditRequestMessage pictureEditRequestMessage, WebSocketSession session, User user, Long pictureId) throws Exception {
            if(!pictureEditingUsers.containsKey(pictureId)){
                pictureEditingUsers.put(pictureId, user.getId());
                PictureEditResponseMessage responseMessage = new PictureEditResponseMessage();
                responseMessage.setType(PictureEditMessageTypeEnum.ENTER_EDIT.getText());
                String message = String.format("%s开始编辑图片",user.getUserName());
                responseMessage.setMessage(message);
                responseMessage.setUser(userService.getUserVO(user));
                broadcastToPicture(pictureId, responseMessage);
            }
    }
    public void handleEditActionMessage(PictureEditRequestMessage pictureEditRequestMessage, WebSocketSession session, User user, Long pictureId) throws Exception {
        Long currentUser = pictureEditingUsers.get(pictureId);
        String editAction = pictureEditRequestMessage.getEditAction();
        PictureEditMessageTypeEnum enumByValue = PictureEditMessageTypeEnum.getEnumByValue(editAction);
        if (enumByValue == null) {
            log.error("操作不对");
            return;
        }
        if(currentUser!=null&& currentUser.equals(user.getId())){
            PictureEditResponseMessage responseMessage = new PictureEditResponseMessage();
            responseMessage.setType(PictureEditMessageTypeEnum.EDIT_ACTION.getText());
            String message = String.format("%s执行了%s",user.getUserName(),editAction);
            responseMessage.setMessage(message);
            responseMessage.setEditAction(editAction);
            responseMessage.setUser(userService.getUserVO(user));
            broadcastToPicture(pictureId, responseMessage, session);
        }
    }

    public void handleExitEditMessage(PictureEditRequestMessage pictureEditRequestMessage, WebSocketSession session, User user, Long pictureId) throws Exception {
        Long currentUser = pictureEditingUsers.get(pictureId);
        if(currentUser!=null&& currentUser.equals(user.getId())){
        pictureEditingUsers.remove(pictureId);
        PictureEditResponseMessage responseMessage = new PictureEditResponseMessage();
        responseMessage.setType(PictureEditMessageTypeEnum.EXIT_EDIT.getText());
        String message = String.format("%s退出编辑图片",user.getUserName());
        responseMessage.setMessage(message);
        responseMessage.setUser(userService.getUserVO(user));
        broadcastToPicture(pictureId, responseMessage);
        }
    }






    @Override
    public void afterConnectionClosed(WebSocketSession session, CloseStatus status) throws Exception {
        super.afterConnectionClosed(session, status);
        User user = (User) session.getAttributes().get("user");
        Long pictureId = (Long) session.getAttributes().get("pictureId");
        handleExitEditMessage(null, session, user, pictureId);
        Set<WebSocketSession> webSocketSessions = pictureSessions.get(pictureId);
        if (webSocketSessions != null) {
            webSocketSessions.remove(session);
            if(webSocketSessions.isEmpty()){
                pictureSessions.remove(pictureId);
            }
        }
        PictureEditResponseMessage responseMessage = new PictureEditResponseMessage();
        responseMessage.setType(PictureEditMessageTypeEnum.INFO.getText());
        String message = String.format("%s退出编辑图片",user.getUserName());
        responseMessage.setMessage(message);
        responseMessage.setUser(userService.getUserVO(user));
        broadcastToPicture(pictureId, responseMessage);
    }

    private void broadcastToPicture(Long pictureId, PictureEditResponseMessage pictureEditResponseMessage, WebSocketSession excludeSession) throws Exception {
        Set<WebSocketSession> sessionSet = pictureSessions.get(pictureId);
        if (CollUtil.isNotEmpty(sessionSet)) {
            // 创建 ObjectMapper
            ObjectMapper objectMapper = new ObjectMapper();
            // 配置序列化：将 Long 类型转为 String，解决丢失精度问题
            SimpleModule module = new SimpleModule();
            module.addSerializer(Long.class, ToStringSerializer.instance);
            module.addSerializer(Long.TYPE, ToStringSerializer.instance); // 支持 long 基本类型
            objectMapper.registerModule(module);
            // 序列化为 JSON 字符串
            String message = objectMapper.writeValueAsString(pictureEditResponseMessage);
            TextMessage textMessage = new TextMessage(message);
            for (WebSocketSession session : sessionSet) {
                // 排除掉的 session 不发送
                if (excludeSession != null && excludeSession.equals(session)) {
                    continue;
                }
                if (session.isOpen()) {
                    session.sendMessage(textMessage);
                }
            }
        }
    }


    private void broadcastToPicture(Long pictureId, PictureEditResponseMessage pictureEditResponseMessage) throws Exception {
        broadcastToPicture(pictureId, pictureEditResponseMessage, null);
    }

}

