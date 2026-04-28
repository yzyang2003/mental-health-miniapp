CREATE TABLE `question` (
    `id` bigint NOT NULL AUTO_INCREMENT,
    `questionnaire_id` bigint NOT NULL COMMENT '所属问卷ID',
    `content` text NOT NULL COMMENT '题目内容',
    `options` json NOT NULL COMMENT '选项数组，每个选项包含 text 和 score',
    `sort_order` int DEFAULT 0 COMMENT '排序',
    `status` tinyint DEFAULT 1 COMMENT '状态',
    PRIMARY KEY (`id`),
    KEY `idx_questionnaire` (`questionnaire_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='问卷题目表';
