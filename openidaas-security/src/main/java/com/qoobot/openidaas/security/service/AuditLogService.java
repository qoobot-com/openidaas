package com.qoobot.openidaas.security.service;

import com.qoobot.openidaas.security.audit.AuditLogEvent;
import com.qoobot.openidaas.security.config.SecurityProperties;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.elasticsearch.core.ElasticsearchOperations;
import org.springframework.data.elasticsearch.core.mapping.IndexCoordinates;
import org.springframework.data.elasticsearch.core.query.IndexQuery;
import org.springframework.data.elasticsearch.core.query.IndexQueryBuilder;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

/**
 * 审计日志服务
 * 
 * 支持将审计日志写入Elasticsearch，便于后续分析和查询
 * 
 * @author Qoobot Team
 * @since 1.0.0
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class AuditLogService {

    private final ElasticsearchOperations elasticsearchTemplate;
    private final SecurityProperties securityProperties;

    /**
     * 记录审计日志事件
     * 
     * @param event 审计日志事件
     */
    @Async("auditLogExecutor")
    public void logEvent(AuditLogEvent event) {
        try {
            if (!securityProperties.getAudit().getEnabled()) {
                return;
            }

            // 构建索引查询
            IndexQuery indexQuery = new IndexQueryBuilder()
                    .withObject(event)
                    .build();

            // 写入Elasticsearch
            String indexName = getIndexName();
            elasticsearchTemplate.index(indexQuery, IndexCoordinates.of(indexName));

            log.debug("Audit log recorded: operation={}, user={}", 
                    event.getOperation(), event.getUsername());
        } catch (Exception e) {
            log.error("Failed to record audit log: {}", e.getMessage());
        }
    }

    /**
     * 获取索引名称（按日期分片）
     * 
     * @return 索引名称
     */
    private String getIndexName() {
        String date = java.time.LocalDate.now().toString();
        return "audit-logs-" + date;
    }
}
