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
}
