package com.qoobot.openidaas.user.model;

import com.alibaba.excel.annotation.ExcelProperty;
import lombok.Data;

/**
 * 用户导入模型
 * 
 * @author Qoobot Team
 * @since 1.0.0
 */
@Data
public class UserImportModel {

    @ExcelProperty("用户名")
    private String username;

    @ExcelProperty("密码")
    private String password;

    @ExcelProperty("姓名")
    private String fullname;

    @ExcelProperty("昵称")
    private String nickname;

    @ExcelProperty("邮箱")
    private String email;

    @ExcelProperty("手机号")
    private String phone;

    @ExcelProperty("员工ID")
    private String employeeId;

    @ExcelProperty("职位")
    private String jobTitle;
}
