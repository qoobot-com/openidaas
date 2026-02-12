import { App } from 'vue'
import lazyLoad from '@/directives/lazyLoad'

export default function registerDirectives(app: App) {
  // 注册图片懒加载指令
  app.directive('lazy', lazyLoad)

  // 可以继续添加其他指令
  // app.directive('xxx', xxxDirective)
}
