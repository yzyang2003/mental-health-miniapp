package com.example.demo.dto;

import lombok.Data;

import java.time.LocalDateTime;

/**
 * 树洞回复返回对象。
 */
@Data
public class ReplyVO {

    private Long id;

    private String content;

    private String replierName;

    private String repliedUserName;

    private Long repliedReplyId;

    private Boolean canDelete;

    private LocalDateTime createTime;
}
