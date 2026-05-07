package com.example.demo.module.consult.skills;

import com.example.demo.module.consult.emotion.CrisisLevelAssessmentService.CrisisLevel;
import com.example.demo.module.consult.emotion.EmotionResult;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;

/**
 * 基础咨询技能层。
 * <p>
 * 提供心理咨询的基础能力：
 * <ul>
 *   <li>倾听与共情</li>
 *   <li>情绪识别响应</li>
 *   <li>危机识别响应</li>
 * </ul>
 */
@Slf4j
@Service
public class BasicCounselingSkills {

    /** 共情响应模板 */
    private static final Map<String, List<String>> EMPATHY_RESPONSES = Map.of(
            "焦虑", List.of(
                    "我听到你感到焦虑和紧张，这一定让你很不舒服。",
                    "焦虑的感觉确实很不好受，我理解你的感受。",
                    "你愿意分享让你焦虑的事情吗？我会认真倾听。"
            ),
            "抑郁", List.of(
                    "我听到你感到低落和沮丧，这一定让你很痛苦。",
                    "抑郁的感觉确实很沉重，我在这里陪着你。",
                    "你愿意和我聊聊是什么让你感到抑郁吗？"
            ),
            "压力", List.of(
                    "我听到你感到压力很大，这一定让你很疲惫。",
                    "压力确实会让人喘不过气来，我理解你的感受。",
                    "你愿意分享是什么让你感到压力吗？"
            ),
            "愤怒", List.of(
                    "我听到你感到愤怒和烦躁，这一定让你很不舒服。",
                    "愤怒的情绪确实很难控制，我理解你的感受。",
                    "你愿意和我聊聊是什么让你感到愤怒吗？"
            ),
            "悲伤", List.of(
                    "我听到你感到悲伤和难过，这一定让你很痛苦。",
                    "悲伤的感觉确实很沉重，我在这里陪着你。",
                    "你愿意和我聊聊是什么让你感到悲伤吗？"
            ),
            "失眠", List.of(
                    "我听到你有失眠的困扰，这一定让你很疲惫。",
                    "失眠确实会影响生活质量，我理解你的感受。",
                    "你愿意分享你的睡眠情况吗？"
            ),
            "注意力分散", List.of(
                    "我听到你感到注意力不集中，这一定让你很困扰。",
                    "注意力分散确实会影响学习和生活，我理解你的感受。",
                    "你愿意和我聊聊是什么让你感到注意力不集中吗？"
            )
    );

    /** 危机响应模板 */
    private static final Map<CrisisLevel, String> CRISIS_RESPONSES = Map.of(
            CrisisLevel.LOW, "我会一直在这里陪着你，愿意和我聊聊吗？",
            CrisisLevel.MODERATE, "我理解你现在的感受。如果你愿意，我们可以一起想办法。",
            CrisisLevel.HIGH, "我听到你现在的感受很痛苦。请记住，你并不孤单。全国24小时心理援助热线：400-161-9995",
            CrisisLevel.CRISIS, "如果你正在经历危机，请立即拨打全国24小时心理援助热线：400-161-9995，或联系学校心理咨询中心。你的生命很重要，有人愿意帮助你。"
    );

    /**
     * 生成共情响应
     *
     * @param emotionResult 情绪识别结果
     * @return 共情响应
     */
    public String generateEmpathyResponse(EmotionResult emotionResult) {
        if (emotionResult == null || emotionResult.isNeutral()) {
            return "我在这里陪着你，愿意和我聊聊吗？";
        }

        String primaryEmotion = emotionResult.getPrimaryEmotion();
        if (primaryEmotion == null) {
            return "我在这里陪着你，愿意和我聊聊吗？";
        }

        List<String> responses = EMPATHY_RESPONSES.get(primaryEmotion);
        if (responses == null || responses.isEmpty()) {
            return "我在这里陪着你，愿意和我聊聊吗？";
        }

        // 随机选择一个响应
        int index = (int) (Math.random() * responses.size());
        return responses.get(index);
    }

    /**
     * 生成危机响应
     *
     * @param crisisLevel 危机等级
     * @return 危机响应
     */
    public String generateCrisisResponse(CrisisLevel crisisLevel) {
        return CRISIS_RESPONSES.getOrDefault(crisisLevel, "我在这里陪着你，愿意和我聊聊吗？");
    }

    /**
     * 生成倾听响应
     *
     * @param userMessage 用户消息
     * @return 倾听响应
     */
    public String generateListeningResponse(String userMessage) {
        // 简单的倾听响应，鼓励用户继续分享
        return "我在认真听你说，愿意继续分享吗？";
    }
}
