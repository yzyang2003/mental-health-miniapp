package com.example.demo.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.baomidou.mybatisplus.extension.handlers.JacksonTypeHandler;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 树洞帖子实体。
 */
@Data
@TableName(value = "topic", autoResultMap = true)
public class Topic {

    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    @TableField("content")
    private String content;

    /**
     * 图片 URL 数组，使用 JSON 字段存储。
     */
    @TableField(value = "images", typeHandler = JacksonTypeHandler.class)
    private List<String> images;

    @TableField("publisher_openid")
    private String publisherOpenid;

    @TableField("anonymous")
    private Boolean anonymous;

    @TableField("status")
    private Integer status;

    @TableField("create_time")
    private LocalDateTime createTime;

    @TableField("update_time")
    private LocalDateTime updateTime;
}
