package com.qoobot.openidaas.core.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.qoobot.openidaas.core.domain.DictItem;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

/**
 * 字典项Mapper接口
 *
 * @author QooBot
 */
public interface DictItemMapper extends BaseMapper<DictItem> {

    /**
     * 根据字典ID查找字典项列表
     */
    @Select("SELECT * FROM dict_items WHERE dict_id = #{dictId} ORDER BY sort_order")
    List<DictItem> findByDictIdOrderBySortOrder(@Param("dictId") Long dictId);

    /**
     * 根据字典编码查找字典项列表
     * 【需在XML中实现】涉及表关联
     */
    List<DictItem> findByDictCodeOrderBySortOrder(@Param("dictCode") String dictCode);

    /**
     * 根据字典ID和启用状态查找字典项列表
     */
    @Select("SELECT * FROM dict_items WHERE dict_id = #{dictId} AND enabled = 1 ORDER BY sort_order")
    List<DictItem> findByDictIdAndEnabledTrueOrderBySortOrder(@Param("dictId") Long dictId);

    /**
     * 根据字典项标签查找字典项
     */
    @Select("SELECT * FROM dict_items WHERE item_label = #{itemLabel} AND dict_id = #{dictId}")
    DictItem findByItemLabelAndDictId(@Param("itemLabel") String itemLabel, @Param("dictId") Long dictId);

    /**
     * 根据字典项值查找字典项
     */
    @Select("SELECT * FROM dict_items WHERE item_value = #{itemValue} AND dict_id = #{dictId}")
    DictItem findByItemValueAndDictId(@Param("itemValue") String itemValue, @Param("dictId") Long dictId);

    /**
     * 根据字典ID查找字典项分页列表
     */
    IPage<DictItem> findByDictId(Page<DictItem> page, @Param("dictId") Long dictId);

    /**
     * 根据启用状态查找字典项列表（分页）
     */
    IPage<DictItem> findByEnabled(Page<DictItem> page, @Param("enabled") Boolean enabled);

    /**
     * 根据字典项标签模糊查询（分页）
     */
    IPage<DictItem> findByItemLabelContainingAndDictId(Page<DictItem> page, @Param("itemLabel") String itemLabel, @Param("dictId") Long dictId);

    /**
     * 根据字典项值模糊查询（分页）
     */
    IPage<DictItem> findByItemValueContainingAndDictId(Page<DictItem> page, @Param("itemValue") String itemValue, @Param("dictId") Long dictId);

    /**
     * 查找启用的字典项列表
     */
    @Select("SELECT * FROM dict_items WHERE dict_id = #{dictId} AND enabled = 1 ORDER BY sort_order")
    List<DictItem> findEnabledItemsByDictId(@Param("dictId") Long dictId);

    /**
     * 统计字典项数量
     */
    @Select("SELECT COUNT(*) FROM dict_items WHERE dict_id = #{dictId}")
    long countByDictId(@Param("dictId") Long dictId);

    /**
     * 统计启用字典项数量
     */
    @Select("SELECT COUNT(*) FROM dict_items WHERE dict_id = #{dictId} AND enabled = 1")
    long countByDictIdAndEnabledTrue(@Param("dictId") Long dictId);

    /**
     * 检查字典项值是否已存在
     */
    @Select("SELECT COUNT(*) > 0 FROM dict_items WHERE item_value = #{itemValue} AND dict_id = #{dictId}")
    boolean existsByItemValueAndDictId(@Param("itemValue") String itemValue, @Param("dictId") Long dictId);
}
