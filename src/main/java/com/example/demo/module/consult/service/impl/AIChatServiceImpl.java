package com.example.demo.module.consult.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.example.demo.module.consult.dto.ChatHistoryVO;
import com.example.demo.module.consult.dto.ChatResponse;
import com.example.demo.module.consult.entity.ChatHistory;
import com.example.demo.module.consult.client.OpenAiCompletionClient;
import com.example.demo.module.consult.mapper.ChatHistoryMapper;
import com.example.demo.module.consult.service.AIChatService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

/**
 * AI 聊天服务实现。
 */
@Service
@Slf4j
@RequiredArgsConstructor
public class AIChatServiceImpl implements AIChatService {

    private static final String ROLE_USER = "user";
    private static final String ROLE_ASSISTANT = "assistant";
    private static final String DEFAULT_FALLBACK_REPLY = "小爱暂时有点忙，请稍后再试。你也可以先去驿站模块看看文章、音乐或自我疗愈内容。";
    private static final int MAX_CHAT_HISTORY_PAGE_SIZE = 100;
    private static final int MAX_USER_MESSAGE_LENGTH = 4000;

    private final ChatHistoryMapper chatHistoryMapper;
    private final OpenAiCompletionClient openAiCompletionClient;

    @Override
    public ChatResponse sendMessage(String openid, String message) {
        String userMessage = StringUtils.hasText(message) ? message.trim() : "";
        if (!StringUtils.hasText(openid)) {
            return buildResponse(DEFAULT_FALLBACK_REPLY);
        }
        if (!StringUtils.hasText(userMessage)) {
            return buildResponse("请输入你想对小爱说的话。");
        }
        if (userMessage.length() > MAX_USER_MESSAGE_LENGTH) {
            return buildResponse("单条消息过长，请缩短后再发送（最多 " + MAX_USER_MESSAGE_LENGTH + " 字）。");
        }

        saveChatHistory(openid, ROLE_USER, userMessage);

        String reply;
        try {
            reply = callAiApi(openid, userMessage);
        } catch (Exception ex) {
            log.warn("AI chat request failed for openid={}", openid, ex);
            reply = DEFAULT_FALLBACK_REPLY;
        }

        if (!StringUtils.hasText(reply)) {
            reply = DEFAULT_FALLBACK_REPLY;
        }

        saveChatHistory(openid, ROLE_ASSISTANT, reply);
        return buildResponse(reply);
    }

    @Override
    public Page<ChatHistoryVO> getChatHistory(String openid, int page, int size) {
        long current = Math.max(page, 1);
        long pageSize = Math.min(Math.max(size, 1), MAX_CHAT_HISTORY_PAGE_SIZE);
        Page<ChatHistoryVO> result = new Page<>(current, pageSize, 0);

        if (!StringUtils.hasText(openid)) {
            result.setRecords(Collections.emptyList());
            return result;
        }

        long total = chatHistoryMapper.selectCount(
                new LambdaQueryWrapper<ChatHistory>()
                        .eq(ChatHistory::getOpenid, openid)
        );
        result.setTotal(total);
        if (total == 0) {
            result.setRecords(Collections.emptyList());
            return result;
        }

        long offset = (current - 1) * pageSize;
        List<ChatHistoryVO> records = chatHistoryMapper.selectList(
                        new LambdaQueryWrapper<ChatHistory>()
                                .eq(ChatHistory::getOpenid, openid)
                                .orderByDesc(ChatHistory::getCreateTime)
                                .last("limit " + offset + "," + pageSize)
                ).stream()
                .map(this::toChatHistoryVO)
                .toList();
        result.setRecords(records);
        return result;
    }

    @Override
    public void resetChatHistory(String openid) {
        if (!StringUtils.hasText(openid)) {
            return;
        }
        chatHistoryMapper.delete(
                new LambdaQueryWrapper<ChatHistory>()
                        .eq(ChatHistory::getOpenid, openid)
        );
    }

    private String callAiApi(String openid, String userMessage) {
        String reply = openAiCompletionClient.complete(buildMessagesForCompletion(openid, userMessage));
        if (!StringUtils.hasText(reply)) {
            return DEFAULT_FALLBACK_REPLY;
        }
        return reply;
    }

    /**
     * 组装发给模型的 messages：在已持久化本轮用户句的前提下，仍以显式 {@code userMessage} 作为最后一条 user，
     * 避免后续若调整「先调 API 再落库」等顺序时出现漏发。
     */
    private List<Map<String, String>> buildMessagesForCompletion(String openid, String userMessage) {
        List<Map<String, String>> messages = new ArrayList<>();

        messages.add(Map.of(
                "role", "system",
                "content", "你是一位温柔、专业、简洁的校园心理陪伴助手，名字叫小爱。请提供支持性、非诊断性的建议，避免医学诊断和绝对化表述。"
        ));

        List<ChatHistory> historyList = chatHistoryMapper.selectList(
                new LambdaQueryWrapper<ChatHistory>()
                        .eq(ChatHistory::getOpenid, openid)
                        .orderByDesc(ChatHistory::getCreateTime)
                        .last("limit 5")
        );
        Collections.reverse(historyList);

        // 去掉末尾与本轮相同的 user 行，避免与下方显式追加重复
        while (!historyList.isEmpty()) {
            ChatHistory tail = historyList.get(historyList.size() - 1);
            if (ROLE_USER.equals(tail.getRole()) && userMessage.equals(tail.getContent())) {
                historyList.remove(historyList.size() - 1);
            } else {
                break;
            }
        }

        for (ChatHistory chatHistory : historyList) {
            messages.add(Map.of(
                    "role", chatHistory.getRole(),
                    "content", chatHistory.getContent()
            ));
        }
        messages.add(Map.of("role", ROLE_USER, "content", userMessage));
        return messages;
    }

    private void saveChatHistory(String openid, String role, String content) {
        ChatHistory chatHistory = new ChatHistory();
        chatHistory.setOpenid(openid);
        chatHistory.setRole(role);
        chatHistory.setContent(content);
        chatHistoryMapper.insert(chatHistory);
    }

    private ChatResponse buildResponse(String reply) {
        ChatResponse chatResponse = new ChatResponse();
        chatResponse.setReply(reply);
        chatResponse.setTimestamp(LocalDateTime.now());
        return chatResponse;
    }

    private ChatHistoryVO toChatHistoryVO(ChatHistory chatHistory) {
        ChatHistoryVO chatHistoryVO = new ChatHistoryVO();
        chatHistoryVO.setId(chatHistory.getId());
        chatHistoryVO.setRole(chatHistory.getRole());
        chatHistoryVO.setContent(chatHistory.getContent());
        chatHistoryVO.setCreateTime(chatHistory.getCreateTime());
        return chatHistoryVO;
    }
}
