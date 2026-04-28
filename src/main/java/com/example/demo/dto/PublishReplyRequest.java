package com.example.demo.dto;

import lombok.Data;

/**
 * 发布回复请求。
 */
@Data
public class PublishReplyRequest {

    /**
     * 所属帖子 ID。
     */
    private Long topicId;

    /**
     * 回复内容。
     */
    private String content;

    /**
     * 被回复的回复 ID，可为空。
     */
    private Long repliedReplyId;

    /**
     * 是否匿名，默认匿名。
     */
    private Boolean anonymous = true;
}
