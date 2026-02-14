package com.qoobot.openidaas.core.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.qoobot.openidaas.core.domain.Menu;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

/**
 * 菜单Mapper接口
 *
 * @author QooBot
 */
public interface MenuMapper extends BaseMapper<Menu> {

    /**
     * 根据菜单名称查找菜单
     */
    @Select("SELECT * FROM menus WHERE menu_name = #{menuName}")
    Menu findByMenuName(@Param("menuName") String menuName);

    /**
     * 根据菜单路径查找菜单
     */
    @Select("SELECT * FROM menus WHERE menu_path = #{menuPath}")
    Menu findByMenuPath(@Param("menuPath") String menuPath);

    /**
     * 根据父菜单ID查找子菜单列表
     */
    @Select("SELECT * FROM menus WHERE parent_id = #{parentId} ORDER BY sort_order")
    List<Menu> findByParentIdOrderBySortOrder(@Param("parentId") Long parentId);

    /**
     * 查找顶级菜单列表
     */
    @Select("SELECT * FROM menus WHERE parent_id IS NULL ORDER BY sort_order")
    List<Menu> findTopMenus();

    /**
     * 根据菜单类型查找菜单列表
     */
    @Select("SELECT * FROM menus WHERE menu_type = #{menuType}")
    List<Menu> findByMenuType(@Param("menuType") String menuType);

    /**
     * 根据启用状态查找菜单列表
     */
    @Select("SELECT * FROM menus WHERE enabled = #{enabled}")
    List<Menu> findByEnabled(@Param("enabled") Boolean enabled);

    /**
     * 根据隐藏状态查找菜单列表
     */
    @Select("SELECT * FROM menus WHERE hidden = #{hidden}")
    List<Menu> findByHidden(@Param("hidden") Boolean hidden);

    /**
     * 根据权限ID查找菜单
     */
    @Select("SELECT * FROM menus WHERE permission_obj_id = #{permissionId}")
    Menu findByPermissionObjId(@Param("permissionId") Long permissionId);

    /**
     * 根据菜单名称模糊查询（分页）
     */
    IPage<Menu> findByMenuNameContaining(Page<Menu> page, @Param("menuName") String menuName);

    /**
     * 根据菜单路径模糊查询（分页）
     */
    IPage<Menu> findByMenuPathContaining(Page<Menu> page, @Param("menuPath") String menuPath);

    /**
     * 查找可见且启用的菜单
     */
    @Select("SELECT * FROM menus WHERE enabled = 1 AND hidden = 0 ORDER BY parent_id, sort_order")
    List<Menu> findVisibleMenus();

    /**
     * 根据父菜单ID查找可见的子菜单
     */
    @Select("SELECT * FROM menus WHERE parent_id = #{parentId} AND enabled = 1 AND hidden = 0 ORDER BY sort_order")
    List<Menu> findVisibleChildrenByParentId(@Param("parentId") Long parentId);

    /**
     * 查找用户有权访问的菜单
     * 【需在XML中实现】涉及多表关联查询
     */
    List<Menu> findMenusByUserId(@Param("userId") Long userId);

    /**
     * 根据角色ID查找菜单列表
     * 【需在XML中实现】涉及多表关联查询
     */
    List<Menu> findMenusByRoleId(@Param("roleId") Long roleId);

    /**
     * 统计菜单数量
     */
    @Select("SELECT COUNT(*) FROM menus")
    long count();

    /**
     * 统计启用菜单数量
     */
    @Select("SELECT COUNT(*) FROM menus WHERE enabled = 1")
    long countByEnabledTrue();

    /**
     * 检查菜单路径是否已存在
     */
    @Select("SELECT COUNT(*) > 0 FROM menus WHERE menu_path = #{menuPath}")
    boolean existsByMenuPath(@Param("menuPath") String menuPath);

    /**
     * 根据ID集合查询菜单
     */
    @Select("<script>" +
            "SELECT * FROM menus WHERE id IN " +
            "<foreach item='id' collection='ids' open='(' separator=',' close=')'>" +
            "#{id}" +
            "</foreach>" +
            "</script>")
    List<Menu> findByIdIn(@Param("ids") List<Long> ids);
}
