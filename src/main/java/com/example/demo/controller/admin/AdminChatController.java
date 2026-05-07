package com.example.demo.controller.admin;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.example.demo.common.Result;
import com.example.demo.module.consult.entity.ChatHistory;
import com.example.demo.module.consult.mapper.ChatHistoryMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.*;

import java.util.*;
import java.util.stream.Collectors;

/**
 * AI 对话记录控制器（管理后台）。
 */
@RestController
@RequestMapping("/api/admin/chat")
@RequiredArgsConstructor
public class AdminChatController {

    private final ChatHistoryMapper chatHistoryMapper;

    /**
     * 全局对话列表（按用户分组）。
     */
    @GetMapping("/list")
    public Result<List<Map<String, Object>>> list(
            @RequestParam(required = false) String openid,
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) String startDate,
            @RequestParam(required = false) String endDate) {

        LambdaQueryWrapper<ChatHistory> wrapper = new LambdaQueryWrapper<>();
        if (StringUtils.hasText(openid)) {
            wrapper.eq(ChatHistory::getOpenid, openid);
        }
        if (StringUtils.hasText(keyword)) {
            wrapper.like(ChatHistory::getContent, keyword);
        }
        wrapper.orderByDesc(ChatHistory::getCreateTime);

        List<ChatHistory> allChats = chatHistoryMapper.selectList(wrapper);

        // 按用户分组
        Map<String, List<ChatHistory>> grouped = allChats.stream()
                .collect(Collectors.groupingBy(ChatHistory::getOpenid));

        List<Map<String, Object>> result = new ArrayList<>();
        for (Map.Entry<String, List<ChatHistory>> entry : grouped.entrySet()) {
            Map<String, Object> item = new HashMap<>();
            item.put("openid", entry.getKey());
            item.put("messageCount", entry.getValue().size());
            item.put("lastMessage", entry.getValue().get(0).getContent());
            item.put("lastActiveTime", entry.getValue().get(0).getCreateTime());
            result.add(item);
        }

        // 按最近活跃时间排序
        result.sort((a, b) -> {
            java.time.LocalDateTime timeA = (java.time.LocalDateTime) a.get("lastActiveTime");
            java.time.LocalDateTime timeB = (java.time.LocalDateTime) b.get("lastActiveTime");
            return timeB.compareTo(timeA);
        });

        return Result.success(result);
    }

    /**
     * 某用户的完整对话记录。
     */
    @GetMapping("/user/{openid}")
    public Result<List<ChatHistory>> userChat(@PathVariable String openid) {
        List<ChatHistory> chats = chatHistoryMapper.selectList(
                new LambdaQueryWrapper<ChatHistory>()
                        .eq(ChatHistory::getOpenid, openid)
                        .orderByAsc(ChatHistory::getCreateTime)
        );
        return Result.success(chats);
    }
}
