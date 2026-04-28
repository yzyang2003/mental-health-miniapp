CREATE TABLE `user` (
    `id` bigint NOT NULL AUTO_INCREMENT COMMENT '主键ID',
    `openid` varchar(50) NOT NULL COMMENT '微信OpenID',
    `nick_name` varchar(50) DEFAULT NULL COMMENT '昵称',
    `avatar_url` varchar(255) DEFAULT NULL COMMENT '头像URL',
    `province` varchar(20) DEFAULT NULL COMMENT '省份',
    `city` varchar(20) DEFAULT NULL COMMENT '城市',
    `country` varchar(20) DEFAULT NULL COMMENT '国家',
    `gender` tinyint DEFAULT '0' COMMENT '性别 0未知 1男 2女',
    `language` varchar(10) DEFAULT NULL COMMENT '语言',
    `create_time` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_time` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_openid` (`openid`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='用户表';
