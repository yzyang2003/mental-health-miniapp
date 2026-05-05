/**
 * 滚动控制模块 —— 从 ai-chat/index.js 提取
 * 职责：布局测量、滚动事件处理、打字跟底、scrollToBottom。
 */

const SCROLL_CONSTANTS = {
  PAUSE_AWAY_PX: 6,
  LOWER_THRESHOLD_PX: 8,
  FOLLOW_TYPING_MIN_MS: 180,
  FOLLOW_TYPING_DELAY_MS: 100,
  TYPING_ANCHOR_TOGGLE_MIN_MS: 220,
  AFTER_TOUCH_COOLDOWN_MS: 350,
  TYPING_FOLLOW_MEASURE_MIN_MS: 120,
  TYPING_FOLLOW_MIN_DELTA_PX: 2,
}

function createScrollManager(ctx) {
  return {
    initLayoutMetrics() {
      const sys = wx.getWindowInfo ? wx.getWindowInfo() : wx.getSystemInfoSync()
      let navTop = 8, topInset = 56, sideInsetPx = 12, shellRadiusPx = 16
      if (wx.getMenuButtonBoundingClientRect) {
        const r = wx.getMenuButtonBoundingClientRect()
        if (r && r.bottom > 0) {
          navTop = Math.max(8, r.top - 2)
          topInset = Math.max(56, r.height + 24)
          const ww = (sys && sys.windowWidth) || r.right + 12
          sideInsetPx = Math.max(4, Math.max(8, Math.round(ww - r.right)) - 4)
          shellRadiusPx = Math.max(14, Math.round(r.height / 2))
        }
      }
      const sa = sys.safeArea || null, wh = sys.windowHeight || 0
      const bottomSafeInset = sa && wh ? Math.max(wh - sa.bottom, 0) : 0
      /* capsuleTopPx: relative offset from content-area start so shell top
         aligns with capsule-button bottom + 8px gap.
         Content area starts at navTop (= capsule.top - 2),
         so offset = (capsule.top + capsule.height + 8) - navTop. */
      let capsuleTopPx = 0
      let capsuleHeight = 32
      if (wx.getMenuButtonBoundingClientRect) {
        const cb = wx.getMenuButtonBoundingClientRect()
        if (cb && cb.bottom > 0) {
          capsuleTopPx = Math.max(0, Math.round((cb.top + cb.height + 8) - navTop))
          capsuleHeight = cb.height
        }
      }
      ctx.setData({
        navTop, sideInsetPx, contentInsetPx: sideInsetPx,
        scrollGutterPx: Math.max(1, Math.round(sideInsetPx * 0.3)),
        historyTopSafePx: Math.max(Math.max(1, Math.round(sideInsetPx * 0.3)), Math.max(0, topInset - 14)),
        shellRadiusPx, topInset, bottomSafeInset, capsuleTopPx, capsuleHeight,
      })
    },

    scheduleHistoryViewportMeasure(delay = 0) {
      if (ctx.historyViewportMeasureTimer) { clearTimeout(ctx.historyViewportMeasureTimer); ctx.historyViewportMeasureTimer = null }
      ctx.historyViewportMeasureTimer = setTimeout(() => {
        ctx.historyViewportMeasureTimer = null
        const query = ctx.createSelectorQuery()
        let hRect = null, cRect = null
        query.select('.chat-history').boundingClientRect((r) => { hRect = r || null })
        query.select('.composer-wrapper').boundingClientRect((r) => { cRect = r || null })
        query.exec(() => {
          if (!hRect || !hRect.height) return
          ctx.historyViewportHeight = Math.ceil(hRect.height)
          if (!cRect) {
            ctx.composerOverlayHeight = 0
            if ((ctx.data.bottomSpacerPx || 0) !== 0) ctx.setData({ bottomSpacerPx: 0 })
            return
          }
          const overlap = Math.max(0, Math.ceil(hRect.bottom - cRect.top))
          const prev = ctx.composerOverlayHeight || 0
          ctx.composerOverlayHeight = overlap
          if ((ctx.data.bottomSpacerPx || 0) !== overlap) {
            ctx.setData({ bottomSpacerPx: overlap }, () => {
              if (ctx.autoStickToBottom && ctx.data.historyList.length && !ctx.data.isTyping) ctx.scrollToBottom(0, false, true)
            })
            return
          }
          if (ctx.autoStickToBottom && ctx.data.historyList.length && !ctx.data.isTyping && Math.abs(overlap - prev) > 0.5) {
            ctx.scrollToBottom(0, false, true)
          }
        })
      }, delay)
    },

    scheduleComposerMeasure(delay = 0) {
      if (ctx.measureComposerTimer) { clearTimeout(ctx.measureComposerTimer); ctx.measureComposerTimer = null }
      ctx.scheduleHistoryViewportMeasure(delay)
    },

    staticBottomScrollForList(list) {
      if (!(list && list.length) || ctx.data.isTyping) return { scrollIntoView: '', scrollWithAnimation: false }
      return { scrollIntoView: 'chat-bottom', scrollWithAnimation: false }
    },

    onHistoryScroll(e) {
      const d = (e && e.detail) || {}
      const st = Number(d.scrollTop || 0), sh = Number(d.scrollHeight || 0)
      ctx.latestHistoryScrollHeight = sh
      if (!ctx.historyViewportHeight) { ctx.scheduleHistoryViewportMeasure(0); return }
      if (ctx._chatFingerDown) {
        if (ctx._lastHistoryScrollTop !== undefined && Math.abs(st - ctx._lastHistoryScrollTop) > 0.5) ctx.autoStickToBottom = false
        ctx._lastHistoryScrollTop = st
        return
      }
      const evh = Math.max(0, ctx.historyViewportHeight - (ctx.composerOverlayHeight || 0))
      const dtb = sh - st - evh - (ctx.data.bottomSpacerPx || 0)
      ctx.lastDistanceToBottom = dtb
      if (ctx.data.isTyping && ctx.autoStickToBottom && !ctx._chatFingerDown) return
      if (ctx.autoStickToBottom && dtb > SCROLL_CONSTANTS.PAUSE_AWAY_PX) ctx.autoStickToBottom = false
      if (!ctx.autoStickToBottom && ctx._chatScrollUserGesture && dtb <= SCROLL_CONSTANTS.LOWER_THRESHOLD_PX) ctx.autoStickToBottom = true
    },

    onChatScrollTouchStart() {
      if (ctx._chatScrollTouchEndTimer) { clearTimeout(ctx._chatScrollTouchEndTimer); ctx._chatScrollTouchEndTimer = null }
      ctx._chatScrollCooldownUntil = 0; ctx._chatFingerDown = true; ctx._chatScrollUserGesture = true; ctx._lastHistoryScrollTop = undefined
    },

    onChatScrollTouchEnd() {
      ctx._chatFingerDown = false; ctx._lastHistoryScrollTop = undefined
      ctx._chatScrollCooldownUntil = Date.now() + SCROLL_CONSTANTS.AFTER_TOUCH_COOLDOWN_MS
      if (ctx._chatScrollTouchEndTimer) clearTimeout(ctx._chatScrollTouchEndTimer)
      ctx._chatScrollTouchEndTimer = setTimeout(() => { ctx._chatScrollTouchEndTimer = null; ctx._chatScrollUserGesture = false }, 500)
    },

    onHistoryScrollToLower() {
      if (ctx._chatScrollUserGesture && ctx.lastDistanceToBottom <= SCROLL_CONSTANTS.LOWER_THRESHOLD_PX) ctx.autoStickToBottom = true
    },

    beginTypingBottomFollow() {
      ctx.typingFollowEnabled = true
      ctx.typingFollowViewportHeight = Math.max(0, ctx.historyViewportHeight - (ctx.composerOverlayHeight || 0))
      ctx.typingFollowBottomSpacer = ctx.data.bottomSpacerPx || 0
      ctx.typingFollowLastTop = ctx.data.historyScrollTop || 0
      ctx.typingFollowMeasurePending = false; ctx.typingFollowLastMeasureAt = 0
    },

    endTypingBottomFollow() {
      ctx.typingFollowEnabled = false; ctx.typingFollowViewportHeight = 0; ctx.typingFollowBottomSpacer = 0
      ctx.typingFollowLastTop = 0; ctx.typingFollowMeasurePending = false; ctx.typingFollowLastMeasureAt = 0
    },

    followTypingBottom(force = false) {
      if (!ctx.typingFollowEnabled || !ctx.autoStickToBottom) return
      if (!force && ctx._chatFingerDown) return
      if (!force && ctx._chatScrollCooldownUntil && Date.now() < ctx._chatScrollCooldownUntil) return
      const now = Date.now()
      if (!force && now - (ctx.typingFollowLastMeasureAt || 0) < SCROLL_CONSTANTS.TYPING_FOLLOW_MEASURE_MIN_MS) return
      if (ctx.typingFollowMeasurePending) return
      ctx.typingFollowLastMeasureAt = now; ctx.typingFollowMeasurePending = true
      const query = ctx.createSelectorQuery()
      query.select('.chat-history').fields({ size: true, scrollOffset: true }, (m) => {
        ctx.typingFollowMeasurePending = false
        const sh = Number((m && m.scrollHeight) || ctx.latestHistoryScrollHeight || 0)
        if (!sh) return
        ctx.latestHistoryScrollHeight = sh
        const desired = Math.max(0, sh - ctx.typingFollowViewportHeight - ctx.typingFollowBottomSpacer)
        const next = Math.max(ctx.typingFollowLastTop || 0, Math.floor(desired))
        if (!force && Math.abs((ctx.data.historyScrollTop || 0) - next) < SCROLL_CONSTANTS.TYPING_FOLLOW_MIN_DELTA_PX) return
        ctx.typingFollowLastTop = next; ctx.lastAutoScrollAt = Date.now()
        ctx.setData({ historyScrollTop: next, scrollIntoView: '', scrollWithAnimation: false })
      })
      query.exec()
    },

    scrollToBottom(delay = 40, animate = true, force = false) {
      if (!force && ctx.typingFollowEnabled && ctx.data.isTyping && ctx.autoStickToBottom) { ctx.followTypingBottom(false); return }
      if (!force && (ctx.autoStickToBottom === false || ctx._chatFingerDown)) return
      if (!force && ctx._chatScrollCooldownUntil && Date.now() < ctx._chatScrollCooldownUntil) return
      const fdr = ctx.data.isTyping && ctx.autoStickToBottom
      if (!force) {
        const el = Date.now() - (ctx.lastAutoScrollAt || 0)
        if (fdr ? el < SCROLL_CONSTANTS.FOLLOW_TYPING_MIN_MS : el < (animate ? 120 : 160)) return
      }
      const mark = () => { ctx.lastAutoScrollAt = Date.now() }
      const clr = () => { if (ctx.scrollBottomTimer) { clearTimeout(ctx.scrollBottomTimer); ctx.scrollBottomTimer = null } }
      const gp = () => force || (ctx.autoStickToBottom !== false && !ctx._chatFingerDown && !(ctx._chatScrollCooldownUntil && Date.now() < ctx._chatScrollCooldownUntil))

      if (!animate) {
        const tgt = 'chat-bottom'
        const apply = () => { mark(); ctx.setData({ scrollIntoView: tgt, scrollWithAnimation: false }) }
        const trigger = () => {
          if (ctx.data.scrollIntoView === tgt) {
            const n = Date.now()
            if (fdr && ctx._lastTypingAnchorToggleAt && n - ctx._lastTypingAnchorToggleAt < SCROLL_CONSTANTS.TYPING_ANCHOR_TOGGLE_MIN_MS) {
              mark(); ctx.setData({ scrollIntoView: tgt, scrollWithAnimation: false }); return
            }
            ctx._lastTypingAnchorToggleAt = n
            ctx.setData({ scrollIntoView: '', scrollWithAnimation: false })
            ;(wx.nextTick || ((fn) => setTimeout(fn, 0)))(apply)
            return
          }
          apply()
        }
        const sd = delay > 0 ? delay : (!force && fdr ? SCROLL_CONSTANTS.FOLLOW_TYPING_DELAY_MS : 0)
        if (sd > 0) {
          if (!force && fdr && ctx.scrollBottomTimer) return
          clr()
          ctx.scrollBottomTimer = setTimeout(() => { ctx.scrollBottomTimer = null; if (gp()) trigger() }, sd)
          return
        }
        clr(); trigger()
        return
      }
      clr()
      ctx.setData({ scrollIntoView: '', scrollWithAnimation: true })
      ctx.scrollBottomTimer = setTimeout(() => {
        ctx.scrollBottomTimer = null
        if (!force && ctx.autoStickToBottom === false) return
        mark(); ctx.setData({ scrollIntoView: 'chat-bottom', scrollWithAnimation: true })
      }, delay)
    },
  }
}

module.exports = { SCROLL_CONSTANTS, createScrollManager }
