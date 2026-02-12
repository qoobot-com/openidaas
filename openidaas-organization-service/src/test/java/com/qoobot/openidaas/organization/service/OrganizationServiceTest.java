package com.qoobot.openidaas.organization.service;

import com.qoobot.openidaas.organization.entity.Organization;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * 组织服务测试类
 *
 * @author Qoobot
 * @version 1.0.0
 */
@SpringBootTest(classes = com.qoobot.openidaas.organization.OpenIDaaSOrganizationApplication.class)
@ActiveProfiles("test")
@Transactional
class OrganizationServiceTest {

    @Autowired
    private OrganizationService organizationService;

    @Test
    void testCreateOrganization() {
        Organization organization = new Organization();
        organization.setName("测试组织");
        organization.setCode("TEST_ORG");
        organization.setType("DEPARTMENT");
        organization.setDescription("测试组织描述");

        Long id = organizationService.createOrganization(organization);
        assertNotNull(id);

        Organization saved = organizationService.getOrganizationById(id);
        assertEquals("测试组织", saved.getName());
        assertEquals("TEST_ORG", saved.getCode());
        assertEquals(1, saved.getLevel().intValue());
    }

    @Test
    void testGetOrganizationTree() {
        // 创建根组织
        Organization rootOrg = new Organization();
        rootOrg.setName("根组织");
        rootOrg.setCode("ROOT");
        rootOrg.setType("COMPANY");
        Long rootId = organizationService.createOrganization(rootOrg);

        // 创建子组织
        Organization childOrg = new Organization();
        childOrg.setName("子组织");
        childOrg.setCode("CHILD");
        childOrg.setType("DEPARTMENT");
        childOrg.setParentId(rootId);
        Long childId = organizationService.createOrganization(childOrg);

        // 获取组织树
        List<Organization> tree = organizationService.getOrganizationTree(null);
        assertNotNull(tree);
        assertFalse(tree.isEmpty());
        
        Organization rootInTree = tree.get(0);
        assertEquals("根组织", rootInTree.getName());
        assertTrue(rootInTree.getLeaf() != null && !rootInTree.getLeaf());
        assertTrue(rootInTree.getChildrenCount() > 0);
    }

    @Test
    void testCodeUniqueness() {
        Organization org1 = new Organization();
        org1.setName("组织1");
        org1.setCode("UNIQUE_CODE");
        Long id1 = organizationService.createOrganization(org1);

        // 测试编码唯一性
        boolean isUnique = organizationService.isCodeUnique("UNIQUE_CODE", null);
        assertFalse(isUnique);

        boolean isUniqueWithExclude = organizationService.isCodeUnique("UNIQUE_CODE", id1);
        assertTrue(isUniqueWithExclude);

        // 测试不同的编码
        boolean isDifferentUnique = organizationService.isCodeUnique("DIFFERENT_CODE", null);
        assertTrue(isDifferentUnique);
    }

    @Test
    void testMoveOrganization() {
        // 创建父组织
        Organization parent1 = new Organization();
        parent1.setName("父组织1");
        parent1.setCode("PARENT1");
        Long parent1Id = organizationService.createOrganization(parent1);

        Organization parent2 = new Organization();
        parent2.setName("父组织2");
        parent2.setCode("PARENT2");
        Long parent2Id = organizationService.createOrganization(parent2);

        // 创建子组织
        Organization child = new Organization();
        child.setName("子组织");
        child.setCode("CHILD");
        child.setParentId(parent1Id);
        Long childId = organizationService.createOrganization(child);

        // 移动组织
        boolean moved = organizationService.moveOrganization(childId, parent2Id);
        assertTrue(moved);

        Organization movedOrg = organizationService.getOrganizationById(childId);
        assertEquals(parent2Id, movedOrg.getParentId());
        assertEquals(2, movedOrg.getLevel().intValue());
    }

    @Test
    void testEnableDisableOrganization() {
        // 创建组织
        Organization org = new Organization();
        org.setName("测试启用禁用组织");
        org.setCode("ENABLE_DISABLE_TEST");
        Long id = organizationService.createOrganization(org);

        // 禁用组织
        boolean disabled = organizationService.disableOrganization(id);
        assertTrue(disabled);

        Organization disabledOrg = organizationService.getOrganizationById(id);
        assertEquals("DISABLED", disabledOrg.getStatus());

        // 启用组织
        boolean enabled = organizationService.enableOrganization(id);
        assertTrue(enabled);

        Organization enabledOrg = organizationService.getOrganizationById(id);
        assertEquals("ENABLED", enabledOrg.getStatus());
    }

    @Test
    void testBatchDeleteOrganizations() {
        // 创建多个组织
        Organization org1 = new Organization();
        org1.setName("批量删除测试1");
        org1.setCode("BATCH_DEL_1");
        Long id1 = organizationService.createOrganization(org1);

        Organization org2 = new Organization();
        org2.setName("批量删除测试2");
        org2.setCode("BATCH_DEL_2");
        Long id2 = organizationService.createOrganization(org2);

        // 批量删除
        boolean deleted = organizationService.batchDeleteOrganizations(Arrays.asList(id1, id2));
        assertTrue(deleted);

        // 验证已删除
        assertNull(organizationService.getOrganizationById(id1));
        assertNull(organizationService.getOrganizationById(id2));
    }

}