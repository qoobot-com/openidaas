package com.qoobot.openidaas.organization.service;

import com.qoobot.openidaas.organization.entity.Organization;
import com.qoobot.openidaas.organization.mapper.OrganizationMapper;
import com.qoobot.openidaas.organization.service.impl.OrganizationServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.util.Arrays;
import java.util.Collections;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

/**
 * 组织服务单元测试
 *
 * @author Qoobot
 * @version 1.0.0
 */
class OrganizationServiceUnitTest {

    @Mock
    private OrganizationMapper organizationMapper;

    @InjectMocks
    private OrganizationServiceImpl organizationService;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void testCreateOrganization_Success() {
        // 准备测试数据
        Organization organization = new Organization();
        organization.setName("测试组织");
        organization.setCode("TEST_ORG");
        organization.setType("DEPARTMENT");

        // 模拟行为
        when(organizationMapper.selectCount(any())).thenReturn(0L);
        when(organizationMapper.insert(any(Organization.class))).thenReturn(1);
        
        // 执行测试
        Long result = organizationService.createOrganization(organization);
        
        // 验证结果
        assertNotNull(result);
        assertEquals("测试组织", organization.getName());
        assertEquals("TEST_ORG", organization.getCode());
        assertEquals(1, organization.getLevel().intValue());
        assertEquals("ENABLED", organization.getStatus());
        
        // 验证调用
        verify(organizationMapper).selectCount(any());
        verify(organizationMapper).insert(any(Organization.class));
    }

    @Test
    void testGetOrganizationById() {
        // 准备测试数据
        Organization organization = new Organization();
        organization.setId(1L);
        organization.setName("测试组织");
        organization.setCode("TEST_ORG");

        // 模拟行为
        when(organizationMapper.selectById(1L)).thenReturn(organization);

        // 执行测试
        Organization result = organizationService.getOrganizationById(1L);

        // 验证结果
        assertNotNull(result);
        assertEquals(1L, result.getId());
        assertEquals("测试组织", result.getName());
        assertEquals("TEST_ORG", result.getCode());

        // 验证调用
        verify(organizationMapper).selectById(1L);
    }

    @Test
    void testIsCodeUnique_True() {
        // 模拟行为 - 编码唯一
        when(organizationMapper.selectCount(any())).thenReturn(0L);

        // 执行测试
        boolean result = organizationService.isCodeUnique("UNIQUE_CODE", null);

        // 验证结果
        assertTrue(result);

        // 验证调用
        verify(organizationMapper).selectCount(any());
    }

    @Test
    void testIsCodeUnique_False() {
        // 模拟行为 - 编码不唯一
        when(organizationMapper.selectCount(any())).thenReturn(1L);

        // 执行测试
        boolean result = organizationService.isCodeUnique("EXISTING_CODE", null);

        // 验证结果
        assertFalse(result);

        // 验证调用
        verify(organizationMapper).selectCount(any());
    }

    @Test
    void testGetUserOrganizations() {
        // 准备测试数据
        Organization org1 = new Organization();
        org1.setId(1L);
        org1.setName("组织1");
        
        Organization org2 = new Organization();
        org2.setId(2L);
        org2.setName("组织2");

        // 模拟行为
        when(organizationMapper.selectByUserId(1L)).thenReturn(Arrays.asList(org1, org2));

        // 执行测试
        var result = organizationService.getUserOrganizations(1L);

        // 验证结果
        assertNotNull(result);
        assertEquals(2, result.size());
        assertEquals("组织1", result.get(0).getName());
        assertEquals("组织2", result.get(1).getName());

        // 验证调用
        verify(organizationMapper).selectByUserId(1L);
    }

    @Test
    void testGetOrganizationTree_Empty() {
        // 模拟行为
        when(organizationMapper.selectByParentId(0L)).thenReturn(Collections.emptyList());

        // 执行测试
        var result = organizationService.getOrganizationTree(null);

        // 验证结果
        assertNotNull(result);
        assertTrue(result.isEmpty());

        // 验证调用
        verify(organizationMapper).selectByParentId(0L);
    }

}