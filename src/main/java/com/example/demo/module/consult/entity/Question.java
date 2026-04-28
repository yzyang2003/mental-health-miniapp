package com.example.demo.module.consult.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.baomidou.mybatisplus.extension.handlers.JacksonTypeHandler;
import com.example.demo.module.consult.dto.QuestionOptionItem;
import lombok.Data;

import java.util.List;

/**
 * 问卷题目实体。
 */
@Data
@TableName(value = "question", autoResultMap = true)
public class Question {

    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    @TableField("questionnaire_id")
    private Long questionnaireId;

    @TableField("content")
    private String content;

    @TableField(value = "options", typeHandler = JacksonTypeHandler.class)
    private List<QuestionOptionItem> options;

    @TableField("sort_order")
    private Integer sortOrder;

    @TableField("status")
    private Integer status;
}
