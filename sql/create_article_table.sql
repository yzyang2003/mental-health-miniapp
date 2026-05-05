-- 创建 article 表（去掉 JSON 列的索引，改用生成列索引）

SET NAMES utf8mb4;

CREATE TABLE IF NOT EXISTS `article` (
    `id` bigint NOT NULL AUTO_INCREMENT,
    `title` varchar(100) NOT NULL COMMENT '标题',
    `cover` varchar(255) DEFAULT NULL COMMENT '封面图URL',
    `summary` varchar(500) DEFAULT NULL COMMENT '摘要',
    `content` mediumtext COMMENT '正文（纯文本，可含换行）',
    `content_url` varchar(255) NOT NULL COMMENT '文章内容URL（可存富文本或外部链接；正文优先展示 content）',
    `tags` json DEFAULT NULL COMMENT '标签数组，如["焦虑","抑郁"]',
    `tags_first` varchar(50) GENERATED ALWAYS AS (JSON_UNQUOTE(JSON_EXTRACT(tags, '$[0]'))) STORED COMMENT '第一个标签，用于索引查询',
    `view_count` int DEFAULT 0 COMMENT '浏览次数',
    `status` tinyint DEFAULT 1 COMMENT '状态 0-下架 1-上架',
    `create_time` datetime DEFAULT CURRENT_TIMESTAMP,
    `update_time` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    PRIMARY KEY (`id`),
    KEY `idx_tags_first` (`tags_first`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='心理文章表';