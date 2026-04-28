package com.example.demo.service.reply;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.example.demo.dto.PublishReplyRequest;
import com.example.demo.dto.ReplyVO;

/**
 * 树洞回复服务接口。
 */
public interface ReplyService {

    /**
     * 发布回复。
     *
     * @param openid  当前登录用户 openid
     * @param request 发布回复请求
     * @return 回复信息
     */
    ReplyVO publishReply(String openid, PublishReplyRequest request);

    /**
     * 按帖子 ID 分页查询回复列表。
     *
     * @param topicId 帖子 ID
     * @param page    当前页
     * @param size    每页大小
     * @return 回复分页结果
     */
    Page<ReplyVO> getRepliesByTopicId(String currentOpenid, Long topicId, int page, int size);

    /**
     * 删除回复（逻辑删除），并级联删除其所有子回复。
     *
     * @param openid 当前登录用户 openid
     * @param replyId 回复 ID
     */
    void deleteReply(String openid, Long replyId);
}
