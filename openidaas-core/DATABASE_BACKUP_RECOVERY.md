# OpenIDaaS 数据库备份与恢复

## 📋 目录

- [概述](#概述)
- [备份策略](#备份策略)
- [恢复流程](#恢复流程)
- [灾难恢复](#灾难恢复)
- [监控告警](#监控告警)

---

## 概述

### 备份目标

- **RPO (恢复点目标)**: < 15 分钟
- **RTO (恢复时间目标)**: < 2 小时
- **数据保留期**: 30 天全量，90 天增量
- **备份存储**: 异地存储

### 数据库信息

- **数据库**: PostgreSQL 15+
- **数据量预估**: ~5TB
- **表数量**: 15+
- **分区数量**: 12 (审计日志按月分区)

---

## 备份策略

### 备份类型

| 备份类型 | 频率 | 保留期 | 存储位置 |
|---------|------|--------|---------|
| 全量备份 | 每天 02:00 | 30 天 | 本地 + 异地 |
| 增量备份 (WAL) | 实时 | 90 天 | 本地 + 异地 |
| 归档备份 | 每月 01:00 | 1 年 | 异地归档 |
| 配置备份 | 每周 00:00 | 1 年 | Git 仓库 |

### 1. 全量备份

#### 使用 pg_dump

```bash
#!/bin/bash
# full_backup.sh

# 配置
DB_HOST="localhost"
DB_PORT="5432"
DB_NAME="openidaas"
DB_USER="postgres"
BACKUP_DIR="/backup/openidaas/full"
DATE=$(date +%Y%m%d_%H%M%S)
BACKUP_FILE="$BACKUP_DIR/openidaas_full_$DATE.dump"
LOG_FILE="/var/log/postgresql/backup_$DATE.log"

# 创建备份目录
mkdir -p "$BACKUP_DIR"

# 执行备份
echo "===== Full Backup Started at $(date) =====" >> "$LOG_FILE"

pg_dump -h "$DB_HOST" \
    -p "$DB_PORT" \
    -U "$DB_USER" \
    -d "$DB_NAME" \
    -F c \
    -f "$BACKUP_FILE" \
    -v 2>&1 | tee -a "$LOG_FILE"

# 检查备份结果
if [ $? -eq 0 ]; then
    # 压缩备份
    gzip "$BACKUP_FILE"
    BACKUP_FILE="$BACKUP_FILE.gz"

    # 计算文件大小和校验和
    BACKUP_SIZE=$(du -h "$BACKUP_FILE" | cut -f1)
    CHECKSUM=$(md5sum "$BACKUP_FILE" | cut -d' ' -f1)

    echo "Backup completed successfully" >> "$LOG_FILE"
    echo "Backup size: $BACKUP_SIZE" >> "$LOG_FILE"
    echo "Checksum: $CHECKSUM" >> "$LOG_FILE"

    # 记录备份信息
    echo "$DATE|$BACKUP_FILE|$BACKUP_SIZE|$CHECKSUM" >> "$BACKUP_DIR/backup_manifest.txt"

    # 上传到异地存储 (S3)
    aws s3 cp "$BACKUP_FILE" \
        s3://openidaas-backup/full/$(basename "$BACKUP_FILE") \
        --storage-class STANDARD_IA

    # 清理30天前的备份
    find "$BACKUP_DIR" -name "openidaas_full_*.dump.gz" -mtime +30 -delete

    echo "===== Full Backup Completed at $(date) =====" >> "$LOG_FILE"
else
    echo "Backup FAILED at $(date)" >> "$LOG_FILE"
    # 发送告警
    send_alert "OpenIDaaS Full Backup Failed"
    exit 1
fi
```

#### 使用 pgBackRest (推荐)

```bash
# /etc/pgbackrest/pgbackrest.conf

[global]
repo1-path=/var/lib/pgbackrest
repo1-retention-full=30
repo1-retention-diff=7
process-max=2
log-level-console=info
log-level-file=debug
start-fast=y
stop-auto=y
delta=y
compress-type=gzip
compress-level=6

[openidaas]
db-host=localhost
db-path=/var/lib/postgresql/15/main
db-port=5432
db-user=postgres

# 全量备份
pgbackrest --stanza=openidaas --type=full backup

# 增量备份
pgbackrest --stanza=openidaas --type=incr backup

# 差异备份
pgbackrest --stanza=openidaas --type=diff backup
```

### 2. WAL 增量备份

#### 启用 WAL 归档

```postgresql
-- postgresql.conf

# WAL 配置
wal_level = replica              # 必须设置为 replica 或 logical
archive_mode = on                # 启用归档
archive_command = 'test ! -f /backup/wal/%f && cp %p /backup/wal/%f'
archive_timeout = 300            # 5分钟未切换WAL则强制归档
max_wal_senders = 10             # 流复制最大连接数
wal_keep_size = 1GB              # 保留多少 WAL 供复制使用

-- 配置 WAL 保留
min_wal_size = 1GB
max_wal_size = 4GB
```

#### WAL 归档脚本

```bash
#!/bin/bash
# wal_archive.sh

WAL_DIR="/backup/wal"
REMOTE_WAL_DIR="/backup/remote/wal"
DATE=$(date +%Y%m%d)

# 归档当天的 WAL 到异地
for wal_file in $WAL_DIR/*.gz; do
    if [ -f "$wal_file" ]; then
        # 上传到异地
        aws s3 cp "$wal_file" \
            "s3://openidaas-backup/wal/$DATE/$(basename $wal_file)" \
            --storage-class STANDARD_IA

        # 同步到远程服务器
        rsync -avz "$wal_file" "backup-server:$REMOTE_WAL_DIR/$DATE/"
    fi
done

# 清理90天前的 WAL
find $WAL_DIR -mtime +90 -delete
```

### 3. 配置备份

```bash
#!/bin/bash
# config_backup.sh

CONFIG_BACKUP_DIR="/backup/config"
DATE=$(date +%Y%m%d)

# 备份配置文件
tar -czf "$CONFIG_BACKUP_DIR/postgresql_config_$DATE.tar.gz" \
    /etc/postgresql/15/main/postgresql.conf \
    /etc/postgresql/15/main/pg_hba.conf \
    /etc/pgbackrest/pgbackrest.conf

# 上传到 Git 仓库 (敏感信息需加密)
git -C /repo/openidaas-config add .
git -C /repo/openidaas-config commit -m "Backup config - $DATE"
git -C /repo/openidaas-config push origin main
```

---

## 恢复流程

### 1. 全量恢复

#### 从 pg_dump 备份恢复

```bash
#!/bin/bash
# restore_from_dump.sh

DUMP_FILE="$1"
NEW_DB_NAME="${2:-openidaas_restore}"

echo "Restoring from $DUMP_FILE to $NEW_DB_NAME..."

# 1. 创建新数据库
createdb -h localhost -U postgres "$NEW_DB_NAME"

# 2. 解压并恢复
gunzip -c "$DUMP_FILE" | pg_restore \
    -h localhost \
    -U postgres \
    -d "$NEW_DB_NAME" \
    -v

echo "Restore completed to $NEW_DB_NAME"
```

#### 使用 pgBackRest 恢复

```bash
# 1. 停止 PostgreSQL
sudo systemctl stop postgresql

# 2. 移除现有数据
sudo rm -rf /var/lib/postgresql/15/main/*

# 3. 恢复最新全量备份
pgbackrest --stanza=openidaas --delta restore

# 4. 启动 PostgreSQL
sudo systemctl start postgresql

# 5. 验证恢复
psql -U postgres -d openidaas -c "SELECT COUNT(*) FROM users;"
```

### 2. 时间点恢复 (PITR)

```bash
# 1. 恢复到指定时间点
pgbackrest --stanza=openidaas \
    --delta \
    --target="2026-02-08 12:00:00" \
    --type=time \
    restore

# 2. 创建 recovery.conf
cat > /var/lib/postgresql/15/main/recovery.conf <<EOF
restore_command = 'cp /backup/wal/%f %p'
recovery_target_time = '2026-02-08 12:00:00'
recovery_target_inclusive = true
EOF

# 3. 重启 PostgreSQL
sudo systemctl restart postgresql
```

### 3. 单表恢复

```bash
#!/bin/bash
# restore_single_table.sh

DUMP_FILE="$1"
TABLE_NAME="$2"
NEW_DB_NAME="${3:-openidaas_restore}"

echo "Restoring table $TABLE_NAME..."

# 1. 提取单表数据
pg_restore -h localhost -U postgres \
    -l "$DUMP_FILE" | grep "TABLE public.$TABLE_NAME" > table_list.txt

# 2. 恢复单表
pg_restore -h localhost -U postgres \
    -d "$NEW_DB_NAME" \
    -L table_list.txt \
    "$DUMP_FILE"

# 3. 导出数据到原表
pg_dump -h localhost -U postgres \
    -t "$TABLE_NAME" \
    "$NEW_DB_NAME" | psql -h localhost -U postgres openidaas

echo "Table $TABLE_NAME restored"
```

### 4. 分区恢复

```bash
#!/bin/bash
# restore_partition.sh

PARTITION_TABLE="$1"
TARGET_DATE="$2"

echo "Restoring partition for $TARGET_DATE..."

# 1. 分离目标分区
psql -U postgres openidaas <<EOF
ALTER TABLE audit_logs DETACH PARTITION audit_logs_$(date -d "$TARGET_DATE" +%Y_%m);
EOF

# 2. 从备份恢复分区
# 使用备份中的分区文件恢复...

# 3. 重新附加分区
psql -U postgres openidaas <<EOF
ALTER TABLE audit_logs ATTACH PARTITION audit_logs_$(date -d "$TARGET_DATE" +%Y_%m)
    FOR VALUES FROM ('$TARGET_DATE') TO ('$(date -d "$TARGET_DATE + 1 month" +%Y-%m-01)');
EOF

echo "Partition restored"
```

---

## 灾难恢复

### 灾难恢复计划 (DRP)

#### 灾难等级定义

| 等级 | 描述 | 影响 | 恢复时间 |
|------|------|------|---------|
| L1 | 单表损坏 | 单表不可用 | < 1 小时 |
| L2 | 数据库崩溃 | 整个数据库不可用 | < 2 小时 |
| L3 | 服务器故障 | 整个服务器不可用 | < 4 小时 |
| L4 | 数据中心故障 | 整个数据中心不可用 | < 8 小时 |

### L1/L2 灾难恢复 (单表/数据库)

```bash
#!/bin/bash
# dr_l1_l2.sh

DISASTER_TYPE="$1"  # table | database
TARGET_NAME="$2"     # 表名或数据库名

if [ "$DISASTER_TYPE" = "table" ]; then
    echo "Recovering table: $TARGET_NAME"

    # 1. 从最新备份恢复表
    ./restore_single_table.sh "$LATEST_FULL_BACKUP" "$TARGET_NAME"

    # 2. 应用 WAL 日志恢复到故障点
    pgbackrest --stanza=openidaas \
        --type=time \
        --target="$(date -d 'now - 1 hour' '+%Y-%m-%d %H:%M:%S')" \
        restore

elif [ "$DISASTER_TYPE" = "database" ]; then
    echo "Recovering database: $TARGET_NAME"

    # 1. 停止数据库
    sudo systemctl stop postgresql

    # 2. 恢复数据库
    pgbackrest --stanza=openidaas --delta restore

    # 3. 启动数据库
    sudo systemctl start postgresql

    # 4. 验证数据
    psql -U postgres -c "SELECT COUNT(*) FROM users;"
fi
```

### L3/L4 灾难恢复 (服务器/数据中心)

#### 服务器故障恢复

```bash
#!/bin/bash
# dr_l3.sh

NEW_SERVER="$1"

echo "Failing over to new server: $NEW_SERVER"

# 1. 在新服务器上安装 PostgreSQL
ssh "$NEW_SERVER" "sudo apt-get install -y postgresql-15"

# 2. 同步配置文件
rsync -avz /etc/postgresql/ "$NEW_SERVER:/etc/postgresql/"

# 3. 从异地备份恢复数据
ssh "$NEW_SERVER" "
    # 从 S3 下载最新备份
    aws s3 cp s3://openidaas-backup/full/latest.dump.gz /tmp/
    gunzip /tmp/latest.dump.gz

    # 恢复数据库
    createdb openidaas
    pg_restore -d openidaas /tmp/latest.dump
"

# 4. 更新 DNS 指向新服务器
# 使用 Cloudflare/AWS Route53 API 更新 DNS

echo "Failover completed"
```

#### 数据中心故障恢复

```bash
#!/bin/bash
# dr_l4.sh

DR_SITE="dr.openidaas.com"

echo "Activating disaster recovery site: $DR_SITE"

# 1. 启动 DR 站点的 PostgreSQL
ssh "$DR_SITE" "sudo systemctl start postgresql"

# 2. 应用 WAL 日志到最新状态
ssh "$DR_SITE" "
    pgbackrest --stanza=openidaas \
        --type=time \
        --target='$(date -d 'now - 15 minutes' '+%Y-%m-%d %H:%M:%S')' \
        restore
"

# 3. 验证数据完整性
ssh "$DR_SITE" "psql -U postgres -c 'SELECT COUNT(*) FROM users;'"

# 4. 切换流量到 DR 站点
# 使用负载均衡器切换流量

echo "DR site activated"
```

---

## 监控告警

### 备份监控

#### 检查备份状态

```sql
-- 创建备份监控表
CREATE TABLE backup_monitor (
    id SERIAL PRIMARY KEY,
    backup_type VARCHAR(32) NOT NULL,
    backup_file VARCHAR(512) NOT NULL,
    backup_size BIGINT,
    backup_checksum VARCHAR(64),
    backup_time TIMESTAMP NOT NULL,
    restore_test_time TIMESTAMP,
    restore_test_result VARCHAR(32),
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- 查看最近备份
SELECT * FROM backup_monitor
WHERE backup_type = 'full'
ORDER BY backup_time DESC
LIMIT 10;
```

#### 备份监控脚本

```bash
#!/bin/bash
# monitor_backup.sh

# 检查最新备份
LATEST_BACKUP=$(ls -t /backup/openidaas/full/*.dump.gz | head -1)
LATEST_DATE=$(basename "$LATEST_BACKUP" | grep -oP '\d{8}_\d{6}')

# 检查备份时间 (应该在24小时内)
BACKUP_TIME=$(date -d "${LATEST_DATE:0:4}-${LATEST_DATE:4:2}-${LATEST_DATE:6:2} ${LATEST_DATE:9:2}:${LATEST_DATE:11:2}:${LATEST_DATE:13:2}" +%s)
NOW=$(date +%s)
DIFF_HOURS=$(( ($NOW - $BACKUP_TIME) / 3600 ))

if [ $DIFF_HOURS -gt 24 ]; then
    echo "WARNING: Latest backup is $DIFF_HOURS hours old"
    send_alert "Backup is outdated"
fi

# 检查备份文件完整性
if ! gzip -t "$LATEST_BACKUP" 2>/dev/null; then
    echo "ERROR: Backup file is corrupted"
    send_alert "Backup file corrupted"
fi

# 检查备份文件大小
BACKUP_SIZE=$(stat -f%z "$LATEST_BACKUP" 2>/dev/null || stat -c%s "$LATEST_BACKUP")
MIN_SIZE=$((1024 * 1024 * 100))  # 100MB

if [ $BACKUP_SIZE -lt $MIN_SIZE ]; then
    echo "WARNING: Backup file is too small ($BACKUP_SIZE bytes)"
    send_alert "Backup file too small"
fi
```

### WAL 监控

```bash
#!/bin/bash
# monitor_wal.sh

# 检查 WAL 归档延迟
WAL_DIR="/backup/wal"
LATEST_WAL=$(ls -t "$WAL_DIR"/*.gz 2>/dev/null | head -1)

if [ -z "$LATEST_WAL" ]; then
    echo "ERROR: No WAL files found"
    send_alert "WAL archive is empty"
    exit 1
fi

WAL_AGE=$(( ($(date +%s) - $(stat -f%m "$LATEST_WAL" 2>/dev/null || stat -c%Y "$LATEST_WAL")) / 60 ))

if [ $WAL_AGE -gt 10 ]; then
    echo "WARNING: Latest WAL is $WAL_AGE minutes old"
    send_alert "WAL archive is delayed"
fi
```

### 恢复测试

```bash
#!/bin/bash
# test_restore.sh

LATEST_BACKUP=$(ls -t /backup/openidaas/full/*.dump.gz | head -1)
TEST_DB="openidaas_test_$(date +%Y%m%d_%H%M%S)"

echo "Testing restore from: $LATEST_BACKUP"

# 1. 创建测试数据库
createdb "$TEST_DB"

# 2. 恢复备份
gunzip -c "$LATEST_BACKUP" | pg_restore -d "$TEST_DB" -v > /tmp/restore_test.log 2>&1

# 3. 验证数据
USER_COUNT=$(psql -d "$TEST_DB" -t -c "SELECT COUNT(*) FROM users;")

if [ "$USER_COUNT" -gt 0 ]; then
    echo "Restore test PASSED: $USER_COUNT users restored"

    # 记录测试结果
    psql -d openidaas <<EOF
    INSERT INTO backup_monitor (backup_type, backup_file, backup_size,
        backup_time, restore_test_time, restore_test_result)
    VALUES ('full', '$LATEST_BACKUP', $(stat -f%z "$LATEST_BACKUP" 2>/dev/null || stat -c%s "$LATEST_BACKUP"),
        NOW(), NOW(), 'SUCCESS');
EOF

    # 删除测试数据库
    dropdb "$TEST_DB"
else
    echo "Restore test FAILED"

    # 记录失败结果
    psql -d openidaas <<EOF
    INSERT INTO backup_monitor (backup_type, backup_file, backup_size,
        backup_time, restore_test_time, restore_test_result)
    VALUES ('full', '$LATEST_BACKUP', $(stat -f%z "$LATEST_BACKUP" 2>/dev/null || stat -c%s "$LATEST_BACKUP"),
        NOW(), NOW(), 'FAILED');
EOF

    send_alert "Restore test FAILED"
    exit 1
fi
```

### 告警配置

#### Prometheus 告警规则

```yaml
groups:
  - name: postgres_backup_alerts
    rules:
      # 备份超时告警
      - alert: PostgresBackupOverdue
        expr: time() - postgres_backup_last_success_time_seconds > 86400
        for: 5m
        labels:
          severity: critical
        annotations:
          summary: PostgreSQL backup is overdue
          description: "Last successful backup was {{ $value }}s ago"

      # WAL 归档延迟告警
      - alert: PostgresWALArchiveDelay
        expr: postgres_wal_archive_delay_seconds > 600
        for: 5m
        labels:
          severity: warning
        annotations:
          summary: PostgreSQL WAL archive is delayed
          description: "WAL archive delay is {{ $value }}s"

      # 备份文件损坏告警
      - alert: PostgresBackupCorrupted
        expr: postgres_backup_integrity == 0
        for: 1m
        labels:
          severity: critical
        annotations:
          summary: PostgreSQL backup is corrupted
          description: "Backup file integrity check failed"
```

---

## 附录

### 定期维护任务

```bash
#!/bin/bash
# maintenance.sh

case "$1" in
  daily)
    echo "Running daily maintenance..."
    # 1. 清理过期备份
    find /backup -mtime +30 -delete

    # 2. 压缩 WAL 日志
    gzip /backup/wal/*.gz 2>/dev/null

    # 3. 更新统计信息
    psql -U postgres -c "ANALYZE;"
    ;;

  weekly)
    echo "Running weekly maintenance..."
    # 1. 重建索引
    psql -U postgres -c "REINDEX DATABASE openidaas;"

    # 2. 清理死元组
    psql -U postgres -c "VACUUM FULL;"

    # 3. 备份测试
    ./test_restore.sh
    ;;

  monthly)
    echo "Running monthly maintenance..."
    # 1. 创建归档备份
    ./archive_backup.sh

    # 2. 更新监控数据
    psql -U postgres <<EOF
    DELETE FROM backup_monitor WHERE created_at < NOW() - INTERVAL '6 months';
EOF
    ;;

  *)
    echo "Usage: $0 {daily|weekly|monthly}"
    exit 1
    ;;
esac
```

### Cron 任务配置

```bash
# crontab -e

# 每天凌晨 2 点全量备份
0 2 * * * /backup/scripts/full_backup.sh >> /var/log/postgresql/backup.log 2>&1

# 每小时检查备份状态
0 * * * * /backup/scripts/monitor_backup.sh >> /var/log/postgresql/monitor.log 2>&1

# 每周日凌晨 3 点恢复测试
0 3 * * 0 /backup/scripts/test_restore.sh >> /var/log/postgresql/restore_test.log 2>&1

# 每天凌晨 1 点 WAL 归档
0 1 * * * /backup/scripts/wal_archive.sh >> /var/log/postgresql/wal_archive.log 2>&1

# 每周一凌晨 4 点周维护
0 4 * * 1 /backup/scripts/maintenance.sh weekly >> /var/log/postgresql/maintenance.log 2>&1
```

---

**文档版本**: 1.0
**最后更新**: 2026-02-08
**维护者**: OpenIDaaS Team
