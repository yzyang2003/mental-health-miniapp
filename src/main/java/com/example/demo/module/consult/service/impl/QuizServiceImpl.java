package com.example.demo.module.consult.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.example.demo.exception.BusinessException;
import com.example.demo.module.consult.dto.AnswerItem;
import com.example.demo.module.consult.dto.QuestionOptionItem;
import com.example.demo.module.consult.dto.QuestionVO;
import com.example.demo.module.consult.dto.QuestionnaireVO;
import com.example.demo.module.consult.dto.QuizResultVO;
import com.example.demo.module.consult.dto.Scl90ReportVO;
import com.example.demo.module.consult.dto.SubmitQuizRequest;
import com.example.demo.module.consult.entity.Question;
import com.example.demo.module.consult.entity.Questionnaire;
import com.example.demo.module.consult.entity.QuizResult;
import com.example.demo.module.consult.mapper.QuestionMapper;
import com.example.demo.module.consult.mapper.QuestionnaireMapper;
import com.example.demo.module.consult.mapper.QuizResultMapper;
import com.example.demo.module.consult.service.QuizService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * 心理测评服务实现。
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class QuizServiceImpl implements QuizService {

    private static final int STATUS_ENABLED = 1;
    private static final long RETRY_AI_GUIDANCE_COOLDOWN_MS = 10_000L;
    private static final String DISCLAIMER = "本测评仅为自助参考，不能替代专业医疗诊断。若情绪困扰持续或加重，请及时寻求专业帮助。";

    private final QuestionnaireMapper questionnaireMapper;
    private final QuestionMapper questionMapper;
    private final QuizResultMapper quizResultMapper;
    private final QuizScoringService quizScoringService;
    private final Scl90ReportService scl90ReportService;
    private final QuizAiGuidanceService quizAiGuidanceService;
    private final Map<String, Long> retryAiGuidanceLastTs = new ConcurrentHashMap<>();

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

        String conclusion = quizScoringService.buildConclusion(totalScore, maxScore, questionnaire.getType());
        int scorePercent = maxScore <= 0 ? 0 : (int) Math.round(totalScore * 100.0 / maxScore);
        List<String> suggestionLines = quizScoringService.buildSuggestionLines(questionnaire.getType(), conclusion);
        String suggestions = String.join("\n", suggestionLines);
        String interpretation = quizScoringService.buildInterpretation(scorePercent, questionnaire.getType(), conclusion);
        String aiChatHint = quizScoringService.buildAiChatHint(questionnaire.getTitle(), conclusion, scorePercent);

        QuizResult previewResult = new QuizResult();
        previewResult.setQuestionnaireId(questionnaire.getId());
        previewResult.setAnswers(request.getAnswers());
        previewResult.setScore(totalScore);
        previewResult.setMaxScore(maxScore);
        previewResult.setScorePercent(scorePercent);
        previewResult.setConclusion(conclusion);
        previewResult.setSuggestions(suggestions);
        Scl90ReportVO scl90Report = scl90ReportService.buildScl90Report(previewResult, questionnaire.getType());

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
        quizResult.setAiGuidance(quizAiGuidanceService.buildLocalGuidance(questionnaire.getType(), conclusion, suggestionLines, scl90Report));
        quizResultMapper.insert(quizResult);

        Long resultId = quizResult.getId();
        quizAiGuidanceService.triggerAsyncAiGuidanceGeneration(
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
                    String interpretation = quizScoringService.buildInterpretation(percent, type, item.getConclusion());
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
        String interpretation = quizScoringService.buildInterpretation(percent, type, result.getConclusion());
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
                : quizScoringService.buildConclusion(score, maxScore, questionnaireType);
        List<String> suggestionLines = splitSuggestionLines(quizResult.getSuggestions());

        Scl90ReportVO scl90Report = scl90ReportService.buildScl90Report(quizResult, questionnaireType);
        String aiGuidance = quizAiGuidanceService.requestAiGuidanceSafe(
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
        String interpretation = quizScoringService.buildInterpretation(scorePercent, questionnaireType, conclusion);
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
        quizResultVO.setScl90Report(scl90ReportService.buildScl90Report(quizResult, questionnaireType));
        quizResultVO.setCreateTime(quizResult.getCreateTime());
        return quizResultVO;
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
}
