package com.example.demo.service.topic;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.example.demo.dto.PublishTopicRequest;
import com.example.demo.dto.TopicVO;

/**
 * 树洞帖子服务接口。
 */
public interface TopicService {

    /**
     * 发布树洞帖子。
     *
     * @param openid  当前登录用户 openid
     * @param request 发布请求
     * @return 发布后的帖子信息
     */
    TopicVO publishTopic(String openid, PublishTopicRequest request);

    /**
     * 获取树洞帖子分页列表。
     *
     * @param currentOpenid 当前访问者 openid（可为空：未登录浏览公共列表）
     * @param page          当前页
     * @param size          每页大小
     * @param mineOnly      是否仅返回当前用户发布的帖子（需要已登录）
     * @return 帖子分页结果
     */
    Page<TopicVO> getTopicPage(String currentOpenid, int page, int size, boolean mineOnly);

    /**
     * 获取帖子详情。
     *
     * @param topicId 帖子 ID
     * @return 帖子详情
     */
    TopicVO getTopicDetail(String currentOpenid, Long topicId);

    /**
     * 删除帖子（逻辑删除），并清空帖子内容与关联回复。
     *
     * @param openid 当前登录用户 openid
     * @param topicId 帖子 ID
     */
    void deleteTopic(String openid, Long topicId);
}
