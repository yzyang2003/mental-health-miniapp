package com.example.demo.module.consult.dto;

import lombok.Data;

/**
 * 用户答题项。
 */
@Data
public class AnswerItem {

    private Long questionId;

    private Integer selectedOptionIndex;
}
