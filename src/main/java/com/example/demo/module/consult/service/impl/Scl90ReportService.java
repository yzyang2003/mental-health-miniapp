package com.example.demo.module.consult.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.example.demo.module.consult.dto.AnswerItem;
import com.example.demo.module.consult.dto.QuestionOptionItem;
import com.example.demo.module.consult.dto.Scl90FactorVO;
import com.example.demo.module.consult.dto.Scl90ReportVO;
import com.example.demo.module.consult.entity.Question;
import com.example.demo.module.consult.entity.QuizResult;
import com.example.demo.module.consult.entity.Scl90FactorConfig;
import com.example.demo.module.consult.mapper.QuestionMapper;
import com.example.demo.module.consult.mapper.Scl90FactorConfigMapper;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * SCL-90 量表报告生成服务。
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class Scl90ReportService {

    private static final int STATUS_ENABLED = 1;
    private static final String TYPE_SCL90_DEMO = "SCL90_DEMO";
    private static final int SCL90_ITEM_COUNT = 90;
    private static final int SCL90_POSITIVE_THRESHOLD = 2;

    private final QuestionMapper questionMapper;
    private final Scl90FactorConfigMapper scl90FactorConfigMapper;

    private Map<String, int[]> factorItems = new LinkedHashMap<>();
    private Map<String, String> factorReference = new LinkedHashMap<>();

    @PostConstruct
    public void init() {
        loadConfig();
    }

    /**
     * 从数据库加载SCL-90因子配置。
     */
    public void loadConfig() {
        List<Scl90FactorConfig> configs = scl90FactorConfigMapper.selectList(
                new LambdaQueryWrapper<Scl90FactorConfig>()
                        .orderByAsc(Scl90FactorConfig::getSortOrder)
        );

        Map<String, int[]> newFactorItems = new LinkedHashMap<>();
        Map<String, String> newFactorReference = new LinkedHashMap<>();

        for (Scl90FactorConfig config : configs) {
            String name = config.getFactorName();
            int[] items = parseItemNumbers(config.getItemNumbers());
            newFactorItems.put(name, items);
            newFactorReference.put(name, config.getReferenceValue());
        }

        this.factorItems = newFactorItems;
        this.factorReference = newFactorReference;
        log.info("Loaded SCL-90 factor config: {} factors", factorItems.size());
    }

    /**
     * 刷新配置（供外部调用热更新）。
     */
    public void refreshConfig() {
        loadConfig();
    }

    private int[] parseItemNumbers(String itemNumbers) {
        if (itemNumbers == null || itemNumbers.isBlank()) {
            return new int[0];
        }
        String[] parts = itemNumbers.split(",");
        int[] result = new int[parts.length];
        for (int i = 0; i < parts.length; i++) {
            result[i] = Integer.parseInt(parts[i].trim());
        }
        return result;
    }

    public Scl90ReportVO buildScl90Report(QuizResult quizResult, String questionnaireType) {
        if (!TYPE_SCL90_DEMO.equalsIgnoreCase(questionnaireType) || quizResult == null) {
            return null;
        }
        List<AnswerItem> answers = quizResult.getAnswers();
        if (answers == null || answers.isEmpty()) {
            return null;
        }

        List<Question> questions = getEnabledQuestions(quizResult.getQuestionnaireId());
        if (questions.isEmpty()) {
            return null;
        }
        Map<Long, Question> questionMap = questions.stream()
                .collect(Collectors.toMap(Question::getId, Function.identity(), (left, right) -> left));

        int[] itemScores = new int[SCL90_ITEM_COUNT + 1];
        int totalScore = 0;
        int positiveCount = 0;
        int positiveScoreSum = 0;

        for (AnswerItem answer : answers) {
            if (answer == null || answer.getQuestionId() == null || answer.getSelectedOptionIndex() == null) {
                continue;
            }
            Question question = questionMap.get(answer.getQuestionId());
            if (question == null || question.getSortOrder() == null) {
                continue;
            }
            int itemNo = question.getSortOrder();
            if (itemNo < 1 || itemNo > SCL90_ITEM_COUNT) {
                continue;
            }
            List<QuestionOptionItem> options = question.getOptions();
            if (options == null || options.isEmpty()) {
                continue;
            }
            int selected = answer.getSelectedOptionIndex();
            if (selected < 0 || selected >= options.size()) {
                continue;
            }
            int score = options.get(selected).getScore() == null ? 0 : options.get(selected).getScore();
            itemScores[itemNo] = score;
            totalScore += score;
            if (score >= SCL90_POSITIVE_THRESHOLD) {
                positiveCount++;
                positiveScoreSum += score;
            }
        }

        int negativeCount = Math.max(0, SCL90_ITEM_COUNT - positiveCount);
        double totalAvg = round1(totalScore / (double) SCL90_ITEM_COUNT);
        double positiveAvg = positiveCount <= 0 ? 0 : round1(positiveScoreSum / (double) positiveCount);

        Scl90ReportVO report = new Scl90ReportVO();
        report.setTotalScore(totalScore);
        report.setTotalAvg(totalAvg);
        report.setPositiveCount(positiveCount);
        report.setNegativeCount(negativeCount);
        report.setPositiveAvg(positiveAvg);
        report.setTotalLevel(resolveScl90Level(totalAvg));
        report.setOverview(buildScl90Overview(totalScore, totalAvg, positiveCount));
        report.setFactors(buildScl90Factors(itemScores));
        return report;
    }

    public List<Scl90FactorVO> buildScl90Factors(int[] itemScores) {
        List<Scl90FactorVO> factors = new ArrayList<>();
        for (Map.Entry<String, int[]> entry : factorItems.entrySet()) {
            String factorName = entry.getKey();
            int sum = 0;
            int count = 0;
            for (int idx : entry.getValue()) {
                if (idx >= 1 && idx < itemScores.length && itemScores[idx] > 0) {
                    sum += itemScores[idx];
                    count++;
                }
            }
            double avg = count == 0 ? 0 : round2(sum / (double) count);
            Scl90FactorVO factor = new Scl90FactorVO();
            factor.setName(factorName);
            factor.setTotalScore(sum);
            factor.setAvgScore(avg);
            factor.setLevel(resolveScl90Level(avg));
            factor.setReference(factorReference.getOrDefault(factorName, "-"));
            factor.setSummary(buildScl90FactorSummary(factorName, sum, avg, count));
            factors.add(factor);
        }
        return factors;
    }

    public static String resolveScl90Level(double score) {
        if (score < 1.5) {
            return "无症状";
        }
        if (score < 2.5) {
            return "轻度";
        }
        if (score < 3.5) {
            return "中度";
        }
        return "重度";
    }

    public static String buildScl90Overview(int totalScore, double totalAvg, int positiveCount) {
        return "总分为 " + totalScore + " 分，总症状指数约 " + totalAvg + "，阳性项目数约 " + positiveCount
                + " 项。该结果用于心理健康教育演示与自助观察，不能替代专业心理诊疗。";
    }

    public static String buildScl90FactorSummary(String factorName, int total, double avg, int count) {
        return factorName + " 维度共 " + count + " 项，当前总分 " + total + "，均分约 " + avg
                + "，提示为「" + resolveScl90Level(avg) + "」。请结合近期学习、人际与作息状态综合判断。";
    }

    public static double round1(double value) {
        return Math.round(value * 10.0) / 10.0;
    }

    public static double round2(double value) {
        return Math.round(value * 100.0) / 100.0;
    }

    private List<Question> getEnabledQuestions(Long questionnaireId) {
        return questionMapper.selectList(
                new LambdaQueryWrapper<Question>()
                        .eq(Question::getQuestionnaireId, questionnaireId)
                        .eq(Question::getStatus, STATUS_ENABLED)
                        .orderByAsc(Question::getSortOrder)
                        .orderByAsc(Question::getId)
        );
    }
}
