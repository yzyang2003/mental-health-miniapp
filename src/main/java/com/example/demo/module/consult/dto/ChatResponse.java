package com.example.demo.module.consult.dto;

import lombok.Data;

import java.time.LocalDateTime;

/**
 * AI 聊天响应。
 */
@Data
public class ChatResponse {

    private String reply;

    private LocalDateTime timestamp;
}
