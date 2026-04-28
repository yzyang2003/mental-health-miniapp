CREATE TABLE `music` (
    `id` bigint NOT NULL AUTO_INCREMENT,
    `song_name` varchar(100) NOT NULL COMMENT '歌曲名',
    `singer` varchar(100) DEFAULT NULL COMMENT '歌手',
    `cover` varchar(255) DEFAULT NULL COMMENT '封面图',
    `emotion_type` varchar(20) NOT NULL COMMENT '情绪类型：放松/振奋/助眠/专注',
    `url` varchar(255) NOT NULL COMMENT '播放链接',
    `duration` int DEFAULT NULL COMMENT '时长（秒）',
    `play_count` int DEFAULT 0 COMMENT '播放次数',
    `status` tinyint DEFAULT 1 COMMENT '状态 0-下架 1-上架',
    `create_time` datetime DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (`id`),
    KEY `idx_emotion` (`emotion_type`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='音乐疗愈表';
