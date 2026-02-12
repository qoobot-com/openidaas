#!/usr/bin/env node

/**
 * API 配置验证脚本
 * 检查所有 API 端点是否配置正确
 */

const fs = require('fs')
const path = require('path')

// 递归查找所有 API 文件
function findApiFiles(dir) {
  const files = []
  const items = fs.readdirSync(dir, { withFileTypes: true })

  for (const item of items) {
    const fullPath = path.join(dir, item.name)
    if (item.isDirectory()) {
      files.push(...findApiFiles(fullPath))
    } else if (item.name.endsWith('.ts') && !item.name.endsWith('.test.ts')) {
      files.push(fullPath)
    }
  }

  return files
}

// 解析 API 文件提取端点
function extractApiEndpoints(filePath) {
  const content = fs.readFileSync(filePath, 'utf-8')
  const endpoints = []
  
  // 匹配 API 函数定义
  const functionPattern = /(\w+):\s*(?:async\s+)?\([^)]*\)\s*:\s*Promise<[^>]*>\s*=>\s*request\.(get|post|put|delete|patch)\s*\(\s*['"`]([^'"`]+)['"`]/g
  
  let match
  while ((match = functionPattern.exec(content)) !== null) {
    const [, name, method, url] = match
    endpoints.push({ name, method, url, file: path.basename(filePath) })
  }

  return endpoints
}

// 验证端点格式
function validateEndpoint(endpoint) {
  const errors = []
  
  // 检查 URL 是否以 /api 开头
  if (!endpoint.url.startsWith('/api')) {
    errors.push('URL should start with /api')
  }

  // 检查是否使用路径参数（用于 DELETE）
  if (endpoint.method === 'delete') {
    if (!endpoint.url.includes('${') && !endpoint.url.includes(':')) {
      errors.push('DELETE should use path parameters')
    }
  }

  // 检查是否包含多余的反斜杠
  if (endpoint.url.includes('//')) {
    errors.push('URL contains double slashes')
  }

  return errors
}

// 主函数
function main() {
  const apiDir = path.join(__dirname, '../src/api')
  
  console.log('🔍 Scanning API files...\n')

  const apiFiles = findApiFiles(apiDir)
  const allEndpoints = []
  const validationErrors = []

  for (const file of apiFiles) {
    const endpoints = extractApiEndpoints(file)
    allEndpoints.push(...endpoints)

    for (const endpoint of endpoints) {
      const errors = validateEndpoint(endpoint)
      if (errors.length > 0) {
        validationErrors.push({ ...endpoint, errors })
      }
    }
  }

  console.log(`📊 Found ${apiFiles.length} API files`)
  console.log(`📡 Total endpoints: ${allEndpoints.length}\n`)

  // 按方法分组统计
  const methodStats = {}
  allEndpoints.forEach(ep => {
    methodStats[ep.method] = (methodStats[ep.method] || 0) + 1
  })
  console.log('📈 Endpoint distribution:')
  Object.entries(methodStats).forEach(([method, count]) => {
    console.log(`   ${method.toUpperCase()}: ${count}`)
  })

  console.log('\n')

  // 显示验证错误
  if (validationErrors.length > 0) {
    console.log('❌ Validation errors found:')
    validationErrors.forEach(err => {
      console.log(`\n   📁 ${err.file} - ${err.name} (${err.method.toUpperCase()} ${err.url})`)
      err.errors.forEach(e => console.log(`      ⚠️  ${e}`))
    })
    console.log(`\n❌ ${validationErrors.length} endpoint(s) have validation errors`)
    process.exit(1)
  } else {
    console.log('✅ All endpoints are valid!')
    
    // 显示重复的端点
    const urlMap = {}
    allEndpoints.forEach(ep => {
      const key = `${ep.method}:${ep.url}`
      if (!urlMap[key]) urlMap[key] = []
      urlMap[key].push(ep)
    })

    const duplicates = Object.entries(urlMap).filter(([_, eps]) => eps.length > 1)
    if (duplicates.length > 0) {
      console.log('\n⚠️  Duplicate endpoints found:')
      duplicates.forEach(([key, eps]) => {
        console.log(`   ${key}`)
        eps.forEach(ep => console.log(`      ${ep.file}: ${ep.name}`))
      })
    } else {
      console.log('✅ No duplicate endpoints')
    }

    process.exit(0)
  }
}

main()
