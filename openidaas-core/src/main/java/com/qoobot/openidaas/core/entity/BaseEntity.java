package com.qoobot.openidaas.core.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;

import java.time.LocalDateTime;

/**
 * 基础实体类
 * 
 * 包含所有实体类共用的字段
 * 
 * @author Qoobot Team
 * @since 1.0.0
 */
@MappedSuperclass
@Getter
@Setter
public abstract class BaseEntity {

    @CreatedDate
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @LastModifiedDate
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    /**
     * 部门状态枚举
     */
    public enum DepartmentStatus {
        ACTIVE("激活"),
        INACTIVE("停用"),
        DELETED("已删除");

        private final String description;

        DepartmentStatus(String description) {
            this.description = description;
        }

        public String getDescription() {
            return description;
        }
    }
}