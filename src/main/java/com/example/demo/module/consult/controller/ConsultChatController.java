package com.example.demo.module.consult.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.example.demo.common.Result;
import com.example.demo.module.consult.dto.ChatHistoryVO;
import com.example.demo.module.consult.dto.ChatRequest;
import com.example.demo.module.consult.dto.ChatResponse;
import com.example.demo.module.consult.service.AIChatService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

/**
 * AI 咨询师控制器。
 */
@Tag(name = "Consult Chat", description = "Consult AI chat APIs")
@RestController
@RequestMapping("/api/consult/chat")
@RequiredArgsConstructor
public class ConsultChatController {

    private final AIChatService aiChatService;

    @Operation(summary = "发送消息给 AI 咨询师")
    @PostMapping("/send")
    public Result<ChatResponse> sendMessage(@RequestBody ChatRequest request, HttpServletRequest httpServletRequest) {
        String message = request == null ? "" : request.getMessage();
        return Result.success(aiChatService.sendMessage(getOpenid(httpServletRequest), message));
    }

    @Operation(summary = "分页获取聊天历史")
    @GetMapping("/history")
    public Result<Page<ChatHistoryVO>> getChatHistory(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int size,
            HttpServletRequest httpServletRequest
    ) {
        return Result.success(aiChatService.getChatHistory(getOpenid(httpServletRequest), page, size));
    }

    @Operation(summary = "清空当前用户聊天记录，开始新对话")
    @PostMapping("/reset")
    public Result<Void> resetChatHistory(HttpServletRequest httpServletRequest) {
        aiChatService.resetChatHistory(getOpenid(httpServletRequest));
        return Result.success(null);
    }

    private String getOpenid(HttpServletRequest request) {
        Object openidAttr = request.getAttribute("openid");
        if (openidAttr instanceof String openid) {
            return openid;
        }
        throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "未登录或登录已失效");
    }
}
