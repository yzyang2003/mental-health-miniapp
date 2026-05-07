package com.example.demo.module.consult.skills;

import com.example.demo.module.consult.emotion.EmotionResult;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;

/**
 * 专业咨询技能层。
 * <p>
 * 提供心理咨询的专业技能：
 * <ul>
 *   <li>心理评估</li>
 *   <li>认知行为疗法（CBT）</li>
 *   <li>人本主义疗法</li>
 * </ul>
 */
@Slf4j
@Service
public class ProfessionalCounselingSkills {

    /** CBT思维挑战模板 */
    private static final Map<String, List<String>> CBT_THOUGHT_CHALLENGES = Map.of(
            "焦虑", List.of(
                    "我注意到你有一些担心的想法。让我们一起来看看这个想法是否完全正确。",
                    "焦虑时我们容易往最坏的方向想。让我们试着从另一个角度看看。",
                    "你担心的事情发生的可能性有多大？我们可以一起评估一下。"
            ),
            "抑郁", List.of(
                    "我注意到你有一些负面的想法。让我们一起来看看这个想法是否完全正确。",
                    "抑郁时我们容易只看到事情的消极面。让我们试着找找积极的方面。",
                    "你对自己的评价可能过于苛刻了。让我们试着更客观地看待。"
            ),
            "压力", List.of(
                    "我注意到你感到压力很大。让我们一起来看看哪些是可以改变的。",
                    "压力时我们容易感到无助。让我们试着把大问题分解成小步骤。",
                    "你不需要一个人承担所有。让我们看看有哪些资源可以帮助你。"
            )
    );

    /** 人本主义响应模板 */
    private static final Map<String, List<String>> PERSON_CENTERED_RESPONSES = Map.of(
            "焦虑", List.of(
                    "无论你现在感受如何，你都是有价值的。我会一直在这里陪着你。",
                    "你的焦虑是真实的，我理解你的感受。我们可以一起面对。",
                    "你不需要独自承受这些。我会一直在这里支持你。"
            ),
            "抑郁", List.of(
                    "无论你现在感受如何，你都是有价值的。我会一直在这里陪着你。",
                    "你的痛苦是真实的，我理解你的感受。我们可以一起面对。",
                    "你不需要独自承受这些。我会一直在这里支持你。"
            ),
            "压力", List.of(
                    "无论你现在感受如何，你都是有价值的。我会一直在这里陪着你。",
                    "你的压力是真实的，我理解你的感受。我们可以一起面对。",
                    "你不需要独自承受这些。我会一直在这里支持你。"
            )
    );

    /**
     * 生成CBT思维挑战响应
     *
     * @param emotionResult 情绪识别结果
     * @return CBT响应
     */
    public String generateCBTResponse(EmotionResult emotionResult) {
        if (emotionResult == null || emotionResult.isNeutral()) {
            return "我注意到你有一些想法。让我们一起来看看这个想法是否完全正确。";
        }

        String primaryEmotion = emotionResult.getPrimaryEmotion();
        if (primaryEmotion == null) {
            return "我注意到你有一些想法。让我们一起来看看这个想法是否完全正确。";
        }

        List<String> challenges = CBT_THOUGHT_CHALLENGES.get(primaryEmotion);
        if (challenges == null || challenges.isEmpty()) {
            return "我注意到你有一些想法。让我们一起来看看这个想法是否完全正确。";
        }

        // 随机选择一个挑战
        int index = (int) (Math.random() * challenges.size());
        return challenges.get(index);
    }

    /**
     * 生成人本主义响应
     *
     * @param emotionResult 情绪识别结果
     * @return 人本主义响应
     */
    public String generatePersonCenteredResponse(EmotionResult emotionResult) {
        if (emotionResult == null || emotionResult.isNeutral()) {
            return "无论你现在感受如何，你都是有价值的。我会一直在这里陪着你。";
        }

        String primaryEmotion = emotionResult.getPrimaryEmotion();
        if (primaryEmotion == null) {
            return "无论你现在感受如何，你都是有价值的。我会一直在这里陪着你。";
        }

        List<String> responses = PERSON_CENTERED_RESPONSES.get(primaryEmotion);
        if (responses == null || responses.isEmpty()) {
            return "无论你现在感受如何，你都是有价值的。我会一直在这里陪着你。";
        }

        // 随机选择一个响应
        int index = (int) (Math.random() * responses.size());
        return responses.get(index);
    }

    /**
     * 生成心理评估建议
     *
     * @param emotionResult 情绪识别结果
     * @return 评估建议
     */
    public String generateAssessmentSuggestion(EmotionResult emotionResult) {
        if (emotionResult == null || emotionResult.isNeutral()) {
            return "如果你想更了解自己的心理状态，可以尝试做一些心理测评。";
        }

        String primaryEmotion = emotionResult.getPrimaryEmotion();
        if (primaryEmotion == null) {
            return "如果你想更了解自己的心理状态，可以尝试做一些心理测评。";
        }

        return switch (primaryEmotion) {
            case "焦虑" -> "如果你想更了解自己的焦虑程度，可以尝试做一下焦虑自评量表。";
            case "抑郁" -> "如果你想更了解自己的情绪状态，可以尝试做一下抑郁自评量表。";
            case "压力" -> "如果你想更了解自己的压力水平，可以尝试做一下综合心理健康评估。";
            default -> "如果你想更了解自己的心理状态，可以尝试做一些心理测评。";
        };
    }
}
