package com.gwq.cloudpicturebackend.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.gwq.cloudpicturebackend.model.dto.space.analyse.*;
import com.gwq.cloudpicturebackend.model.entity.Space;
import com.gwq.cloudpicturebackend.model.entity.User;
import com.gwq.cloudpicturebackend.model.vo.space.analyst.*;

import java.util.List;

public interface SpaceAnalyzeService extends IService<Space> {

    // 空间使用量
    SpaceUsageAnalyzeResponse getSpaceUsageAnalyze(SpaceUsageAnalyzeRequest spaceUsageAnalyzeRequest, User loginUser);

    // 空间类别分析
    List<SpaceCategoryAnalyzeResponse> getSpaceCategoryAnalyze(SpaceCategoryAnalyzeRequest spaceCategoryAnalyzeRequest, User loginUser);

    // 空间tags分析
    List<SpaceTagAnalyzeResponse> getSpaceTagAnalyze(SpaceTagAnalyzeRequest spaceTagAnalyzeRequest, User loginUser);

    List<SpaceSizeAnalyzeResponse> getSpaceSizeAnalyze(SpaceSizeAnalyzeRequest spaceSizeAnalyzeRequest, User loginUser);

    List<SpaceUserAnalyzeResponse> getSpaceUserAnalyze(SpaceUserAnalyzeRequest spaceUserAnalyzeRequest, User loginUser);

    List<Space> getSpaceRankAnalyze(SpaceRankAnalyzeRequest spaceRankAnalyzeRequest, User loginUser);
}
