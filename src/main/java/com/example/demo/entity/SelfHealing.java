package com.example.demo.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.baomidou.mybatisplus.extension.handlers.JacksonTypeHandler;
import com.example.demo.dto.SelfHealingStepItem;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 自我疗愈实体。
 */
@Data
@TableName(value = "self_healing", autoResultMap = true)
public class SelfHealing {

    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    @TableField("title")
    private String title;

    @TableField("cover")
    private String cover;

    @TableField("description")
    private String description;

    @TableField("issue_type")
    private String issueType;

    @TableField(value = "steps", typeHandler = JacksonTypeHandler.class)
    private List<SelfHealingStepItem> steps;

    @TableField("duration")
    private Integer duration;

    @TableField("status")
    private Integer status;

    @TableField("create_time")
    private LocalDateTime createTime;

    @TableField("update_time")
    private LocalDateTime updateTime;
}
