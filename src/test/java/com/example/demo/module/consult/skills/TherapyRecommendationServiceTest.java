package com.example.demo.module.consult.skills;

import com.example.demo.module.consult.skills.TherapyRecommendationService.TherapyRecommendation;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * 疗法推荐服务测试
 */
class TherapyRecommendationServiceTest {

    private TherapyRecommendationService therapyRecommendationService;

    @BeforeEach
    void setUp() {
        therapyRecommendationService = new TherapyRecommendationService();
    }

    @Test
    void recommendByEmotion_nullEmotion_returnsEmpty() {
        List<TherapyRecommendation> recommendations = therapyRecommendationService.recommendByEmotion(null);
        assertTrue(recommendations.isEmpty());
    }

    @Test
    void recommendByEmotion_emptyEmotion_returnsEmpty() {
        List<TherapyRecommendation> recommendations = therapyRecommendationService.recommendByEmotion("");
        assertTrue(recommendations.isEmpty());
    }

    @Test
    void recommendByEmotion_unknownEmotion_returnsEmpty() {
        List<TherapyRecommendation> recommendations = therapyRecommendationService.recommendByEmotion("开心");
        assertTrue(recommendations.isEmpty());
    }

    @Test
    void recommendByEmotion_anxiety_returnsBreathingExercise() {
        List<TherapyRecommendation> recommendations = therapyRecommendationService.recommendByEmotion("焦虑");
        assertFalse(recommendations.isEmpty());
        assertTrue(recommendations.stream().anyMatch(t -> t.getTherapyId() == 1L), 
            "Should recommend '三分钟呼吸空间' (ID=1)");
    }

    @Test
    void recommendByEmotion_tension_returnsBreathingExercise() {
        List<TherapyRecommendation> recommendations = therapyRecommendationService.recommendByEmotion("紧张");
        assertFalse(recommendations.isEmpty());
        assertTrue(recommendations.stream().anyMatch(t -> t.getTherapyId() == 1L), 
            "Should recommend '三分钟呼吸空间' (ID=1)");
    }

    @Test
    void recommendByEmotion_insomnia_returnsSleepChecklist() {
        List<TherapyRecommendation> recommendations = therapyRecommendationService.recommendByEmotion("失眠");
        assertFalse(recommendations.isEmpty());
        assertTrue(recommendations.stream().anyMatch(t -> t.getTherapyId() == 2L), 
            "Should recommend '睡前放松清单' (ID=2)");
    }

    @Test
    void recommendByEmotion_lowMood_returnsMicroAction() {
        List<TherapyRecommendation> recommendations = therapyRecommendationService.recommendByEmotion("情绪低落");
        assertFalse(recommendations.isEmpty());
        assertTrue(recommendations.stream().anyMatch(t -> t.getTherapyId() == 3L), 
            "Should recommend '情绪低落时的微行动' (ID=3)");
    }

    @Test
    void recommendByEmotion_depression_returnsMicroAction() {
        List<TherapyRecommendation> recommendations = therapyRecommendationService.recommendByEmotion("抑郁");
        assertFalse(recommendations.isEmpty());
        assertTrue(recommendations.stream().anyMatch(t -> t.getTherapyId() == 3L), 
            "Should recommend '情绪低落时的微行动' (ID=3)");
    }

    @Test
    void recommendByEmotion_pressure_returnsPressureRelease() {
        List<TherapyRecommendation> recommendations = therapyRecommendationService.recommendByEmotion("压力");
        assertFalse(recommendations.isEmpty());
        assertTrue(recommendations.stream().anyMatch(t -> t.getTherapyId() == 4L), 
            "Should recommend '压力卸载四步法' (ID=4)");
    }

    @Test
    void recommendByEmotion_attention_returnsAttentionExercise() {
        List<TherapyRecommendation> recommendations = therapyRecommendationService.recommendByEmotion("注意力分散");
        assertFalse(recommendations.isEmpty());
        assertTrue(recommendations.stream().anyMatch(t -> t.getTherapyId() == 5L), 
            "Should recommend '注意力回收练习' (ID=5)");
    }

    @ParameterizedTest
    @CsvSource({
        "焦虑, 1, 三分钟呼吸空间",
        "紧张, 1, 三分钟呼吸空间",
        "失眠, 2, 睡前放松清单",
        "情绪低落, 3, 情绪低落时的微行动",
        "抑郁, 3, 情绪低落时的微行动",
        "压力, 4, 压力卸载四步法",
        "注意力分散, 5, 注意力回收练习"
    })
    void recommendByEmotion_returnsCorrectTherapyInfo(String emotion, Long expectedId, String expectedName) {
        List<TherapyRecommendation> recommendations = therapyRecommendationService.recommendByEmotion(emotion);
        assertTrue(recommendations.stream()
            .anyMatch(t -> t.getTherapyId().equals(expectedId) && t.getTherapyName().equals(expectedName)));
    }
}
