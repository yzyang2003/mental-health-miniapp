package com.example.demo.module.consult.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.example.demo.exception.BusinessException;
import com.example.demo.module.consult.dto.AnswerItem;
import com.example.demo.module.consult.dto.QuestionOptionItem;
import com.example.demo.module.consult.dto.QuestionVO;
import com.example.demo.module.consult.dto.QuestionnaireVO;
import com.example.demo.module.consult.dto.QuizResultVO;
import com.example.demo.module.consult.dto.Scl90FactorVO;
import com.example.demo.module.consult.dto.Scl90ReportVO;
import com.example.demo.module.consult.dto.SubmitQuizRequest;
import com.example.demo.module.consult.entity.Question;
import com.example.demo.module.consult.entity.Questionnaire;
import com.example.demo.module.consult.entity.QuizResult;
import com.example.demo.module.consult.mapper.QuestionMapper;
import com.example.demo.module.consult.mapper.QuestionnaireMapper;
import com.example.demo.module.consult.client.OpenAiCompletionClient;
import com.example.demo.module.consult.mapper.QuizResultMapper;
import com.example.demo.module.consult.service.QuizService;
import jakarta.annotation.PreDestroy;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.function.Function;
import java.util.Comparator;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

/**
 * 心理测评服务实现。
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class QuizServiceImpl implements QuizService {

    private static final int STATUS_ENABLED = 1;
    private static final long RETRY_AI_GUIDANCE_COOLDOWN_MS = 10_000L;
    private static final ExecutorService QUIZ_AI_EXECUTOR = Executors.newFixedThreadPool(4);

    private static final int MAX_AI_GUIDANCE_CHARS = 8000;

    private static final String DISCLAIMER = "本测评仅为自助参考，不能替代专业医疗诊断。若情绪困扰持续或加重，请及时寻求专业帮助。";
    private static final String TYPE_SCL90_DEMO = "SCL90_DEMO";
    private static final int SCL90_ITEM_COUNT = 90;
    private static final int SCL90_POSITIVE_THRESHOLD = 2;
    private static final int SCL90_AI_MAX_FACTORS = 10;

    private static final Map<String, int[]> SCL90_FACTOR_ITEMS = new LinkedHashMap<>();
    private static final Map<String, String> SCL90_FACTOR_REFERENCE = new LinkedHashMap<>();

    static {
        SCL90_FACTOR_ITEMS.put("躯体化", new int[]{1, 4, 12, 27, 40, 42, 48, 49, 52, 53, 56, 58});
        SCL90_FACTOR_ITEMS.put("强迫症状", new int[]{3, 9, 10, 28, 38, 45, 46, 51, 55, 65});
        SCL90_FACTOR_ITEMS.put("人际敏感", new int[]{6, 21, 34, 36, 37, 41, 61, 69, 73});
        SCL90_FACTOR_ITEMS.put("抑郁", new int[]{5, 14, 15, 20, 22, 26, 29, 30, 31, 32, 54, 71, 79});
        SCL90_FACTOR_ITEMS.put("焦虑", new int[]{2, 17, 23, 33, 39, 57, 72, 78, 80, 86});
        SCL90_FACTOR_ITEMS.put("敌对", new int[]{11, 24, 63, 67, 74, 81});
        SCL90_FACTOR_ITEMS.put("恐怖", new int[]{13, 25, 47, 50, 70, 75, 82});
        SCL90_FACTOR_ITEMS.put("偏执", new int[]{8, 18, 43, 68, 76, 83});
        SCL90_FACTOR_ITEMS.put("精神病性", new int[]{7, 16, 35, 62, 77, 84, 85, 87, 88, 90});
        SCL90_FACTOR_ITEMS.put("其他", new int[]{19, 44, 59, 60, 64, 66, 89});

        SCL90_FACTOR_REFERENCE.put("躯体化", "1.37±0.48");
        SCL90_FACTOR_REFERENCE.put("强迫症状", "1.62±0.58");
        SCL90_FACTOR_REFERENCE.put("人际敏感", "1.65±0.61");
        SCL90_FACTOR_REFERENCE.put("抑郁", "1.50±0.59");
        SCL90_FACTOR_REFERENCE.put("焦虑", "1.39±0.43");
        SCL90_FACTOR_REFERENCE.put("敌对", "1.46±0.55");
        SCL90_FACTOR_REFERENCE.put("恐怖", "1.23±0.41");
        SCL90_FACTOR_REFERENCE.put("偏执", "1.43±0.57");
        SCL90_FACTOR_REFERENCE.put("精神病性", "1.29±0.42");
        SCL90_FACTOR_REFERENCE.put("其他", "-");
    }

    private static final String QUIZ_AI_SYSTEM_PROMPT = """
            你是校园心理陪伴助手「小爱」的测评报告分支。根据用户提供的结构化测评摘要，输出两段内容（使用简体中文）：
            1）第一行标题为「解读摘要」，换行后写 2～4 句支持性、非诊断性总结，避免医学诊断与绝对化表述。
            2）空一行后写标题「行动建议」，换行后给出 3～5 条具体、可执行的自助建议（每条一行，可用「·」或数字开头）。
            明确说明内容仅供自我调节参考，不可替代专业诊疗。语气温柔、简洁。""";

    private final QuestionnaireMapper questionnaireMapper;
    private final QuestionMapper questionMapper;
    private final QuizResultMapper quizResultMapper;
    private final OpenAiCompletionClient openAiCompletionClient;
    private final Map<String, Long> retryAiGuidanceLastTs = new ConcurrentHashMap<>();

    @PreDestroy
    void shutdownQuizAiExecutor() {
        QUIZ_AI_EXECUTOR.shutdown();
        try {
            if (!QUIZ_AI_EXECUTOR.awaitTermination(3, TimeUnit.SECONDS)) {
                QUIZ_AI_EXECUTOR.shutdownNow();
            }
        } catch (InterruptedException ex) {
            Thread.currentThread().interrupt();
            QUIZ_AI_EXECUTOR.shutdownNow();
        }
    }

    @Override
    public List<QuestionnaireVO> getAvailableQuestionnaires() {
        return questionnaireMapper.selectList(
                        new LambdaQueryWrapper<Questionnaire>()
                                .eq(Questionnaire::getStatus, STATUS_ENABLED)
                                .orderByDesc(Questionnaire::getCreateTime)
                ).stream()
                .map(item -> toQuestionnaireVO(item, false))
                .toList();
    }

    @Override
    public QuestionnaireVO getQuestionnaireDetail(Long id) {
        Questionnaire questionnaire = getEnabledQuestionnaire(id);
        return toQuestionnaireVO(questionnaire, true);
    }

    @Override
    public QuizResultVO submitQuiz(String openid, SubmitQuizRequest request) {
        if (!StringUtils.hasText(openid)) {
            throw new BusinessException("openid 不能为空");
        }
        if (request == null || request.getQuestionnaireId() == null) {
            throw new BusinessException("问卷 ID 不能为空");
        }
        if (request.getAnswers() == null || request.getAnswers().isEmpty()) {
            throw new BusinessException("答题内容不能为空");
        }

        Questionnaire questionnaire = getEnabledQuestionnaire(request.getQuestionnaireId());
        List<Question> questions = getEnabledQuestions(questionnaire.getId());
        if (questions.isEmpty()) {
            throw new BusinessException("当前问卷暂无可用题目");
        }

        Map<Long, AnswerItem> answerMap = request.getAnswers()
                .stream()
                .filter(item -> item.getQuestionId() != null)
                .collect(Collectors.toMap(AnswerItem::getQuestionId, Function.identity(), (left, right) -> right, HashMap::new));

        int totalScore = 0;
        int maxScore = 0;
        for (Question question : questions) {
            List<QuestionOptionItem> options = question.getOptions();
            if (options == null || options.isEmpty()) {
                throw new BusinessException("问卷题目选项配置异常");
            }

            int currentMaxScore = options.stream()
                    .map(QuestionOptionItem::getScore)
                    .filter(score -> score != null)
                    .max(Integer::compareTo)
                    .orElse(0);
            maxScore += currentMaxScore;

            AnswerItem answerItem = answerMap.get(question.getId());
            if (answerItem == null || answerItem.getSelectedOptionIndex() == null) {
                throw new BusinessException("请完成全部题目后再提交");
            }

            int selectedIndex = answerItem.getSelectedOptionIndex();
            if (selectedIndex < 0 || selectedIndex >= options.size()) {
                throw new BusinessException("提交的答案索引不合法");
            }

            QuestionOptionItem selectedOption = options.get(selectedIndex);
            totalScore += selectedOption.getScore() == null ? 0 : selectedOption.getScore();
        }

        String conclusion = buildConclusion(totalScore, maxScore, questionnaire.getType());
        int scorePercent = maxScore <= 0 ? 0 : (int) Math.round(totalScore * 100.0 / maxScore);
        List<String> suggestionLines = buildSuggestionLines(questionnaire.getType(), conclusion);
        String suggestions = String.join("\n", suggestionLines);
        String interpretation = buildInterpretation(scorePercent, questionnaire.getType(), conclusion);
        String aiChatHint = buildAiChatHint(questionnaire.getTitle(), conclusion, scorePercent);

        QuizResult previewResult = new QuizResult();
        previewResult.setQuestionnaireId(questionnaire.getId());
        previewResult.setAnswers(request.getAnswers());
        previewResult.setScore(totalScore);
        previewResult.setMaxScore(maxScore);
        previewResult.setScorePercent(scorePercent);
        previewResult.setConclusion(conclusion);
        previewResult.setSuggestions(suggestions);
        Scl90ReportVO scl90Report = buildScl90Report(previewResult, questionnaire.getType());

        QuizResult quizResult = new QuizResult();
        quizResult.setOpenid(openid);
        quizResult.setQuestionnaireId(questionnaire.getId());
        quizResult.setAnswers(request.getAnswers());
        quizResult.setScore(totalScore);
        quizResult.setMaxScore(maxScore);
        quizResult.setScorePercent(scorePercent);
        quizResult.setConclusion(conclusion);
        quizResult.setSuggestions(suggestions);
        quizResult.setAiChatHint(aiChatHint);
        quizResult.setAiGuidance(buildLocalGuidance(questionnaire.getType(), conclusion, suggestionLines, scl90Report));
        quizResultMapper.insert(quizResult);

        Long resultId = quizResult.getId();
        triggerAsyncAiGuidanceGeneration(
                resultId,
                questionnaire.getTitle(),
                questionnaire.getType(),
                totalScore,
                maxScore,
                scorePercent,
                conclusion,
                suggestionLines,
                scl90Report
        );

        QuizResult savedResult = quizResultMapper.selectById(resultId);
        return toQuizResultVO(
                savedResult,
                questionnaire.getTitle(),
                questionnaire.getType(),
                interpretation,
                suggestionLines
        );
    }

    @Override
    public List<QuizResultVO> getUserHistory(String openid) {
        if (!StringUtils.hasText(openid)) {
            return Collections.emptyList();
        }

        List<QuizResult> results = quizResultMapper.selectList(
                new LambdaQueryWrapper<QuizResult>()
                        .eq(QuizResult::getOpenid, openid)
                        .orderByDesc(QuizResult::getCreateTime)
        );
        if (results.isEmpty()) {
            return Collections.emptyList();
        }

        List<Questionnaire> questionnaires = questionnaireMapper.selectBatchIds(
                results.stream().map(QuizResult::getQuestionnaireId).distinct().toList()
        );
        Map<Long, Questionnaire> questionnaireMap = questionnaires.stream()
                .collect(Collectors.toMap(Questionnaire::getId, Function.identity(), (left, right) -> left));

        return results.stream()
                .map(item -> {
                    Questionnaire q = questionnaireMap.get(item.getQuestionnaireId());
                    String title = q != null ? q.getTitle() : "未知问卷";
                    String type = q != null ? q.getType() : "";
                    int percent = item.getScorePercent() == null ? 0 : item.getScorePercent();
                    String interpretation = buildInterpretation(percent, type, item.getConclusion());
                    List<String> lines = splitSuggestionLines(item.getSuggestions());
                    return toQuizResultVO(item, title, type, interpretation, lines);
                })
                .toList();
    }

    @Override
    public QuizResultVO getResultDetail(String openid, Long resultId) {
        if (!StringUtils.hasText(openid)) {
            throw new BusinessException("openid 不能为空");
        }
        if (resultId == null) {
            throw new BusinessException("测评结果 ID 不能为空");
        }
        QuizResult result = quizResultMapper.selectById(resultId);
        if (result == null) {
            throw new BusinessException("测评结果不存在");
        }
        if (!openid.equals(result.getOpenid())) {
            throw new BusinessException("无权查看该测评结果");
        }
        Questionnaire questionnaire = questionnaireMapper.selectById(result.getQuestionnaireId());
        String title = questionnaire != null ? questionnaire.getTitle() : "未知问卷";
        String type = questionnaire != null ? questionnaire.getType() : "";
        int percent = result.getScorePercent() == null ? 0 : result.getScorePercent();
        String interpretation = buildInterpretation(percent, type, result.getConclusion());
        List<String> lines = splitSuggestionLines(result.getSuggestions());
        return toQuizResultVO(result, title, type, interpretation, lines);
    }

    @Override
    public QuizResultVO retryAiGuidance(String openid, Long resultId) {
        if (!StringUtils.hasText(openid)) {
            throw new BusinessException("openid 不能为空");
        }
        if (resultId == null) {
            throw new BusinessException("测评结果 ID 不能为空");
        }
        String retryKey = openid + ":" + resultId;
        long now = System.currentTimeMillis();
        Long lastTs = retryAiGuidanceLastTs.get(retryKey);
        if (lastTs != null && now - lastTs < RETRY_AI_GUIDANCE_COOLDOWN_MS) {
            long remainSec = (RETRY_AI_GUIDANCE_COOLDOWN_MS - (now - lastTs) + 999) / 1000;
            throw new BusinessException("操作太频繁，请 " + remainSec + " 秒后再试");
        }
        retryAiGuidanceLastTs.put(retryKey, now);

        QuizResult quizResult = quizResultMapper.selectById(resultId);
        if (quizResult == null) {
            throw new BusinessException("测评结果不存在");
        }
        if (!openid.equals(quizResult.getOpenid())) {
            throw new BusinessException("无权操作该测评结果");
        }

        Questionnaire questionnaire = questionnaireMapper.selectById(quizResult.getQuestionnaireId());
        String questionnaireTitle = questionnaire != null ? questionnaire.getTitle() : "心理测评";
        String questionnaireType = questionnaire != null ? questionnaire.getType() : "";

        int score = quizResult.getScore() == null ? 0 : quizResult.getScore();
        int maxScore = quizResult.getMaxScore() == null ? 0 : quizResult.getMaxScore();
        int scorePercent = quizResult.getScorePercent() == null ? 0 : quizResult.getScorePercent();
        String conclusion = StringUtils.hasText(quizResult.getConclusion())
                ? quizResult.getConclusion()
                : buildConclusion(score, maxScore, questionnaireType);
        List<String> suggestionLines = splitSuggestionLines(quizResult.getSuggestions());

        Scl90ReportVO scl90Report = buildScl90Report(quizResult, questionnaireType);
        String aiGuidance = requestAiGuidanceSafe(
                questionnaireTitle,
                questionnaireType,
                score,
                maxScore,
                scorePercent,
                conclusion,
                suggestionLines,
                scl90Report
        );
        if (!StringUtils.hasText(aiGuidance)) {
            throw new BusinessException("AI 指导暂时不可用，请稍后重试");
        }

        QuizResult update = new QuizResult();
        update.setId(quizResult.getId());
        update.setAiGuidance(aiGuidance);
        quizResultMapper.updateById(update);

        QuizResult refreshed = quizResultMapper.selectById(resultId);
        String interpretation = buildInterpretation(scorePercent, questionnaireType, conclusion);
        return toQuizResultVO(refreshed, questionnaireTitle, questionnaireType, interpretation, suggestionLines);
    }

    private Questionnaire getEnabledQuestionnaire(Long id) {
        if (id == null) {
            throw new BusinessException("问卷 ID 不能为空");
        }

        Questionnaire questionnaire = questionnaireMapper.selectById(id);
        if (questionnaire == null || questionnaire.getStatus() == null || questionnaire.getStatus() != STATUS_ENABLED) {
            throw new BusinessException("问卷不存在或已停用");
        }
        return questionnaire;
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

    private QuestionnaireVO toQuestionnaireVO(Questionnaire questionnaire, boolean includeQuestions) {
        QuestionnaireVO questionnaireVO = new QuestionnaireVO();
        questionnaireVO.setId(questionnaire.getId());
        questionnaireVO.setTitle(questionnaire.getTitle());
        questionnaireVO.setDescription(questionnaire.getDescription());
        questionnaireVO.setCover(questionnaire.getCover());
        questionnaireVO.setType(questionnaire.getType());
        questionnaireVO.setCreateTime(questionnaire.getCreateTime());
        questionnaireVO.setUpdateTime(questionnaire.getUpdateTime());

        if (includeQuestions) {
            List<QuestionVO> questionVOS = getEnabledQuestions(questionnaire.getId())
                    .stream()
                    .map(this::toQuestionVO)
                    .toList();
            questionnaireVO.setQuestions(questionVOS);
        } else {
            questionnaireVO.setQuestions(Collections.emptyList());
        }
        return questionnaireVO;
    }

    private QuestionVO toQuestionVO(Question question) {
        QuestionVO questionVO = new QuestionVO();
        questionVO.setId(question.getId());
        questionVO.setContent(question.getContent());
        questionVO.setSortOrder(question.getSortOrder());
        questionVO.setOptions(question.getOptions() == null ? Collections.emptyList() : question.getOptions());
        return questionVO;
    }

    private QuizResultVO toQuizResultVO(
            QuizResult quizResult,
            String questionnaireTitle,
            String questionnaireType,
            String interpretation,
            List<String> suggestionLines
    ) {
        QuizResultVO quizResultVO = new QuizResultVO();
        quizResultVO.setId(quizResult.getId());
        quizResultVO.setQuestionnaireId(quizResult.getQuestionnaireId());
        quizResultVO.setQuestionnaireTitle(questionnaireTitle);
        quizResultVO.setQuestionnaireType(questionnaireType);
        quizResultVO.setScore(quizResult.getScore());
        quizResultVO.setMaxScore(quizResult.getMaxScore());
        quizResultVO.setScorePercent(quizResult.getScorePercent());
        quizResultVO.setConclusion(quizResult.getConclusion());
        quizResultVO.setInterpretation(interpretation);
        quizResultVO.setSuggestions(quizResult.getSuggestions());
        quizResultVO.setSuggestionLines(suggestionLines == null ? Collections.emptyList() : suggestionLines);
        quizResultVO.setDisclaimer(DISCLAIMER);
        quizResultVO.setAiChatHint(quizResult.getAiChatHint());
        quizResultVO.setAiGuidance(quizResult.getAiGuidance());
        quizResultVO.setScl90Report(buildScl90Report(quizResult, questionnaireType));
        quizResultVO.setCreateTime(quizResult.getCreateTime());
        return quizResultVO;
    }

    /**
     * 单次调用大模型生成测评指导；任何失败返回 {@code null}，不阻塞提交。
     */
    private String requestAiGuidanceSafe(
            String questionnaireTitle,
            String questionnaireType,
            int score,
            int maxScore,
            int scorePercent,
            String conclusion,
            List<String> ruleSuggestionLines,
            Scl90ReportVO scl90Report
    ) {
        try {
            List<Map<String, String>> messages;
            if (TYPE_SCL90_DEMO.equalsIgnoreCase(questionnaireType) && scl90Report != null) {
                String stage1Prompt = buildScl90AiUserPrompt(scl90Report);
                messages = List.of(
                        Map.of("role", "system", "content", "你是一位具有临床心理评估经验的 AI 助手，请严格依据用户提供的量表数据与常模输出结构化报告。"),
                        Map.of("role", "user", "content", stage1Prompt)
                );
                log.info("SCL90 AI stage1 prompt prepared, length={}", stage1Prompt.length());
            } else {
                String userContent = buildQuizAiUserPrompt(
                        questionnaireTitle,
                        questionnaireType,
                        score,
                        maxScore,
                        scorePercent,
                        conclusion,
                        ruleSuggestionLines
                );
                messages = List.of(
                        Map.of("role", "system", "content", QUIZ_AI_SYSTEM_PROMPT.trim()),
                        Map.of("role", "user", "content", userContent)
                );
            }
            String raw = openAiCompletionClient.complete(messages);
            if (!StringUtils.hasText(raw)) {
                log.warn("Quiz AI guidance empty content, type={}", questionnaireType);
                if (TYPE_SCL90_DEMO.equalsIgnoreCase(questionnaireType) && scl90Report != null) {
                    return buildScl90FallbackGuidance(scl90Report);
                }
                return null;
            }
            if (raw.length() > MAX_AI_GUIDANCE_CHARS) {
                return raw.substring(0, MAX_AI_GUIDANCE_CHARS).trim();
            }
            return raw.trim();
        } catch (Exception ex) {
            log.warn("Quiz AI guidance generation failed", ex);
            if (TYPE_SCL90_DEMO.equalsIgnoreCase(questionnaireType) && scl90Report != null) {
                return buildScl90FallbackGuidance(scl90Report);
            }
            return null;
        }
    }

    private static String buildQuizAiUserPrompt(
            String questionnaireTitle,
            String questionnaireType,
            int score,
            int maxScore,
            int scorePercent,
            String conclusion,
            List<String> ruleSuggestionLines
    ) {
        String title = StringUtils.hasText(questionnaireTitle) ? questionnaireTitle : "心理测评";
        String type = StringUtils.hasText(questionnaireType) ? questionnaireType : "未标注";
        StringBuilder sb = new StringBuilder();
        sb.append("问卷：").append(title).append("\n");
        sb.append("类型标识：").append(type).append("\n");
        sb.append("得分：").append(score).append(" / ").append(maxScore).append("\n");
        sb.append("得分占比：").append(scorePercent).append("%\n");
        sb.append("系统规则结论档：").append(conclusion).append("\n");
        sb.append("系统规则建议条目：\n");
        if (ruleSuggestionLines != null && !ruleSuggestionLines.isEmpty()) {
            IntStream.range(0, ruleSuggestionLines.size()).forEach(i ->
                    sb.append(i + 1).append(". ").append(ruleSuggestionLines.get(i)).append("\n")
            );
        } else {
            sb.append("（无）\n");
        }
        sb.append("请基于以上摘要生成「解读摘要」与「行动建议」，不要重复逐条照抄规则建议，应结合整体得分做个性化表述。");
        return sb.toString();
    }

    private static String buildScl90AiUserPrompt(Scl90ReportVO report) {
        StringBuilder sb = new StringBuilder();
        sb.append("请基于下列 SCL-90 数据输出结构化专业报告（第一阶段草稿），控制在 900 字内，强调关键风险与建议。\n\n");
        sb.append("【常模（N=1388）】\n");
        sb.append("- 躯体化：1.37±0.48\n");
        sb.append("- 强迫症状：1.62±0.58\n");
        sb.append("- 人际敏感：1.65±0.61\n");
        sb.append("- 抑郁：1.50±0.59\n");
        sb.append("- 焦虑：1.39±0.43\n");
        sb.append("- 敌对：1.46±0.55\n");
        sb.append("- 恐怖：1.23±0.41\n");
        sb.append("- 偏执：1.43±0.57\n");
        sb.append("- 精神病性：1.29±0.42\n\n");
        sb.append("【本次评估】\n");
        sb.append("- 总分：【").append(report.getTotalScore()).append("】\n");
        sb.append("- 总症状指数：【").append(report.getTotalAvg()).append("】\n");
        sb.append("- 阳性项目数：【").append(report.getPositiveCount()).append("】\n");
        List<Scl90FactorVO> factors = report.getFactors() == null ? Collections.emptyList() : new ArrayList<>(report.getFactors());
        factors.sort(Comparator.comparing(Scl90FactorVO::getAvgScore, Comparator.nullsLast(Double::compareTo)).reversed());
        if (factors.size() > SCL90_AI_MAX_FACTORS) {
            factors = factors.subList(0, SCL90_AI_MAX_FACTORS);
        }
        for (Scl90FactorVO factor : factors) {
            sb.append("- ").append(factor.getName()).append("：均分").append(factor.getAvgScore())
                    .append("，总分").append(factor.getTotalScore()).append("\n");
        }
        sb.append("\n【生成规则】\n");
        sb.append("1) 开头必须写免责声明：“本报告基于量表数据自动生成，仅供自我了解参考，不能替代专业医疗诊断。”\n");
        sb.append("2) 严重程度分级：\n");
        sb.append("   - 无症状：均分 < 常模 M+SD\n");
        sb.append("   - 轻度：均分介于 M+SD 至 M+2SD 之间，或 2.0~2.9 区间\n");
        sb.append("   - 中度及以上：均分 ≥ 3.0 或超过 M+2SD\n");
        sb.append("3) 各维度必须按均分降序分析。\n");
        sb.append("4) 必须体现“抑郁-焦虑-躯体化”与“强迫-焦虑”关联。\n");
        sb.append("5) “其它”因子需单独解读睡眠/饮食并关联抑郁或焦虑。\n\n");
        sb.append("【输出结构】\n");
        sb.append("## 一、总体筛查结果简报\n");
        sb.append("## 二、各维度症状详细解读（降序）\n");
        sb.append("## 三、综合分析与生活建议\n");
        sb.append("## 四、专业求助指引\n");
        sb.append("\n请直接输出报告正文。");
        return sb.toString();
    }

    private static String buildScl90FallbackGuidance(Scl90ReportVO report) {
        List<Scl90FactorVO> factors = report.getFactors() == null ? Collections.emptyList() : new ArrayList<>(report.getFactors());
        factors.sort(Comparator.comparing(Scl90FactorVO::getAvgScore, Comparator.nullsLast(Double::compareTo)).reversed());
        String top1 = factors.isEmpty() ? "无明显突出维度" : factors.get(0).getName() + "（均分 " + factors.get(0).getAvgScore() + "）";
        String top2 = factors.size() < 2 ? "—" : factors.get(1).getName() + "（均分 " + factors.get(1).getAvgScore() + "）";
        return """
                本报告基于量表数据自动生成，仅供自我了解参考，不能替代专业医疗诊断。

                ## 一、总体筛查结果简报
                本次总分为 %d，总症状指数为 %.1f，阳性项目数为 %d，整体提示为 %s 区间。量表结果仅反映近期状态，不能作为确诊依据。

                ## 二、各维度症状详细解读（按均分降序）
                当前相对突出的维度：%s、%s。请优先关注这些维度在学习、作息和人际中的实际影响，并持续观察两周以上的变化趋势。

                ## 三、综合分析与生活建议
                建议从规律睡眠、行为激活（小步可完成任务）、每日 10 分钟呼吸放松入手；若伴随明显躯体不适，可同步做基础体检排查。

                ## 四、专业求助指引
                若连续两周以上心境低落、兴趣明显下降、焦虑与躯体不适持续加重，或出现自伤想法，请尽快联系心理咨询师或精神科医生。
                """.formatted(
                report.getTotalScore(),
                report.getTotalAvg() == null ? 0 : report.getTotalAvg(),
                report.getPositiveCount() == null ? 0 : report.getPositiveCount(),
                report.getTotalLevel() == null ? "待评估" : report.getTotalLevel(),
                top1,
                top2
        ).trim();
    }

    private static String buildLocalGuidance(String questionnaireType, String conclusion, List<String> suggestionLines, Scl90ReportVO scl90Report) {
        if (TYPE_SCL90_DEMO.equalsIgnoreCase(questionnaireType) && scl90Report != null) {
            return buildScl90FallbackGuidance(scl90Report);
        }
        StringBuilder sb = new StringBuilder();
        sb.append("本报告基于量表数据自动生成，仅供自我了解参考，不能替代专业医疗诊断。\n\n");
        sb.append("当前结果提示为「").append(conclusion).append("」。\n");
        if (suggestionLines != null && !suggestionLines.isEmpty()) {
            sb.append("建议你优先尝试以下做法：\n");
            for (int i = 0; i < suggestionLines.size(); i++) {
                sb.append(i + 1).append(". ").append(suggestionLines.get(i)).append("\n");
            }
        } else {
            sb.append("建议先保持规律作息、适度运动，并在情绪波动持续时及时寻求专业支持。\n");
        }
        sb.append("\n系统会在后台继续生成更个性化的 AI 指导，稍后将自动更新。");
        return sb.toString().trim();
    }

    private void triggerAsyncAiGuidanceGeneration(
            Long resultId,
            String questionnaireTitle,
            String questionnaireType,
            int score,
            int maxScore,
            int scorePercent,
            String conclusion,
            List<String> suggestionLines,
            Scl90ReportVO scl90Report
    ) {
        if (resultId == null) {
            return;
        }
        QUIZ_AI_EXECUTOR.submit(() -> {
            try {
                String aiGuidance = requestAiGuidanceSafe(
                        questionnaireTitle,
                        questionnaireType,
                        score,
                        maxScore,
                        scorePercent,
                        conclusion,
                        suggestionLines,
                        scl90Report
                );
                if (!StringUtils.hasText(aiGuidance)) {
                    return;
                }
                QuizResult update = new QuizResult();
                update.setId(resultId);
                update.setAiGuidance(aiGuidance);
                quizResultMapper.updateById(update);
            } catch (Exception ex) {
                log.warn("Async quiz AI guidance generation failed for resultId={}", resultId, ex);
            }
        });
    }

    private List<String> splitSuggestionLines(String suggestions) {
        if (!StringUtils.hasText(suggestions)) {
            return Collections.emptyList();
        }
        String[] parts = suggestions.split("\n");
        List<String> lines = new ArrayList<>();
        for (String part : parts) {
            if (StringUtils.hasText(part)) {
                lines.add(part.trim());
            }
        }
        return lines;
    }

    private Scl90ReportVO buildScl90Report(QuizResult quizResult, String questionnaireType) {
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

    private List<Scl90FactorVO> buildScl90Factors(int[] itemScores) {
        List<Scl90FactorVO> factors = new ArrayList<>();
        for (Map.Entry<String, int[]> entry : SCL90_FACTOR_ITEMS.entrySet()) {
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
            factor.setReference(SCL90_FACTOR_REFERENCE.getOrDefault(factorName, "-"));
            factor.setSummary(buildScl90FactorSummary(factorName, sum, avg, count));
            factors.add(factor);
        }
        return factors;
    }

    private static String resolveScl90Level(double score) {
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

    private static String buildScl90Overview(int totalScore, double totalAvg, int positiveCount) {
        return "总分为 " + totalScore + " 分，总症状指数约 " + totalAvg + "，阳性项目数约 " + positiveCount
                + " 项。该结果用于心理健康教育演示与自助观察，不能替代专业心理诊疗。";
    }

    private static String buildScl90FactorSummary(String factorName, int total, double avg, int count) {
        return factorName + " 维度共 " + count + " 项，当前总分 " + total + "，均分约 " + avg
                + "，提示为「" + resolveScl90Level(avg) + "」。请结合近期学习、人际与作息状态综合判断。";
    }

    private static double round1(double value) {
        return Math.round(value * 10.0) / 10.0;
    }

    private static double round2(double value) {
        return Math.round(value * 100.0) / 100.0;
    }

    /**
     * PHQ9_DEMO / GAD7_DEMO / SCL90_DEMO 使用演示用总分划界（教学演示非临床）；其余问卷仍按得分占比三档。
     */
    private String buildConclusion(int totalScore, int maxScore, String questionnaireType) {
        String type = questionnaireType == null ? "" : questionnaireType.trim();
        if ("PHQ9_DEMO".equalsIgnoreCase(type)) {
            if (totalScore <= 4) {
                return "低风险";
            }
            if (totalScore <= 14) {
                return "中度风险";
            }
            return "高风险";
        }
        if ("GAD7_DEMO".equalsIgnoreCase(type)) {
            if (totalScore <= 4) {
                return "低风险";
            }
            if (totalScore <= 12) {
                return "中度风险";
            }
            return "高风险";
        }
        if ("SCL90_DEMO".equalsIgnoreCase(type)) {
            if (totalScore <= 180) {
                return "低风险";
            }
            if (totalScore <= 270) {
                return "中度风险";
            }
            return "高风险";
        }

        if (maxScore <= 0) {
            return "低风险";
        }

        double percent = totalScore * 100.0 / maxScore;
        if (percent <= 33) {
            return "低风险";
        }
        if (percent <= 66) {
            return "中度风险";
        }
        return "高风险";
    }

    private List<String> buildSuggestionLines(String type, String conclusion) {
        String resolvedType = StringUtils.hasText(type) ? type : "情绪";
        List<String> lines = new ArrayList<>();
        switch (conclusion) {
            case "低风险" -> {
                lines.add("保持规律作息与适度运动，继续观察自身状态变化。");
                lines.add("可在「驿站」浏览放松类文章与音乐疗愈内容。");
                lines.add("如需倾诉，可使用「树洞」或向「小爱」聊聊近况。");
            }
            case "中度风险" -> {
                lines.add("近期与「" + resolvedType + "」相关的困扰可能较明显，建议减少刺激性信息输入，并记录触发情境。");
                lines.add("尝试拆分任务、安排短休息，并结合驿站中的呼吸与正念类练习。");
                lines.add("若困扰持续，建议寻求专业心理咨询或医疗评估。");
            }
            default -> {
                lines.add("当前测评提示风险偏高，请优先关注自身安全，并尽快联系可信任的人或专业机构。");
                lines.add("在获得专业支持前，可先使用驿站与树洞进行情绪疏导，但不应替代面对面评估。");
                lines.add("如出现自伤念头，请立即拨打当地紧急求助电话或前往医院急诊。");
            }
        }
        return lines;
    }

    private String buildInterpretation(int scorePercent, String type, String conclusion) {
        String resolvedType = StringUtils.hasText(type) ? type : "情绪";
        String level;
        switch (conclusion) {
            case "低风险" -> level = "偏低";
            case "中度风险" -> level = "中等";
            default -> level = "偏高";
        }
        return "本次测评得分约占总分的 " + scorePercent + "%，在「" + resolvedType + "」维度上，当前提示为「"
                + conclusion + "」，困扰程度整体 " + level + "。以下为自助建议，请结合自身实际情况参考。";
    }

    private String buildAiChatHint(String questionnaireTitle, String conclusion, int scorePercent) {
        String title = StringUtils.hasText(questionnaireTitle) ? questionnaireTitle : "心理测评";
        return "我刚完成了「" + title + "」，结果是「" + conclusion + "」（约 " + scorePercent
                + "%）。想请你帮我一起看看接下来可以怎么调整。";
    }
}
