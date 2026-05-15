package com.gwq.cloudpicturebackend.api.imageSearch.sub;

import cn.hutool.core.util.RandomUtil;
import cn.hutool.core.util.URLUtil;
import cn.hutool.http.HttpRequest;
import cn.hutool.http.HttpResponse;
import cn.hutool.http.HttpStatus;
import cn.hutool.json.JSONUtil;
import com.gwq.cloudpicturebackend.exception.BusinessException;
import com.gwq.cloudpicturebackend.exception.ErrorCode;
import lombok.extern.slf4j.Slf4j;

import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;

@Slf4j
public class GetImagePageUrlApi {

    /**
     * 获取图片页面地址
     *
     * @param imageUrl
     * @return
     */
    public static String getImagePageUrl(String imageUrl) {
        // 1. 准备请求参数
        Map<String, Object> formData = new HashMap<>();
        formData.put("image", imageUrl);
        formData.put("tn", "pc");
        formData.put("from", "pc");
        formData.put("image_source", "PC_UPLOAD_URL");
        // 获取当前时间戳
        long uptime = System.currentTimeMillis();
        // 请求地址
        String url = "https://graph.baidu.com/upload?uptime=" + uptime;
        String token = "1777958270004_1777971581023_DgGlmrU6hcpA5qhWIy5+wrNqCA9zCEqlC/RcyGlNMcWHu+iNvMgBLJFCRnKFBge3j9ZXeARqg84NxwWbSxSW8jsbDTcSq54rZlR0bM5YhWGA0qG8yohBaTOIZCXfJFW2o8EF4yT3+P8AXHGOW6tAXfJAuCcXEvk+5yxAOBqn51tmN8r0piT9pqMWDENp7m9xWKkjrHETiTqjqr5C87aRFwhz2l8fszBEPpIw9YsXF58RkLgxBnDqSLSuK2XPC1WfJlHJsXe+pz5xLHqzT/VM+WOoVQPZ3lfqtiJ3KuXx+H1sfqIMy68hlq/kdgyR/q6ELO1f4tHveW4FmkbyZsHPQxZeqishTACZBal8ExHSwaGGnvEDoKPDLKkViCmfbPJTACKRfpDkPhAaaY5hh3SQzkFyxOXYogTStNqalXyBAMuiQn3741Rdpx4w4lGrj+kwvBYDOWRlqw3DDqgpYOR0d6yCTgkKvQ1+9vzpMD1DAFg=";


        try {
            // 2. 发送 POST 请求到百度接口
            HttpResponse response = HttpRequest.post(url)
                    .form(formData)
                    .header("Acs-Token", token)
                    .timeout(5000)
                    .execute();
            // 判断响应状态
            if (HttpStatus.HTTP_OK != response.getStatus()) {
                throw new BusinessException(ErrorCode.OPERATION_ERROR, "接口调用失败1");
            }
            // 解析响应
            String responseBody = response.body();
            Map<String, Object> result = JSONUtil.toBean(responseBody, Map.class);
            // 3. 处理响应结果
            if (result == null || !Integer.valueOf(0).equals(result.get("status"))) {
                throw new BusinessException(ErrorCode.OPERATION_ERROR, "接口调用失败2");
            }
            Map<String, Object> data = (Map<String, Object>) result.get("data");
            String rawUrl = (String) data.get("url");
            // 对 URL 进行解码
            String searchResultUrl = URLUtil.decode(rawUrl, StandardCharsets.UTF_8);
            // 如果 URL 为空
            if (searchResultUrl == null) {
                throw new BusinessException(ErrorCode.OPERATION_ERROR, "未返回有效结果");
            }
            return searchResultUrl;
        } catch (Exception e) {
            log.error("搜索失败", e);
            throw new BusinessException(ErrorCode.OPERATION_ERROR, "搜索失败");
        }
    }

    public static void main(String[] args) {
        // 测试以图搜图功能
        String imageUrl = "https://www.codefather.cn/logo.png";
        String result = getImagePageUrl(imageUrl);
        System.out.println("搜索成功，结果 URL：" + result);
    }
}


