CREATE TABLE `music_playlist` (
    `id` bigint NOT NULL AUTO_INCREMENT,
    `name` varchar(100) NOT NULL COMMENT '歌单名称',
    `description` varchar(255) DEFAULT NULL COMMENT '歌单描述',
    `cover_image` varchar(255) DEFAULT NULL COMMENT '封面图URL',
    `share_url` varchar(500) NOT NULL COMMENT '网易云歌单链接',
    `emotion_type` varchar(20) NOT NULL COMMENT '情绪类型：放松/专注/助眠/释放/振奋',
    `duration_text` varchar(20) DEFAULT NULL COMMENT '时长描述，如60分钟',
    `play_count` int DEFAULT 0 COMMENT '播放次数',
    `status` tinyint DEFAULT 1 COMMENT '状态 0-下架 1-上架',
    `create_time` datetime DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (`id`),
    KEY `idx_emotion` (`emotion_type`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='音乐疗愈歌单表';
