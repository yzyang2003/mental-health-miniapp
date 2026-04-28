CREATE TABLE `questionnaire` (
    `id` bigint NOT NULL AUTO_INCREMENT COMMENT '问卷ID',
    `title` varchar(100) NOT NULL COMMENT '问卷标题（如“焦虑自评量表”）',
    `description` varchar(500) DEFAULT NULL COMMENT '问卷描述',
    `cover` varchar(255) DEFAULT NULL COMMENT '封面图',
    `type` varchar(20) NOT NULL COMMENT '问卷类型（如焦虑、抑郁）',
    `scoring_rule` json DEFAULT NULL COMMENT '评分规则（如各选项分值）',
    `status` tinyint DEFAULT 1 COMMENT '状态 0-停用 1-启用',
    `create_time` datetime DEFAULT CURRENT_TIMESTAMP,
    `update_time` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='问卷主表';
