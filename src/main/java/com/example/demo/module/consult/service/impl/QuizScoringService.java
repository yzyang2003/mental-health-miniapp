package com.example.demo.module.consult.service.impl;

import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.util.ArrayList;
import java.util.List;

/**
 * 测评评分逻辑：结论、建议、解读、AI 聊天提示。
 */
@Service
public class QuizScoringService {

    /**
     * PHQ9_DEMO / GAD7_DEMO / SCL90_DEMO 使用演示用总分划界（教学演示非临床）；其余问卷仍按得分占比三档。
     */
    public String buildConclusion(int totalScore, int maxScore, String questionnaireType) {
        String type = questionnaireType == null ? "" : questionnaireType.trim();
        if ("PHQ9_DEMO".equalsIgnoreCase(type)) {
            if (totalScore <= 4) {
                return "低风险";
            }
            if (totalScore <= 14) {
                return "中度风险";
            }
            return "高风险";
        }
        if ("GAD7_DEMO".equalsIgnoreCase(type)) {
            if (totalScore <= 4) {
                return "低风险";
            }
            if (totalScore <= 12) {
                return "中度风险";
            }
            return "高风险";
        }
        if ("SCL90_DEMO".equalsIgnoreCase(type)) {
            if (totalScore <= 180) {
                return "低风险";
            }
            if (totalScore <= 270) {
                return "中度风险";
            }
            return "高风险";
        }

        if (maxScore <= 0) {
            return "低风险";
        }

        double percent = totalScore * 100.0 / maxScore;
        if (percent <= 33) {
            return "低风险";
        }
        if (percent <= 66) {
            return "中度风险";
        }
        return "高风险";
    }

    public List<String> buildSuggestionLines(String type, String conclusion) {
        String resolvedType = StringUtils.hasText(type) ? type : "情绪";
        List<String> lines = new ArrayList<>();
        switch (conclusion) {
            case "低风险" -> {
                lines.add("保持规律作息与适度运动，继续观察自身状态变化。");
                lines.add("可在「驿站」浏览放松类文章与音乐疗愈内容。");
                lines.add("如需倾诉，可使用「树洞」或向「小爱」聊聊近况。");
            }
            case "中度风险" -> {
                lines.add("近期与「" + resolvedType + "」相关的困扰可能较明显，建议减少刺激性信息输入，并记录触发情境。");
                lines.add("尝试拆分任务、安排短休息，并结合驿站中的呼吸与正念类练习。");
                lines.add("若困扰持续，建议寻求专业心理咨询或医疗评估。");
            }
            default -> {
                lines.add("当前测评提示风险偏高，请优先关注自身安全，并尽快联系可信任的人或专业机构。");
                lines.add("在获得专业支持前，可先使用驿站与树洞进行情绪疏导，但不应替代面对面评估。");
                lines.add("如出现自伤念头，请立即拨打当地紧急求助电话或前往医院急诊。");
            }
        }
        return lines;
    }

    public String buildInterpretation(int scorePercent, String type, String conclusion) {
        String resolvedType = StringUtils.hasText(type) ? type : "情绪";
        String level;
        switch (conclusion) {
            case "低风险" -> level = "偏低";
            case "中度风险" -> level = "中等";
            default -> level = "偏高";
        }
        return "本次测评得分约占总分的 " + scorePercent + "%，在「" + resolvedType + "」维度上，当前提示为「"
                + conclusion + "」，困扰程度整体 " + level + "。以下为自助建议，请结合自身实际情况参考。";
    }

    public String buildAiChatHint(String questionnaireTitle, String conclusion, int scorePercent) {
        String title = StringUtils.hasText(questionnaireTitle) ? questionnaireTitle : "心理测评";
        return "我刚完成了「" + title + "」，结果是「" + conclusion + "」（约 " + scorePercent
                + "%）。想请你帮我一起看看接下来可以怎么调整。";
    }
}
