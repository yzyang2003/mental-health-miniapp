CREATE TABLE `topic` (
    `id` bigint NOT NULL AUTO_INCREMENT COMMENT '帖子ID',
    `content` text NOT NULL COMMENT '帖子内容',
    `images` json DEFAULT NULL COMMENT '图片URL数组，存储为JSON',
    `publisher_openid` varchar(50) NOT NULL COMMENT '发布者openid',
    `anonymous` tinyint(1) NOT NULL DEFAULT '1' COMMENT '是否匿名 0-实名 1-匿名',
    `status` tinyint NOT NULL DEFAULT '1' COMMENT '状态 0-审核中 1-正常 2-已删除',
    `create_time` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_time` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    PRIMARY KEY (`id`),
    KEY `idx_publisher` (`publisher_openid`),
    KEY `idx_status_time` (`status`, `create_time`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='树洞帖子表';
