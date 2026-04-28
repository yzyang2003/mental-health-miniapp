package com.example.demo.module.consult.dto;

import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 测评结果返回对象。
 */
@Data
public class QuizResultVO {

    private Long id;

    private Long questionnaireId;

    private String questionnaireTitle;

    /** 问卷类型标识，如 PHQ9_DEMO / GAD7_DEMO，前端可用于展示「教学演示」等标签 */
    private String questionnaireType;

    private Integer score;

    private Integer maxScore;

    private Integer scorePercent;

    private String conclusion;

    private String interpretation;

    private String suggestions;

    private List<String> suggestionLines;

    private String disclaimer;

    private String aiChatHint;

    /** AI 个性化指导；为空时表示未生成或 API 不可用，页面可展示规则建议说明 */
    private String aiGuidance;

    /** SCL-90 教学演示量表的结构化报告（仅 questionnaireType = SCL90_DEMO 时返回） */
    private Scl90ReportVO scl90Report;

    private LocalDateTime createTime;
}
