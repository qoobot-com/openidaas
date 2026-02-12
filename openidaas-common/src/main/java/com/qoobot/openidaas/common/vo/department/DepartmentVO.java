package com.qoobot.openidaas.common.vo.department;

import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 部门信息VO
 *
 * @author QooBot
 */
@Data
@Schema(description = "部门信息")
public class DepartmentVO {

    @Schema(description = "部门ID", example = "1")
    private Long id;

    @Schema(description = "部门编码", example = "DEPT_001")
    private String deptCode;

    @Schema(description = "部门名称", example = "技术部")
    private String deptName;

    @Schema(description = "部门描述", example = "技术研发部门")
    private String description;

    @Schema(description = "父部门ID", example = "0")
    private Long parentId;

    @Schema(description = "父部门名称", example = "总公司")
    private String parentName;

    @Schema(description = "部门负责人ID", example = "1")
    private Long leaderId;

    @Schema(description = "部门负责人姓名", example = "张三")
    private String leaderName;

    @Schema(description = "联系电话", example = "010-12345678")
    private String phone;

    @Schema(description = "邮箱", example = "tech@example.com")
    private String email;

    @Schema(description = "排序", example = "1")
    private Integer sortOrder;

    @Schema(description = "是否启用", example = "true")
    private Boolean enabled;

    @Schema(description = "层级路径", example = "/1/2/3/")
    private String treePath;

    @Schema(description = "创建时间")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime createdAt;

    @Schema(description = "更新时间")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime updatedAt;

    @Schema(description = "租户ID", example = "1")
    private Long tenantId;

    @Schema(description = "子部门列表")
    private List<DepartmentVO> children;

    @Schema(description = "部门下用户数量", example = "20")
    private Long userCount;
}