package com.example.demo.module.consult.skills;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import static org.junit.jupiter.api.Assertions.*;

/**
 * 校园场景技能测试
 */
class CampusScenarioSkillsTest {

    private CampusScenarioSkills campusScenarioSkills;

    @BeforeEach
    void setUp() {
        campusScenarioSkills = new CampusScenarioSkills();
    }

    @Test
    void identifyScenario_nullContent_returnsNull() {
        String scenario = campusScenarioSkills.identifyScenario(null);
        assertNull(scenario);
    }

    @Test
    void identifyScenario_emptyContent_returnsNull() {
        String scenario = campusScenarioSkills.identifyScenario("");
        assertNull(scenario);
    }

    @Test
    void identifyScenario_noScenario_returnsNull() {
        String scenario = campusScenarioSkills.identifyScenario("今天天气不错");
        assertNull(scenario);
    }

    @ParameterizedTest
    @CsvSource({
        "期末考试快到了, 学业压力",
        "挂科了怎么办, 学业压力",
        "成绩不好, 学业压力",
        "论文写不完, 学业压力",
        "考研压力大, 学业压力",
        "室友关系不好, 人际关系",
        "被同学欺负, 人际关系",
        "和朋友吵架, 人际关系",
        "被孤立了, 人际关系",
        "社交困难, 人际关系",
        "失恋了, 情感问题",
        "分手了, 情感问题",
        "暗恋一个人, 情感问题",
        "感情问题, 情感问题",
        "找不到工作, 职业规划",
        "实习好迷茫, 职业规划",
        "职业规划, 职业规划",
        "就业压力, 职业规划",
        "未来方向, 职业规划"
    })
    void identifyScenario_variousScenarios_detectsCorrectly(String content, String expectedScenario) {
        String scenario = campusScenarioSkills.identifyScenario(content);
        assertEquals(expectedScenario, scenario);
    }

    @Test
    void generateScenarioResponse_academicPressure_returnsResponse() {
        String response = campusScenarioSkills.generateScenarioResponse("学业压力");
        assertNotNull(response);
        assertFalse(response.isEmpty());
    }

    @Test
    void generateScenarioResponse_interpersonal_returnsResponse() {
        String response = campusScenarioSkills.generateScenarioResponse("人际关系");
        assertNotNull(response);
        assertFalse(response.isEmpty());
    }

    @Test
    void generateScenarioResponse_emotional_returnsResponse() {
        String response = campusScenarioSkills.generateScenarioResponse("情感问题");
        assertNotNull(response);
        assertFalse(response.isEmpty());
    }

    @Test
    void generateScenarioResponse_career_returnsResponse() {
        String response = campusScenarioSkills.generateScenarioResponse("职业规划");
        assertNotNull(response);
        assertFalse(response.isEmpty());
    }

    @Test
    void generateScenarioResponse_unknownScenario_returnsNull() {
        String response = campusScenarioSkills.generateScenarioResponse("未知场景");
        assertNull(response);
    }

    @Test
    void generateScenarioResponse_nullScenario_returnsNull() {
        String response = campusScenarioSkills.generateScenarioResponse(null);
        assertNull(response);
    }
}
