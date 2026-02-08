package com.qoobot.openidaas.user.model;

import com.alibaba.excel.annotation.ExcelProperty;
import com.alibaba.excel.annotation.write.style.ColumnWidth;
import lombok.Data;

/**
 * 用户导出模型
 * 
 * @author Qoobot Team
 * @since 1.0.0
 */
@Data
@ColumnWidth(20)
public class UserExportModel {

    @ExcelProperty("用户名")
    @ColumnWidth(15)
    private String username;

    @ExcelProperty("姓名")
    @ColumnWidth(15)
    private String fullname;

    @ExcelProperty("昵称")
    @ColumnWidth(15)
    private String nickname;

    @ExcelProperty("邮箱")
    @ColumnWidth(25)
    private String email;

    @ExcelProperty("手机号")
    @ColumnWidth(15)
    private String phone;

    @ExcelProperty("员工ID")
    @ColumnWidth(15)
    private String employeeId;

    @ExcelProperty("职位")
    @ColumnWidth(15)
    private String jobTitle;

    @ExcelProperty("状态")
    @ColumnWidth(10)
    private String status;

    @ExcelProperty("创建时间")
    @ColumnWidth(20)
    private String createdAt;
}
