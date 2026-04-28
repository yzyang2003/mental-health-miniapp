CREATE TABLE `chat_history` (
    `id` bigint NOT NULL AUTO_INCREMENT,
    `openid` varchar(50) NOT NULL COMMENT '用户openid',
    `role` varchar(20) NOT NULL COMMENT '角色：user / assistant',
    `content` text NOT NULL COMMENT '消息内容',
    `create_time` datetime DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (`id`),
    KEY `idx_user_time` (`openid`, `create_time`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='AI聊天记录表';
