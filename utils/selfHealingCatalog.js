const CATEGORIES = [
  { id: 0, name: '全部练习' },
  { id: 1, name: '缓解焦虑' },
  { id: 2, name: '改善情绪' },
  { id: 3, name: '应对压力' },
  { id: 4, name: '自我关怀' },
  { id: 5, name: '睡眠改善' },
  { id: 6, name: '专注当下' },
]

const EXERCISES = [
  {
    id: 1,
    categoryId: 1,
    categoryName: '缓解焦虑',
    title: '4-7-8 呼吸法',
    durationText: '3分钟',
    shortDesc: '通过特定的呼吸节奏，快速平复紧张情绪，激活身体的放松反应。',
    principle:
      '4-7-8呼吸法通过延长呼气时间，激活副交感神经系统，帮助身体从“战斗或逃跑”模式切换到“休息和消化”模式。福州市疾控中心推荐用于缓解日常焦虑。',
    steps: [
      { order: 1, title: '准备', description: '找一个舒适的位置坐下或躺下，背部挺直，舌尖顶住上颚。' },
      { order: 2, title: '吸气4秒', description: '闭上嘴巴，用鼻子轻轻吸气，在心里默数4秒。' },
      { order: 3, title: '屏息7秒', description: '屏住呼吸，在心里默数7秒。' },
      { order: 4, title: '呼气8秒', description: '用嘴巴缓缓呼气，发出轻微的“呼”声，在心里默数8秒。' },
      { order: 5, title: '重复', description: '以上为一组，建议连续做4-5组。练习后静坐片刻，感受身体的变化。' },
    ],
  },
  {
    id: 2,
    categoryId: 1,
    categoryName: '缓解焦虑',
    title: '5-4-3-2-1 接地法',
    durationText: '3分钟',
    shortDesc: '通过调动五感将注意力拉回当下，快速打断焦虑思维的循环。',
    principle:
      '当焦虑来袭时，我们的注意力常被未来导向的“如果...怎么办？”所占据。5-4-3-2-1接地法通过刻意将注意力转向当下的感官输入，将大脑从焦虑模式切换至观察模式，有效打断灾难化思维。美国焦虑症协会推荐为即时干预技术。',
    steps: [
      {
        order: 1,
        title: '视觉',
        description:
          '环顾四周，在心里说出你看到的5样东西。例如：“我看到一盏台灯、一个水杯、一本书、一扇窗、一棵绿植。”',
      },
      {
        order: 2,
        title: '听觉',
        description:
          '仔细聆听，在心里说出你听到的4种声音。例如：“我听到空调声、键盘声、窗外的车声、自己的呼吸声。”',
      },
      {
        order: 3,
        title: '触觉',
        description:
          '感受身体的触感，在心里说出你接触到的3样东西。例如：“脚踩在地上的感觉、手放在腿上的温度、衣服与皮肤的接触。”',
      },
      {
        order: 4,
        title: '嗅觉',
        description:
          '用心去闻，在心里说出你闻到的2种气味。例如：“空气中淡淡的咖啡香、纸张的味道。”',
      },
      {
        order: 5,
        title: '味觉',
        description:
          '留意口腔里的味道，在心里说出1种味道。可以是刚刚喝过的水、吃过的食物，或只是空气的味道。',
      },
    ],
  },
  {
    id: 3,
    categoryId: 2,
    categoryName: '改善情绪',
    title: '感恩微记录',
    durationText: '5分钟',
    shortDesc: '每天记录三件微小确幸，用积极关注重塑情绪体验。',
    principle:
      '感恩记录通过刻意将注意力转向生活中的积极体验，打破大脑天然的“负面偏好”。研究表明，持续2周练习者的积极情绪体验提升34%（宾夕法尼亚大学实验）。',
    steps: [
      { order: 1, title: '准备', description: '准备一张纸或打开手机备忘录，深呼吸让自己安静下来。' },
      { order: 2, title: '回忆第一件小事', description: '回想今天发生的一件让你感到温暖或开心的小事，并写下来。例如：“同事和我打招呼时笑得很温暖。”' },
      { order: 3, title: '回忆第二件小事', description: '再回忆一件让你感到满足的小事，并写下来。例如：“午餐吃到了自己想吃的东西。”' },
      { order: 4, title: '回忆第三件小事', description: '最后回忆一件让你感到平静或轻松的小事，并写下来。例如：“下班路上看到了很美的晚霞。”' },
      { order: 5, title: '感受感恩', description: '闭上眼睛，在心里逐一想这三件事，感受它们带来的积极情绪，哪怕只是一点点温暖。' },
    ],
  },
  {
    id: 4,
    categoryId: 2,
    categoryName: '改善情绪',
    title: '自由书写',
    durationText: '10分钟',
    shortDesc: '不加评判地写下任何想法，用书写倾倒情绪垃圾。',
    principle:
      '自由书写是一种非评判性表达练习。通过不加过滤地将头脑中的念头倾倒到纸上，可以帮助释放压抑的情绪，理清混乱的思绪，同时减轻焦虑和压力。',
    steps: [
      { order: 1, title: '准备', description: '拿出纸笔或打开手机便签，给自己设定10分钟不被打扰的时间。' },
      { order: 2, title: '开始书写', description: '不停笔地写满10分钟。想到什么就写什么，不需要逻辑，不需要修饰，不需要评判好坏。' },
      {
        order: 3,
        title: '可以写这些',
        description:
          '可以写当下的感受：“我觉得有点烦……”；可以写脑中闪过的念头：“昨天的梦还记得……”；可以写身体的感觉：“肩膀有点紧……”',
      },
      { order: 4, title: '坚持不中断', description: '不要停下来修改或重读，也不要担心错别字。如果突然不知道写什么，就写“我不知道写什么”直到新念头出现。' },
      { order: 5, title: '结束与处理', description: '10分钟后停笔。可以选择保存下来，也可以直接撕掉扔掉——重点是表达本身，而不是留存。' },
    ],
  },
  {
    id: 5,
    categoryId: 3,
    categoryName: '应对压力',
    title: '三口呼吸',
    durationText: '2分钟',
    shortDesc: '随时随地用三口深呼吸快速重置身心状态。',
    principle:
      '在压力情境中，呼吸往往是身体第一个发生变化的信号（变浅、变急促）。有意识地做三口深呼吸，能快速打断压力反应的链条，让身体从紧张状态中暂时脱离。',
    steps: [
      { order: 1, title: '第一口呼吸', description: '先放下手头的事。深深地吸气，感受气息进入身体；缓缓地呼气，留意呼吸进出的感觉。' },
      { order: 2, title: '第二口呼吸', description: '深深地吸气，缓缓地呼气。呼气的时候，刻意把肩膀放松，让全身松弛下来。' },
      { order: 3, title: '第三口呼吸', description: '深深地吸气，缓缓地呼气。呼气之后，轻轻问自己：“现在，什么最重要？”' },
      { order: 4, title: '回归当下', description: '感受一下身体的变化，带着刚才问题的答案，重新回到当前的任务中。' },
    ],
  },
  {
    id: 6,
    categoryId: 4,
    categoryName: '自我关怀',
    title: '蝴蝶抱',
    durationText: '3分钟',
    shortDesc: '通过简单的自我拥抱和规律轻拍，给自己安全感和安抚。',
    principle:
      '蝴蝶抱是一种双侧刺激的自我安抚技术，最初用于创伤治疗。规律节奏结合呼吸与自我拥抱，可以提升安全感与对身体觉察，舒缓紧张不安。',
    steps: [
      { order: 1, title: '准备姿势', description: '找一个安静的地方坐下。将双手交叉放在胸前，右手放在左肩上，左手放在右肩上，像蝴蝶翅膀一样。' },
      { order: 2, title: '开始轻拍', description: '闭上眼睛，用双手交替轻轻拍打肩膀，左一下、右一下，保持缓慢而规律的节奏。' },
      { order: 3, title: '配合呼吸', description: '在轻拍的同时，做深长而缓慢的呼吸。吸气时感受胸腔的扩张，呼气时感受身体的放松。' },
      { order: 4, title: '默念安抚语', description: '在心里对自己说一些温和的话，比如：“我是安全的。”“一切都会好起来的。”“我在这里陪着你。”' },
      { order: 5, title: '结束练习', description: '持续2-3分钟后，慢慢停下轻拍，双手自然放在腿上，静坐片刻，感受身体的变化。' },
    ],
  },
  {
    id: 7,
    categoryId: 4,
    categoryName: '自我关怀',
    title: '自我拥抱',
    durationText: '2分钟',
    shortDesc: '练习把自己拥入怀中，用身体的动作传递自我接纳与关怀。',
    principle:
      '自我拥抱是一种通过身体动作传递自我关怀的练习。身体的接触会释放催产素，即使这个拥抱来自自己，也能激活大脑中与安全感相关的区域。',
    steps: [
      { order: 1, title: '准备姿势', description: '找一个安静舒适的姿势坐下或站立。深呼吸一两次，让自己安静下来。' },
      { order: 2, title: '拥抱自己', description: '抬起双臂，环抱住自己的肩膀或上臂。用力程度以自己感到舒适为宜，就像拥抱一个需要安慰的好朋友。' },
      { order: 3, title: '保持与感受', description: '闭上眼睛，保持这个姿势1-2分钟。感受手臂传递的温度和压力，感受胸腔的起伏。' },
      { order: 4, title: '对自己说句话', description: '在心里对自己说一句温柔的话，比如：“辛苦了。”“没关系，一切都会过去。”“我在这里陪着你。”' },
      { order: 5, title: '慢慢放下', description: '慢慢松开手臂，深呼吸一次，感受身体的变化。可以轻轻拍拍自己的肩膀或手臂作为结束。' },
    ],
  },
  {
    id: 8,
    categoryId: 5,
    categoryName: '睡眠改善',
    title: '身体扫描',
    durationText: '10分钟',
    shortDesc: '睡前将注意力依次放在身体各部位，释放紧张，准备入睡。',
    principle:
      '身体扫描是一种正念练习，通过将注意力依次带到身体不同部位，帮助大脑从思维活动切换到身体感受，从而降低大脑活跃度，为入睡做准备。',
    steps: [
      { order: 1, title: '准备', description: '平躺在床上，闭上眼睛，做几次深呼吸让自己放松下来。' },
      {
        order: 2,
        title: '从脚开始',
        description:
          '将注意力带到脚趾。感受脚趾的温度、与床单的接触，不需要改变任何感觉，只是观察。然后慢慢将注意力移到脚掌、脚踝。',
      },
      { order: 3, title: '向上移动', description: '继续将注意力依次向上移动：小腿 → 膝盖 → 大腿 → 臀部 → 腹部 → 胸部。' },
      {
        order: 4,
        title: '继续向上',
        description:
          '注意力继续移动到背部 → 肩膀 → 手臂 → 手掌 → 手指。在每个部位停留片刻，感受那里的感觉，有紧张感就试着在呼气时让它释放。',
      },
      {
        order: 5,
        title: '最后到头颈',
        description:
          '注意力依次经过颈部 → 下巴 → 嘴巴 → 鼻子 → 眼睛 → 额头 → 头顶。最后，感受整个身体作为一个整体，沉浸在放松的状态中。',
      },
    ],
  },
  {
    id: 9,
    categoryId: 5,
    categoryName: '睡眠改善',
    title: '478呼吸助眠',
    durationText: '5分钟',
    shortDesc: '在床上即可完成的呼吸练习，帮助身体切换到放松模式，自然入睡。',
    principle:
      '延长呼气时间有助于激活副交感神经系统，降低唤醒水平，为入睡创造更舒适的生理条件。',
    steps: [
      { order: 1, title: '准备', description: '平躺在床上，枕头高度舒适。将舌尖顶住上颚前部，保持整个练习过程不变。' },
      { order: 2, title: '完全呼气', description: '先张嘴完全呼出一口气，发出轻微的“呼”声。' },
      { order: 3, title: '吸气4秒', description: '闭嘴，用鼻子轻轻吸气，在心里默数4秒。' },
      { order: 4, title: '屏息7秒', description: '屏住呼吸，在心里默数7秒。' },
      { order: 5, title: '呼气8秒', description: '张嘴缓缓呼气，发出“呼”声，在心里默数8秒。重复3-5组。完成后自然呼吸，让身体继续放松。' },
    ],
  },
  {
    id: 10,
    categoryId: 6,
    categoryName: '专注当下',
    title: '正念呼吸',
    durationText: '5分钟',
    shortDesc: '将注意力完全放在呼吸上，训练大脑聚焦当下的能力。',
    principle:
      '正念呼吸是最基础的正念练习。通过反复将注意力带回呼吸，训练大脑的“注意力肌肉”，提高专注力，减少思维散乱。',
    steps: [
      { order: 1, title: '准备姿势', description: '找一把椅子坐下，背部自然挺直但不僵硬，双手放在腿上，闭上眼睛或视线柔和下垂。' },
      { order: 2, title: '觉察呼吸', description: '将注意力带到呼吸上。感受气息进入鼻腔、经过喉咙、充满胸腔和腹部的过程。' },
      { order: 3, title: '跟随呼吸', description: '不需要刻意改变呼吸的节奏或深度，只是观察它。知道自己在吸气，知道自己在呼气。' },
      { order: 4, title: '走神时拉回来', description: '当发现注意力飘走时（这是正常且必然发生的），不需要评判自己，只是温和地把注意力重新带回呼吸。' },
      { order: 5, title: '结束', description: '练习5分钟后，慢慢睁开眼睛，感受一下此刻身心状态的变化，再继续日常活动。' },
    ],
  },
  {
    id: 11,
    categoryId: 6,
    categoryName: '专注当下',
    title: '五感品茶',
    durationText: '5分钟',
    shortDesc: '将喝茶变成正念练习，用五感充分体验当下的片刻。',
    principle:
      '将日常习惯转化为正念练习是正念生活化的核心理念。喝茶时调动五感，能把注意力锚定在当下。',
    steps: [
      { order: 1, title: '视觉', description: '拿起茶杯，观察茶汤的颜色、透明度。看茶叶在水中舒展的姿态，看热气袅袅升起的样子。' },
      { order: 2, title: '触觉', description: '双手捧着茶杯，感受杯壁传来的温度。感受茶水的重量在手中的变化。' },
      { order: 3, title: '嗅觉', description: '将杯子靠近鼻子，闭上眼睛，深深闻一下茶的香气。注意香气的层次。' },
      { order: 4, title: '味觉', description: '小口啜饮，让茶水在口腔中停留片刻。感受茶汤在舌尖、舌侧、舌根的不同味道，感受温度和质地的变化。' },
      { order: 5, title: '听觉', description: '喝茶时留意周围的声音。可能是水烧开的声音、茶杯与桌面接触的声音，或者只是安静的背景声。' },
    ],
  },
]

const EXERCISE_BY_ID = EXERCISES.reduce((acc, item) => {
  acc[item.id] = item
  return acc
}, {})

const COMPLETED_MAP_KEY = 'selfHealingCompletedMap'

function decorateExercise(item) {
  return {
    ...item,
    stepCount: Array.isArray(item.steps) ? item.steps.length : 0,
  }
}

function decorateExercises(exercises) {
  return exercises.map(decorateExercise)
}

function readCompletedMap() {
  const raw = wx.getStorageSync(COMPLETED_MAP_KEY) || {}
  return raw && typeof raw === 'object' ? raw : {}
}

function writeCompleted(id, completed) {
  const map = readCompletedMap()
  map[String(id)] = Boolean(completed)
  wx.setStorageSync(COMPLETED_MAP_KEY, map)
}

function mergeCompleted(exercises) {
  const map = readCompletedMap()
  return exercises.map((item) => ({
    ...item,
    completed: Boolean(map[String(item.id)]),
  }))
}

function filterExercises(exercises, categoryId) {
  if (!categoryId) {
    return exercises
  }
  return exercises.filter((item) => item.categoryId === categoryId)
}

module.exports = {
  CATEGORIES,
  EXERCISES,
  EXERCISE_BY_ID,
  decorateExercises,
  mergeCompleted,
  filterExercises,
  writeCompleted,
}
