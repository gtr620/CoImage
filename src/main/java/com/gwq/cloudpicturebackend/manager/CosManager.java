package com.gwq.cloudpicturebackend.manager;


import cn.hutool.core.io.FileUtil;
import com.gwq.cloudpicturebackend.config.CosClientConfigure;
import com.qcloud.cos.COSClient;
import com.qcloud.cos.model.COSObject;
import com.qcloud.cos.model.GetObjectRequest;
import com.qcloud.cos.model.PutObjectRequest;
import com.qcloud.cos.model.PutObjectResult;
import com.qcloud.cos.model.ciModel.job.PicProcessOperation;
import com.qcloud.cos.model.ciModel.persistence.PicOperations;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import javax.annotation.Resource;
import java.io.File;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.List;

@Component
public class CosManager {

    @Resource
    private CosClientConfigure cosClientConfigure;

    @Resource
    private COSClient cosClient;


    public PutObjectResult putObject(String key, File file) {
        PutObjectRequest putObjectRequest = new PutObjectRequest(cosClientConfigure.getBucket(), key, file);
        return cosClient.putObject(putObjectRequest);
    }

    public COSObject getObject(String key) {
        GetObjectRequest getObjectRequest = new GetObjectRequest(cosClientConfigure.getBucket(), key);
        return cosClient.getObject(getObjectRequest);
    }

    public PutObjectResult PutPictureObject(String key, File file) {
        PutObjectRequest putObjectRequest = new PutObjectRequest(cosClientConfigure.getBucket(), key, file);
        PicOperations picOperations = new PicOperations();
        //获取原图消息
        picOperations.setIsPicInfo(1);
        // 图片压缩
        List<PicOperations.Rule> rules = new ArrayList<>();
        String webKey = FileUtil.mainName(key) + ".webp";
        PicOperations.Rule compressRule = new PicOperations.Rule();
        compressRule.setFileId(webKey);
        compressRule.setBucket(cosClientConfigure.getBucket());
        compressRule.setRule("imageMogr2/format/webp");
        rules.add(compressRule);
        picOperations.setRules(rules);
        // 缩放规则
        if (file.length() > 2 * 1024) {
            PicOperations.Rule thumbnailRule = new PicOperations.Rule();
            String fileSuffix = FileUtil.getSuffix(key).isEmpty() ? ".jpg" : FileUtil.getSuffix(key);
            String thumbnailKey = FileUtil.mainName(key) + "_thumbnail." + fileSuffix;
            thumbnailRule.setFileId(thumbnailKey);
            thumbnailRule.setBucket(cosClientConfigure.getBucket());
            thumbnailRule.setRule(String.format("imageMogr2/thumbnail/%sx%s>", 256, 256));
            rules.add(thumbnailRule);
        }

        putObjectRequest.setPicOperations(picOperations);
        return cosClient.putObject(putObjectRequest);
    }

    public void deleteObject(String key) {
        String bucket = cosClientConfigure.getBucket();
        cosClient.deleteObject(bucket, key);
    }
}
