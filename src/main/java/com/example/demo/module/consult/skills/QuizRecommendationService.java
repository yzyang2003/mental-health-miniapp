package com.example.demo.module.consult.skills;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;

/**
 * 量表推荐服务。
 * <p>
 * 根据用户情绪推荐数据库中已有的量表：
 * <ul>
 *   <li>PHQ-9 教学演示版 (ID: 3) - 抑郁</li>
 *   <li>GAD-7 教学演示版 (ID: 4) - 焦虑</li>
 *   <li>SCL-90 教学演示版 (ID: 7) - 综合</li>
 *   <li>抑郁自评量表 SDS (ID: 8) - 抑郁</li>
 *   <li>焦虑自评量表 SAS (ID: 9) - 焦虑</li>
 * </ul>
 */
@Slf4j
@Service
public class QuizRecommendationService {

    /** 情绪-量表映射（仅使用数据库已有量表） */
    private static final Map<String, List<QuizRecommendation>> EMOTION_QUIZ_MAP = Map.of(
            "抑郁", List.of(
                    new QuizRecommendation(3L, "PHQ-9 教学演示版", "抑郁程度评估"),
                    new QuizRecommendation(8L, "抑郁自评量表（SDS）", "抑郁程度评估")
            ),
            "焦虑", List.of(
                    new QuizRecommendation(4L, "GAD-7 教学演示版", "焦虑程度评估"),
                    new QuizRecommendation(9L, "焦虑自评量表（SAS）", "焦虑程度评估")
            ),
            "压力", List.of(
                    new QuizRecommendation(7L, "SCL-90 教学演示版", "综合心理健康评估")
            ),
            "情绪低落", List.of(
                    new QuizRecommendation(3L, "PHQ-9 教学演示版", "抑郁程度评估"),
                    new QuizRecommendation(8L, "抑郁自评量表（SDS）", "抑郁程度评估")
            ),
            "紧张", List.of(
                    new QuizRecommendation(4L, "GAD-7 教学演示版", "焦虑程度评估"),
                    new QuizRecommendation(9L, "焦虑自评量表（SAS）", "焦虑程度评估")
            ),
            "失眠", List.of(
                    new QuizRecommendation(7L, "SCL-90 教学演示版", "综合心理健康评估")
            ),
            "注意力分散", List.of(
                    new QuizRecommendation(7L, "SCL-90 教学演示版", "综合心理健康评估")
            )
    );

    /**
     * 根据情绪推荐量表
     *
     * @param emotionType 情绪类型
     * @return 量表推荐列表
     */
    public List<QuizRecommendation> recommendByEmotion(String emotionType) {
        if (emotionType == null || emotionType.isBlank()) {
            return List.of();
        }

        List<QuizRecommendation> recommendations = EMOTION_QUIZ_MAP.get(emotionType);
        if (recommendations == null) {
            return List.of();
        }

        log.info("根据情绪 {} 推荐量表: {}", emotionType, recommendations);
        return recommendations;
    }

    /**
     * 量表推荐内部类
     */
    public static class QuizRecommendation {
        private Long quizId;
        private String quizName;
        private String description;

        public QuizRecommendation(Long quizId, String quizName, String description) {
            this.quizId = quizId;
            this.quizName = quizName;
            this.description = description;
        }

        // Getters and Setters
        public Long getQuizId() { return quizId; }
        public void setQuizId(Long quizId) { this.quizId = quizId; }
        public String getQuizName() { return quizName; }
        public void setQuizName(String quizName) { this.quizName = quizName; }
        public String getDescription() { return description; }
        public void setDescription(String description) { this.description = description; }

        @Override
        public String toString() {
            return "QuizRecommendation{" +
                    "quizId=" + quizId +
                    ", quizName='" + quizName + '\'' +
                    ", description='" + description + '\'' +
                    '}';
        }
    }
}
