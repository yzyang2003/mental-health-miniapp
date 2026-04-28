package com.example.demo.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 树洞回复实体。
 */
@Data
@TableName("reply")
public class Reply {

    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    @TableField("topic_id")
    private Long topicId;

    @TableField("content")
    private String content;

    @TableField("replier_openid")
    private String replierOpenid;

    @TableField("replied_user_openid")
    private String repliedUserOpenid;

    @TableField("replied_reply_id")
    private Long repliedReplyId;

    @TableField("anonymous")
    private Boolean anonymous;

    @TableField("status")
    private Integer status;

    @TableField("create_time")
    private LocalDateTime createTime;

    @TableField("update_time")
    private LocalDateTime updateTime;
}
