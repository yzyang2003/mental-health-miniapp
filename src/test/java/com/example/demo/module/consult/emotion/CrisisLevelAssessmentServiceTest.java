package com.example.demo.module.consult.emotion;

import com.example.demo.module.consult.emotion.CrisisLevelAssessmentService.CrisisLevel;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import static org.junit.jupiter.api.Assertions.*;

/**
 * 危机等级判断服务测试
 */
class CrisisLevelAssessmentServiceTest {

    private CrisisLevelAssessmentService crisisLevelAssessmentService;
    private EmotionRecognitionService emotionRecognitionService;

    @BeforeEach
    void setUp() {
        crisisLevelAssessmentService = new CrisisLevelAssessmentService();
        emotionRecognitionService = new EmotionRecognitionService();
    }

    @Test
    void assess_nullContent_returnsLow() {
        EmotionResult emotionResult = emotionRecognitionService.recognize(null);
        CrisisLevel level = crisisLevelAssessmentService.assess(null, emotionResult);
        assertEquals(CrisisLevel.LOW, level);
    }

    @Test
    void assess_emptyContent_returnsLow() {
        EmotionResult emotionResult = emotionRecognitionService.recognize("");
        CrisisLevel level = crisisLevelAssessmentService.assess("", emotionResult);
        assertEquals(CrisisLevel.LOW, level);
    }

    @Test
    void assess_normalContent_returnsLow() {
        EmotionResult emotionResult = emotionRecognitionService.recognize("今天天气不错");
        CrisisLevel level = crisisLevelAssessmentService.assess("今天天气不错", emotionResult);
        assertEquals(CrisisLevel.LOW, level);
    }

    @ParameterizedTest
    @CsvSource({
        "想自杀, CRISIS",
        "想跳楼, CRISIS",
        "想割腕, CRISIS",
        "想死, CRISIS",
        "结束生命, CRISIS",
        "活不下去, HIGH",
        "不想活, HIGH",
        "活着没意思, HIGH",
        "撑不下去了, HIGH",
        "压力很大, LOW",
        "很焦虑, MODERATE",
        "很抑郁, MODERATE",
        "有点难过, LOW",
        "有点担心, LOW"
    })
    void assess_variousContent_returnsCorrectLevel(String content, String expectedLevel) {
        EmotionResult emotionResult = emotionRecognitionService.recognize(content);
        CrisisLevel level = crisisLevelAssessmentService.assess(content, emotionResult);
        assertEquals(CrisisLevel.valueOf(expectedLevel), level);
    }

    @Test
    void assess_highIntensityEmotion_returnsModerate() {
        EmotionResult emotionResult = emotionRecognitionService.recognize("非常焦虑");
        CrisisLevel level = crisisLevelAssessmentService.assess("非常焦虑", emotionResult);
        // 高强度情绪应提升到MODERATE
        assertEquals(CrisisLevel.MODERATE, level);
    }

    @Test
    void assess_lowIntensityEmotion_returnsLow() {
        EmotionResult emotionResult = emotionRecognitionService.recognize("有点难过");
        CrisisLevel level = crisisLevelAssessmentService.assess("有点难过", emotionResult);
        assertEquals(CrisisLevel.LOW, level);
    }
}
