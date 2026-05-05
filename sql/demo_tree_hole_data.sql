-- ============================================================
-- 树洞演示数据：5个虚拟用户 + 帖子 + 回复互动
-- 用于封测阶段展示多用户互动功能
-- ============================================================

-- 1. 插入5个虚拟用户
INSERT INTO `user` (`openid`, `nick_name`, `avatar_url`, `gender`) VALUES
('demo_openid_001', '烧碱', NULL, 1),
('demo_openid_002', '蜀黍', NULL, 1),
('demo_openid_003', '巫总', NULL, 2),
('demo_openid_004', '浩哥哥', NULL, 1),
('demo_openid_005', '熊熊熊', NULL, 0);

-- 2. 插入树洞帖子（不同用户发布，内容关于心理健康）
INSERT INTO `topic` (`content`, `images`, `publisher_openid`, `anonymous`, `status`) VALUES
-- 烧碱发的帖子（实名）
('最近期末周压力好大，每天晚上都睡不着，脑子里一直在想考试的事情。有没有同学跟我一样焦虑的？感觉需要找个人聊聊。', '[]', 'demo_openid_001', 0, 1),

-- 蜀黍发的帖子（匿名）
('今天去做了学校的心理咨询，咨询师人很好，聊完感觉轻松多了。其实迈出第一步真的很难，但试过之后发现没那么可怕。推荐大家也试试，不用害怕。', '[]', 'demo_openid_002', 1, 1),

-- 巫总发的帖子（匿名）
('想分享一个缓解焦虑的小方法：当你感到很紧张的时候，试试深呼吸，吸气4秒，屏住4秒，呼气6秒。我每次面试前都会用这个方法，真的有用！', '[]', 'demo_openid_003', 1, 1),

-- 浩哥哥发的帖子（实名）
('大一新生真的好迷茫啊，不知道该选什么方向，看到别人都有目标就很焦虑。有没有学长学姐能给点建议？', '[]', 'demo_openid_004', 0, 1),

-- 熊熊熊发的帖子（匿名）
('分享一下我的心情日记法：每天睡前写下三件今天让你开心的小事，哪怕只是吃到好吃的、天气很好这种小事。坚持了一个月，发现自己看问题的角度变了好多。', '[]', 'demo_openid_005', 1, 1),

-- 烧碱又发了一条（实名）
('谢谢大家的关心，昨天发完帖子后收到了好多私信，真的很感动。原来大家都或多或少有类似的经历，我们并不孤单。加油！', '[]', 'demo_openid_001', 0, 1);

-- 3. 插入回复互动（模拟用户之间的交流）
-- 帖子1（烧碱的压力帖）的回复
INSERT INTO `reply` (`topic_id`, `content`, `replier_openid`, `replied_user_openid`, `replied_reply_id`, `anonymous`, `status`) VALUES
(1, '抱抱你，我也是期末周焦虑星人。可以试试去操场跑几圈，运动完会好很多。', 'demo_openid_002', 'demo_openid_001', NULL, 1, 1),
(1, '同感同感，我昨天复习到凌晨三点，结果今天脑子一片空白。我们都要注意休息啊。', 'demo_openid_003', 'demo_openid_001', NULL, 1, 1),
(1, '学长建议：期末周也要保证睡眠，熬夜复习效率其实很低的。早点睡，明天精神好复习更快。', 'demo_openid_004', 'demo_openid_001', NULL, 0, 1);

-- 回复的楼中楼回复
INSERT INTO `reply` (`topic_id`, `content`, `replier_openid`, `replied_user_openid`, `replied_reply_id`, `anonymous`, `status`) VALUES
(1, '说得对，我昨晚跑了三公里，回来洗个澡确实舒服多了。', 'demo_openid_001', 'demo_openid_002', 1, 0, 1),
(1, '哈哈我也是，熬夜真的得不偿失。', 'demo_openid_005', 'demo_openid_003', 2, 1, 1);

-- 帖子2（蜀黍的咨询推荐帖）的回复
INSERT INTO `reply` (`topic_id`, `content`, `replier_openid`, `replied_user_openid`, `replied_reply_id`, `anonymous`, `status`) VALUES
(2, '请问学校的心理咨询在哪里预约啊？我一直想去但不知道怎么操作。', 'demo_openid_004', 'demo_openid_002', NULL, 0, 1),
(2, '感谢分享！我之前也一直犹豫要不要去，看完你的帖子决定试试了。', 'demo_openid_005', 'demo_openid_002', NULL, 1, 1);

-- 对楼中楼的回复
INSERT INTO `reply` (`topic_id`, `content`, `replier_openid`, `replied_user_openid`, `replied_reply_id`, `anonymous`, `status`) VALUES
(2, '在学校心理健康中心的公众号上就可以预约，或者直接去行政楼三楼问问。', 'demo_openid_002', 'demo_openid_004', 6, 1, 1),
(2, '加油！迈出第一步就好了，祝你顺利。', 'demo_openid_003', 'demo_openid_005', 7, 1, 1);

-- 帖子4（浩哥哥的迷茫帖）的回复
INSERT INTO `reply` (`topic_id`, `content`, `replier_openid`, `replied_user_openid`, `replied_reply_id`, `anonymous`, `status`) VALUES
(4, '学长来了：大一迷茫很正常，多尝试不同的事情，加入社团、听讲座、做志愿者，慢慢就会找到方向的。', 'demo_openid_001', 'demo_openid_004', NULL, 0, 1),
(4, '我大一也这样，后来发现与其焦虑不如先把手头的事情做好。成绩搞上去，机会自然就来了。', 'demo_openid_003', 'demo_openid_004', NULL, 1, 1);

-- 帖子5（熊熊熊的心情日记法）的回复
INSERT INTO `reply` (`topic_id`, `content`, `replier_openid`, `replied_user_openid`, `replied_reply_id`, `anonymous`, `status`) VALUES
(5, '这个方法听起来不错，我也要试试！从今天开始记录。', 'demo_openid_001', 'demo_openid_005', NULL, 0, 1),
(5, '坚持写日记真的有用，我已经写了一学期了，翻回去看发现自己成长了好多。', 'demo_openid_004', 'demo_openid_005', NULL, 0, 1);

-- 帖子6（烧碱的感谢帖）的回复
INSERT INTO `reply` (`topic_id`, `content`, `replier_openid`, `replied_user_openid`, `replied_reply_id`, `anonymous`, `status`) VALUES
(6, '看到你心情变好了真开心！我们树洞社区就是要有这种互相支持的氛围。', 'demo_openid_003', 'demo_openid_001', NULL, 1, 1),
(6, '加油！有需要随时来树洞倾诉，大家都在。', 'demo_openid_005', 'demo_openid_001', NULL, 1, 1);

-- ============================================================
-- 数据插入完成
-- 预览效果：
-- - 5个用户：烧碱、蜀黍、巫总、浩哥哥、熊熊熊
-- - 6个帖子：涵盖考试焦虑、心理咨询推荐、缓解技巧、迷茫求助、心情日记、感谢分享
-- - 15条回复：包含楼中楼互动，模拟真实社区氛围
-- - 匿名/实名混合：更真实
-- ============================================================
