CREATE TABLE `quiz_result` (
    `id` bigint NOT NULL AUTO_INCREMENT,
    `openid` varchar(50) NOT NULL COMMENT '用户openid',
    `questionnaire_id` bigint NOT NULL COMMENT '问卷ID',
    `answers` json NOT NULL COMMENT '用户答案，存储题目id和选项索引/分数',
    `score` int NOT NULL COMMENT '总得分',
    `max_score` int DEFAULT NULL COMMENT '满分',
    `score_percent` int DEFAULT NULL COMMENT '得分占比 0-100',
    `conclusion` text NOT NULL COMMENT '测评结论（如“轻度焦虑”）',
    `suggestions` text COMMENT '建议',
    `ai_chat_hint` varchar(500) DEFAULT NULL COMMENT '给小爱的一句话开场',
    `ai_guidance` text DEFAULT NULL COMMENT 'AI 生成的解读与建议（可空）',
    `create_time` datetime DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (`id`),
    KEY `idx_user` (`openid`),
    KEY `idx_quiz` (`questionnaire_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='测评结果表';
