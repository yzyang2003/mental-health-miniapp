CREATE TABLE `reply` (
    `id` bigint NOT NULL AUTO_INCREMENT COMMENT '回复ID',
    `topic_id` bigint NOT NULL COMMENT '所属帖子ID',
    `content` text NOT NULL COMMENT '回复内容',
    `replier_openid` varchar(50) NOT NULL COMMENT '回复者openid',
    `replied_user_openid` varchar(50) DEFAULT NULL COMMENT '被回复用户openid（用于楼层回复）',
    `replied_reply_id` bigint DEFAULT NULL COMMENT '被回复的回复ID（用于级联删除）',
    `anonymous` tinyint(1) NOT NULL DEFAULT '1' COMMENT '是否匿名 1-匿名 0-实名',
    `status` tinyint NOT NULL DEFAULT '1' COMMENT '状态 0-审核中 1-正常 2-已删除',
    `create_time` datetime DEFAULT CURRENT_TIMESTAMP,
    `update_time` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    PRIMARY KEY (`id`),
    KEY `idx_topic_id` (`topic_id`),
    KEY `idx_replied_reply_id` (`replied_reply_id`),
    KEY `idx_replier` (`replier_openid`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='树洞回复表';
