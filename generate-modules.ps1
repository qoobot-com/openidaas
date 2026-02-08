# OpenIDaaS 模块文件生成脚本
# 此脚本用于批量生成所有子模块的基础文件

# 创建剩余模块的目录结构
$modules = @(
    "openidaas-user",
    "openidaas-tenant", 
    "openidaas-gateway",
    "openidaas-security",
    "openidaas-starter",
    "openidaas-samples"
)

$baseDir = "d:\05workspaces\openidaas"

foreach ($module in $modules) {
    $moduleDir = Join-Path $baseDir $module
    $srcDir = Join-Path $moduleDir "src\main\java\com\qoobot\openidaas"
    $testDir = Join-Path $moduleDir "src\test\java\com\qoobot\openidaas"
    $resourcesDir = Join-Path $moduleDir "src\main\resources"
    
    # 创建目录
    New-Item -ItemType Directory -Force -Path $srcDir | Out-Null
    New-Item -ItemType Directory -Force -Path $testDir | Out-Null
    New-Item -ItemType Directory -Force -Path $resourcesDir | Out-Null
    
    Write-Host "Created directories for $module" -ForegroundColor Green
}

Write-Host "All directories created successfully!" -ForegroundColor Green
