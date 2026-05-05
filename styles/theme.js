/**
 * Enhanced Theme System for MindBridge
 * Supports light/dark mode, glassmorphism, semantic colors, and design tokens
 */

// ==================== Color Palettes ====================

const semanticColors = {
  light: {
    // Primary & Accent
    primary: '#5b6abf',
    primaryLight: '#8591d8',
    primaryDark: '#3a499e',
    accent: '#e8836b',
    accentLight: '#f0a795',
    accentDark: '#c9604a',
    gold: '#cfaa52',
    goldLight: '#dfc47a',

    // Background (layered depth)
    bg: '#f5f6fa',
    bgElevated: '#ffffff',
    bgOverlay: 'rgba(0, 0, 0, 0.04)',
    bgBackdrop: 'rgba(0, 0, 0, 0.3)',

    // Surface (cards, panels)
    surface: '#ffffff',
    surfaceHover: '#f8f9fc',
    surfaceActive: '#f0f1f6',
    surfaceDisabled: '#e8e9ee',

    // Text
    textPrimary: '#282c34',
    textSecondary: '#5a5e6b',
    textTertiary: '#8b8f9c',
    textInverse: '#ffffff',
    textLink: '#5b6abf',

    // Borders
    border: '#e4e6ec',
    borderLight: '#eef0f4',
    borderFocus: '#5b6abf',

    // Status
    success: '#27ae60',
    successBg: '#e8f8ef',
    warning: '#e9a23b',
    warningBg: '#fef4e5',
    danger: '#e74c3c',
    dangerBg: '#fce8e6',
    info: '#5b6abf',
    infoBg: '#edeef8',

    // Shadows
    shadowSm: '0 2px 8px rgba(43, 46, 60, 0.06)',
    shadowMd: '0 4px 20px rgba(43, 46, 60, 0.08)',
    shadowLg: '0 8px 40px rgba(43, 46, 60, 0.12)',
    shadowPrimary: '0 4px 20px rgba(91, 106, 191, 0.25)',

    // Glass
    glassBg: 'rgba(255, 255, 255, 0.72)',
    glassBorder: 'rgba(255, 255, 255, 0.5)',
    glassShadow: '0 8px 32px rgba(43, 46, 60, 0.08)',
  },
  dark: {
    primary: '#8591d8',
    primaryLight: '#a8b2e8',
    primaryDark: '#5b6abf',
    accent: '#f0a795',
    accentLight: '#f5c4b8',
    accentDark: '#e8836b',
    gold: '#dfc47a',
    goldLight: '#ecdaa0',

    bg: '#111420',
    bgElevated: '#1a1e2e',
    bgOverlay: 'rgba(255, 255, 255, 0.04)',
    bgBackdrop: 'rgba(0, 0, 0, 0.6)',

    surface: '#1a1e2e',
    surfaceHover: '#222740',
    surfaceActive: '#2a3050',
    surfaceDisabled: '#2c3044',

    textPrimary: '#f0f1f6',
    textSecondary: '#a0a4b4',
    textTertiary: '#6b6f80',
    textInverse: '#282c34',
    textLink: '#a8b2e8',

    border: '#2c3044',
    borderLight: '#222740',
    borderFocus: '#8591d8',

    success: '#27ae60',
    successBg: 'rgba(39, 174, 96, 0.12)',
    warning: '#e9a23b',
    warningBg: 'rgba(233, 162, 59, 0.12)',
    danger: '#e74c3c',
    dangerBg: 'rgba(231, 76, 60, 0.12)',
    info: '#8591d8',
    infoBg: 'rgba(91, 106, 191, 0.12)',

    shadowSm: '0 2px 8px rgba(0, 0, 0, 0.2)',
    shadowMd: '0 4px 20px rgba(0, 0, 0, 0.25)',
    shadowLg: '0 8px 40px rgba(0, 0, 0, 0.35)',
    shadowPrimary: '0 4px 20px rgba(133, 145, 216, 0.2)',

    glassBg: 'rgba(26, 30, 46, 0.72)',
    glassBorder: 'rgba(255, 255, 255, 0.08)',
    glassShadow: '0 8px 32px rgba(0, 0, 0, 0.25)',
  },
};

// ==================== Typography ====================

const typography = {
  sizes: {
    xs: 20,
    sm: 24,
    md: 28,
    lg: 32,
    xl: 36,
    '2xl': 44,
    '3xl': 52,
    '4xl': 64,
  },
  weights: {
    regular: '400',
    medium: '500',
    semibold: '600',
    bold: '700',
  },
  lineHeights: {
    tight: 1.2,
    normal: 1.5,
    relaxed: 1.75,
  },
  families: {
    system: '-apple-system, BlinkMacSystemFont, "Segoe UI", Roboto, sans-serif',
  },
};

// ==================== Spacing ====================

const spacing = {
  0: 0,
  1: 8,
  2: 16,
  3: 24,
  4: 32,
  5: 40,
  6: 48,
  8: 64,
  10: 80,
  12: 96,
  16: 128,
};

// ==================== Border Radius ====================

const radius = {
  none: 0,
  sm: 12,
  md: 16,
  lg: 24,
  xl: 32,
  '2xl': 40,
  full: 9999,
};

// ==================== Animation ====================

const animation = {
  durations: {
    instant: 100,
    fast: 200,
    normal: 350,
    slow: 500,
  },
  easings: {
    ease: 'cubic-bezier(0.25, 0.1, 0.25, 1)',
    easeIn: 'cubic-bezier(0.55, 0.085, 0.68, 0.53)',
    easeOut: 'cubic-bezier(0.215, 0.61, 0.355, 1)',
    easeInOut: 'cubic-bezier(0.455, 0.03, 0.515, 0.955)',
    spring: 'cubic-bezier(0.34, 1.56, 0.64, 1)',
  },
  scale: {
    tap: 0.97,
    hover: 1.02,
  },
};

// ==================== Z-Index ====================

const zIndex = {
  base: 0,
  dropdown: 10,
  sticky: 20,
  header: 30,
  overlay: 40,
  modal: 50,
  toast: 60,
  tooltip: 70,
};

// ==================== Component Tokens ====================

const components = {
  button: {
    heights: {
      sm: 68,
      md: 88,
      lg: 104,
    },
    padding: {
      sm: '24rpx 32rpx',
      md: '28rpx 48rpx',
      lg: '32rpx 64rpx',
    },
    borderRadius: {
      sm: radius.lg,
      md: radius.xl,
      full: radius.full,
    },
  },
  card: {
    padding: spacing[4],
    borderRadius: radius.xl,
    gap: spacing[3],
  },
  input: {
    height: 96,
    borderRadius: radius.lg,
    padding: '24rpx 32rpx',
  },
  avatar: {
    sizes: {
      xs: 48,
      sm: 64,
      md: 80,
      lg: 120,
      xl: 160,
    },
  },
  navBar: {
    height: 176,
    statusBarHeight: 44,
  },
  bottomNav: {
    height: 140,
    safeArea: 34,
  },
};

// ==================== Glass Presets ====================

const glass = {
  light: {
    background: 'rgba(255, 255, 255, 0.72)',
    border: '1rpx solid rgba(255, 255, 255, 0.5)',
    shadow: '0 8px 32px rgba(43, 46, 60, 0.08)',
    backdropFilter: 'blur(20px) saturate(180%)',
  },
  dark: {
    background: 'rgba(26, 30, 46, 0.72)',
    border: '1rpx solid rgba(255, 255, 255, 0.08)',
    shadow: '0 8px 32px rgba(0, 0, 0, 0.25)',
    backdropFilter: 'blur(20px) saturate(180%)',
  },
  primary: {
    background: 'rgba(91, 106, 191, 0.12)',
    border: '1rpx solid rgba(91, 106, 191, 0.2)',
    shadow: '0 4px 16px rgba(91, 106, 191, 0.15)',
  },
  accent: {
    background: 'rgba(232, 131, 107, 0.12)',
    border: '1rpx solid rgba(232, 131, 107, 0.2)',
    shadow: '0 4px 16px rgba(232, 131, 107, 0.15)',
  },
  success: {
    background: 'rgba(39, 174, 96, 0.12)',
    border: '1rpx solid rgba(39, 174, 96, 0.2)',
  },
  danger: {
    background: 'rgba(231, 76, 60, 0.12)',
    border: '1rpx solid rgba(231, 76, 60, 0.2)',
  },
};

// ==================== Build Complete Theme ====================

function buildTheme(mode = 'light') {
  const colors = semanticColors[mode];

  return {
    // Mode
    mode,

    // Core palettes
    colors,
    // Backward-compatible flat color access
    primary: colors.primary,
    accent: colors.accent,
    gold: colors.gold,
    success: colors.success,
    grey: colors.textTertiary,
    warning: colors.warning,
    danger: colors.danger,
    dark: colors.textPrimary,
    light: colors.bg,
    border: colors.border,
    text: colors.textPrimary,

    // Design tokens
    typography,
    spacing,
    radius,
    animation,
    zIndex,
    components,
    glass,
  };
}

// ==================== Default Exports ====================

const theme = buildTheme('light');
const darkTheme = buildTheme('dark');

module.exports = {
  theme,
  darkTheme,
  buildTheme,
  semanticColors,
  typography,
  spacing,
  radius,
  animation,
  zIndex,
  components,
  glass,
};