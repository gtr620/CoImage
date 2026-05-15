package com.gwq.cloudpicturebackend.manager.upload;


import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.date.DateUtil;
import cn.hutool.core.io.FileUtil;
import cn.hutool.core.util.RandomUtil;
import cn.hutool.core.util.StrUtil;
import com.gwq.cloudpicturebackend.config.CosClientConfigure;
import com.gwq.cloudpicturebackend.exception.BusinessException;
import com.gwq.cloudpicturebackend.exception.ErrorCode;
import com.gwq.cloudpicturebackend.manager.CosManager;
import com.gwq.cloudpicturebackend.model.dto.file.UploadPictureResult;
import com.qcloud.cos.model.PutObjectResult;
import com.qcloud.cos.model.ciModel.persistence.CIObject;
import com.qcloud.cos.model.ciModel.persistence.ImageInfo;
import com.qcloud.cos.model.ciModel.persistence.ProcessResults;
import lombok.extern.slf4j.Slf4j;

import javax.annotation.Resource;
import java.io.File;
import java.io.IOException;
import java.util.Date;
import java.util.List;

@Slf4j
public abstract class PictureUploadTemplate {
    @Resource
    private CosClientConfigure cosClientConfigure;

    @Resource
    private CosManager cosManager;


    public UploadPictureResult uploadPicture(Object inputSource, String uploadPathPrefix) {
        //校验图片
        String fileSubfix = validPicture(inputSource);
        //上传图片
        String uuid = RandomUtil.randomString(16);

        String originalFilename = getOriginalFilename(inputSource);
        String uploadFileName = String.format("%s_%s.%s", DateUtil.formatDate(new Date()), uuid, fileSubfix);
        String uploadPath = String.format("/%s/%s", uploadPathPrefix, uploadFileName);
        //解析图片信息

        File file = null;
        try {
            file = File.createTempFile(uploadPath, fileSubfix);
            getProcessFile(inputSource, file);
            PutObjectResult putObjectResult = cosManager.PutPictureObject(uploadPath, file);
            ImageInfo imageInfo = putObjectResult.getCiUploadResult().getOriginalInfo().getImageInfo();

            ProcessResults processResults = putObjectResult.getCiUploadResult().getProcessResults();
            List<CIObject> objectList = processResults.getObjectList();
            if (CollUtil.isNotEmpty(objectList)) {
                CIObject compressCiObject = objectList.get(0);
                CIObject thumbnailCiObject = compressCiObject;
                if (objectList.size() > 1) {
                    thumbnailCiObject = objectList.get(1);
                }

                return buildResult(originalFilename, compressCiObject, thumbnailCiObject, imageInfo);
            }

            return buildResult(originalFilename, file, uploadPath, imageInfo);
        } catch (IOException e) {
            log.error("file upload error", e);
            throw new BusinessException(ErrorCode.OPERATION_ERROR, "file upload error");
        } finally {
            deleteTempFile(file);

        }
    }

    private UploadPictureResult buildResult(String originalFilename, CIObject compressCiObject, CIObject thumbnailCiObject, ImageInfo imageInfo) {
        int picWidth = compressCiObject.getWidth();
        int picHeight = compressCiObject.getHeight();
        double picScale = (double) picWidth / (double) picHeight;

        UploadPictureResult uploadPictureResult = new UploadPictureResult();
        uploadPictureResult.setUrl(cosClientConfigure.getHost() + "/" + compressCiObject.getKey());
        uploadPictureResult.setPicName(FileUtil.mainName(originalFilename));
        uploadPictureResult.setPicSize(compressCiObject.getSize().longValue());
        uploadPictureResult.setPicWidth(picWidth);
        uploadPictureResult.setPicHeight(picHeight);
        uploadPictureResult.setPicScale(picScale);
        uploadPictureResult.setPicFormat(compressCiObject.getFormat());
        uploadPictureResult.setThumbnailUrl(cosClientConfigure.getHost() + "/" + thumbnailCiObject.getKey());
        uploadPictureResult.setPicColor(imageInfo.getAve());

        return uploadPictureResult;
    }

    protected abstract String validPicture(Object inputSource);

    protected abstract void getProcessFile(Object inputSource, File file) throws IOException;

    protected abstract String getOriginalFilename(Object inputSource);

    private static void deleteTempFile(File file) {
        if (file != null) {
            boolean delete = file.delete();
            if (!delete) {
                log.error("file delete error");
            }
        }
    }

    private UploadPictureResult buildResult(String originalFilename, File file, String uploadPath, ImageInfo imageInfo) {
        int picWidth = imageInfo.getWidth();
        int picHeight = imageInfo.getHeight();
        double picScale = (double) picWidth / (double) picHeight;

        UploadPictureResult uploadPictureResult = new UploadPictureResult();
        uploadPictureResult.setUrl(cosClientConfigure.getBucket() + "/" + uploadPath);
        uploadPictureResult.setPicName(FileUtil.mainName(originalFilename));
        uploadPictureResult.setPicSize(FileUtil.size(file));
        uploadPictureResult.setPicWidth(picWidth);
        uploadPictureResult.setPicHeight(picHeight);
        uploadPictureResult.setPicScale(picScale);
        uploadPictureResult.setPicFormat(imageInfo.getFormat());
        uploadPictureResult.setPicColor(imageInfo.getAve());

        return uploadPictureResult;
    }


}
