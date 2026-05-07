package com.example.demo.controller.admin;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.example.demo.common.Result;
import com.example.demo.entity.Reply;
import com.example.demo.entity.Topic;
import com.example.demo.mapper.ReplyMapper;
import com.example.demo.mapper.TopicMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 树洞帖子审核控制器（管理后台）。
 */
@RestController
@RequestMapping("/api/admin/topic")
@RequiredArgsConstructor
public class AdminTopicController {

    private final TopicMapper topicMapper;
    private final ReplyMapper replyMapper;

    /**
     * 帖子分页列表（支持状态筛选）。
     */
    @GetMapping("/list")
    public Result<Page<Topic>> list(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(required = false) Integer status) {

        Page<Topic> pageParam = new Page<>(page, size);
        LambdaQueryWrapper<Topic> wrapper = new LambdaQueryWrapper<>();
        if (status != null) {
            wrapper.eq(Topic::getStatus, status);
        }
        wrapper.orderByDesc(Topic::getCreateTime);
        Page<Topic> result = topicMapper.selectPage(pageParam, wrapper);
        return Result.success(result);
    }

    /**
     * 帖子详情（含回复列表）。
     */
    @GetMapping("/{id}")
    public Result<Map<String, Object>> detail(@PathVariable Long id) {
        Topic topic = topicMapper.selectById(id);
        if (topic == null) {
            return Result.error(404, "帖子不存在");
        }

        // 查询回复
        List<Reply> replies = replyMapper.selectList(
                new LambdaQueryWrapper<Reply>()
                        .eq(Reply::getTopicId, id)
                        .orderByAsc(Reply::getCreateTime)
        );

        Map<String, Object> result = new HashMap<>();
        result.put("topic", topic);
        result.put("replies", replies);
        return Result.success(result);
    }

    /**
     * 审核帖子（1=通过，2=删除）。
     */
    @PutMapping("/{id}/status")
    public Result<Void> updateStatus(@PathVariable Long id, @RequestBody Map<String, Integer> body) {
        Integer status = body.get("status");
        if (status == null || (status != 1 && status != 2)) {
            return Result.error(400, "状态值无效");
        }
        Topic topic = new Topic();
        topic.setId(id);
        topic.setStatus(status);
        topic.setUpdateTime(LocalDateTime.now());
        topicMapper.updateById(topic);
        return Result.success(null);
    }

    /**
     * 获取帖子的回复列表。
     */
    @GetMapping("/{id}/replies")
    public Result<List<Reply>> replies(@PathVariable Long id) {
        List<Reply> replies = replyMapper.selectList(
                new LambdaQueryWrapper<Reply>()
                        .eq(Reply::getTopicId, id)
                        .orderByAsc(Reply::getCreateTime)
        );
        return Result.success(replies);
    }

    /**
     * 删除回复。
     */
    @PutMapping("/reply/{id}/status")
    public Result<Void> updateReplyStatus(@PathVariable Long id, @RequestBody Map<String, Integer> body) {
        Integer status = body.get("status");
        if (status == null) {
            return Result.error(400, "状态值无效");
        }
        Reply reply = new Reply();
        reply.setId(id);
        reply.setStatus(status);
        reply.setUpdateTime(LocalDateTime.now());
        replyMapper.updateById(reply);
        return Result.success(null);
    }
}
