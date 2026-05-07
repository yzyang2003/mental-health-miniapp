package com.example.demo.module.consult.skills;

import com.example.demo.module.consult.emotion.CrisisLevelAssessmentService.CrisisLevel;
import com.example.demo.module.consult.emotion.EmotionResult;
import com.example.demo.module.consult.skills.CrisisInterventionService.CrisisInterventionResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * 危机干预服务测试
 */
class CrisisInterventionServiceTest {

    private CrisisInterventionService crisisInterventionService;

    @BeforeEach
    void setUp() {
        crisisInterventionService = new CrisisInterventionService();
    }

    @Test
    void intervene_crisisLevel_returnsHotline() {
        EmotionResult emotionResult = EmotionResult.neutral();
        CrisisInterventionResponse response = crisisInterventionService.intervene(CrisisLevel.CRISIS, emotionResult);
        
        assertNotNull(response.getMessage());
        assertTrue(response.getMessage().contains("400-161-9995"), "Should contain hotline number");
        assertEquals("400-161-9995", response.getHotline());
        assertTrue(response.isRequiresImmediateAction());
    }

    @Test
    void intervene_highLevel_returnsHotline() {
        EmotionResult emotionResult = EmotionResult.neutral();
        CrisisInterventionResponse response = crisisInterventionService.intervene(CrisisLevel.HIGH, emotionResult);
        
        assertNotNull(response.getMessage());
        assertTrue(response.getMessage().contains("400-161-9995"), "Should contain hotline number");
        assertEquals("400-161-9995", response.getHotline());
        assertFalse(response.isRequiresImmediateAction());
    }

    @Test
    void intervene_moderateLevel_returnsSupport() {
        EmotionResult emotionResult = EmotionResult.neutral();
        CrisisInterventionResponse response = crisisInterventionService.intervene(CrisisLevel.MODERATE, emotionResult);
        
        assertNotNull(response.getMessage());
        assertTrue(response.getMessage().contains("400-161-9995"), "Should contain hotline number");
        assertNull(response.getHotline());
        assertFalse(response.isRequiresImmediateAction());
    }

    @Test
    void intervene_lowLevel_returnsCompanionship() {
        EmotionResult emotionResult = EmotionResult.neutral();
        CrisisInterventionResponse response = crisisInterventionService.intervene(CrisisLevel.LOW, emotionResult);
        
        assertNotNull(response.getMessage());
        assertFalse(response.getMessage().contains("400-161-9995"), "Low level should not contain hotline");
        assertNull(response.getHotline());
        assertFalse(response.isRequiresImmediateAction());
    }

    @Test
    void intervene_allLevels_haveMessages() {
        EmotionResult emotionResult = EmotionResult.neutral();
        
        for (CrisisLevel level : CrisisLevel.values()) {
            CrisisInterventionResponse response = crisisInterventionService.intervene(level, emotionResult);
            assertNotNull(response.getMessage(), "Message should not be null for level: " + level);
            assertFalse(response.getMessage().isEmpty(), "Message should not be empty for level: " + level);
        }
    }

    @Test
    void intervene_crisisLevel_setsCorrectLevel() {
        EmotionResult emotionResult = EmotionResult.neutral();
        CrisisInterventionResponse response = crisisInterventionService.intervene(CrisisLevel.CRISIS, emotionResult);
        assertEquals(CrisisLevel.CRISIS, response.getCrisisLevel());
    }
}
