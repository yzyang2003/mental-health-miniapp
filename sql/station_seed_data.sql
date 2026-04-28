-- 驿站演示数据：文章 / 音乐 / 自我疗愈
-- 请先执行 article_add_content.sql（若 article 表尚无 content 列）
-- 说明：content_url 保留为占位；小程序详情以 content 正文展示。

SET NAMES utf8mb4;

DELETE FROM `article` WHERE `id` IN (1, 2, 3, 4, 5, 6);
INSERT INTO `article` (`id`, `title`, `cover`, `summary`, `content`, `content_url`, `tags`, `view_count`, `status`)
VALUES
(1, '压力大时，先照顾好自己的呼吸',
 NULL,
 '用几分钟把注意力带回身体，给神经系统一点缓冲。',
 '当我们感到压力大时，身体往往会不自觉地绷紧：肩膀耸起、呼吸变浅、心跳加快。此时不必急着「解决问题」，可以先做一件很小的事：把注意力轻轻放到呼吸上。

一、找一个相对安静的位置，坐直或站直，双脚自然踩地。
二、用鼻子缓慢吸气 4 秒，感受腹部微微鼓起；再缓慢呼气 6 秒，让肩膀随呼气落下一点。
三、重复 6～8 次，只做这一件事，不评判自己是否「做得够好」。

这不会立刻消除所有压力源，但能帮助身体从「战斗模式」里退半步，为接下来的行动留出空间。',
 '#inline',
 JSON_ARRAY('压力', '放松', '呼吸'),
 12, 1),
(2, '睡不着的夜晚，可以怎样温柔对待自己',
 NULL,
 '减少与睡眠对抗，用可执行的小步骤降低睡前唤醒度。',
 '偶尔失眠很常见，越用力「逼自己睡着」，反而越容易焦虑。可以试试把目标从「马上睡着」换成「让身体更舒服一点」。

睡前一小时：调暗灯光，减少刺激性信息输入；温水洗漱也有助于切换状态。
躺床后：如果 20 分钟仍很清醒，可以起身到另一个房间做轻松的事，再回床，避免把床和「清醒挣扎」绑在一起。
白天：固定起床时间比「补觉到中午」更有利于建立节律。

若长期严重失眠或伴随强烈情绪低落，建议寻求专业评估与支持。',
 '#inline',
 JSON_ARRAY('睡眠', '焦虑'),
 8, 1),
(3, '情绪低落时，三件小事帮你稳住一天',
 NULL,
 '从微行动开始积累掌控感，而不是一次做完所有改变。',
 '情绪低谷时，大脑容易把一切都看成「很难」。可以把任务拆到足够小，小到几乎不可能失败。

1. 喝一口水，吃一点东西（如果很久没进食）。
2. 出门走 5 分钟，或站在窗边看看远处。
3. 给一个你信任的人发一句「今天有点难」，不必解释很多。

如果这些尝试仍难以缓解，或出现自伤念头，请尽快联系身边可信赖的人或专业援助渠道。',
 '#inline',
 JSON_ARRAY('情绪', '自我关怀'),
 15, 1),
(4, '把注意力带回当下：三分钟着陆练习',
 NULL,
 '用五感锚定「此刻」，减少思绪在担忧里打转。',
 '当脑子停不下来时，可以用「五感着陆」把注意力带回当下：

看一看：说出你周围看到的 5 样东西（颜色、形状即可）。
听一听：辨认 4 种不同的声音来源。
摸一摸：感受 3 种触感（衣服、桌面、手心温度）。
闻一闻：注意 2 种气味（没有也可以如实说「闻不到」）。
尝一尝：喝一口水，感受 1 种味道或温度。

练习结束后，不必评价效果，只要完成就已经是在照顾自己。',
 '#inline',
 JSON_ARRAY('正念', '焦虑'),
 6, 1),
(5, '考试周情绪拉满时，如何快速降压',
 NULL,
 '通过可执行的 10 分钟减压流程，帮自己从慌乱切回可行动状态。',
 '临近考试时，压力感会让我们误以为「必须一直学，不能停」。实际上，短暂停下来做减压，能让注意力更快回到正轨。

先做 1 分钟缓慢呼吸，告诉身体「现在是安全的」；再用 3 分钟写下待办，只保留今天必须完成的 3 件事；
最后做 5 分钟专注冲刺，期间只做一件最小任务。

这一套流程看似简单，却能明显降低无效焦虑，提升执行感。',
 '#inline',
 JSON_ARRAY('压力', '学习', '专注'),
 10, 1),
(6, '低能量的一天，先恢复而不是硬撑',
 NULL,
 '接住自己的低谷状态，避免陷入“越焦虑越做不动”的循环。',
 '当你感到提不起劲时，不代表你不努力，可能只是神经系统已经超负荷。

建议先做一个「恢复优先」清单：补充水分、吃点东西、离开屏幕 10 分钟、拉伸肩颈；
然后给任务重新分级，只做最关键的一件。

恢复不是偷懒，而是为了有力气继续前进。',
 '#inline',
 JSON_ARRAY('情绪管理', '自我关怀', '抑郁'),
 9, 1);

DELETE FROM `music` WHERE `id` IN (1, 2, 3, 4, 5);
INSERT INTO `music` (`id`, `song_name`, `singer`, `cover`, `emotion_type`, `url`, `duration`, `play_count`, `status`)
VALUES
(1, '轻音舒缓示例', 'SoundHelix', NULL, '放松', 'https://www.soundhelix.com/examples/mp3/SoundHelix-Song-1.mp3', 420, 0, 1),
(2, '助眠氛围示例', 'SoundHelix', NULL, '助眠', 'https://www.soundhelix.com/examples/mp3/SoundHelix-Song-2.mp3', 380, 0, 1),
(3, '专注背景示例', 'SoundHelix', NULL, '专注', 'https://www.soundhelix.com/examples/mp3/SoundHelix-Song-3.mp3', 400, 0, 1),
(4, '振奋启动示例', 'SoundHelix', NULL, '振奋', 'https://www.soundhelix.com/examples/mp3/SoundHelix-Song-4.mp3', 360, 0, 1),
(5, '深夜安稳示例', 'SoundHelix', NULL, '助眠', 'https://www.soundhelix.com/examples/mp3/SoundHelix-Song-5.mp3', 430, 0, 1);

DELETE FROM `self_healing` WHERE `id` IN (1, 2, 3, 4, 5);
INSERT INTO `self_healing` (`id`, `title`, `cover`, `description`, `issue_type`, `steps`, `duration`, `status`)
VALUES
(1, '三分钟呼吸空间',
 NULL,
 '用短练习把注意力带回身体，适合焦虑或头脑发紧时使用。',
 '焦虑',
 JSON_ARRAY(
   JSON_OBJECT('title', '觉察', 'description', '暂停一下，觉察此刻身体最紧绷的一处，不必改变它。', 'duration', 30),
   JSON_OBJECT('title', '呼吸', 'description', '鼻吸口呼，呼气略长于吸气，重复 6～8 次。', 'duration', 120),
   JSON_OBJECT('title', '扩展', 'description', '把注意力轻轻带回当下要做的一件小事。', 'duration', 30)
 ),
 180, 1),
(2, '睡前放松清单',
 NULL,
 '用温和步骤降低睡前唤醒度，减少与失眠对抗。',
 '失眠',
 JSON_ARRAY(
   JSON_OBJECT('title', '环境', 'description', '调暗灯光，手机放远一些。', 'duration', 60),
   JSON_OBJECT('title', '身体', 'description', '温水洗漱，做两次肩部绕环。', 'duration', 120),
   JSON_OBJECT('title', '呼吸', 'description', '躺好后只做缓慢腹式呼吸 1 分钟。', 'duration', 60)
 ),
 240, 1),
(3, '情绪低落时的微行动',
 NULL,
 '三件小事帮助稳定节律与掌控感。',
 '情绪低落',
 JSON_ARRAY(
   JSON_OBJECT('title', '补给', 'description', '喝一口水，若长时间未进食可补充少量食物。', 'duration', 60),
   JSON_OBJECT('title', '移动', 'description', '起身走动 2 分钟或拉伸肩颈。', 'duration', 120),
   JSON_OBJECT('title', '连接', 'description', '给信任的人发一句简短消息，或写下一句此刻的感受。', 'duration', 120)
 ),
 300, 1),
(4, '压力卸载四步法',
 NULL,
 '快速识别压力来源并分配可执行动作，适合工作学习高压时段。',
 '压力',
 JSON_ARRAY(
   JSON_OBJECT('title', '识别触发点', 'description', '写下此刻最强烈的压力来源。', 'duration', 60),
   JSON_OBJECT('title', '划分可控项', 'description', '把事情分成可控与不可控两栏。', 'duration', 90),
   JSON_OBJECT('title', '立刻行动', 'description', '挑一个可控项，执行 5 分钟。', 'duration', 120),
   JSON_OBJECT('title', '身体放松', 'description', '做一次深呼吸并拉伸肩颈。', 'duration', 60)
 ),
 330, 1),
(5, '注意力回收练习',
 NULL,
 '减少分心，适合学习与工作中断频繁时使用。',
 '注意力分散',
 JSON_ARRAY(
   JSON_OBJECT('title', '清空干扰', 'description', '关闭无关通知，把手机放到视线外。', 'duration', 60),
   JSON_OBJECT('title', '设定单一目标', 'description', '写下接下来 15 分钟唯一要完成的任务。', 'duration', 60),
   JSON_OBJECT('title', '番茄冲刺', 'description', '专注执行 10 分钟，期间不切换任务。', 'duration', 600),
   JSON_OBJECT('title', '结束复盘', 'description', '记录完成情况和下一步起点。', 'duration', 90)
 ),
 810, 1);
