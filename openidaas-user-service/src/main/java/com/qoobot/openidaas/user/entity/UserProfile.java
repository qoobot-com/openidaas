package com.qoobot.openidaas.user.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.qoobot.openidaas.common.enumeration.GenderEnum;
import lombok.Data;

import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * 用户档案实体类
 *
 * @author QooBot
 */
@Data
@TableName("user_profiles")
public class UserProfile {

    @TableId(type = IdType.INPUT)
    private Long userId;

    /**
     * 头像URL
     */
    private String avatarUrl;

    /**
     * 真实姓名
     */
    private String fullName;

    /**
     * 昵称
     */
    private String nickname;

    /**
     * 性别：1-男，2-女，0-未知
     */
    private Integer gender;

    /**
     * 出生日期
     */
    private LocalDate birthDate;

    /**
     * 身份证号（加密存储）
     */
    private String idCard;

    /**
     * 员工编号
     */
    private String employeeId;

    /**
     * 入职日期
     */
    private LocalDate hireDate;

    /**
     * 紧急联系人
     */
    private String emergencyContact;

    /**
     * 紧急联系电话
     */
    private String emergencyPhone;

    /**
     * 数据是否已脱敏：0-否，1-是
     */
    private Integer dataMasked;

    /**
     * 脱敏字段记录（JSON格式）
     */
    private String maskedFields;

    /**
     * 创建时间
     */
    private LocalDateTime createdAt;

    /**
     * 更新时间
     */
    private LocalDateTime updatedAt;
}
