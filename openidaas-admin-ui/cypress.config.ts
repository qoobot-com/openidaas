import { defineConfig } from 'cypress'
import { devServer, startDevServer } from 'cypress-vite-dev-server'

export default defineConfig({
  e2e: {
    baseUrl: 'http://localhost:3000',
    specPattern: 'cypress/e2e/**/*.cy.{js,jsx,ts,tsx}',
    supportFile: 'cypress/support/e2e.ts',
    viewportWidth: 1280,
    viewportHeight: 720,
    video: true,
    videoCompression: 32,
    screenshotOnRunFailure: true,
    trashAssetsBeforeRuns: true,
    watchForFileChanges: false,
    defaultCommandTimeout: 10000,
    requestTimeout: 10000,
    responseTimeout: 10000,
    pageLoadTimeout: 60000,
    execTimeout: 60000,
    taskTimeout: 60000,
    retries: {
      runMode: 2,
      openMode: 0
    },
    env: {
      apiBaseUrl: 'http://localhost:8080/api',
      username: 'admin',
      password: 'admin123',
      language: 'zh-CN'
    },
    setupNodeEvents(on, config) {
      // 使用 Vite 作为开发服务器
      on('dev-server:start', async (options) => {
        return startDevServer({
          options,
          viteConfig: {
            configFile: 'vite.config.ts'
          }
        })
      })

      // 任务：清空测试数据
      on('task', {
        log(message) {
          console.log(message)
          return null
        },
        seedDatabase() {
          // 这里可以添加数据库重置逻辑
          console.log('Database seeded for testing')
          return null
        }
      })

      // 监听浏览器控制台日志
      on('before:browser:launch', (browser, launchOptions) => {
        if (browser.family === 'chromium' && browser.name !== 'electron') {
          launchOptions.args.push('--disable-dev-shm-usage')
        }
        return launchOptions
      })

      return config
    }
  },
  component: {
    devServer: {
      framework: 'vue',
      bundler: 'vite'
    },
    specPattern: 'src/**/*.cy.{js,jsx,ts,tsx}',
    supportFile: 'cypress/support/component.ts'
  }
})
