package com.example.demo.module.consult.skills;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;

/**
 * 疗法推荐服务。
 * <p>
 * 根据用户情绪推荐小程序中已有的自我疗愈内容：
 * <ul>
 *   <li>ID: 1 - 三分钟呼吸空间 (焦虑)</li>
 *   <li>ID: 2 - 睡前放松清单 (失眠)</li>
 *   <li>ID: 3 - 情绪低落时的微行动 (情绪低落)</li>
 *   <li>ID: 4 - 压力卸载四步法 (压力)</li>
 *   <li>ID: 5 - 注意力回收练习 (注意力分散)</li>
 * </ul>
 */
@Slf4j
@Service
public class TherapyRecommendationService {

    /** 情绪-疗法映射（仅使用小程序已有自我疗愈） */
    private static final Map<String, List<TherapyRecommendation>> EMOTION_THERAPY_MAP = Map.of(
            "焦虑", List.of(
                    new TherapyRecommendation(1L, "三分钟呼吸空间", "缓解焦虑")
            ),
            "紧张", List.of(
                    new TherapyRecommendation(1L, "三分钟呼吸空间", "缓解紧张")
            ),
            "失眠", List.of(
                    new TherapyRecommendation(2L, "睡前放松清单", "改善睡眠")
            ),
            "情绪低落", List.of(
                    new TherapyRecommendation(3L, "情绪低落时的微行动", "提升情绪")
            ),
            "抑郁", List.of(
                    new TherapyRecommendation(3L, "情绪低落时的微行动", "提升情绪")
            ),
            "压力", List.of(
                    new TherapyRecommendation(4L, "压力卸载四步法", "缓解压力")
            ),
            "注意力分散", List.of(
                    new TherapyRecommendation(5L, "注意力回收练习", "提升专注")
            )
    );

    /**
     * 根据情绪推荐疗法
     *
     * @param emotionType 情绪类型
     * @return 疗法推荐列表
     */
    public List<TherapyRecommendation> recommendByEmotion(String emotionType) {
        if (emotionType == null || emotionType.isBlank()) {
            return List.of();
        }

        List<TherapyRecommendation> recommendations = EMOTION_THERAPY_MAP.get(emotionType);
        if (recommendations == null) {
            return List.of();
        }

        log.info("根据情绪 {} 推荐疗法: {}", emotionType, recommendations);
        return recommendations;
    }

    /**
     * 疗法推荐内部类
     */
    public static class TherapyRecommendation {
        private Long therapyId;
        private String therapyName;
        private String description;

        public TherapyRecommendation(Long therapyId, String therapyName, String description) {
            this.therapyId = therapyId;
            this.therapyName = therapyName;
            this.description = description;
        }

        // Getters and Setters
        public Long getTherapyId() { return therapyId; }
        public void setTherapyId(Long therapyId) { this.therapyId = therapyId; }
        public String getTherapyName() { return therapyName; }
        public void setTherapyName(String therapyName) { this.therapyName = therapyName; }
        public String getDescription() { return description; }
        public void setDescription(String description) { this.description = description; }

        @Override
        public String toString() {
            return "TherapyRecommendation{" +
                    "therapyId=" + therapyId +
                    ", therapyName='" + therapyName + '\'' +
                    ", description='" + description + '\'' +
                    '}';
        }
    }
}
