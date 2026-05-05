/**
 * 打字机模块 —— 从 ai-chat/index.js 提取
 *
 * 职责：逐字渲染助手回复的打字机效果。
 * 所有方法通过 createTypewriter(ctx) 绑定到页面实例。
 */

// ── 常量 ──────────────────────────────────────────────────────────────
const TYPEWRITER_CONSTANTS = {
  /** 单字步进间隔（ms） */
  INTERVAL_MS: 32,
  /** 每步渲染字符数 */
  CHARS_PER_STEP: 1,
}

// ── 工厂 ──────────────────────────────────────────────────────────────
function createTypewriter(ctx) {
  return {
    /**
     * 启动打字机：逐字将 fullText 渲染到 assistantId 对应的气泡中。
     * 依赖 ctx 上的：
     *   findMessageIndexById, clearOutputTasks, beginTypingBottomFollow,
     *   followTypingBottom, finishAssistantMessage
     */
    startTypewriter(assistantId, fullText) {
      const index = ctx.findMessageIndexById(assistantId)
      if (index < 0) {
        return
      }

      ctx.clearOutputTasks({ includeNetwork: false })
      ctx.activeAssistantId = assistantId
      ctx.pendingReplyText = fullText || ''
      ctx.renderedReplyText = ''

      ctx.setData({
        [`historyList[${index}].status`]: 'typing',
        sending: false,
        isTyping: true,
      }, () => {
        ctx.beginTypingBottomFollow()
        ctx.followTypingBottom(true)
      })

      const step = () => {
        const rest = ctx.pendingReplyText.slice(ctx.renderedReplyText.length)
        if (!rest.length) {
          ctx.finishAssistantMessage(assistantId)
          return
        }

        const nextChunk = rest.slice(0, TYPEWRITER_CONSTANTS.CHARS_PER_STEP)
        ctx.renderedReplyText += nextChunk
        const currentIndex = ctx.findMessageIndexById(assistantId)
        if (currentIndex < 0) {
          ctx.clearOutputTasks({ includeNetwork: false })
          ctx.setData({
            sending: false,
            isTyping: false,
          })
          return
        }

        ctx.setData({
          [`historyList[${currentIndex}].content`]: ctx.renderedReplyText,
        })
        ctx.followTypingBottom(false)

        ctx.typewriterTimer = setTimeout(step, TYPEWRITER_CONSTANTS.INTERVAL_MS)
      }

      step()
    },
  }
}

module.exports = {
  TYPEWRITER_CONSTANTS,
  createTypewriter,
}
