package com.qoobot.openidaas.core.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.qoobot.openidaas.core.domain.Dict;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

/**
 * 字典Mapper接口
 *
 * @author QooBot
 */
public interface DictMapper extends BaseMapper<Dict> {

    /**
     * 根据字典编码查找字典
     */
    @Select("SELECT * FROM dicts WHERE dict_code = #{dictCode}")
    Dict findByDictCode(@Param("dictCode") String dictCode);

    /**
     * 根据字典名称查找字典
     */
    @Select("SELECT * FROM dicts WHERE dict_name = #{dictName}")
    Dict findByDictName(@Param("dictName") String dictName);

    /**
     * 根据启用状态查找字典列表（分页）
     */
    IPage<Dict> findByEnabled(Page<Dict> page, @Param("enabled") Boolean enabled);

    /**
     * 根据字典名称模糊查询（分页）
     */
    IPage<Dict> findByDictNameContaining(Page<Dict> page, @Param("dictName") String dictName);

    /**
     * 根据字典编码模糊查询（分页）
     */
    IPage<Dict> findByDictCodeContaining(Page<Dict> page, @Param("dictCode") String dictCode);

    /**
     * 查找启用的字典列表
     */
    @Select("SELECT * FROM dicts WHERE enabled = 1 ORDER BY sort_order")
    List<Dict> findByEnabledTrueOrderBySortOrder();

    /**
     * 统计字典数量
     */
    @Select("SELECT COUNT(*) FROM dicts")
    long count();

    /**
     * 统计启用字典数量
     */
    @Select("SELECT COUNT(*) FROM dicts WHERE enabled = 1")
    long countByEnabledTrue();

    /**
     * 检查字典编码是否已存在
     */
    @Select("SELECT COUNT(*) > 0 FROM dicts WHERE dict_code = #{dictCode}")
    boolean existsByDictCode(@Param("dictCode") String dictCode);
}
