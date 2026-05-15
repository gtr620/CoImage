package com.gwq.cloudpicturebackend.model.dto.space.analyse;

import lombok.Data;

import java.io.Serializable;

@Data
public class SpaceAnalyzeRequest implements Serializable {
    private Long spaceId;

    private boolean queryPublic;

    private boolean queryAll;
}
