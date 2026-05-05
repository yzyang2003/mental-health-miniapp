package com.example.demo.module.consult.controller;

import com.example.demo.exception.GlobalExceptionHandler;
import com.example.demo.module.consult.dto.ChatResponse;
import com.example.demo.module.consult.service.AIChatService;
import com.example.demo.module.consult.service.RateLimitService;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.time.LocalDateTime;

import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * 无 Spring 容器的轻量测试：校验未注入 openid 时 {@link org.springframework.web.server.ResponseStatusException}
 * 经 {@link GlobalExceptionHandler} 返回 401，而非被兜底为 500。
 */
class ConsultChatControllerStandaloneTest {

    @Test
    void sendMessage_withoutOpenid_returns401() throws Exception {
        AIChatService aiChatService = mock(AIChatService.class);
        RateLimitService rateLimitService = mock(RateLimitService.class);
        ConsultChatController controller = new ConsultChatController(aiChatService, rateLimitService);
        MockMvc mockMvc = MockMvcBuilders.standaloneSetup(controller)
                .setControllerAdvice(new GlobalExceptionHandler())
                .build();

        mockMvc.perform(post("/api/consult/chat/send")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"message\":\"hello\"}"))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.code").value(401));

        verifyNoInteractions(aiChatService);
    }

    @Test
    void sendMessage_withOpenid_invokesService() throws Exception {
        AIChatService aiChatService = mock(AIChatService.class);
        RateLimitService rateLimitService = mock(RateLimitService.class);
        when(rateLimitService.isAllowed("test-openid")).thenReturn(true);

        ChatResponse chatResponse = new ChatResponse();
        chatResponse.setReply("ok");
        chatResponse.setTimestamp(LocalDateTime.now());
        when(aiChatService.sendMessage(eq("test-openid"), anyString())).thenReturn(chatResponse);

        ConsultChatController controller = new ConsultChatController(aiChatService, rateLimitService);
        MockMvc mockMvc = MockMvcBuilders.standaloneSetup(controller)
                .setControllerAdvice(new GlobalExceptionHandler())
                .build();

        mockMvc.perform(post("/api/consult/chat/send")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"message\":\"hello\"}")
                        .requestAttr("openid", "test-openid"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data.reply").value("ok"));

        verify(aiChatService).sendMessage(eq("test-openid"), eq("hello"));
    }

    @Test
    void sendMessage_rateLimited_returns429() throws Exception {
        AIChatService aiChatService = mock(AIChatService.class);
        RateLimitService rateLimitService = mock(RateLimitService.class);
        when(rateLimitService.isAllowed("test-openid")).thenReturn(false);
        when(rateLimitService.getRemainingWaitSeconds("test-openid")).thenReturn(42);

        ConsultChatController controller = new ConsultChatController(aiChatService, rateLimitService);
        MockMvc mockMvc = MockMvcBuilders.standaloneSetup(controller)
                .setControllerAdvice(new GlobalExceptionHandler())
                .build();

        mockMvc.perform(post("/api/consult/chat/send")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"message\":\"hello\"}")
                        .requestAttr("openid", "test-openid"))
                .andExpect(status().isTooManyRequests())
                .andExpect(jsonPath("$.code").value(429))
                .andExpect(jsonPath("$.message").value("消息发送过于频繁，请 42 秒后再试"));

        verifyNoInteractions(aiChatService);
    }
}
