package com.example.demo.controller.admin;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.example.demo.common.Result;
import com.example.demo.entity.Topic;
import com.example.demo.entity.User;
import com.example.demo.mapper.TopicMapper;
import com.example.demo.mapper.UserMapper;
import com.example.demo.module.consult.entity.ChatHistory;
import com.example.demo.module.consult.entity.QuizResult;
import com.example.demo.module.consult.mapper.ChatHistoryMapper;
import com.example.demo.module.consult.mapper.QuizResultMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.*;

import java.util.*;

/**
 * 用户管理控制器（管理后台）。
 */
@RestController
@RequestMapping("/api/admin/user")
@RequiredArgsConstructor
public class AdminUserController {

    private final UserMapper userMapper;
    private final QuizResultMapper quizResultMapper;
    private final ChatHistoryMapper chatHistoryMapper;
    private final TopicMapper topicMapper;

    /**
     * 用户分页列表。
     */
    @GetMapping("/list")
    public Result<Page<User>> list(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(required = false) String keyword) {

        Page<User> pageParam = new Page<>(page, size);
        LambdaQueryWrapper<User> wrapper = new LambdaQueryWrapper<>();
        if (StringUtils.hasText(keyword)) {
            wrapper.and(w -> w
                    .like(User::getNickName, keyword)
                    .or()
                    .like(User::getOpenid, keyword)
            );
        }
        wrapper.orderByDesc(User::getCreateTime);
        Page<User> result = userMapper.selectPage(pageParam, wrapper);
        return Result.success(result);
    }

    /**
     * 用户详情。
     */
    @GetMapping("/{openid}")
    public Result<Map<String, Object>> detail(@PathVariable String openid) {
        User user = userMapper.selectOne(
                new LambdaQueryWrapper<User>()
                        .eq(User::getOpenid, openid)
                        .last("limit 1")
        );
        if (user == null) {
            return Result.error(404, "用户不存在");
        }

        // 查询测评记录数
        Long quizCount = quizResultMapper.selectCount(
                new LambdaQueryWrapper<QuizResult>()
                        .eq(QuizResult::getOpenid, openid)
        );

        // 查询对话数
        Long chatCount = chatHistoryMapper.selectCount(
                new LambdaQueryWrapper<ChatHistory>()
                        .eq(ChatHistory::getOpenid, openid)
        );

        // 查询树洞帖子数
        Long topicCount = topicMapper.selectCount(
                new LambdaQueryWrapper<Topic>()
                        .eq(Topic::getPublisherOpenid, openid)
        );

        Map<String, Object> result = new HashMap<>();
        result.put("user", user);
        result.put("quizCount", quizCount);
        result.put("chatCount", chatCount);
        result.put("topicCount", topicCount);
        return Result.success(result);
    }

    /**
     * 用户的测评记录。
     */
    @GetMapping("/{openid}/quizzes")
    public Result<List<QuizResult>> quizzes(
            @PathVariable String openid,
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int size) {

        Page<QuizResult> pageParam = new Page<>(page, size);
        Page<QuizResult> result = quizResultMapper.selectPage(pageParam,
                new LambdaQueryWrapper<QuizResult>()
                        .eq(QuizResult::getOpenid, openid)
                        .orderByDesc(QuizResult::getCreateTime)
        );
        return Result.success(result.getRecords());
    }

    /**
     * 用户的对话记录。
     */
    @GetMapping("/{openid}/chats")
    public Result<List<ChatHistory>> chats(
            @PathVariable String openid,
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "20") int size) {

        Page<ChatHistory> pageParam = new Page<>(page, size);
        Page<ChatHistory> result = chatHistoryMapper.selectPage(pageParam,
                new LambdaQueryWrapper<ChatHistory>()
                        .eq(ChatHistory::getOpenid, openid)
                        .orderByDesc(ChatHistory::getCreateTime)
        );
        return Result.success(result.getRecords());
    }
}
