package com.qoobot.openidaas.audit.schedule;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

/**
 * 数据归档定时任务
 *
 * @author QooBot
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class DataArchiveJob {

    private final JdbcTemplate jdbcTemplate;

    @Value("${app.archive.audit-logs-months:3}")
    private int auditLogsMonthsToArchive;

    @Value("${app.archive.mfa-logs-months:3}")
    private int mfaLogsMonthsToArchive;

    @Value("${app.archive.login-sessions-months:6}")
    private int loginSessionsMonthsToArchive;

    @Value("${app.archive.auth-tokens-days:7}")
    private int authTokenDaysToArchive;

    /**
     * 归档审计日志
     * 每月1日凌晨2点执行
     */
    @Scheduled(cron = "0 0 2 1 * ?")
    public void archiveAuditLogs() {
        LocalDate cutoffDate = LocalDate.now().minusMonths(auditLogsMonthsToArchive);
        String cutoffDateStr = cutoffDate.format(DateTimeFormatter.ofPattern("yyyy-MM-dd"));

        log.info("开始归档审计日志,归档截止日期: {}", cutoffDateStr);

        try {
            // 1. 创建归档表(如果不存在)
            createArchiveTableIfNotExists("audit_logs_archive", "audit_logs");

            // 2. 归档数据
            String archiveSql = String.format(
                "INSERT INTO audit_logs_archive " +
                "SELECT * FROM audit_logs WHERE created_at < '%s'",
                cutoffDateStr
            );

            int archivedCount = jdbcTemplate.update(archiveSql);
            log.info("归档审计日志 {} 条", archivedCount);

            // 3. 删除已归档的数据
            String deleteSql = String.format(
                "DELETE FROM audit_logs WHERE created_at < '%s'",
                cutoffDateStr
            );

            int deletedCount = jdbcTemplate.update(deleteSql);
            log.info("删除审计日志 {} 条", deletedCount);

            // 4. 优化表
            jdbcTemplate.execute("OPTIMIZE TABLE audit_logs");

            log.info("审计日志归档完成");

        } catch (Exception e) {
            log.error("归档审计日志失败", e);
        }
    }

    /**
     * 归档MFA日志
     * 每月1日凌晨2:30执行
     */
    @Scheduled(cron = "0 30 2 1 * ?")
    public void archiveMFALogs() {
        LocalDate cutoffDate = LocalDate.now().minusMonths(mfaLogsMonthsToArchive);
        String cutoffDateStr = cutoffDate.format(DateTimeFormatter.ofPattern("yyyy-MM-dd"));

        log.info("开始归档MFA日志,归档截止日期: {}", cutoffDateStr);

        try {
            createArchiveTableIfNotExists("mfa_logs_archive", "mfa_logs");

            String archiveSql = String.format(
                "INSERT INTO mfa_logs_archive " +
                "SELECT * FROM mfa_logs WHERE created_at < '%s'",
                cutoffDateStr
            );

            int archivedCount = jdbcTemplate.update(archiveSql);
            log.info("归档MFA日志 {} 条", archivedCount);

            String deleteSql = String.format(
                "DELETE FROM mfa_logs WHERE created_at < '%s'",
                cutoffDateStr
            );

            int deletedCount = jdbcTemplate.update(deleteSql);
            log.info("删除MFA日志 {} 条", deletedCount);

            jdbcTemplate.execute("OPTIMIZE TABLE mfa_logs");

            log.info("MFA日志归档完成");

        } catch (Exception e) {
            log.error("归档MFA日志失败", e);
        }
    }

    /**
     * 归档登录会话
     * 每月1日凌晨3点执行
     */
    @Scheduled(cron = "0 0 3 1 * ?")
    public void archiveLoginSessions() {
        LocalDate cutoffDate = LocalDate.now().minusMonths(loginSessionsMonthsToArchive);
        String cutoffDateStr = cutoffDate.format(DateTimeFormatter.ofPattern("yyyy-MM-dd"));

        log.info("开始归档登录会话,归档截止日期: {}", cutoffDateStr);

        try {
            createArchiveTableIfNotExists("login_sessions_archive", "login_sessions");

            String archiveSql = String.format(
                "INSERT INTO login_sessions_archive " +
                "SELECT * FROM login_sessions WHERE login_at < '%s'",
                cutoffDateStr
            );

            int archivedCount = jdbcTemplate.update(archiveSql);
            log.info("归档登录会话 {} 条", archivedCount);

            String deleteSql = String.format(
                "DELETE FROM login_sessions WHERE login_at < '%s'",
                cutoffDateStr
            );

            int deletedCount = jdbcTemplate.update(deleteSql);
            log.info("删除登录会话 {} 条", deletedCount);

            jdbcTemplate.execute("OPTIMIZE TABLE login_sessions");

            log.info("登录会话归档完成");

        } catch (Exception e) {
            log.error("归档登录会话失败", e);
        }
    }

    /**
     * 清理过期Token
     * 每天凌晨3:30执行
     */
    @Scheduled(cron = "0 30 3 * * ?")
    public void cleanupExpiredTokens() {
        log.info("开始清理过期Token");

        try {
            String deleteSql = String.format(
                "DELETE FROM auth_tokens WHERE expire_at < NOW() - INTERVAL %d DAY",
                authTokenDaysToArchive
            );

            int deletedCount = jdbcTemplate.update(deleteSql);
            log.info("清理过期Token {} 条", deletedCount);

            jdbcTemplate.execute("OPTIMIZE TABLE auth_tokens");

            log.info("过期Token清理完成");

        } catch (Exception e) {
            log.error("清理过期Token失败", e);
        }
    }

    /**
     * 归档安全事件
     * 每月1日凌晨4点执行
     */
    @Scheduled(cron = "0 0 4 1 * ?")
    public void archiveSecurityEvents() {
        LocalDate cutoffDate = LocalDate.now().minusMonths(12); // 保留1年
        String cutoffDateStr = cutoffDate.format(DateTimeFormatter.ofPattern("yyyy-MM-dd"));

        log.info("开始归档安全事件,归档截止日期: {}", cutoffDateStr);

        try {
            createArchiveTableIfNotExists("security_events_archive", "security_events");

            // 只归档已处理的事件
            String archiveSql = String.format(
                "INSERT INTO security_events_archive " +
                "SELECT * FROM security_events WHERE created_at < '%s' AND resolved = 1",
                cutoffDateStr
            );

            int archivedCount = jdbcTemplate.update(archiveSql);
            log.info("归档安全事件 {} 条", archivedCount);

            String deleteSql = String.format(
                "DELETE FROM security_events WHERE created_at < '%s' AND resolved = 1",
                cutoffDateStr
            );

            int deletedCount = jdbcTemplate.update(deleteSql);
            log.info("删除安全事件 {} 条", deletedCount);

            jdbcTemplate.execute("OPTIMIZE TABLE security_events");

            log.info("安全事件归档完成");

        } catch (Exception e) {
            log.error("归档安全事件失败", e);
        }
    }

    /**
     * 创建归档表(如果不存在)
     */
    private void createArchiveTableIfNotExists(String archiveTableName, String sourceTableName) {
        String checkSql = String.format(
            "SELECT COUNT(*) FROM information_schema.tables " +
            "WHERE table_schema = DATABASE() AND table_name = '%s'",
            archiveTableName
        );

        Integer count = jdbcTemplate.queryForObject(checkSql, Integer.class);

        if (count == null || count == 0) {
            String createSql = String.format(
                "CREATE TABLE %s LIKE %s",
                archiveTableName,
                sourceTableName
            );

            jdbcTemplate.execute(createSql);
            log.info("创建归档表: {}", archiveTableName);
        }
    }
}
