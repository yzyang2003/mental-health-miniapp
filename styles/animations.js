/**
 * Animation Presets for MindBridge
 * Reusable animation configurations for WeChat Mini Program
 */

const easings = {
  ease: 'cubic-bezier(0.25, 0.1, 0.25, 1)',
  easeIn: 'cubic-bezier(0.55, 0.085, 0.68, 0.53)',
  easeOut: 'cubic-bezier(0.215, 0.61, 0.355, 1)',
  easeInOut: 'cubic-bezier(0.455, 0.03, 0.515, 0.955)',
  spring: 'cubic-bezier(0.34, 1.56, 0.64, 1)',
};

const durations = {
  instant: 100,
  fast: 200,
  normal: 350,
  slow: 500,
};

/**
 * Create keyframe animation config
 * @param {Object} options
 * @param {number} options.duration - Duration in ms
 * @param {string} options.timingFunction - Easing function
 * @param {number} options.delay - Delay in ms (default 0)
 * @param {string} options.fillMode - Fill mode (default 'forwards')
 * @param {number} options.iterationCount - Loop count (default 1, use Infinity for infinite)
 * @returns {Object} Animation config object
 */
function createKeyframe({
  duration = durations.normal,
  timingFunction = easings.ease,
  delay = 0,
  fillMode = 'forwards',
  iterationCount = 1,
} = {}) {
  return {
    duration,
    timingFunction,
    delay,
    fill: fillMode,
    iterationCount,
  };
}

// ==================== Fade Animations ====================

const fadeIn = createKeyframe({ duration: durations.normal, timingFunction: easings.easeOut });
const fadeInFast = createKeyframe({ duration: durations.fast, timingFunction: easings.easeOut });
const fadeInSlow = createKeyframe({ duration: durations.slow, timingFunction: easings.easeOut });
const fadeOut = createKeyframe({ duration: durations.normal, timingFunction: easings.easeIn });

// ==================== Scale Animations ====================

const scaleUp = createKeyframe({ duration: durations.fast, timingFunction: easings.spring });
const scaleDown = createKeyframe({ duration: durations.fast, timingFunction: easings.easeIn });

// ==================== Slide Animations ====================

const slideInBottom = createKeyframe({ duration: durations.normal, timingFunction: easings.easeOut });
const slideInTop = createKeyframe({ duration: durations.normal, timingFunction: easings.easeOut });
const slideOutBottom = createKeyframe({ duration: durations.fast, timingFunction: easings.easeIn });

// ==================== Pulse Animation ====================

const pulse = createKeyframe({
  duration: 1200,
  timingFunction: easings.easeInOut,
  iterationCount: Infinity,
});

/**
 * Apply scale animation on tap (for bindtouchstart/bindtouchend)
 * @param {Object} pageInstance - Page this (optional, for setData)
 * @param {string} dataKey - Data key to update (optional)
 * @returns {Object} Event handlers { onTouchStart, onTouchEnd }
 */
function tapScale(pageInstance, dataKey) {
  let scaleTimer = null;

  const scaleDown = () => {
    if (pageInstance && dataKey) {
      pageInstance.setData({ [dataKey]: 0.97 });
    }
  };

  const scaleUp = () => {
    if (scaleTimer) clearTimeout(scaleTimer);
    scaleTimer = setTimeout(() => {
      if (pageInstance && dataKey) {
        pageInstance.setData({ [dataKey]: 1 });
      }
    }, 150);
  };

  return {
    onTouchStart: scaleDown,
    onTouchEnd: scaleUp,
    onTouchCancel: scaleUp,
  };
}

/**
 * Create a delayed animation start
 * @param {number} delay - Delay in ms
 * @param {Object} baseConfig - Base animation config to add delay to
 * @returns {Object} New config with delay
 */
function withDelay(delay, baseConfig = createKeyframe()) {
  return {
    ...baseConfig,
    delay,
  };
}

/**
 * Create staggered animation configs for a list of items
 * @param {number} count - Number of items
 * @param {number} staggerDelay - Delay between each item in ms (default 60)
 * @param {Object} baseConfig - Base animation config
 * @returns {Array<Object>} Array of animation configs with staggered delays
 */
function stagger(count, staggerDelay = 60, baseConfig = fadeIn) {
  return Array.from({ length: count }, (_, i) => withDelay(i * staggerDelay, baseConfig));
}

/**
 * Get CSS transition string for a property
 * @param {string} property - CSS property name
 * @param {number} duration - Duration in ms
 * @param {string} timingFunction - Easing function
 * @param {number} delay - Delay in ms
 * @returns {string} CSS transition value
 */
function transition(property = 'all', duration = durations.normal, timingFunction = easings.ease, delay = 0) {
  return `${property} ${duration}ms ${timingFunction} ${delay}ms`;
}

/**
 * Common WX animation helper - create wx.createAnimation compatible config
 * Uses page's animation data to drive transitions
 */
const wxAnimation = {
  /**
   * Create fade-in animation
   * @param {number} duration - Duration in ms
   * @returns {Object} Animation steps for wx.createAnimation
   */
  fadeIn(duration = durations.normal) {
    return {
      actions: [{
        opacity: 0,
        offset: 0,
      }, {
        opacity: 1,
        offset: 1,
        duration,
        timingFunction: easings.easeOut,
      }],
    };
  },

  /**
   * Create slide-up animation
   * @param {number} translateY - Translate Y in rpx
   * @param {number} duration - Duration in ms
   * @returns {Object} Animation steps
   */
  slideUp(translateY = 40, duration = durations.normal) {
    return {
      actions: [{
        translateY,
        opacity: 0,
        offset: 0,
      }, {
        translateY: 0,
        opacity: 1,
        offset: 1,
        duration,
        timingFunction: easings.easeOut,
      }],
    };
  },

  /**
   * Create scale-in animation
   * @param {number} duration - Duration in ms
   * @returns {Object} Animation steps
   */
  scaleIn(duration = durations.fast) {
    return {
      actions: [{
        scale: 0.9,
        opacity: 0,
        offset: 0,
      }, {
        scale: 1,
        opacity: 1,
        offset: 1,
        duration,
        timingFunction: easings.spring,
      }],
    };
  },
};

// ==================== Exports ====================

module.exports = {
  easings,
  durations,
  createKeyframe,
  fadeIn,
  fadeInFast,
  fadeInSlow,
  fadeOut,
  scaleUp,
  scaleDown,
  slideInBottom,
  slideInTop,
  slideOutBottom,
  pulse,
  tapScale,
  withDelay,
  stagger,
  transition,
  wxAnimation,
};