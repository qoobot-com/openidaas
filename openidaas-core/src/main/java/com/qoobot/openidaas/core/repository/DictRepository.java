package com.qoobot.openidaas.core.repository;

import com.qoobot.openidaas.core.domain.Dict;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

/**
 * 字典仓储接口
 *
 * @author QooBot
 */
public interface DictRepository extends BaseRepository<Dict, Long> {

    /**
     * 根据字典编码查找字典
     */
    Optional<Dict> findByDictCode(String dictCode);

    /**
     * 根据字典名称查找字典
     */
    Optional<Dict> findByDictName(String dictName);

    /**
     * 根据启用状态查找字典列表
     */
    Page<Dict> findByEnabled(Boolean enabled, Pageable pageable);

    /**
     * 根据字典名称模糊查询
     */
    @Query("SELECT d FROM Dict d WHERE d.dictName LIKE %:dictName%")
    Page<Dict> findByDictNameContaining(@Param("dictName") String dictName, Pageable pageable);

    /**
     * 根据字典编码模糊查询
     */
    @Query("SELECT d FROM Dict d WHERE d.dictCode LIKE %:dictCode%")
    Page<Dict> findByDictCodeContaining(@Param("dictCode") String dictCode, Pageable pageable);

    /**
     * 查找启用的字典列表
     */
    List<Dict> findByEnabledTrueOrderBySortOrder();

    /**
     * 统计字典数量
     */
    long count();

    /**
     * 统计启用字典数量
     */
    long countByEnabledTrue();

    /**
     * 检查字典编码是否已存在
     */
    boolean existsByDictCode(String dictCode);
}