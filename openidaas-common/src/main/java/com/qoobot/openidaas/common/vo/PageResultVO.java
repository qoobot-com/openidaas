package com.qoobot.openidaas.common.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.util.List;

/**
 * 分页结果封装类
 *
 * @param <T> 数据类型
 * @author QooBot
 */
@Data
@Schema(description = "分页结果")
public class PageResultVO<T> {

    @Schema(description = "当前页码")
    private Long currentPage;

    @Schema(description = "每页大小")
    private Long pageSize;

    @Schema(description = "总记录数")
    private Long total;

    @Schema(description = "总页数")
    private Long totalPages;

    @Schema(description = "数据列表")
    private List<T> records;

    public PageResultVO() {}

    public PageResultVO(Long currentPage, Long pageSize, Long total, List<T> records) {
        this.currentPage = currentPage;
        this.pageSize = pageSize;
        this.total = total;
        this.records = records;
        this.totalPages = (total + pageSize - 1) / pageSize;
    }

    /**
     * 创建空的分页结果
     */
    public static <T> PageResultVO<T> empty(Long currentPage, Long pageSize) {
        return new PageResultVO<>(currentPage, pageSize, 0L, List.of());
    }

    /**
     * 创建分页结果
     */
    public static <T> PageResultVO<T> of(Long currentPage, Long pageSize, Long total, List<T> records) {
        return new PageResultVO<>(currentPage, pageSize, total, records);
    }
}