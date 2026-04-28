package com.example.demo.module.consult.service;

import com.example.demo.module.consult.dto.QuestionnaireVO;
import com.example.demo.module.consult.dto.QuizResultVO;
import com.example.demo.module.consult.dto.SubmitQuizRequest;

import java.util.List;

/**
 * 心理测评服务接口。
 */
public interface QuizService {

    /**
     * 查询所有启用中的问卷。
     *
     * @return 问卷列表
     */
    List<QuestionnaireVO> getAvailableQuestionnaires();

    /**
     * 查询问卷详情，包含题目。
     *
     * @param id 问卷 ID
     * @return 问卷详情
     */
    QuestionnaireVO getQuestionnaireDetail(Long id);

    /**
     * 提交测评。
     *
     * @param openid  当前登录用户 openid
     * @param request 提交请求
     * @return 测评结果
     */
    QuizResultVO submitQuiz(String openid, SubmitQuizRequest request);

    /**
     * 查询当前用户历史测评记录。
     *
     * @param openid 当前登录用户 openid
     * @return 历史结果列表
     */
    List<QuizResultVO> getUserHistory(String openid);

    /**
     * 获取单条测评结果详情（仅允许当前登录用户查看自己的结果）。
     *
     * @param openid   当前登录用户 openid
     * @param resultId 测评结果 ID
     * @return 测评结果详情
     */
    QuizResultVO getResultDetail(String openid, Long resultId);

    /**
     * 对指定测评结果重试生成 AI 指导（仅允许当前登录用户自己的结果）。
     *
     * @param openid   当前登录用户 openid
     * @param resultId 测评结果 ID
     * @return 更新后的测评结果
     */
    QuizResultVO retryAiGuidance(String openid, Long resultId);
}
