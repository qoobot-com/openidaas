// 主题配置
export const THEME_CONFIG = {
  // 主题颜色
  primaryColor: '#409EFF',
  successColor: '#67C23A',
  warningColor: '#E6A23C',
  dangerColor: '#F56C6C',
  infoColor: '#909399',
  
  // 侧边栏配置
  sidebar: {
    width: '210px',
    collapsedWidth: '64px',
    backgroundColor: '#304156',
    textColor: '#bfcbd9',
    activeTextColor: '#409EFF'
  },
  
  // 头部配置
  header: {
    height: '50px',
    backgroundColor: '#ffffff'
  },
  
  // 标签页配置
  tagsView: {
    height: '34px',
    backgroundColor: '#ffffff'
  }
}

// 暗色主题配置
export const DARK_THEME = {
  ...THEME_CONFIG,
  sidebar: {
    ...THEME_CONFIG.sidebar,
    backgroundColor: '#1f1f1f',
    textColor: '#aaaaaa',
    activeTextColor: '#409EFF'
  },
  header: {
    ...THEME_CONFIG.header,
    backgroundColor: '#1f1f1f'
  }
}