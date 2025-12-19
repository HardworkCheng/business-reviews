// 主题配置对象
export const themeConfig = {
  colors: {
    primary: '#FF7D00',
    primaryLight: 'rgba(255, 125, 0, 0.1)',
    primaryHover: '#E67100',
    secondary: '#4CAF50',
    secondaryLight: 'rgba(76, 175, 80, 0.1)',
    success: '#10B981',
    successLight: 'rgba(16, 185, 129, 0.1)',
    warning: '#F59E0B',
    warningLight: 'rgba(245, 158, 11, 0.1)',
    danger: '#EF4444',
    dangerLight: 'rgba(239, 68, 68, 0.1)',
    info: '#3B82F6',
    infoLight: 'rgba(59, 130, 246, 0.1)',
    neutral: {
      100: '#F5F5F5',
      200: '#E5E5E5',
      300: '#D4D4D4',
      400: '#A3A3A3',
      500: '#737373',
      600: '#525252',
      700: '#404040',
      800: '#262626',
      900: '#171717'
    }
  },
  borderRadius: {
    card: '12px',
    button: '8px',
    input: '8px',
    tag: '9999px'
  },
  shadows: {
    card: '0 4px 20px rgba(0, 0, 0, 0.05)',
    cardHover: '0 8px 30px rgba(0, 0, 0, 0.1)',
    sm: '0 2px 8px rgba(0, 0, 0, 0.08)'
  },
  transitions: {
    default: 'all 0.3s ease',
    fast: 'all 0.2s ease'
  },
  fonts: {
    primary: "'Inter', -apple-system, BlinkMacSystemFont, 'Segoe UI', Roboto, sans-serif"
  }
}

// 状态样式映射
export const statusStyleMap = {
  // 笔记状态
  approved: { bg: '#DCFCE7', text: '#15803D', label: '已通过' },
  pending: { bg: '#FEF9C3', text: '#A16207', label: '待审核' },
  rejected: { bg: '#FEE2E2', text: '#DC2626', label: '已驳回' },
  offline: { bg: '#F3F4F6', text: '#6B7280', label: '已下架' },
  // 优惠券状态
  active: { bg: '#DCFCE7', text: '#15803D', dot: '#22C55E', label: '进行中' },
  expired: { bg: '#FEE2E2', text: '#DC2626', dot: '#EF4444', label: '已过期' },
  upcoming: { bg: '#FEF9C3', text: '#A16207', dot: '#EAB308', label: '未开始' },
  paused: { bg: '#F3F4F6', text: '#6B7280', dot: '#9CA3AF', label: '已暂停' }
}

// 获取状态样式
export const getStatusStyle = (status: string) => {
  return statusStyleMap[status as keyof typeof statusStyleMap] || {
    bg: '#F3F4F6',
    text: '#6B7280',
    label: status
  }
}

// Element Plus 主题覆盖配置
export const elementPlusTheme = {
  '--el-color-primary': '#FF7D00',
  '--el-color-primary-light-3': '#FF9933',
  '--el-color-primary-light-5': '#FFB366',
  '--el-color-primary-light-7': '#FFCC99',
  '--el-color-primary-light-8': '#FFE5CC',
  '--el-color-primary-light-9': '#FFF2E5',
  '--el-color-primary-dark-2': '#CC6400',
  '--el-color-success': '#4CAF50',
  '--el-color-warning': '#F59E0B',
  '--el-color-danger': '#EF4444',
  '--el-color-info': '#3B82F6',
  '--el-border-radius-base': '8px',
  '--el-border-radius-small': '6px',
  '--el-border-radius-round': '20px',
  '--el-font-family': "'Inter', -apple-system, BlinkMacSystemFont, 'Segoe UI', Roboto, sans-serif"
}

export default themeConfig
