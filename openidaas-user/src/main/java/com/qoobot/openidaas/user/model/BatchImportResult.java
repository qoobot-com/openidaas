package com.qoobot.openidaas.user.model;

import lombok.Data;

import java.util.ArrayList;
import java.util.List;

/**
 * 批量导入结果
 * 
 * @author Qoobot Team
 * @since 1.0.0
 */
@Data
public class BatchImportResult {

    private int totalCount;
    private int successCount;
    private int failureCount;
    private long duration; // 毫秒
    private List<Long> userIds = new ArrayList<>();
    private List<String> errors = new ArrayList<>();

    public void incrementSuccess() {
        this.successCount++;
    }

    public void incrementFailure() {
        this.failureCount++;
    }

    public void addUserId(Long userId) {
        this.userIds.add(userId);
    }

    public void addError(String error) {
        this.errors.add(error);
    }
}
