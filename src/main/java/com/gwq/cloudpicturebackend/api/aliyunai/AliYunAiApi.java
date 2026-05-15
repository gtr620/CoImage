package com.gwq.cloudpicturebackend.api.aliyunai;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.http.Header;
import cn.hutool.http.HttpRequest;
import cn.hutool.http.HttpResponse;
import cn.hutool.http.HttpUtil;
import cn.hutool.json.JSONUtil;
import com.gwq.cloudpicturebackend.api.aliyunai.model.CreateOutPaintingTaskRequest;
import com.gwq.cloudpicturebackend.api.aliyunai.model.CreateOutPaintingTaskResponse;
import com.gwq.cloudpicturebackend.api.aliyunai.model.GetOutPaintingTaskResponse;
import com.gwq.cloudpicturebackend.exception.BusinessException;
import com.gwq.cloudpicturebackend.exception.ErrorCode;
import com.gwq.cloudpicturebackend.exception.ThrowUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class AliYunAiApi {
    @Value("${aliYunAi.apiKey}")
    private String apiKey;

    // 创建任务地址
    public static final String CREATE_OUT_PAINTING_TASK_URL = "https://dashscope.aliyuncs.com/api/v1/services/aigc/image2image/out-painting";

    // 查询任务状态
    public static final String GET_OUT_PAINTING_TASK_URL = "https://dashscope.aliyuncs.com/api/v1/tasks/%s";

    public CreateOutPaintingTaskResponse createOutPaintingTaskRequest(CreateOutPaintingTaskRequest createOutPaintingTaskRequest) {
        if (createOutPaintingTaskRequest == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "阔图请求为空");
        }

        HttpRequest httpRequest = HttpRequest.post(CREATE_OUT_PAINTING_TASK_URL)
                .header(Header.AUTHORIZATION, "Bearer " + apiKey)
                .header("X-DashScope-Async", "enable")
                .header(Header.CONTENT_TYPE, "application/json")
                .body(JSONUtil.toJsonStr(createOutPaintingTaskRequest));
        try (HttpResponse httpResponse = httpRequest.execute();) {
            if (!httpResponse.isOk()) {
                log.error(httpResponse.body());
                throw new BusinessException(ErrorCode.OPERATION_ERROR, "ai扩图失败");
            }
            CreateOutPaintingTaskResponse createOutPaintingTaskResponse = JSONUtil.toBean(httpResponse.body(), CreateOutPaintingTaskResponse.class);
            if (StrUtil.isNotBlank(createOutPaintingTaskResponse.getCode())) {
                log.error(httpResponse.body());
                throw new BusinessException(ErrorCode.OPERATION_ERROR, "ai扩图失败");
            }
            return createOutPaintingTaskResponse;
        }
    }

    public GetOutPaintingTaskResponse getOutPaintingTask(String taskId) {
        if (taskId == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "阔图请求为空");
        }
        try (HttpResponse httpResponse = HttpRequest.get(String.format(GET_OUT_PAINTING_TASK_URL, taskId))
                .header(Header.AUTHORIZATION, "Bearer " + apiKey)
                .execute()) {
            if (!httpResponse.isOk()) {
                throw new BusinessException(ErrorCode.OPERATION_ERROR, "获取任务失败");
            }
            return JSONUtil.toBean(httpResponse.body(), GetOutPaintingTaskResponse.class);
        }
    }
}

