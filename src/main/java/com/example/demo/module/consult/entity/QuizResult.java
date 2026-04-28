package com.example.demo.module.consult.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.baomidou.mybatisplus.extension.handlers.JacksonTypeHandler;
import com.example.demo.module.consult.dto.AnswerItem;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 测评结果实体。
 */
@Data
@TableName(value = "quiz_result", autoResultMap = true)
public class QuizResult {

    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    @TableField("openid")
    private String openid;

    @TableField("questionnaire_id")
    private Long questionnaireId;

    @TableField(value = "answers", typeHandler = JacksonTypeHandler.class)
    private List<AnswerItem> answers;

    @TableField("score")
    private Integer score;

    @TableField("max_score")
    private Integer maxScore;

    @TableField("score_percent")
    private Integer scorePercent;

    @TableField("conclusion")
    private String conclusion;

    @TableField("suggestions")
    private String suggestions;

    @TableField("ai_chat_hint")
    private String aiChatHint;

    @TableField("ai_guidance")
    private String aiGuidance;

    @TableField("create_time")
    private LocalDateTime createTime;
}
