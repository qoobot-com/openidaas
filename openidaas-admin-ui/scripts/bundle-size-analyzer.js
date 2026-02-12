#!/usr/bin/env node

/**
 * Bundle Size 分析脚本
 * 分析打包后的文件大小
 */

const fs = require('fs')
const path = require('path')
const gzipSize = require('gzip-size')

// 解析打包输出目录
function analyzeBundleSize(distDir) {
  const stats = {
    total: 0,
    files: [],
    categories: {
      js: { count: 0, size: 0 },
      css: { count: 0, size: 0 },
      images: { count: 0, size: 0 },
      other: { count: 0, size: 0 }
    }
  }

  function analyzeDir(dir) {
    const items = fs.readdirSync(dir, { withFileTypes: true })

    for (const item of items) {
      const fullPath = path.join(dir, item.name)
      
      if (item.isDirectory()) {
        analyzeDir(fullPath)
      } else {
        const size = fs.statSync(fullPath).size
        const ext = path.extname(item.name)
        const relativePath = path.relative(distDir, fullPath)
        
        // 计算 gzip 大小
        let gzipSizeValue = null
        if (ext === '.js' || ext === '.css' || ext === '.json' || ext === '.html') {
          try {
            gzipSizeValue = gzipSize.sync(fs.readFileSync(fullPath))
          } catch (e) {
            // 忽略错误
          }
        }

        const fileStat = {
          path: relativePath,
          size,
          gzipSize: gzipSizeValue,
          ext
        }
        
        stats.files.push(fileStat)
        stats.total += size

        // 分类统计
        if (ext === '.js') {
          stats.categories.js.count++
          stats.categories.js.size += size
        } else if (ext === '.css') {
          stats.categories.css.count++
          stats.categories.css.size += size
        } else if (['.png', '.jpg', '.jpeg', '.gif', '.svg', '.webp', '.ico'].includes(ext)) {
          stats.categories.images.count++
          stats.categories.images.size += size
        } else {
          stats.categories.other.count++
          stats.categories.other.size += size
        }
      }
    }
  }

  analyzeDir(distDir)
  return stats
}

// 格式化文件大小
function formatSize(bytes) {
  if (bytes === 0) return '0 B'
  const k = 1024
  const sizes = ['B', 'KB', 'MB', 'GB']
  const i = Math.floor(Math.log(bytes) / Math.log(k))
  return parseFloat((bytes / Math.pow(k, i)).toFixed(2)) + ' ' + sizes[i]
}

// 主函数
function main() {
  const distDir = path.join(__dirname, '../public')

  console.log('📦 Analyzing bundle size...\n')

  if (!fs.existsSync(distDir)) {
    console.log('❌ Dist directory not found. Please build first with: npm run build')
    process.exit(1)
  }

  const stats = analyzeBundleSize(distDir)

  // 总体统计
  console.log('📊 Overall Statistics:')
  console.log(`   Total Size: ${formatSize(stats.total)}`)
  console.log(`   Total Files: ${stats.files.length}`)

  // 分类统计
  console.log('\n📁 File Categories:')
  console.log(`   JavaScript: ${stats.categories.js.count} files, ${formatSize(stats.categories.js.size)}`)
  console.log(`   CSS: ${stats.categories.css.count} files, ${formatSize(stats.categories.css.size)}`)
  console.log(`   Images: ${stats.categories.images.count} files, ${formatSize(stats.categories.images.size)}`)
  console.log(`   Other: ${stats.categories.other.count} files, ${formatSize(stats.categories.other.size)}`)

  // 最大文件
  console.log('\n🔍 Largest Files (Top 10):')
  const sortedBySize = [...stats.files].sort((a, b) => b.size - a.size).slice(0, 10)
  sortedBySize.forEach((file, index) => {
    const gzipInfo = file.gzipSize !== null ? ` (gzip: ${formatSize(file.gzipSize)})` : ''
    console.log(`   ${index + 1}. ${file.path} - ${formatSize(file.size)}${gzipInfo}`)
  })

  // 检查是否有过大的文件
  const largeFiles = stats.files.filter(f => f.size > 500 * 1024) // 大于 500KB
  if (largeFiles.length > 0) {
    console.log('\n⚠️  Large Files (> 500KB):')
    largeFiles.forEach(file => {
      console.log(`   ${file.path} - ${formatSize(file.size)}`)
    })
  }

  // 检查 gzip 压缩效果
  const compressibleFiles = stats.files.filter(f => f.gzipSize !== null)
  if (compressibleFiles.length > 0) {
    const originalSize = compressibleFiles.reduce((sum, f) => sum + f.size, 0)
    const gzipTotalSize = compressibleFiles.reduce((sum, f) => sum + f.gzipSize, 0)
    const compressionRatio = ((1 - gzipTotalSize / originalSize) * 100).toFixed(2)
    
    console.log('\n📉 Compression Statistics:')
    console.log(`   Original: ${formatSize(originalSize)}`)
    console.log(`   Gzipped: ${formatSize(gzipTotalSize)}`)
    console.log(`   Compression Ratio: ${compressionRatio}%`)
  }

  // 总结
  console.log('\n✨ Analysis Complete!')
  
  // 检查是否达标
  const jsSize = stats.categories.js.size
  const maxRecommendedSize = 2 * 1024 * 1024 // 2MB
  
  if (jsSize > maxRecommendedSize) {
    console.log('\n⚠️  Warning: JavaScript bundle size exceeds recommended limit (2MB)')
    console.log('   Consider code splitting, tree shaking, or removing unused dependencies.')
    process.exit(1)
  } else {
    console.log('\n✅ Bundle size is within recommended limits!')
    process.exit(0)
  }
}

main()
