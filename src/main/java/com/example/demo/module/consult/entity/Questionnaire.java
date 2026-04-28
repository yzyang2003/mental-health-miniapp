package com.example.demo.module.consult.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.baomidou.mybatisplus.extension.handlers.JacksonTypeHandler;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.Map;

/**
 * 问卷主表实体。
 */
@Data
@TableName(value = "questionnaire", autoResultMap = true)
public class Questionnaire {

    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    @TableField("title")
    private String title;

    @TableField("description")
    private String description;

    @TableField("cover")
    private String cover;

    @TableField("type")
    private String type;

    @TableField(value = "scoring_rule", typeHandler = JacksonTypeHandler.class)
    private Map<String, Object> scoringRule;

    @TableField("status")
    private Integer status;

    @TableField("create_time")
    private LocalDateTime createTime;

    @TableField("update_time")
    private LocalDateTime updateTime;
}
