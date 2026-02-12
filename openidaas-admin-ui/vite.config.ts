import { defineConfig, loadEnv } from 'vite'
import vue from '@vitejs/plugin-vue'
import vueJsx from '@vitejs/plugin-vue-jsx'
import { resolve } from 'path'

// https://vitejs.dev/config/
export default defineConfig(({ mode }) => {
  const env = loadEnv(mode, process.cwd(), '')
  
  return {
    root: '.',
    outputDir: 'public',
    publicDir: 'public',
    plugins: [
      vue(),
      vueJsx()
    ],
    resolve: {
      alias: {
        '@': resolve(__dirname, 'src'),
        '~': resolve(__dirname, 'src')
      }
    },
    css: {
      preprocessorOptions: {
        scss: {
          additionalData: `@import "@/styles/variables.scss";`
        }
      }
    },
    server: {
      host: 'localhost',
      port: Number(env.VITE_PORT) || 3000,
      open: true,
      proxy: {
        '/api': {
          target: env.VITE_API_BASE_URL || 'http://localhost:8080',
          changeOrigin: true,
          rewrite: (path) => path.replace(/^\/api/, '/api')
        }
      }
    },
    build: {
      outDir: 'public',
      assetsDir: 'assets',
      sourcemap: false,
      chunkSizeWarningLimit: 2000,
      rollupOptions: {
        output: {
          chunkFileNames: 'static/js/[name]-[hash].js',
          entryFileNames: 'static/js/[name]-[hash].js',
          assetFileNames: 'static/[ext]/[name]-[hash].[ext]',
          manualChunks(id) {
            // node_modules 依赖分包
            if (id.includes('node_modules')) {
              // Vue 核心
              if (id.includes('vue') || id.includes('pinia') || id.includes('@vue')) {
                return 'vue-vendor'
              }
              // Element Plus
              if (id.includes('element-plus')) {
                return 'element-plus'
              }
              // Axios
              if (id.includes('axios')) {
                return 'axios'
              }
              // ECharts 图表库
              if (id.includes('echarts')) {
                return 'echarts'
              }
              // 其他第三方库
              return 'vendor'
            }

            // 业务模块分包
            if (id.includes('/src/modules/')) {
              const moduleMatch = id.match(/src\/modules\/(\w+)/)
              if (moduleMatch) {
                return `module-${moduleMatch[1]}`
              }
            }

            // API 分包
            if (id.includes('/src/api/')) {
              return 'api'
            }

            // 组件分包
            if (id.includes('/src/components/')) {
              return 'components'
            }
          }
        }
      },
      // 压缩配置
      minify: 'terser',
      terserOptions: {
        compress: {
          drop_console: process.env.NODE_ENV === 'production',
          drop_debugger: true
        }
      }
    },
    define: {
      __APP_INFO__: JSON.stringify({
        version: process.env.npm_package_version,
        lastBuildTime: new Date().toLocaleString()
      })
    }
  }
})