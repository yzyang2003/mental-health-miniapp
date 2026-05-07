package com.example.demo.module.consult.safety;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import static org.junit.jupiter.api.Assertions.*;

/**
 * 内容安全检测服务测试
 */
class DefaultContentSafetyServiceTest {

    private DefaultContentSafetyService contentSafetyService;

    @BeforeEach
    void setUp() {
        contentSafetyService = new DefaultContentSafetyService(null);
    }

    @Test
    void check_nullContent_returnsSafe() {
        SafetyResult result = contentSafetyService.check(null);
        assertEquals(SafetyResult.SAFE, result);
    }

    @Test
    void check_emptyContent_returnsSafe() {
        SafetyResult result = contentSafetyService.check("");
        assertEquals(SafetyResult.SAFE, result);
    }

    @Test
    void check_blankContent_returnsSafe() {
        SafetyResult result = contentSafetyService.check("   ");
        assertEquals(SafetyResult.SAFE, result);
    }

    @Test
    void check_normalContent_returnsSafe() {
        SafetyResult result = contentSafetyService.check("今天天气不错");
        assertEquals(SafetyResult.SAFE, result);
    }

    @ParameterizedTest
    @CsvSource({
        "想自杀, CRISIS",
        "想跳楼, CRISIS",
        "想割腕, CRISIS",
        "想死, CRISIS",
        "结束生命, CRISIS",
        "活不下去, CRISIS",
        "不想活, CRISIS",
        "活着没意思, CRISIS",
        "想离开这个世界, CRISIS",
        "想消失, CRISIS",
        "想解脱, CRISIS",
        "遗书, CRISIS",
        "遗言, CRISIS"
    })
    void check_crisisKeywords_returnsCrisis(String content, String expected) {
        SafetyResult result = contentSafetyService.check(content);
        assertEquals(SafetyResult.valueOf(expected), result);
    }

    @ParameterizedTest
    @CsvSource({
        "色情, UNSAFE",
        "暴力, UNSAFE",
        "赌博, UNSAFE",
        "毒品, UNSAFE",
        "枪支, UNSAFE",
        "炸弹, UNSAFE"
    })
    void check_unsafeKeywords_returnsUnsafe(String content, String expected) {
        SafetyResult result = contentSafetyService.check(content);
        assertEquals(SafetyResult.valueOf(expected), result);
    }

    @Test
    void check_crisisKeywordCaseInsensitive_returnsCrisis() {
        SafetyResult result = contentSafetyService.check("想自杀");
        assertEquals(SafetyResult.CRISIS, result);
    }

    @Test
    void check_crisisKeywordInSentence_returnsCrisis() {
        SafetyResult result = contentSafetyService.check("我最近压力好大，想自杀");
        assertEquals(SafetyResult.CRISIS, result);
    }

    @Test
    void check_campusCrisisKeywords_returnsCrisis() {
        SafetyResult result = contentSafetyService.check("考试考砸了人生完了");
        assertEquals(SafetyResult.CRISIS, result);
    }

    @Test
    void check_emotionalCrisisKeywords_returnsCrisis() {
        SafetyResult result = contentSafetyService.check("失恋了活不下去");
        assertEquals(SafetyResult.CRISIS, result);
    }

    @Test
    void check_variantCrisisKeywords_returnsCrisis() {
        SafetyResult result = contentSafetyService.check("活着没意思");
        assertEquals(SafetyResult.CRISIS, result);
    }
}
