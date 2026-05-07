package com.example.demo.module.consult.emotion;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import static org.junit.jupiter.api.Assertions.*;

/**
 * 情绪识别服务测试
 */
class EmotionRecognitionServiceTest {

    private EmotionRecognitionService emotionRecognitionService;

    @BeforeEach
    void setUp() {
        emotionRecognitionService = new EmotionRecognitionService();
    }

    @Test
    void recognize_nullContent_returnsNeutral() {
        EmotionResult result = emotionRecognitionService.recognize(null);
        assertTrue(result.isNeutral());
        assertTrue(result.getEmotions().isEmpty());
    }

    @Test
    void recognize_emptyContent_returnsNeutral() {
        EmotionResult result = emotionRecognitionService.recognize("");
        assertTrue(result.isNeutral());
    }

    @Test
    void recognize_blankContent_returnsNeutral() {
        EmotionResult result = emotionRecognitionService.recognize("   ");
        assertTrue(result.isNeutral());
    }

    @Test
    void recognize_noEmotion_returnsNeutral() {
        EmotionResult result = emotionRecognitionService.recognize("今天天气不错");
        assertTrue(result.isNeutral());
    }

    @ParameterizedTest
    @CsvSource({
        "我很焦虑, 焦虑",
        "感到紧张, 焦虑",
        "很担心, 焦虑",
        "心情低落, 抑郁",
        "感到抑郁, 抑郁",
        "很沮丧, 抑郁",
        "压力好大, 压力",
        "很疲惫, 压力",
        "很生气, 愤怒",
        "很烦躁, 愤怒",
        "很难过, 悲伤",
        "想哭, 悲伤",
        "睡不着, 失眠",
        "失眠了, 失眠",
        "注意力不集中, 注意力分散",
        "无法专注, 注意力分散"
    })
    void recognize_variousEmotions_detectsCorrectly(String content, String expectedEmotion) {
        EmotionResult result = emotionRecognitionService.recognize(content);
        assertFalse(result.isNeutral());
        assertTrue(result.hasEmotion(expectedEmotion), 
            "Expected emotion '" + expectedEmotion + "' not found in " + result.getEmotions());
    }

    @Test
    void recognize_highIntensity_detectsCorrectly() {
        EmotionResult result = emotionRecognitionService.recognize("非常焦虑");
        assertFalse(result.isNeutral());
        assertEquals("高强度", result.getIntensity());
    }

    @Test
    void recognize_mediumIntensity_detectsCorrectly() {
        EmotionResult result = emotionRecognitionService.recognize("很焦虑");
        assertFalse(result.isNeutral());
        assertEquals("中强度", result.getIntensity());
    }

    @Test
    void recognize_lowIntensity_detectsCorrectly() {
        EmotionResult result = emotionRecognitionService.recognize("有点焦虑");
        assertFalse(result.isNeutral());
        assertEquals("低强度", result.getIntensity());
    }

    @Test
    void recognize_multipleEmotions_detectsAll() {
        EmotionResult result = emotionRecognitionService.recognize("很焦虑，压力大，睡不着");
        assertFalse(result.isNeutral());
        assertTrue(result.hasEmotion("焦虑"));
        assertTrue(result.hasEmotion("压力"));
        assertTrue(result.hasEmotion("失眠"));
    }

    @Test
    void getPrimaryEmotion_withEmotion_returnsFirst() {
        EmotionResult result = emotionRecognitionService.recognize("很焦虑");
        assertEquals("焦虑", result.getPrimaryEmotion());
    }

    @Test
    void getPrimaryEmotion_neutral_returnsNull() {
        EmotionResult result = emotionRecognitionService.recognize("今天天气不错");
        assertNull(result.getPrimaryEmotion());
    }

    @Test
    void getEmotionDescription_returnsCorrect() {
        assertEquals("焦虑和紧张", emotionRecognitionService.getEmotionDescription("焦虑"));
        assertEquals("抑郁和低落", emotionRecognitionService.getEmotionDescription("抑郁"));
        assertEquals("压力和疲惫", emotionRecognitionService.getEmotionDescription("压力"));
        assertEquals("情绪波动", emotionRecognitionService.getEmotionDescription("未知"));
    }
}
