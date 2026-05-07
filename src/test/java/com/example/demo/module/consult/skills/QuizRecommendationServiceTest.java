package com.example.demo.module.consult.skills;

import com.example.demo.module.consult.skills.QuizRecommendationService.QuizRecommendation;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * 量表推荐服务测试
 */
class QuizRecommendationServiceTest {

    private QuizRecommendationService quizRecommendationService;

    @BeforeEach
    void setUp() {
        quizRecommendationService = new QuizRecommendationService();
    }

    @Test
    void recommendByEmotion_nullEmotion_returnsEmpty() {
        List<QuizRecommendation> recommendations = quizRecommendationService.recommendByEmotion(null);
        assertTrue(recommendations.isEmpty());
    }

    @Test
    void recommendByEmotion_emptyEmotion_returnsEmpty() {
        List<QuizRecommendation> recommendations = quizRecommendationService.recommendByEmotion("");
        assertTrue(recommendations.isEmpty());
    }

    @Test
    void recommendByEmotion_unknownEmotion_returnsEmpty() {
        List<QuizRecommendation> recommendations = quizRecommendationService.recommendByEmotion("开心");
        assertTrue(recommendations.isEmpty());
    }

    @Test
    void recommendByEmotion_depression_returnsPHQ9() {
        List<QuizRecommendation> recommendations = quizRecommendationService.recommendByEmotion("抑郁");
        assertFalse(recommendations.isEmpty());
        assertTrue(recommendations.stream().anyMatch(q -> q.getQuizId() == 3L), "Should recommend PHQ-9 (ID=3)");
        assertTrue(recommendations.stream().anyMatch(q -> q.getQuizId() == 8L), "Should recommend SDS (ID=8)");
    }

    @Test
    void recommendByEmotion_anxiety_returnsGAD7() {
        List<QuizRecommendation> recommendations = quizRecommendationService.recommendByEmotion("焦虑");
        assertFalse(recommendations.isEmpty());
        assertTrue(recommendations.stream().anyMatch(q -> q.getQuizId() == 4L), "Should recommend GAD-7 (ID=4)");
        assertTrue(recommendations.stream().anyMatch(q -> q.getQuizId() == 9L), "Should recommend SAS (ID=9)");
    }

    @Test
    void recommendByEmotion_pressure_returnsSCL90() {
        List<QuizRecommendation> recommendations = quizRecommendationService.recommendByEmotion("压力");
        assertFalse(recommendations.isEmpty());
        assertTrue(recommendations.stream().anyMatch(q -> q.getQuizId() == 7L), "Should recommend SCL-90 (ID=7)");
    }

    @Test
    void recommendByEmotion_lowMood_returnsPHQ9() {
        List<QuizRecommendation> recommendations = quizRecommendationService.recommendByEmotion("情绪低落");
        assertFalse(recommendations.isEmpty());
        assertTrue(recommendations.stream().anyMatch(q -> q.getQuizId() == 3L), "Should recommend PHQ-9 (ID=3)");
    }

    @Test
    void recommendByEmotion_tension_returnsGAD7() {
        List<QuizRecommendation> recommendations = quizRecommendationService.recommendByEmotion("紧张");
        assertFalse(recommendations.isEmpty());
        assertTrue(recommendations.stream().anyMatch(q -> q.getQuizId() == 4L), "Should recommend GAD-7 (ID=4)");
    }

    @Test
    void recommendByEmotion_insomnia_returnsSCL90() {
        List<QuizRecommendation> recommendations = quizRecommendationService.recommendByEmotion("失眠");
        assertFalse(recommendations.isEmpty());
        assertTrue(recommendations.stream().anyMatch(q -> q.getQuizId() == 7L), "Should recommend SCL-90 (ID=7)");
    }

    @Test
    void recommendByEmotion_attention_returnsSCL90() {
        List<QuizRecommendation> recommendations = quizRecommendationService.recommendByEmotion("注意力分散");
        assertFalse(recommendations.isEmpty());
        assertTrue(recommendations.stream().anyMatch(q -> q.getQuizId() == 7L), "Should recommend SCL-90 (ID=7)");
    }

    @ParameterizedTest
    @CsvSource({
        "抑郁, 3, PHQ-9 教学演示版",
        "抑郁, 8, 抑郁自评量表（SDS）",
        "焦虑, 4, GAD-7 教学演示版",
        "焦虑, 9, 焦虑自评量表（SAS）",
        "压力, 7, SCL-90 教学演示版"
    })
    void recommendByEmotion_returnsCorrectQuizInfo(String emotion, Long expectedId, String expectedName) {
        List<QuizRecommendation> recommendations = quizRecommendationService.recommendByEmotion(emotion);
        assertTrue(recommendations.stream()
            .anyMatch(q -> q.getQuizId().equals(expectedId) && q.getQuizName().equals(expectedName)));
    }
}
