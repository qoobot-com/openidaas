#!/bin/bash

################################################################################
# OpenIDaaS 数据库初始化脚本
#
# 用途: 初始化 OpenIDaaS 数据库
# 使用: ./init.sh [options]
#
# 选项:
#   -h, --help              显示帮助信息
#   -H, --host HOST         数据库主机 (默认: localhost)
#   -P, --port PORT         数据库端口 (默认: 5432)
#   -u, --user USER         数据库用户 (默认: postgres)
#   -d, --database DB       数据库名称 (默认: openidaas)
#   -p, --password PASS     数据库密码 (默认: 从环境变量 PGPASSWORD 读取)
#   --skip-schema           跳过 schema.sql
#   --skip-index            跳过 index.sql
#   --skip-partition        跳过 partition.sql
#   --skip-init-data        跳过 init-data.sql
#   --skip-migration        跳过 migration.sql
#   --dry-run               模拟运行，不执行实际操作
#
# 环境变量:
#   PGPASSWORD              数据库密码
#   DB_HOST                 数据库主机
#   DB_PORT                 数据库端口
#   DB_USER                 数据库用户
#   DB_NAME                 数据库名称
################################################################################

set -e  # 遇到错误立即退出

# 颜色输出
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
BLUE='\033[0;34m'
NC='\033[0m' # No Color

# 默认配置
DB_HOST="${DB_HOST:-localhost}"
DB_PORT="${DB_PORT:-5432}"
DB_USER="${DB_USER:-postgres}"
DB_NAME="${DB_NAME:-openidaas}"
DB_PASSWORD="${PGPASSWORD:-}"

# 脚本目录
SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"

# 选项标志
SKIP_SCHEMA=false
SKIP_INDEX=false
SKIP_PARTITION=false
SKIP_INIT_DATA=false
SKIP_MIGRATION=false
DRY_RUN=false

# 日志函数
log_info() {
    echo -e "${BLUE}[INFO]${NC} $1"
}

log_success() {
    echo -e "${GREEN}[SUCCESS]${NC} $1"
}

log_warning() {
    echo -e "${YELLOW}[WARNING]${NC} $1"
}

log_error() {
    echo -e "${RED}[ERROR]${NC} $1"
}

# 显示帮助
show_help() {
    cat << EOF
OpenIDaaS Database Initialization Script

Usage: $0 [options]

Options:
  -h, --help              Show this help message
  -H, --host HOST         Database host (default: localhost)
  -P, --port PORT         Database port (default: 5432)
  -u, --user USER         Database user (default: postgres)
  -d, --database DB       Database name (default: openidaas)
  -p, --password PASS     Database password (default: read from PGPASSWORD env var)
  --skip-schema           Skip schema.sql
  --skip-index            Skip index.sql
  --skip-partition        Skip partition.sql
  --skip-init-data        Skip init-data.sql
  --skip-migration        Skip migration.sql
  --dry-run               Dry run, don't execute actual commands

Environment Variables:
  PGPASSWORD              Database password
  DB_HOST                 Database host
  DB_PORT                 Database port
  DB_USER                 Database user
  DB_NAME                 Database name

Examples:
  # Initialize with default settings
  $0

  # Initialize with custom host and port
  $0 -H db.example.com -P 5433

  # Initialize and skip initial data
  $0 --skip-init-data

  # Dry run to check what will be executed
  $0 --dry-run
EOF
}

# 解析命令行参数
parse_arguments() {
    while [[ $# -gt 0 ]]; do
        case $1 in
            -h|--help)
                show_help
                exit 0
                ;;
            -H|--host)
                DB_HOST="$2"
                shift 2
                ;;
            -P|--port)
                DB_PORT="$2"
                shift 2
                ;;
            -u|--user)
                DB_USER="$2"
                shift 2
                ;;
            -d|--database)
                DB_NAME="$2"
                shift 2
                ;;
            -p|--password)
                DB_PASSWORD="$2"
                shift 2
                ;;
            --skip-schema)
                SKIP_SCHEMA=true
                shift
                ;;
            --skip-index)
                SKIP_INDEX=true
                shift
                ;;
            --skip-partition)
                SKIP_PARTITION=true
                shift
                ;;
            --skip-init-data)
                SKIP_INIT_DATA=true
                shift
                ;;
            --skip-migration)
                SKIP_MIGRATION=true
                shift
                ;;
            --dry-run)
                DRY_RUN=true
                shift
                ;;
            *)
                log_error "Unknown option: $1"
                show_help
                exit 1
                ;;
        esac
    done
}

# 验证连接
verify_connection() {
    log_info "Verifying database connection..."
    log_info "Host: $DB_HOST, Port: $DB_PORT, User: $DB_USER, Database: $DB_NAME"

    if [ "$DRY_RUN" = true ]; then
        log_info "[DRY RUN] Would verify connection to $DB_HOST:$DB_PORT"
        return 0
    fi

    if ! psql -h "$DB_HOST" -p "$DB_PORT" -U "$DB_USER" -d "$DB_NAME" -c "SELECT version();" > /dev/null 2>&1; then
        log_error "Failed to connect to database"
        log_info "Please check:"
        log_info "  1. Database host and port"
        log_info "  2. Database user and password"
        log_info "  3. Database exists"
        exit 1
    fi

    log_success "Database connection verified"
}

# 执行 SQL 文件
execute_sql_file() {
    local file="$1"
    local description="$2"

    log_info "Executing $description..."

    if [ "$DRY_RUN" = true ]; then
        log_info "[DRY RUN] Would execute: $file"
        return 0
    fi

    if [ ! -f "$file" ]; then
        log_error "SQL file not found: $file"
        return 1
    fi

    if ! psql -h "$DB_HOST" -p "$DB_PORT" -U "$DB_USER" -d "$DB_NAME" -f "$file" > /dev/null 2>&1; then
        log_error "Failed to execute $file"
        log_error "Please check the SQL file for errors"
        return 1
    fi

    log_success "$description completed"
}

# 显示初始化摘要
show_summary() {
    log_info "==================================================================="
    log_info "Database Initialization Summary"
    log_info "==================================================================="
    log_info "Host:        $DB_HOST:$DB_PORT"
    log_info "User:        $DB_USER"
    log_info "Database:    $DB_NAME"
    log_info "Dry Run:     $DRY_RUN"
    log_info "==================================================================="

    log_info "Execution Plan:"
    [ "$SKIP_SCHEMA" = false ] && log_info "  [✓] schema.sql" || log_info "  [✗] schema.sql (skipped)"
    [ "$SKIP_INDEX" = false ] && log_info "  [✓] index.sql" || log_info "  [✗] index.sql (skipped)"
    [ "$SKIP_PARTITION" = false ] && log_info "  [✓] partition.sql" || log_info "  [✗] partition.sql (skipped)"
    [ "$SKIP_INIT_DATA" = false ] && log_info "  [✓] init-data.sql" || log_info "  [✗] init-data.sql (skipped)"
    [ "$SKIP_MIGRATION" = false ] && log_info "  [✓] migration.sql" || log_info "  [✗] migration.sql (skipped)"
    log_info "==================================================================="
}

# 主函数
main() {
    parse_arguments "$@"
    show_summary

    if [ "$DRY_RUN" = true ]; then
        log_warning "Dry run mode: no actual changes will be made"
        exit 0
    fi

    # 验证连接
    verify_connection

    # 执行 SQL 文件
    if [ "$SKIP_SCHEMA" = false ]; then
        execute_sql_file "$SCRIPT_DIR/schema.sql" "Schema creation"
    fi

    if [ "$SKIP_INDEX" = false ]; then
        execute_sql_file "$SCRIPT_DIR/index.sql" "Index creation"
    fi

    if [ "$SKIP_PARTITION" = false ]; then
        execute_sql_file "$SCRIPT_DIR/partition.sql" "Partition setup"
    fi

    if [ "$SKIP_INIT_DATA" = false ]; then
        execute_sql_file "$SCRIPT_DIR/init-data.sql" "Initial data loading"
    fi

    if [ "$SKIP_MIGRATION" = false ]; then
        execute_sql_file "$SCRIPT_DIR/migration.sql" "Migration scripts"
    fi

    log_success "==================================================================="
    log_success "Database initialization completed successfully!"
    log_success "==================================================================="
    log_info "Default admin credentials:"
    log_info "  Username: admin"
    log_info "  Password: Admin@123"
    log_info "  Email:    admin@openidaas.com"
    log_success "==================================================================="
}

# 执行主函数
main "$@"
