package com.example.demo.module.consult.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * AI 聊天记录实体。
 */
@Data
@TableName("chat_history")
public class ChatHistory {

    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    @TableField("openid")
    private String openid;

    @TableField("role")
    private String role;

    @TableField("content")
    private String content;

    @TableField("create_time")
    private LocalDateTime createTime;
}
