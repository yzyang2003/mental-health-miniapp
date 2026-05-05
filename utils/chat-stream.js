/**
 * 流式处理模块 —— 从 ai-chat/index.js 提取
 * 职责：SSE / WebSocket 流式通信、响应解析、流式缓冲。
 */

const { requestRaw, formatRequestError } = require('./request')

const STREAM_MODES = { TYPING: 'typing', CHUNK: 'chunk', SOCKET: 'socket' }
const REQUEST_ENDPOINT = '/api/consult/chat/send'
const CHUNK_STREAM_ENDPOINT = '/api/consult/chat/stream'

function createStreamHandler(ctx) {
  return {
    parseChatSendResponse(res) {
      let body = res && res.data
      if (typeof body === 'string') { try { body = JSON.parse(body) } catch (e) { return { ok: false, error: '服务器返回非 JSON，请检查后端与 baseUrl' } } }
      if (!body || typeof body !== 'object') return { ok: false, error: '响应格式异常' }
      if (Object.prototype.hasOwnProperty.call(body, 'code') && !(body.code >= 200 && body.code < 300)) return { ok: false, error: (body && body.message) || '发送消息失败' }
      let payload = Object.prototype.hasOwnProperty.call(body, 'data') ? body.data : body
      if (payload == null || typeof payload !== 'object') return { ok: false, error: '缺少回复数据' }
      const text = typeof payload.reply === 'string' ? payload.reply.trim() : ''
      if (!text) return { ok: false, error: '小爱未返回有效内容，请重试' }
      if (text === 'request:ok') return { ok: false, error: '解析异常，请勿将网络状态当作回复。请重试或检查后端接口。' }
      return { ok: true, reply: text }
    },

    decodeChunk(arrayBuffer) {
      try { return new TextDecoder('utf-8').decode(arrayBuffer) } catch (e) { return '' }
    },

    sendStandardMessage(message, assistantId) {
      const { task, promise } = requestRaw({ url: REQUEST_ENDPOINT, method: 'POST', data: { message }, timeout: 20000 })
      ctx.standardRequestTask = task
      promise
        .then((res) => {
          if (ctx.stoppedAssistantId === assistantId) return
          const parsed = ctx.parseChatSendResponse(res)
          if (!parsed.ok) {
            const errorText = parsed.error || '发送消息失败'
            ctx.markAssistantError(assistantId, errorText)
            ctx.setData({ errorMessage: errorText, lastFailedMessage: message, message, sending: false, isTyping: false })
            ctx.scheduleComposerMeasure(40); ctx.flushDeferredHistoryRefresh()
            return
          }
          ctx.startTypewriter(assistantId, parsed.reply)
        })
        .catch((error) => {
          if (error && error.errMsg && error.errMsg.includes('abort')) return
          const errorText = formatRequestError(error, '发送消息失败')
          ctx.markAssistantError(assistantId, errorText)
          if (ctx.isUnauthorizedError(error)) {
            ctx.setData({ sending: false, isTyping: false })
          } else {
            ctx.setData({ errorMessage: errorText, lastFailedMessage: message, message, sending: false, isTyping: false })
          }
          ctx.scheduleComposerMeasure(40); ctx.flushDeferredHistoryRefresh()
        })
        .finally(() => { if (ctx.standardRequestTask === task) ctx.standardRequestTask = null })
    },

    sendChunkStreamMessage(message, assistantId) {
      const token = getApp().globalData.token || wx.getStorageSync('token') || ''
      ctx.streamResidue = ''
      const requestTask = wx.request({
        url: `${getApp().globalData.baseUrl}${CHUNK_STREAM_ENDPOINT}`,
        method: 'POST', enableChunked: true, responseType: 'arraybuffer',
        header: { 'content-type': 'application/json', ...(token ? { Authorization: `Bearer ${token}` } : {}) },
        data: { message },
        success: () => { ctx.finishAssistantMessage(assistantId) },
        fail: (error) => {
          const errorText = formatRequestError(error, '流式请求失败')
          ctx.markAssistantError(assistantId, errorText)
          if (ctx.isUnauthorizedError(error)) {
            ctx.setData({ sending: false, isTyping: false })
          } else {
            ctx.setData({ errorMessage: errorText, lastFailedMessage: message, message, sending: false, isTyping: false })
          }
          ctx.scheduleComposerMeasure(40); ctx.flushDeferredHistoryRefresh()
        },
      })
      ctx.streamRequestTask = requestTask
      requestTask.onChunkReceived((res) => { ctx.consumeSseChunk(ctx.decodeChunk(res.data), assistantId) })
    },

    sendSocketStreamMessage(message, assistantId, socketStreamUrl) {
      if (!socketStreamUrl) {
        const errorText = '当前未配置 WebSocket 流式地址'
        ctx.markAssistantError(assistantId, errorText)
        ctx.setData({ errorMessage: errorText, lastFailedMessage: message, message, sending: false, isTyping: false })
        ctx.scheduleComposerMeasure(40); ctx.flushDeferredHistoryRefresh()
        return
      }
      const socketTask = wx.connectSocket({ url: socketStreamUrl })
      ctx.socketFailed = false; ctx.socketTask = socketTask
      socketTask.onOpen(() => { socketTask.send({ data: JSON.stringify({ message }) }) })
      socketTask.onMessage((res) => { ctx.consumeSocketChunk(res.data, assistantId) })
      socketTask.onError(() => {
        ctx.socketFailed = true
        ctx.markAssistantError(assistantId, '连接流式服务失败')
        ctx.setData({ errorMessage: '连接流式服务失败', lastFailedMessage: message, message, sending: false, isTyping: false })
        ctx.scheduleComposerMeasure(40)
      })
      socketTask.onClose(() => {
        if (ctx.socketFailed) { ctx.clearOutputTasks(); ctx.scheduleComposerMeasure(40); ctx.flushDeferredHistoryRefresh(); return }
        ctx.finishAssistantMessage(assistantId)
      })
    },

    consumeSseChunk(chunkText, assistantId) {
      if (!chunkText) return
      const merged = `${ctx.streamResidue || ''}${chunkText}`
      const events = merged.split('\n\n')
      ctx.streamResidue = events.pop() || ''
      events.forEach((evt) => {
        const line = evt.split('\n').find((l) => l.trim().startsWith('data:'))
        if (!line) return
        const payload = line.replace(/^data:\s*/, '').trim()
        if (!payload || payload === '[DONE]') { ctx.finishAssistantMessage(assistantId); return }
        ctx.appendStreamContent(payload, assistantId)
      })
    },

    consumeSocketChunk(payload, assistantId) {
      if (payload) ctx.appendStreamContent(payload, assistantId)
    },

    appendStreamContent(payload, assistantId) {
      let content = payload
      try {
        const p = JSON.parse(payload)
        content = p.content || p.delta || p.reply || ''
        if (p.done) ctx.finishAssistantMessage(assistantId)
      } catch (e) { content = payload }
      if (content) ctx.bufferStreamAppend(assistantId, content)
    },

    bufferStreamAppend(assistantId, content) {
      ctx.activeAssistantId = assistantId
      ctx.streamAppendBuffer = `${ctx.streamAppendBuffer || ''}${content}`
      ctx.setData({ sending: false, isTyping: true }, () => { ctx.beginTypingBottomFollow() })
      if (ctx.streamFlushTimer) return
      ctx.streamFlushTimer = setTimeout(() => { ctx.flushStreamAppend() }, 48)
    },

    flushStreamAppend() {
      if (ctx.streamFlushTimer) { clearTimeout(ctx.streamFlushTimer); ctx.streamFlushTimer = null }
      if (!ctx.activeAssistantId || !ctx.streamAppendBuffer) return
      const index = ctx.findMessageIndexById(ctx.activeAssistantId)
      if (index < 0) {
        ctx.streamAppendBuffer = ''; ctx.clearOutputTasks({ includeNetwork: false })
        ctx.setData({ sending: false, isTyping: false }); return
      }
      const next = `${ctx.data.historyList[index].content || ''}${ctx.streamAppendBuffer}`
      ctx.streamAppendBuffer = ''
      ctx.setData({ [`historyList[${index}].status`]: 'typing', [`historyList[${index}].content`]: next })
      ctx.followTypingBottom(false)
    },
  }
}

module.exports = { STREAM_MODES, createStreamHandler }
