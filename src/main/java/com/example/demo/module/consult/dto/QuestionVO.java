package com.example.demo.module.consult.dto;

import lombok.Data;

import java.util.List;

/**
 * 问卷题目返回对象。
 */
@Data
public class QuestionVO {

    private Long id;

    private String content;

    private Integer sortOrder;

    private List<QuestionOptionItem> options;
}
