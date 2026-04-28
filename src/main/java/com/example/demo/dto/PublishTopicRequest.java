package com.example.demo.dto;

import lombok.Data;

import java.util.List;

/**
 * 发布树洞帖子请求。
 */
@Data
public class PublishTopicRequest {

    /**
     * 帖子内容。
     */
    private String content;

    /**
     * 图片 URL 列表。
     */
    private List<String> images;

    /**
     * 是否匿名，默认匿名。
     */
    private Boolean anonymous = true;
}
