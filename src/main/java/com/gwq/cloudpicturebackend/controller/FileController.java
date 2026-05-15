package com.gwq.cloudpicturebackend.controller;

import com.gwq.cloudpicturebackend.annotation.AuthCheck;
import com.gwq.cloudpicturebackend.common.BaseResponse;
import com.gwq.cloudpicturebackend.common.ResultUtils;
import com.gwq.cloudpicturebackend.constant.UserConstant;
import com.gwq.cloudpicturebackend.exception.BusinessException;
import com.gwq.cloudpicturebackend.exception.ErrorCode;
import com.gwq.cloudpicturebackend.manager.CosManager;
import com.qcloud.cos.model.COSObject;
import com.qcloud.cos.utils.IOUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletResponse;
import java.io.*;

@Slf4j
@RestController
@RequestMapping("/file")
public class FileController {

    @Resource
    private CosManager cosManager;

    @AuthCheck(mustRole = UserConstant.ADMIN_ROLE)
    @PostMapping("/test/upload")
    public BaseResponse<String> upload(@RequestParam("file") MultipartFile multipartFile) {
        String fileName = multipartFile.getOriginalFilename();
        String filePath = String.format("/test/upload/%s", fileName);
        File file = null;
        try {
            file = File.createTempFile(filePath, null);
            multipartFile.transferTo(file);
            cosManager.putObject(filePath, file);
            return ResultUtils.success(filePath);
        } catch (IOException e) {
            log.error("file upload error", e);
            throw new BusinessException(ErrorCode.OPERATION_ERROR, "file upload error");
        } finally {
            if (file != null) {
                boolean delete = file.delete();
                if (!delete) {
                    log.error("file delete error");
                }
            }
        }
    }

    @GetMapping("/test/download")
    public void getObject(String filepath, HttpServletResponse response) throws IOException {
        InputStream inputStream = null;
        try {
            COSObject cosObject = cosManager.getObject(filepath);
            inputStream = cosObject.getObjectContent();

            byte[] bytes = IOUtils.toByteArray(inputStream);
            // 设置响应头
            response.setContentType("application/octet-stream;charset=UTF-8");
            response.setHeader("Content-Disposition", "attachment; filename=" + filepath);
            // 写入响应
            response.getOutputStream().write(bytes);
            response.getOutputStream().flush();
        } catch (IOException e) {
            log.error("file download error", e);
            throw new BusinessException(ErrorCode.OPERATION_ERROR, "file download error");
        } finally {
            if (inputStream != null) {
                inputStream.close();
            }
        }

    }

}
