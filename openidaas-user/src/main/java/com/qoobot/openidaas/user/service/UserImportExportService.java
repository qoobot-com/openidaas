package com.qoobot.openidaas.user.service;

import com.alibaba.excel.EasyExcel;
import com.alibaba.excel.write.metadata.WriteSheet;
import com.alibaba.excel.write.style.column.LongestMatchColumnWidthStyleStrategy;
import com.qoobot.openidaas.user.dto.CreateUserRequest;
import com.qoobot.openidaas.user.dto.UserDTO;
import com.qoobot.openidaas.user.entity.User;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * 用户导入导出服务
 * 
 * @author Qoobot Team
 * @since 1.0.0
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class UserImportExportService {

    private final UserService userService;

    /**
     * 导出用户到Excel
     */
    public byte[] exportUsersToExcel(List<UserDTO> users) throws IOException {
        log.info("Exporting {} users to Excel", users.size());

        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();

        EasyExcel.write(outputStream, UserExportModel.class)
                .registerWriteHandler(new LongestMatchColumnWidthStyleStrategy())
                .sheet("用户列表")
                .doWrite(convertToExportModels(users));

        return outputStream.toByteArray();
    }

    /**
     * 从Excel导入用户
     */
    @Transactional(rollbackFor = Exception.class)
    public List<UserImportResult> importUsersFromExcel(MultipartFile file) throws IOException {
        log.info("Importing users from Excel: {}", file.getOriginalFilename());

        List<UserImportModel> importModels = EasyExcel.read(file.getInputStream())
                .head(UserImportModel.class)
                .sheet()
                .doReadSync();

        List<UserImportResult> results = new ArrayList<>();

        for (int i = 0; i < importModels.size(); i++) {
            UserImportModel model = importModels.get(i);
            int rowNum = i + 2; // Excel行号（从2开始，因为第1行是表头）

            try {
                CreateUserRequest request = convertToCreateRequest(model);
                UserDTO user = userService.createUser(request);

                results.add(UserImportResult.builder()
                        .rowNum(rowNum)
                        .username(model.getUsername())
                        .success(true)
                        .userId(user.getId())
                        .message("导入成功")
                        .build());

            } catch (Exception e) {
                log.error("Failed to import user at row {}: {}", rowNum, e.getMessage());

                results.add(UserImportResult.builder()
                        .rowNum(rowNum)
                        .username(model.getUsername())
                        .success(false)
                        .message(e.getMessage())
                        .build());
            }
        }

        log.info("Import completed: {} success, {} failed", 
                results.stream().filter(UserImportResult::isSuccess).count(),
                results.stream().filter(r -> !r.isSuccess()).count());

        return results;
    }

    /**
     * 批量导入用户（高性能）
     */
    @Transactional(rollbackFor = Exception.class)
    public BatchImportResult batchImportUsers(List<CreateUserRequest> requests) {
        log.info("Batch importing {} users", requests.size());

        BatchImportResult result = new BatchImportResult();
        long startTime = System.currentTimeMillis();

        for (CreateUserRequest request : requests) {
            try {
                UserDTO user = userService.createUser(request);
                result.incrementSuccess();
                result.addUserId(user.getId());
            } catch (Exception e) {
                log.error("Failed to import user: {}", request.getUsername(), e);
                result.incrementFailure();
            }
        }

        long duration = System.currentTimeMillis() - startTime;
        result.setDuration(duration);

        log.info("Batch import completed: {} success, {} failed, duration: {}ms",
                result.getSuccessCount(), result.getFailureCount(), duration);

        return result;
    }

    /**
     * 转换为导出模型
     */
    private List<UserExportModel> convertToExportModels(List<UserDTO> users) {
        return users.stream()
                .map(this::toExportModel)
                .collect(java.util.stream.Collectors.toList());
    }

    /**
     * 转换为导出模型
     */
    private UserExportModel toExportModel(UserDTO dto) {
        UserExportModel model = new UserExportModel();
        model.setUsername(dto.getUsername());
        model.setFullname(dto.getFullname());
        model.setNickname(dto.getNickname());
        model.setEmail(dto.getEmail());
        model.setPhone(dto.getPhone());
        model.setEmployeeId(dto.getEmployeeId());
        model.setJobTitle(dto.getJobTitle());
        model.setStatus(dto.getStatus() != null ? dto.getStatus().name() : "");
        model.setCreatedAt(dto.getCreatedAt() != null ? dto.getCreatedAt().toString() : "");
        return model;
    }

    /**
     * 转换为创建请求
     */
    private CreateUserRequest convertToCreateRequest(UserImportModel model) {
        CreateUserRequest request = new CreateUserRequest();
        request.setUsername(model.getUsername());
        request.setPassword(model.getPassword() != null ? model.getPassword() : "OpenIdaas@123");
        request.setFullname(model.getFullname());
        request.setNickname(model.getNickname());
        request.setEmail(model.getEmail());
        request.setPhone(model.getPhone());
        request.setEmployeeId(model.getEmployeeId());
        request.setJobTitle(model.getJobTitle());
        request.setRequirePasswordChange(true);
        return request;
    }
}
