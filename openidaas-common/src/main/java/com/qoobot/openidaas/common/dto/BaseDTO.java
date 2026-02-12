package com.qoobot.openidaas.common.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.io.Serializable;

/**
 * DTO基类
 *
 * @author QooBot
 */
@Data
public abstract class BaseDTO implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 当前页码
     */
    @Schema(description = "当前页码")
    @NotNull(message = "页码不能为空")
    private Long currentPage = 1L;

    /**
     * 每页大小
     */
    @Schema(description = "每页大小")
    @NotNull(message = "每页大小不能为空")
    private Long pageSize = 10L;

    /**
     * 排序字段
     */
    @Schema(description = "排序字段")
    private String orderBy;

    /**
     * 排序方向 (ASC/DESC)
     */
    @Schema(description = "排序方向")
    private String orderDirection = "DESC";

    /**
     * 获取偏移量
     */
    public Long getOffset() {
        return (currentPage - 1) * pageSize;
    }

    /**
     * 获取限制数量
     */
    public Long getLimit() {
        return pageSize;
    }
    
    /**
     * 获取页码（兼容方法）
     */
    public Long getPage() {
        return currentPage;
    }
    
    /**
     * 获取页面大小（兼容方法）
     */
    public Long getSize() {
        return pageSize;
    }
}