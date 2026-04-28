package com.example.demo.module.consult.dto;

import lombok.Data;

import java.util.List;

/**
 * 提交测评请求。
 */
@Data
public class SubmitQuizRequest {

    private Long questionnaireId;

    private List<AnswerItem> answers;
}
