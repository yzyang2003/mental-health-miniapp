package com.example.demo.module.consult.controller;

import com.example.demo.common.Result;
import com.example.demo.module.consult.dto.QuestionnaireVO;
import com.example.demo.module.consult.dto.QuizResultVO;
import com.example.demo.module.consult.dto.SubmitQuizRequest;
import com.example.demo.module.consult.service.QuizService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

/**
 * 心理测评控制器。
 */
@Tag(name = "Consult Quiz", description = "Consult quiz APIs")
@RestController
@RequestMapping("/api/consult/quiz")
@RequiredArgsConstructor
public class ConsultQuizController {

    private final QuizService quizService;

    @Operation(summary = "获取可用问卷列表")
    @GetMapping("/list")
    public Result<List<QuestionnaireVO>> getAvailableQuestionnaires() {
        return Result.success(quizService.getAvailableQuestionnaires());
    }

    @Operation(summary = "获取问卷详情")
    @GetMapping("/{id}")
    public Result<QuestionnaireVO> getQuestionnaireDetail(@PathVariable Long id) {
        return Result.success(quizService.getQuestionnaireDetail(id));
    }

    @Operation(summary = "提交问卷测评")
    @PostMapping("/submit")
    public Result<QuizResultVO> submitQuiz(@RequestBody SubmitQuizRequest request, HttpServletRequest httpServletRequest) {
        return Result.success(quizService.submitQuiz(getOpenid(httpServletRequest), request));
    }

    @Operation(summary = "获取当前用户测评历史")
    @GetMapping("/history")
    public Result<List<QuizResultVO>> getUserHistory(HttpServletRequest httpServletRequest) {
        return Result.success(quizService.getUserHistory(getOpenid(httpServletRequest)));
    }

    @Operation(summary = "获取单条测评结果详情")
    @GetMapping("/result/{id}")
    public Result<QuizResultVO> getResultDetail(@PathVariable("id") Long id, HttpServletRequest httpServletRequest) {
        return Result.success(quizService.getResultDetail(getOpenid(httpServletRequest), id));
    }

    @Operation(summary = "重试生成测评 AI 指导")
    @PostMapping("/result/{id}/retry-ai-guidance")
    public Result<QuizResultVO> retryAiGuidance(@PathVariable("id") Long id, HttpServletRequest httpServletRequest) {
        return Result.success(quizService.retryAiGuidance(getOpenid(httpServletRequest), id));
    }

    private String getOpenid(HttpServletRequest request) {
        Object openidAttr = request.getAttribute("openid");
        if (openidAttr instanceof String openid) {
            return openid;
        }
        throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "未登录或登录已失效");
    }
}
