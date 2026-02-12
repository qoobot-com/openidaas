package com.qoobot.openidaas.organization.service;

import com.qoobot.openidaas.common.dto.position.PositionCreateDTO;
import com.qoobot.openidaas.common.dto.position.PositionUpdateDTO;
import com.qoobot.openidaas.common.exception.BusinessException;
import com.qoobot.openidaas.common.vo.position.PositionVO;
import com.qoobot.openidaas.organization.entity.Position;
import com.qoobot.openidaas.organization.mapper.PositionMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * 职位服务单元测试
 *
 * @author QooBot
 */
@SpringBootTest
@ActiveProfiles("test")
@Transactional
@DisplayName("职位服务测试")
class PositionServiceTest {

    @Autowired
    private PositionService positionService;

    @Autowired
    private PositionMapper positionMapper;

    @Autowired
    private DepartmentService departmentService;

    private Position testPosition;
    private PositionCreateDTO createDTO;
    private PositionUpdateDTO updateDTO;
    private Long testDeptId;

    @BeforeEach
    void setUp() {
        // 创建测试部门
        com.qoobot.openidaas.common.dto.department.DepartmentCreateDTO deptDTO =
                new com.qoobot.openidaas.common.dto.department.DepartmentCreateDTO();
        deptDTO.setDeptCode("TEST_POS_DEPT");
        deptDTO.setDeptName("测试部门");
        deptDTO.setParentId(0L);
        deptDTO.setEnabled(true);
        com.qoobot.openidaas.common.vo.department.DepartmentVO dept = departmentService.createDepartment(deptDTO);
        testDeptId = dept.getId();

        // 创建测试职位
        testPosition = new Position();
        testPosition.setPositionCode("TEST_POS");
        testPosition.setPositionName("测试职位");
        testPosition.setDeptId(testDeptId);
        testPosition.setLevel(10);
        testPosition.setJobGrade("P10");
        testPosition.setIsManager(0);
        positionMapper.insert(testPosition);

        // 创建DTO
        createDTO = new PositionCreateDTO();
        createDTO.setPositionCode("NEW_POS");
        createDTO.setPositionName("新职位");
        createDTO.setDeptId(testDeptId);
        createDTO.setLevel(12);
        createDTO.setJobGrade("P12");
        createDTO.setIsManager(0);
        createDTO.setDescription("新创建的职位");

        updateDTO = new PositionUpdateDTO();
        updateDTO.setId(testPosition.getId());
        updateDTO.setPositionCode("UPDATED_POS");
        updateDTO.setPositionName("更新后的职位");
        updateDTO.setJobGrade("P14");
        updateDTO.setDescription("更新后的描述");
    }

    @Test
    @DisplayName("测试获取职位列表 - 全部")
    void testGetPositionList_All() {
        // 执行
        List<PositionVO> positions = positionService.getPositionList(null);

        // 验证
        assertNotNull(positions);
        assertFalse(positions.isEmpty());
    }

    @Test
    @DisplayName("测试获取职位列表 - 按部门")
    void testGetPositionList_ByDepartment() {
        // 执行
        List<PositionVO> positions = positionService.getPositionList(testDeptId);

        // 验证
        assertNotNull(positions);
        assertFalse(positions.isEmpty());
        assertTrue(positions.stream().allMatch(p -> p.getDeptId().equals(testDeptId)));
    }

    @Test
    @DisplayName("测试获取职位列表 - 不存在的部门")
    void testGetPositionList_DepartmentNotExist() {
        // 执行
        List<PositionVO> positions = positionService.getPositionList(99999L);

        // 验证
        assertNotNull(positions);
        assertTrue(positions.isEmpty());
    }

    @Test
    @DisplayName("测试创建职位 - 成功")
    void testCreatePosition_Success() {
        // 执行
        PositionVO result = positionService.createPosition(createDTO);

        // 验证
        assertNotNull(result);
        assertNotNull(result.getId());
        assertEquals("NEW_POS", result.getPositionCode());
        assertEquals("新职位", result.getPositionName());
        assertEquals(testDeptId, result.getDeptId());
        assertEquals(12, result.getLevel());
        assertEquals("P12", result.getJobGrade());
        assertFalse(result.getManager());
    }

    @Test
    @DisplayName("测试创建职位 - 重复职位编码")
    void testCreatePosition_DuplicateCode() {
        // 准备
        createDTO.setPositionCode("TEST_POS");

        // 执行和验证
        assertThrows(BusinessException.class, () -> positionService.createPosition(createDTO));
    }

    @Test
    @DisplayName("测试创建职位 - 部门不存在")
    void testCreatePosition_DepartmentNotExist() {
        // 准备
        createDTO.setPositionCode("NEW_POS_1");
        createDTO.setDeptId(99999L);

        // 执行和验证
        assertThrows(BusinessException.class, () -> positionService.createPosition(createDTO));
    }

    @Test
    @DisplayName("测试创建职位 - Null部门")
    void testCreatePosition_NullDepartment() {
        // 准备
        createDTO.setPositionCode("NEW_POS_2");
        createDTO.setDeptId(null);

        // 执行
        PositionVO result = positionService.createPosition(createDTO);

        // 验证
        assertNotNull(result);
        assertNull(result.getDeptId());
    }

    @Test
    @DisplayName("测试创建职位 - 带汇报对象")
    void testCreatePosition_WithReportsTo() {
        // 准备
        createDTO.setPositionCode("NEW_POS_3");
        createDTO.setReportsTo(testPosition.getId());

        // 执行
        PositionVO result = positionService.createPosition(createDTO);

        // 验证
        assertNotNull(result);
        assertEquals(testPosition.getId(), result.getReportsTo());
    }

    @Test
    @DisplayName("测试创建职位 - 汇报对象不存在")
    void testCreatePosition_ReportsToNotExist() {
        // 准备
        createDTO.setPositionCode("NEW_POS_4");
        createDTO.setReportsTo(99999L);

        // 执行和验证
        assertThrows(BusinessException.class, () -> positionService.createPosition(createDTO));
    }

    @Test
    @DisplayName("测试创建职位 - 管理岗位")
    void testCreatePosition_ManagerPosition() {
        // 准备
        createDTO.setPositionCode("MANAGER_POS");
        createDTO.setIsManager(1);

        // 执行
        PositionVO result = positionService.createPosition(createDTO);

        // 验证
        assertNotNull(result);
        assertTrue(result.getManager());
    }

    @Test
    @DisplayName("测试创建职位 - 默认值")
    void testCreatePosition_DefaultValues() {
        // 准备
        createDTO.setPositionCode("DEFAULT_POS");
        createDTO.setLevel(null);
        createDTO.setIsManager(null);

        // 执行
        PositionVO result = positionService.createPosition(createDTO);

        // 验证
        assertNotNull(result);
        assertEquals(1, result.getLevel());
        assertFalse(result.getManager());
    }

    @Test
    @DisplayName("测试更新职位 - 成功")
    void testUpdatePosition_Success() {
        // 执行
        PositionVO result = positionService.updatePosition(updateDTO);

        // 验证
        assertNotNull(result);
        assertEquals("UPDATED_POS", result.getPositionCode());
        assertEquals("更新后的职位", result.getPositionName());
        assertEquals("P14", result.getJobGrade());
        assertEquals("更新后的描述", result.getDescription());
    }

    @Test
    @DisplayName("测试更新职位 - 职位不存在")
    void testUpdatePosition_PositionNotExist() {
        // 准备
        updateDTO.setId(99999L);

        // 执行和验证
        assertThrows(BusinessException.class, () -> positionService.updatePosition(updateDTO));
    }

    @Test
    @DisplayName("测试更新职位 - 重复职位编码")
    void testUpdatePosition_DuplicateCode() {
        // 准备
        updateDTO.setPositionCode("CTO");

        // 执行和验证
        assertThrows(BusinessException.class, () -> positionService.updatePosition(updateDTO));
    }

    @Test
    @DisplayName("测试更新职位 - 更新部门")
    void testUpdatePosition_UpdateDepartment() {
        // 创建新部门
        com.qoobot.openidaas.common.dto.department.DepartmentCreateDTO newDeptDTO =
                new com.qoobot.openidaas.common.dto.department.DepartmentCreateDTO();
        newDeptDTO.setDeptCode("NEW_DEPT");
        newDeptDTO.setDeptName("新部门");
        newDeptDTO.setParentId(0L);
        newDeptDTO.setEnabled(true);
        com.qoobot.openidaas.common.vo.department.DepartmentVO newDept = departmentService.createDepartment(newDeptDTO);

        // 更新职位部门
        updateDTO.setDeptId(newDept.getId());

        // 执行
        PositionVO result = positionService.updatePosition(updateDTO);

        // 验证
        assertNotNull(result);
        assertEquals(newDept.getId(), result.getDeptId());
        assertEquals("新部门", result.getDeptName());
    }

    @Test
    @DisplayName("测试更新职位 - 更新汇报对象")
    void testUpdatePosition_UpdateReportsTo() {
        // 创建新的汇报对象
        PositionCreateDTO reportsToDTO = new PositionCreateDTO();
        reportsToDTO.setPositionCode("NEW_REPORTS_TO");
        reportsToDTO.setPositionName("新汇报对象");
        reportsToDTO.setDeptId(testDeptId);
        reportsToDTO.setLevel(20);
        reportsToDTO.setIsManager(1);
        PositionVO reportsTo = positionService.createPosition(reportsToDTO);

        // 更新汇报对象
        updateDTO.setReportsTo(reportsTo.getId());

        // 执行
        PositionVO result = positionService.updatePosition(updateDTO);

        // 验证
        assertNotNull(result);
        assertEquals(reportsTo.getId(), result.getReportsTo());
        assertEquals("新汇报对象", result.getReportsToName());
    }

    @Test
    @DisplayName("测试更新职位 - 汇报对象不能是自己")
    void testUpdatePosition_ReportsToCannotBeSelf() {
        // 准备
        updateDTO.setReportsTo(testPosition.getId());

        // 执行和验证
        assertThrows(BusinessException.class, () -> positionService.updatePosition(updateDTO));
    }

    @Test
    @DisplayName("测试更新职位 - 汇报对象不存在")
    void testUpdatePosition_ReportsToNotExist() {
        // 准备
        updateDTO.setReportsTo(99999L);

        // 执行和验证
        assertThrows(BusinessException.class, () -> positionService.updatePosition(updateDTO));
    }

    @Test
    @DisplayName("测试删除职位 - 成功")
    void testDeletePosition_Success() {
        // 创建一个没有用户的职位
        PositionCreateDTO dto = new PositionCreateDTO();
        dto.setPositionCode("TO_DELETE");
        dto.setPositionName("待删除");
        dto.setDeptId(testDeptId);
        dto.setLevel(10);
        PositionVO created = positionService.createPosition(dto);

        // 执行
        positionService.deletePosition(created.getId());

        // 验证
        Position deleted = positionMapper.selectById(created.getId());
        assertNull(deleted);
    }

    @Test
    @DisplayName("测试删除职位 - 职位不存在")
    void testDeletePosition_PositionNotExist() {
        // 执行和验证
        assertThrows(BusinessException.class, () -> positionService.deletePosition(99999L));
    }

    @Test
    @DisplayName("测试获取职位详情 - 成功")
    void testGetPositionById_Success() {
        // 执行
        PositionVO result = positionService.getPositionById(testPosition.getId());

        // 验证
        assertNotNull(result);
        assertEquals(testPosition.getId(), result.getId());
        assertEquals("TEST_POS", result.getPositionCode());
        assertEquals("测试职位", result.getPositionName());
    }

    @Test
    @DisplayName("测试获取职位详情 - 职位不存在")
    void testGetPositionById_PositionNotExist() {
        // 执行和验证
        assertThrows(BusinessException.class, () -> positionService.getPositionById(99999L));
    }

    @Test
    @DisplayName("测试职位层级排序")
    void testPositionLevelOrdering() {
        // 创建多个不同层级的职位
        positionService.createPosition(new PositionCreateDTO() {{
            setPositionCode("LEVEL_10");
            setPositionName("10级");
            setDeptId(testDeptId);
            setLevel(10);
        }});

        positionService.createPosition(new PositionCreateDTO() {{
            setPositionCode("LEVEL_20");
            setPositionName("20级");
            setDeptId(testDeptId);
            setLevel(20);
        }});

        positionService.createPosition(new PositionCreateDTO() {{
            setPositionCode("LEVEL_15");
            setPositionName("15级");
            setDeptId(testDeptId);
            setLevel(15);
        }});

        // 执行
        List<PositionVO> positions = positionService.getPositionList(testDeptId);

        // 验证排序
        assertNotNull(positions);
        // 检查是否按level升序排列
        for (int i = 1; i < positions.size(); i++) {
            assertTrue(positions.get(i - 1).getLevel() <= positions.get(i).getLevel());
        }
    }

    @Test
    @DisplayName("测试管理岗位标识")
    void testManagerPosition() {
        // 创建管理岗位
        PositionCreateDTO managerDTO = new PositionCreateDTO();
        managerDTO.setPositionCode("MGR");
        managerDTO.setPositionName("经理");
        managerDTO.setDeptId(testDeptId);
        managerDTO.setIsManager(1);
        PositionVO manager = positionService.createPosition(managerDTO);

        // 创建普通岗位
        PositionCreateDTO staffDTO = new PositionCreateDTO();
        staffDTO.setPositionCode("STAFF");
        staffDTO.setPositionName("员工");
        staffDTO.setDeptId(testDeptId);
        staffDTO.setIsManager(0);
        PositionVO staff = positionService.createPosition(staffDTO);

        // 验证
        assertTrue(manager.getManager());
        assertFalse(staff.getManager());
    }

    @Test
    @DisplayName("测试职位关联信息")
    void testPositionRelationInfo() {
        // 创建汇报对象
        PositionCreateDTO reportsToDTO = new PositionCreateDTO();
        reportsToDTO.setPositionCode("LEADER");
        reportsToDTO.setPositionName("领导");
        reportsToDTO.setDeptId(testDeptId);
        reportsToDTO.setLevel(20);
        PositionVO leader = positionService.createPosition(reportsToDTO);

        // 创建下级职位
        PositionCreateDTO subordinateDTO = new PositionCreateDTO();
        subordinateDTO.setPositionCode("MEMBER");
        subordinateDTO.setPositionName("成员");
        subordinateDTO.setDeptId(testDeptId);
        subordinateDTO.setLevel(10);
        subordinateDTO.setReportsTo(leader.getId());
        PositionVO member = positionService.createPosition(subordinateDTO);

        // 获取职位详情，验证关联信息
        PositionVO memberDetail = positionService.getPositionById(member.getId());

        // 验证
        assertNotNull(memberDetail);
        assertEquals(leader.getId(), memberDetail.getReportsTo());
        assertEquals("领导", memberDetail.getReportsToName());
        assertEquals("测试部门", memberDetail.getDeptName());
    }

    @Test
    @DisplayName("测试更新职位 - 部分字段")
    void testUpdatePosition_PartialFields() {
        // 准备
        PositionUpdateDTO partialUpdate = new PositionUpdateDTO();
        partialUpdate.setId(testPosition.getId());
        partialUpdate.setDescription("仅更新描述");

        // 执行
        PositionVO result = positionService.updatePosition(partialUpdate);

        // 验证
        assertNotNull(result);
        assertEquals("仅更新描述", result.getDescription());
    }
}
