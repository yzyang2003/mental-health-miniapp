package com.example.demo.dto;

import lombok.Data;

import java.util.List;

/**
 * 树洞帖子详情返回对象。
 */
@Data
public class TopicDetailVO {

    private TopicVO topic;

    private List<ReplyVO> replies;

    /**
     * 回复总数。
     */
    private Long replyTotal;

    /**
     * 回复总页数。
     */
    private Long replyPages;

    /**
     * 当前回复页码。
     */
    private Long replyCurrent;
}
