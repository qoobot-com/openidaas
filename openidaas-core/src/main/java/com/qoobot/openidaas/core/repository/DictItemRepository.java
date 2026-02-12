package com.qoobot.openidaas.core.repository;

import com.qoobot.openidaas.core.domain.DictItem;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

/**
 * 字典项仓储接口
 *
 * @author QooBot
 */
public interface DictItemRepository extends BaseRepository<DictItem, Long> {

    /**
     * 根据字典ID查找字典项列表
     */
    List<DictItem> findByDictIdOrderBySortOrder(Long dictId);

    /**
     * 根据字典编码查找字典项列表
     */
    @Query("SELECT di FROM DictItem di JOIN di.dict d WHERE d.dictCode = :dictCode ORDER BY di.sortOrder")
    List<DictItem> findByDictCodeOrderBySortOrder(@Param("dictCode") String dictCode);

    /**
     * 根据字典ID和启用状态查找字典项列表
     */
    List<DictItem> findByDictIdAndEnabledTrueOrderBySortOrder(Long dictId);

    /**
     * 根据字典项标签查找字典项
     */
    Optional<DictItem> findByItemLabelAndDictId(String itemLabel, Long dictId);

    /**
     * 根据字典项值查找字典项
     */
    Optional<DictItem> findByItemValueAndDictId(String itemValue, Long dictId);

    /**
     * 根据字典ID查找字典项分页列表
     */
    Page<DictItem> findByDictId(Long dictId, Pageable pageable);

    /**
     * 根据启用状态查找字典项列表
     */
    Page<DictItem> findByEnabled(Boolean enabled, Pageable pageable);

    /**
     * 根据字典项标签模糊查询
     */
    @Query("SELECT di FROM DictItem di WHERE di.itemLabel LIKE %:itemLabel% AND di.dictId = :dictId")
    Page<DictItem> findByItemLabelContainingAndDictId(@Param("itemLabel") String itemLabel, @Param("dictId") Long dictId, Pageable pageable);

    /**
     * 根据字典项值模糊查询
     */
    @Query("SELECT di FROM DictItem di WHERE di.itemValue LIKE %:itemValue% AND di.dictId = :dictId")
    Page<DictItem> findByItemValueContainingAndDictId(@Param("itemValue") String itemValue, @Param("dictId") Long dictId, Pageable pageable);

    /**
     * 查找启用的字典项列表
     */
    @Query("SELECT di FROM DictItem di WHERE di.dictId = :dictId AND di.enabled = true ORDER BY di.sortOrder")
    List<DictItem> findEnabledItemsByDictId(@Param("dictId") Long dictId);

    /**
     * 统计字典项数量
     */
    long countByDictId(Long dictId);

    /**
     * 统计启用字典项数量
     */
    long countByDictIdAndEnabledTrue(Long dictId);

    /**
     * 检查字典项值是否已存在
     */
    boolean existsByItemValueAndDictId(String itemValue, Long dictId);
}