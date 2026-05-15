package com.gwq.cloudpicturebackend.manager.upload;

import cn.hutool.core.io.FileUtil;
import com.gwq.cloudpicturebackend.exception.ErrorCode;
import com.gwq.cloudpicturebackend.exception.ThrowUtils;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import java.util.List;

@Service
public class FilePictureUpload extends PictureUploadTemplate {
    @Override
    protected String validPicture(Object inputSource) {
        MultipartFile file = (MultipartFile) inputSource;
        ThrowUtils.throwIf(file == null, ErrorCode.PARAMS_ERROR, "文件不能为空");
        long fileSize = file.getSize();
        final long ONE_MB = 1024 * 1024;
        ThrowUtils.throwIf(fileSize > ONE_MB * 2, ErrorCode.PARAMS_ERROR, "文件过大");
        String fileSuffix = FileUtil.getSuffix(file.getOriginalFilename());
        List<String> allowedExtensions = Arrays.asList("jpg", "png", "gif", "bmp", "jpeg");
        ThrowUtils.throwIf(!allowedExtensions.contains(fileSuffix), ErrorCode.PARAMS_ERROR, "非法格式");
        return fileSuffix;
    }

    @Override
    protected void getProcessFile(Object inputSource, File file) throws IOException {
        MultipartFile multipartFile = (MultipartFile) inputSource;
        multipartFile.transferTo(file);
    }

    @Override
    protected String getOriginalFilename(Object inputSource) {
        MultipartFile multipartFile = (MultipartFile) inputSource;
        return multipartFile.getOriginalFilename();
    }
}
