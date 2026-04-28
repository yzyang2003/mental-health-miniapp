package com.example.demo.module.consult.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.example.demo.module.consult.dto.ChatHistoryVO;
import com.example.demo.module.consult.dto.ChatResponse;

/**
 * AI 聊天服务接口。
 */
public interface AIChatService {

    /**
     * 发送消息给 AI 咨询师。
     *
     * @param openid  当前登录用户 openid
     * @param message 用户消息
     * @return AI 回复
     */
    ChatResponse sendMessage(String openid, String message);

    /**
     * 分页查询当前用户聊天历史。
     *
     * @param openid 当前登录用户 openid
     * @param page   当前页
     * @param size   每页大小
     * @return 聊天历史分页结果
     */
    Page<ChatHistoryVO> getChatHistory(String openid, int page, int size);

    /**
     * 清空当前用户在数据库中的聊天记录（新对话）。
     */
    void resetChatHistory(String openid);
}
