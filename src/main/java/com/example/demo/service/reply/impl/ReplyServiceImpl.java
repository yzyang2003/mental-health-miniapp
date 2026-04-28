package com.example.demo.service.reply.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.example.demo.dto.PublishReplyRequest;
import com.example.demo.dto.ReplyVO;
import com.example.demo.entity.Reply;
import com.example.demo.entity.Topic;
import com.example.demo.mapper.ReplyMapper;
import com.example.demo.mapper.TopicMapper;
import com.example.demo.service.UserService;
import com.example.demo.service.reply.ReplyService;
import com.example.demo.utils.SensitiveWordUtil;
import com.example.demo.exception.BusinessException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * 树洞回复服务实现。
 */
@Service
@RequiredArgsConstructor
public class ReplyServiceImpl implements ReplyService {

    private static final int STATUS_NORMAL = 1;
    private static final int STATUS_DELETED = 2;
    private static final String ANONYMOUS_NAME = "匿名用户";

    private final ReplyMapper replyMapper;
    private final TopicMapper topicMapper;
    private final UserService userService;

    @Override
    public ReplyVO publishReply(String openid, PublishReplyRequest request) {
        if (!StringUtils.hasText(openid)) {
            throw new BusinessException("openid 不能为空");
        }
        if (request == null || request.getTopicId() == null) {
            throw new BusinessException("topicId 不能为空");
        }
        if (!StringUtils.hasText(request.getContent())) {
            throw new BusinessException("回复内容不能为空");
        }
        if (SensitiveWordUtil.containsSensitiveWord(request.getContent())) {
            throw new BusinessException("回复内容包含敏感词，请修改后重试");
        }

        Topic topic = topicMapper.selectById(request.getTopicId());
        if (topic == null || topic.getStatus() == null || topic.getStatus() != STATUS_NORMAL) {
            throw new BusinessException("帖子不存在或不可回复");
        }

        Reply repliedReply = null;
        if (request.getRepliedReplyId() != null) {
            repliedReply = replyMapper.selectById(request.getRepliedReplyId());
            if (repliedReply == null || repliedReply.getStatus() == null || repliedReply.getStatus() != STATUS_NORMAL) {
                throw new BusinessException("被回复的回复不存在或已删除");
            }
            if (!request.getTopicId().equals(repliedReply.getTopicId())) {
                throw new BusinessException("回复关系与帖子不匹配");
            }
        }

        Reply reply = new Reply();
        reply.setTopicId(request.getTopicId());
        reply.setContent(request.getContent().trim());
        reply.setReplierOpenid(openid);
        reply.setRepliedReplyId(request.getRepliedReplyId());
        reply.setRepliedUserOpenid(
                repliedReply != null && !Boolean.TRUE.equals(repliedReply.getAnonymous())
                        ? repliedReply.getReplierOpenid()
                        : null
        );
        reply.setAnonymous(!Boolean.FALSE.equals(request.getAnonymous()));
        reply.setStatus(STATUS_NORMAL);
        replyMapper.insert(reply);

        return toReplyVO(replyMapper.selectById(reply.getId()), openid);
    }

    @Override
    public Page<ReplyVO> getRepliesByTopicId(String currentOpenid, Long topicId, int page, int size) {
        if (topicId == null) {
            Page<ReplyVO> emptyPage = new Page<>(1, Math.max(size, 1), 0);
            emptyPage.setRecords(Collections.emptyList());
            return emptyPage;
        }

        long current = Math.max(page, 1);
        long pageSize = Math.max(size, 1);

        long total = replyMapper.selectCount(
                new LambdaQueryWrapper<Reply>()
                        .eq(Reply::getTopicId, topicId)
                        .eq(Reply::getStatus, STATUS_NORMAL)
        );

        Page<ReplyVO> result = new Page<>(current, pageSize, total);
        if (total == 0) {
            result.setRecords(Collections.emptyList());
            return result;
        }

        long offset = (current - 1) * pageSize;
        List<ReplyVO> records = replyMapper.selectList(
                        new LambdaQueryWrapper<Reply>()
                                .eq(Reply::getTopicId, topicId)
                                .eq(Reply::getStatus, STATUS_NORMAL)
                                .orderByAsc(Reply::getCreateTime)
                                .last("limit " + offset + "," + pageSize)
                ).stream()
                .map(reply -> toReplyVO(reply, currentOpenid))
                .toList();

        result.setRecords(records);
        return result;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void deleteReply(String openid, Long replyId) {
        if (!StringUtils.hasText(openid)) {
            throw new BusinessException("openid 不能为空");
        }
        if (replyId == null) {
            throw new BusinessException("replyId 不能为空");
        }

        Reply rootReply = replyMapper.selectById(replyId);
        if (rootReply == null || rootReply.getStatus() == null || rootReply.getStatus() != STATUS_NORMAL) {
            throw new BusinessException("回复不存在或已删除");
        }
        if (!openid.equals(rootReply.getReplierOpenid())) {
            throw new BusinessException("仅可删除自己发布的回复");
        }

        Set<Long> pending = new HashSet<>();
        pending.add(replyId);
        Set<Long> allToDelete = new HashSet<>(pending);

        while (!pending.isEmpty()) {
            List<Long> parentIds = new ArrayList<>(pending);
            pending.clear();
            List<Reply> children = replyMapper.selectList(
                    new LambdaQueryWrapper<Reply>()
                            .in(Reply::getRepliedReplyId, parentIds)
                            .eq(Reply::getStatus, STATUS_NORMAL)
            );
            for (Reply child : children) {
                if (allToDelete.add(child.getId())) {
                    pending.add(child.getId());
                }
            }
        }

        if (allToDelete.isEmpty()) {
            return;
        }

        Reply deletingReply = new Reply();
        deletingReply.setStatus(STATUS_DELETED);
        deletingReply.setContent("");
        deletingReply.setRepliedUserOpenid(null);
        deletingReply.setRepliedReplyId(null);
        replyMapper.update(
                deletingReply,
                new LambdaQueryWrapper<Reply>()
                        .in(Reply::getId, allToDelete)
                        .eq(Reply::getStatus, STATUS_NORMAL)
        );
    }

    private ReplyVO toReplyVO(Reply reply, String currentOpenid) {
        ReplyVO replyVO = new ReplyVO();
        replyVO.setId(reply.getId());
        replyVO.setContent(reply.getContent());
        replyVO.setCreateTime(reply.getCreateTime());
        replyVO.setReplierName(Boolean.TRUE.equals(reply.getAnonymous())
                ? ANONYMOUS_NAME
                : fallbackNickname(userService.getNicknameByOpenid(reply.getReplierOpenid())));

        if (StringUtils.hasText(reply.getRepliedUserOpenid())) {
            String repliedUserName = userService.getNicknameByOpenid(reply.getRepliedUserOpenid());
            replyVO.setRepliedUserName(StringUtils.hasText(repliedUserName) ? repliedUserName : ANONYMOUS_NAME);
        }
        replyVO.setRepliedReplyId(reply.getRepliedReplyId());
        replyVO.setCanDelete(StringUtils.hasText(currentOpenid) && currentOpenid.equals(reply.getReplierOpenid()));
        return replyVO;
    }

    private String fallbackNickname(String nickname) {
        return StringUtils.hasText(nickname) ? nickname : "微信用户";
    }
}
