CREATE TABLE `self_healing` (
    `id` bigint NOT NULL AUTO_INCREMENT,
    `title` varchar(100) NOT NULL COMMENT '练习名称',
    `cover` varchar(255) DEFAULT NULL COMMENT '封面图',
    `description` varchar(500) DEFAULT NULL COMMENT '简介',
    `issue_type` varchar(20) NOT NULL COMMENT '对应问题类型：失眠/焦虑/情绪低落等',
    `steps` json NOT NULL COMMENT '步骤数组，每个步骤包含 title, description, duration 等',
    `duration` int DEFAULT NULL COMMENT '预计总时长（秒）',
    `status` tinyint DEFAULT 1 COMMENT '状态 0-下架 1-上架',
    `create_time` datetime DEFAULT CURRENT_TIMESTAMP,
    `update_time` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    PRIMARY KEY (`id`),
    KEY `idx_issue_type` (`issue_type`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='自我疗愈表';
