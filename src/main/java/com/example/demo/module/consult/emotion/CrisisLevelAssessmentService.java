package com.example.demo.module.consult.emotion;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;

/**
 * 危机等级判断服务。
 * <p>
 * 基于情绪识别结果和内容分析，判断危机等级：
 * <ul>
 *   <li>LOW - 低危：正常情绪波动</li>
 *   <li>MODERATE - 中危：需要关注</li>
 *   <li>HIGH - 高危：需要干预</li>
 *   <li>CRISIS - 紧急：立即响应</li>
 * </ul>
 */
@Slf4j
@Service
public class CrisisLevelAssessmentService {

    /** 危机等级关键词 */
    private static final Map<CrisisLevel, List<String>> CRISIS_LEVEL_KEYWORDS = Map.of(
            CrisisLevel.CRISIS, List.of(
                    "自杀", "自残", "跳楼", "想死", "结束生命",
                    "割腕", "上吊", "跳河", "服毒", "轻生"
            ),
            CrisisLevel.HIGH, List.of(
                    "活不下去", "不想活", "活着没意思", "不如死了算了",
                    "痛苦到想死", "绝望到想死", "崩溃到想死",
                    "撑不下去了", "坚持不下去了"
            ),
            CrisisLevel.MODERATE, List.of(
                    "抑郁", "焦虑", "压力大", "崩溃", "受不了",
                    "失眠", "注意力不集中", "情绪低落", "悲伤"
            ),
            CrisisLevel.LOW, List.of(
                    "难过", "伤心", "担心", "紧张", "疲惫"
            )
    );

    /**
     * 评估危机等级
     *
     * @param content      用户输入内容
     * @param emotionResult 情绪识别结果
     * @return 危机等级
     */
    public CrisisLevel assess(String content, EmotionResult emotionResult) {
        if (content == null || content.isBlank()) {
            return CrisisLevel.LOW;
        }

        String lowerContent = content.toLowerCase();

        // 检查危机等级关键词
        for (Map.Entry<CrisisLevel, List<String>> entry : CRISIS_LEVEL_KEYWORDS.entrySet()) {
            for (String keyword : entry.getValue()) {
                if (lowerContent.contains(keyword)) {
                    log.warn("危机等级评估: {} (关键词: {})", entry.getKey(), keyword);
                    return entry.getKey();
                }
            }
        }

        // 基于情绪强度调整危机等级
        if (emotionResult != null && !emotionResult.isNeutral()) {
            String intensity = emotionResult.getIntensity();
            if ("高强度".equals(intensity)) {
                // 高强度情绪可能需要更多关注
                log.info("高强度情绪，危机等级调整为MODERATE");
                return CrisisLevel.MODERATE;
            }
        }

        return CrisisLevel.LOW;
    }

    /**
     * 危机等级枚举
     */
    public enum CrisisLevel {
        LOW,      // 低危：正常情绪波动
        MODERATE, // 中危：需要关注
        HIGH,     // 高危：需要干预
        CRISIS    // 紧急：立即响应
    }
}
