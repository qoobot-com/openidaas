package com.qoobot.openidaas.core.repository;

import com.qoobot.openidaas.core.domain.Menu;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;
import java.util.Set;

/**
 * 菜单仓储接口
 *
 * @author QooBot
 */
public interface MenuRepository extends BaseRepository<Menu, Long> {

    /**
     * 根据菜单名称查找菜单
     */
    Optional<Menu> findByMenuName(String menuName);

    /**
     * 根据菜单路径查找菜单
     */
    Optional<Menu> findByMenuPath(String menuPath);

    /**
     * 根据父菜单ID查找子菜单列表
     */
    List<Menu> findByParentIdOrderBySortOrder(Long parentId);

    /**
     * 查找顶级菜单列表
     */
    @Query("SELECT m FROM Menu m WHERE m.parentId IS NULL ORDER BY m.sortOrder")
    List<Menu> findTopMenus();

    /**
     * 根据菜单类型查找菜单列表
     */
    List<Menu> findByMenuType(String menuType);

    /**
     * 根据启用状态查找菜单列表
     */
    List<Menu> findByEnabled(Boolean enabled);

    /**
     * 根据隐藏状态查找菜单列表
     */
    List<Menu> findByHidden(Boolean hidden);

    /**
     * 根据权限ID查找菜单
     */
    Optional<Menu> findByPermissionObjId(Long permissionId);

    /**
     * 根据菜单名称模糊查询
     */
    @Query("SELECT m FROM Menu m WHERE m.menuName LIKE %:menuName%")
    Page<Menu> findByMenuNameContaining(@Param("menuName") String menuName, Pageable pageable);

    /**
     * 根据菜单路径模糊查询
     */
    @Query("SELECT m FROM Menu m WHERE m.menuPath LIKE %:menuPath%")
    Page<Menu> findByMenuPathContaining(@Param("menuPath") String menuPath, Pageable pageable);

    /**
     * 查找可见且启用的菜单
     */
    @Query("SELECT m FROM Menu m WHERE m.enabled = true AND m.hidden = false ORDER BY m.parentId, m.sortOrder")
    List<Menu> findVisibleMenus();

    /**
     * 根据父菜单ID查找可见的子菜单
     */
    @Query("SELECT m FROM Menu m WHERE m.parentId = :parentId AND m.enabled = true AND m.hidden = false ORDER BY m.sortOrder")
    List<Menu> findVisibleChildrenByParentId(@Param("parentId") Long parentId);

    /**
     * 查找用户有权访问的菜单
     */
    @Query("SELECT DISTINCT m FROM Menu m " +
           "JOIN m.permissionObj p " +
           "JOIN p.roles r " +
           "JOIN r.users u " +
           "WHERE u.id = :userId AND m.enabled = true AND m.hidden = false " +
           "ORDER BY m.parentId, m.sortOrder")
    List<Menu> findMenusByUserId(@Param("userId") Long userId);

    /**
     * 根据角色ID查找菜单列表
     */
    @Query("SELECT DISTINCT m FROM Menu m " +
           "JOIN m.permissionObj p " +
           "JOIN p.roles r " +
           "WHERE r.id = :roleId AND m.enabled = true " +
           "ORDER BY m.parentId, m.sortOrder")
    List<Menu> findMenusByRoleId(@Param("roleId") Long roleId);

    /**
     * 根据菜单ID集合查找菜单列表
     */
    List<Menu> findByIdIn(Set<Long> menuIds);

    /**
     * 统计菜单数量
     */
    long count();

    /**
     * 统计启用菜单数量
     */
    long countByEnabledTrue();

    /**
     * 检查菜单路径是否已存在
     */
    boolean existsByMenuPath(String menuPath);
}