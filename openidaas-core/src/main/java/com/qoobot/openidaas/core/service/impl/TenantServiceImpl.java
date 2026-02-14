package com.qoobot.openidaas.core.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.qoobot.openidaas.core.domain.Tenant;
import com.qoobot.openidaas.core.mapper.TenantMapper;
import com.qoobot.openidaas.core.service.TenantService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 租户服务实现类（MyBatis-Plus版本）
 *
 * @author QooBot
 */
@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class TenantServiceImpl implements TenantService {

    private final TenantMapper tenantMapper;

    @Override
    @Transactional
    public Tenant createTenant(Tenant tenant) {
        tenant.setCreatedAt(LocalDateTime.now());
        tenant.setUpdatedAt(LocalDateTime.now());
        tenant.setEnabled(true);
        tenant.setStatus("ACTIVE");
        tenantMapper.insert(tenant);
        return tenant;
    }

    @Override
    @Transactional
    public Tenant updateTenant(Tenant tenant) {
        tenant.setUpdatedAt(LocalDateTime.now());
        tenantMapper.updateById(tenant);
        return tenant;
    }

    @Override
    @Transactional
    public void deleteTenant(Long tenantId) {
        tenantMapper.deleteById(tenantId);
    }

    @Override
    public Tenant getTenantById(Long tenantId) {
        return tenantMapper.selectById(tenantId);
    }

    @Override
    public Tenant getTenantByCode(String tenantCode) {
        return tenantMapper.findByTenantCode(tenantCode);
    }

    @Override
    public Tenant getTenantByDomain(String domain) {
        return tenantMapper.findByDomain(domain);
    }

    @Override
    public List<Tenant> getAllTenants() {
        return tenantMapper.selectList(null);
    }

    @Override
    public List<Tenant> getEnabledTenants() {
        LambdaQueryWrapper<Tenant> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Tenant::getEnabled, true);
        return tenantMapper.selectList(wrapper);
    }

    @Override
    @Transactional
    public void enableTenant(Long tenantId) {
        Tenant tenant = getTenantById(tenantId);
        if (tenant != null) {
            tenant.setEnabled(true);
            tenant.setStatus("ACTIVE");
            tenant.setUpdatedAt(LocalDateTime.now());
            tenantMapper.updateById(tenant);
        }
    }

    @Override
    @Transactional
    public void disableTenant(Long tenantId) {
        Tenant tenant = getTenantById(tenantId);
        if (tenant != null) {
            tenant.setEnabled(false);
            tenant.setStatus("DISABLED");
            tenant.setUpdatedAt(LocalDateTime.now());
            tenantMapper.updateById(tenant);
        }
    }

    @Override
    public boolean isTenantExpired(Long tenantId) {
        Tenant tenant = getTenantById(tenantId);
        if (tenant == null) {
            return true;
        }
        return tenant.getExpireTime() != null && tenant.getExpireTime().isBefore(LocalDateTime.now());
    }

    @Override
    @Transactional
    public void updateUsedUsers(Long tenantId, Integer usedUsers) {
        tenantMapper.updateUsedUsers(tenantId, usedUsers);
    }

    @Override
    @Transactional
    public void updateUsedApps(Long tenantId, Integer usedApps) {
        tenantMapper.updateUsedApps(tenantId, usedApps);
    }
}
