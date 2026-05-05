INSERT INTO `music` (`id`, `song_name`, `singer`, `cover`, `emotion_type`, `url`, `duration`, `play_count`, `status`)
VALUES (6, '助睡眠白噪音：大海、溪流、雨', '大虾201314', '/assets/music/b839683faa7eb6daa413c0e9f692a3a8.jpg', '助眠', 'https://music.163.com/m/playlist?id=5016977210&creatorId=3329468118', 0, 0, 1)
ON DUPLICATE KEY UPDATE
  `song_name` = '助睡眠白噪音：大海、溪流、雨',
  `cover` = '/assets/music/b839683faa7eb6daa413c0e9f692a3a8.jpg',
  `url` = 'https://music.163.com/m/playlist?id=5016977210&creatorId=3329468118';
