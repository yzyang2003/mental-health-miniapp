package com.example.demo.module.consult.dto;

import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 问卷返回对象。
 */
@Data
public class QuestionnaireVO {

    private Long id;

    private String title;

    private String description;

    private String cover;

    private String type;

    private List<QuestionVO> questions;

    private LocalDateTime createTime;

    private LocalDateTime updateTime;
}
