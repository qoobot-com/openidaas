package com.qoobot.openidaas.user.model;

import lombok.Builder;
import lombok.Data;

/**
 * 用户导入结果
 * 
 * @author Qoobot Team
 * @since 1.0.0
 */
@Data
@Builder
public class UserImportResult {

    private Integer rowNum;
    private String username;
    private Boolean success;
    private Long userId;
    private String message;
}
