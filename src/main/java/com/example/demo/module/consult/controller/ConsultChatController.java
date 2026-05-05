package com.example.demo.module.consult.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.example.demo.common.Result;
import com.example.demo.module.consult.dto.ChatHistoryVO;
import com.example.demo.module.consult.dto.ChatRequest;
import com.example.demo.module.consult.dto.ChatResponse;
import com.example.demo.module.consult.service.AIChatService;
import com.example.demo.module.consult.service.RateLimitService;
import com.example.demo.module.consult.service.VoiceService;
import com.example.demo.utils.OpenidUtil;
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
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.server.ResponseStatusException;

import java.util.Map;

/**
 * AI 咨询师控制器。
 */
@Tag(name = "Consult Chat", description = "Consult AI chat APIs")
@RestController
@RequestMapping("/api/consult/chat")
@RequiredArgsConstructor
public class ConsultChatController {

    private final AIChatService aiChatService;
    private final RateLimitService rateLimitService;
    private final VoiceService voiceService;

    @Operation(summary = "发送消息给 AI 咨询师")
    @PostMapping("/send")
    public Result<ChatResponse> sendMessage(@RequestBody ChatRequest request, HttpServletRequest httpServletRequest) {
        String openid = OpenidUtil.getOpenid(httpServletRequest);

        // 速率限制检查
        if (!rateLimitService.isAllowed(openid)) {
            int waitSeconds = rateLimitService.getRemainingWaitSeconds(openid);
            throw new ResponseStatusException(HttpStatus.TOO_MANY_REQUESTS,
                    "消息发送过于频繁，请 " + waitSeconds + " 秒后再试");
        }

        String message = request == null ? "" : request.getMessage();
        return Result.success(aiChatService.sendMessage(openid, message));
    }

    @Operation(summary = "分页获取聊天历史")
    @GetMapping("/history")
    public Result<Page<ChatHistoryVO>> getChatHistory(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int size,
            HttpServletRequest httpServletRequest
    ) {
        return Result.success(aiChatService.getChatHistory(OpenidUtil.getOpenid(httpServletRequest), page, size));
    }

    @Operation(summary = "清空当前用户聊天记录，开始新对话")
    @PostMapping("/reset")
    public Result<Void> resetChatHistory(HttpServletRequest httpServletRequest) {
        aiChatService.resetChatHistory(OpenidUtil.getOpenid(httpServletRequest));
        return Result.success(null);
    }

    @Operation(summary = "语音转文字（ASR）")
    @PostMapping("/stt")
    public Result<String> speechToText(@RequestParam("audio") MultipartFile audioFile) {
        try {
            String text = voiceService.speechToText(audioFile);
            return Result.success(text);
        } catch (Exception e) {
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "语音识别失败: " + e.getMessage());
        }
    }

    @Operation(summary = "文字转语音（TTS）")
    @PostMapping("/tts")
    public Result<Map<String, String>> textToSpeech(@RequestBody Map<String, String> request) {
        String text = request.get("text");
        if (text == null || text.isEmpty()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "文本不能为空");
        }
        try {
            String audioUrl = voiceService.textToSpeech(text);
            return Result.success(Map.of("audioUrl", audioUrl));
        } catch (Exception e) {
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "语音合成失败: " + e.getMessage());
        }
    }
}
