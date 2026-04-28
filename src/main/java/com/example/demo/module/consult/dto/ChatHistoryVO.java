package com.example.demo.module.consult.dto;

import lombok.Data;

import java.time.LocalDateTime;

/**
 * 聊天历史返回对象。
 */
@Data
public class ChatHistoryVO {

    private Long id;

    private String role;

    private String content;

    private LocalDateTime createTime;
}
