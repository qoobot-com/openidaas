package com.qoobot.openidaas.organization.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 组织VO
 *
 * @author Qoobot
 * @version 1.0.0
 */
@Data
@Schema(description = "组织VO")
public class OrganizationVO {

    @Schema(description = "组织ID")
    private Long id;

    @Schema(description = "组织名称")
    private String name;

    @Schema(description = "组织编码")
    private String code;

    @Schema(description = "组织类型")
    private String type;

    @Schema(description = "父组织ID")
    private Long parentId;

    @Schema(description = "组织层级")
    private Integer level;

    @Schema(description = "组织路径")
    private String path;

    @Schema(description = "描述")
    private String description;

    @Schema(description = "负责人ID")
    private Long managerId;

    @Schema(description = "负责人姓名")
    private String managerName;

    @Schema(description = "联系电话")
    private String phone;

    @Schema(description = "联系邮箱")
    private String email;

    @Schema(description = "状态")
    private String status;

    @Schema(description = "排序")
    private Integer sort;

    @Schema(description = "是否叶子节点")
    private Boolean leaf;

    @Schema(description = "子组织数量")
    private Integer childrenCount;

    @Schema(description = "创建时间")
    private LocalDateTime createTime;

    @Schema(description = "更新时间")
    private LocalDateTime updateTime;

    @Schema(description = "子组织列表")
    private List<OrganizationVO> children;

}