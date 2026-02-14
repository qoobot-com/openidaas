# JPA到MyBatis-Plus迁移指南

## 目录
1. [迁移概述](#迁移概述)
2. [Maven依赖配置](#maven依赖配置)
3. [实体类注解调整](#实体类注解调整)
4. [数据层接口改造](#数据层接口改造)
5. [查询条件构建方式转换](#查询条件构建方式转换)
6. [事务管理配置变更](#事务管理配置变更)
7. [多对多关系处理](#多对多关系处理)
8. [分页查询改造](#分页查询改造)
9. [动态查询改造](#动态查询改造)
10. [性能优化建议](#性能优化建议)

---

## 迁移概述

### 1.1 项目现状

OpenIDaaS项目当前采用混合ORM架构：
- **openidaas-core模块**：使用Spring Data JPA（12个实体，11个Repository）
- **业务服务模块**：已使用MyBatis-Plus

### 1.2 迁移目标

将`openidaas-core`模块从JPA完全迁移到MyBatis-Plus，实现：
- ✅ 统一持久层框架
- ✅ 保持业务功能不变
- ✅ 保持性能水平或提升
- ✅ 简化维护成本

### 1.3 迁移范围

| 模块 | 实体数 | Repository数 | 复杂度 |
|------|--------|-------------|--------|
| User | 1 | 1 | 高 |
| Role | 1 | 1 | 高 |
| Permission | 1 | 1 | 高 |
| Menu | 1 | 1 | 中 |
| Tenant | 1 | 1 | 中 |
| Department | 1 | 1 | 中 |
| Dict | 2 | 2 | 低 |
| DictItem | 1 | 1 | 低 |
| Application | 1 | 1 | 低 |
| AuditLog | 1 | 1 | 低 |
| DataPermission | 1 | 1 | 低 |
| **合计** | **12** | **11** | - |

---

## Maven依赖配置

### 2.1 移除JPA依赖

在`openidaas-core/pom.xml`中：

```xml
<!-- ❌ 移除以下依赖 -->
<dependency>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter-data-jpa</artifactId>
</dependency>
```

### 2.2 添加MyBatis-Plus依赖

```xml
<!-- ✅ 添加MyBatis-Plus依赖 -->
<dependency>
    <groupId>com.baomidou</groupId>
    <artifactId>mybatis-plus-boot-starter</artifactId>
</dependency>

<!-- MySQL驱动（保留） -->
<dependency>
    <groupId>com.mysql</groupId>
    <artifactId>mysql-connector-j</artifactId>
</dependency>
```

### 2.3 完整的openidaas-core/pom.xml

```xml
<?xml version="1.0" encoding="UTF-8"?>
<project xmlns="http://maven.apache.org/POM/4.0.0"
         xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
         xsi:schemaLocation="http://maven.apache.org/POM/4.0.0 
         https://maven.apache.org/xsd/maven-4.0.0.xsd">
    <modelVersion>4.0.0</modelVersion>
    
    <parent>
        <groupId>com.qoobot</groupId>
        <artifactId>openidaas</artifactId>
        <version>1.0.0-SNAPSHOT</version>
    </parent>
    
    <artifactId>openidaas-core</artifactId>
    <packaging>jar</packaging>
    
    <name>OpenIDaaS Core</name>
    <description>Core domain models and business logic for OpenIDaaS</description>
    
    <dependencies>
        <!-- Project Dependencies -->
        <dependency>
            <groupId>com.qoobot</groupId>
            <artifactId>openidaas-common</artifactId>
            <version>${project.version}</version>
        </dependency>
        
        <!-- ✅ MyBatis-Plus（替代JPA） -->
        <dependency>
            <groupId>com.baomidou</groupId>
            <artifactId>mybatis-plus-boot-starter</artifactId>
        </dependency>
        
        <!-- MySQL Driver -->
        <dependency>
            <groupId>com.mysql</groupId>
            <artifactId>mysql-connector-j</artifactId>
        </dependency>
        
        <!-- Redis -->
        <dependency>
            <groupId>org.springframework.boot</groupId>
            <artifactId>spring-boot-starter-data-redis</artifactId>
        </dependency>
        
        <dependency>
            <groupId>org.redisson</groupId>
            <artifactId>redisson-spring-boot-starter</artifactId>
        </dependency>
        
        <!-- Security -->
        <dependency>
            <groupId>org.springframework.boot</groupId>
            <artifactId>spring-boot-starter-security</artifactId>
        </dependency>
        
        <!-- JWT -->
        <dependency>
            <groupId>com.nimbusds</groupId>
            <artifactId>nimbus-jose-jwt</artifactId>
        </dependency>
        
        <!-- Password Encryption -->
        <dependency>
            <groupId>org.mindrot</groupId>
            <artifactId>jbcrypt</artifactId>
        </dependency>
        
        <dependency>
            <groupId>org.bouncycastle</groupId>
            <artifactId>bcprov-jdk18on</artifactId>
        </dependency>
        
        <dependency>
            <groupId>de.mkammerer</groupId>
            <artifactId>argon2-jvm</artifactId>
        </dependency>
        
        <!-- Lombok -->
        <dependency>
            <groupId>org.projectlombok</groupId>
            <artifactId>lombok</artifactId>
            <optional>true</optional>
        </dependency>
        
        <!-- Test Dependencies -->
        <dependency>
            <groupId>org.springframework.boot</groupId>
            <artifactId>spring-boot-starter-test</artifactId>
            <scope>test</scope>
        </dependency>
        
        <dependency>
            <groupId>org.springframework.security</groupId>
            <artifactId>spring-security-test</artifactId>
            <scope>test</scope>
        </dependency>
    </dependencies>
</project>
```

---

## 实体类注解调整

### 3.1 注解映射对照表

| JPA注解 | MyBatis-Plus注解 | 说明 |
|---------|-----------------|------|
| `@Entity` | `@TableName` | 表名映射 |
| `@Table(name="xxx")` | `@TableName("xxx")` | 表名映射 |
| `@Id` | `@TableId` | 主键标识 |
| `@GeneratedValue` | `@TableId(type=...)` | 主键生成策略 |
| `@Column(name="xxx")` | `@TableField("xxx")` | 字段名映射 |
| `@Transient` | `@TableField(exist=false)` | 非数据库字段 |
| `@Enumerated` | 无特殊处理 | 枚举类型存储 |
| `@ManyToOne` | 需手动处理 | 多对一关系 |
| `@OneToMany` | 需手动处理 | 一对多关系 |
| `@ManyToMany` | 需手动处理 | 多对多关系 |
| `@JoinTable` | 需创建中间表实体 | 中间表映射 |

### 3.2 主键生成策略对照

```java
// JPA方式
@Id
@GeneratedValue(strategy = GenerationType.IDENTITY)
private Long id;

// MyBatis-Plus方式
@TableId(type = IdType.AUTO)        // 数据库自增
private Long id;

// 其他主键策略
@TableId(type = IdType.INPUT)       // 用户输入
@TableId(type = IdType.ASSIGN_ID)   // 雪花算法（默认）
@TableId(type = IdType.ASSIGN_UUID) // UUID
@TableId(type = IdType.NONE)        // 无策略
```

### 3.3 实体类完整改造示例

#### 示例1：User实体改造

**JPA版本（修改前）**
```java
package com.qoobot.openidaas.core.domain;

import com.qoobot.openidaas.common.entity.BaseEntity;
import com.qoobot.openidaas.common.enumeration.UserStatusEnum;
import jakarta.persistence.*;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.time.LocalDateTime;
import java.util.Set;

@Data
@EqualsAndHashCode(callSuper = true)
@Entity
@Table(name = "users")
public class User extends BaseEntity {

    @Column(name = "username", length = 50, nullable = false, unique = true)
    private String username;

    @Column(name = "email", length = 100, unique = true)
    private String email;

    @Column(name = "mobile", length = 20, unique = true)
    private String mobile;

    @Column(name = "password", length = 255)
    private String password;

    @Column(name = "nickname", length = 50)
    private String nickname;

    @Column(name = "real_name", length = 50)
    private String realName;

    @Column(name = "avatar", length = 255)
    private String avatar;

    @Column(name = "gender")
    private Integer gender;

    @Column(name = "birthday")
    private LocalDateTime birthday;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", length = 20)
    private UserStatusEnum status;

    @Column(name = "last_login_time")
    private LocalDateTime lastLoginTime;

    @Column(name = "last_login_ip", length = 50)
    private String lastLoginIp;

    @Column(name = "login_fail_count")
    private Integer loginFailCount = 0;

    @Column(name = "lock_time")
    private LocalDateTime lockTime;

    @Column(name = "pwd_expire_time")
    private LocalDateTime pwdExpireTime;

    @Column(name = "pwd_reset_required")
    private Boolean pwdResetRequired = false;

    @Column(name = "pwd_reset_time")
    private LocalDateTime pwdResetTime;

    @Column(name = "pwd_reset_by")
    private Long pwdResetBy;

    // 多对多关系（需要特殊处理）
    @ManyToMany(fetch = FetchType.LAZY)
    @JoinTable(
        name = "user_roles",
        joinColumns = @JoinColumn(name = "user_id"),
        inverseJoinColumns = @JoinColumn(name = "role_id")
    )
    private Set<Role> roles;

    @ManyToMany(fetch = FetchType.LAZY)
    @JoinTable(
        name = "user_departments",
        joinColumns = @JoinColumn(name = "user_id"),
        inverseJoinColumns = @JoinColumn(name = "dept_id")
    )
    private Set<Department> departments;

    @ManyToMany(fetch = FetchType.LAZY)
    @JoinTable(
        name = "user_permissions",
        joinColumns = @JoinColumn(name = "user_id"),
        inverseJoinColumns = @JoinColumn(name = "perm_id")
    )
    private Set<Permission> permissions;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "tenant_id")
    private Tenant tenant;
    
    public Boolean getEnabled() {
        return this.status == UserStatusEnum.ACTIVE || this.status == UserStatusEnum.NORMAL;
    }
    
    public void setEnabled(Boolean enabled) {
        this.status = enabled ? UserStatusEnum.ACTIVE : UserStatusEnum.DISABLED;
    }
}
```

**MyBatis-Plus版本（修改后）**
```java
package com.qoobot.openidaas.core.domain;

import com.baomidou.mybatisplus.annotation.*;
import com.qoobot.openidaas.common.entity.BaseEntity;
import com.qoobot.openidaas.common.enumeration.UserStatusEnum;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.time.LocalDateTime;
import java.util.Set;

/**
 * 用户领域模型
 *
 * @author QooBot
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("users")
public class User extends BaseEntity {

    /**
     * 主键ID
     */
    @TableId(type = IdType.AUTO)
    private Long id;

    /**
     * 租户ID
     */
    @TableField("tenant_id")
    private Long tenantId;

    /**
     * 用户名
     */
    @TableField("username")
    private String username;

    /**
     * 邮箱
     */
    @TableField("email")
    private String email;

    /**
     * 手机号
     */
    @TableField("mobile")
    private String mobile;

    /**
     * 密码（加密存储）
     */
    @TableField("password")
    private String password;

    /**
     * 昵称
     */
    @TableField("nickname")
    private String nickname;

    /**
     * 真实姓名
     */
    @TableField("real_name")
    private String realName;

    /**
     * 头像URL
     */
    @TableField("avatar")
    private String avatar;

    /**
     * 性别（0-未知，1-男，2-女）
     */
    @TableField("gender")
    private Integer gender;

    /**
     * 生日
     */
    @TableField("birthday")
    private LocalDateTime birthday;

    /**
     * 状态
     */
    @TableField("status")
    private UserStatusEnum status;

    /**
     * 最后登录时间
     */
    @TableField("last_login_time")
    private LocalDateTime lastLoginTime;

    /**
     * 最后登录IP
     */
    @TableField("last_login_ip")
    private String lastLoginIp;

    /**
     * 登录失败次数
     */
    @TableField("login_fail_count")
    private Integer loginFailCount = 0;

    /**
     * 账户锁定时间
     */
    @TableField("lock_time")
    private LocalDateTime lockTime;

    /**
     * 密码过期时间
     */
    @TableField("pwd_expire_time")
    private LocalDateTime pwdExpireTime;

    /**
     * 是否需要重置密码
     */
    @TableField("pwd_reset_required")
    private Boolean pwdResetRequired = false;

    /**
     * 密码重置时间
     */
    @TableField("pwd_reset_time")
    private LocalDateTime pwdResetTime;

    /**
     * 密码重置操作人
     */
    @TableField("pwd_reset_by")
    private Long pwdResetBy;

    /**
     * 用户拥有的角色集合（非数据库字段，需要手动加载）
     */
    @TableField(exist = false)
    private Set<Role> roles;

    /**
     * 用户所属部门集合（非数据库字段，需要手动加载）
     */
    @TableField(exist = false)
    private Set<Department> departments;

    /**
     * 用户拥有的权限集合（非数据库字段，需要手动加载）
     */
    @TableField(exist = false)
    private Set<Permission> permissions;

    /**
     * 用户所属租户（非数据库字段，需要手动加载）
     */
    @TableField(exist = false)
    private Tenant tenant;

    /**
     * 兼容方法：获取启用状态
     */
    public Boolean getEnabled() {
        return this.status == UserStatusEnum.ACTIVE || this.status == UserStatusEnum.NORMAL;
    }

    /**
     * 兼容方法：设置启用状态
     */
    public void setEnabled(Boolean enabled) {
        this.status = enabled ? UserStatusEnum.ACTIVE : UserStatusEnum.DISABLED;
    }
}
```

#### 示例2：Role实体改造

**JPA版本（修改前）**
```java
@Data
@EqualsAndHashCode(callSuper = true)
@Entity
@Table(name = "roles")
public class Role extends BaseEntity {

    @Column(name = "role_name", length = 50, nullable = false)
    private String roleName;

    @Column(name = "role_code", length = 50, nullable = false, unique = true)
    private String roleCode;

    @Column(name = "description", length = 255)
    private String description;

    @Column(name = "is_system")
    private Boolean isSystem = false;

    @ManyToMany(fetch = FetchType.LAZY)
    @JoinTable(
        name = "role_permissions",
        joinColumns = @JoinColumn(name = "role_id"),
        inverseJoinColumns = @JoinColumn(name = "perm_id")
    )
    private Set<Permission> permissions;

    @ManyToMany(fetch = FetchType.LAZY)
    @JoinTable(
        name = "user_roles",
        joinColumns = @JoinColumn(name = "role_id"),
        inverseJoinColumns = @JoinColumn(name = "user_id")
    )
    private Set<User> users;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "tenant_id")
    private Tenant tenant;
}
```

**MyBatis-Plus版本（修改后）**
```java
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("roles")
public class Role extends BaseEntity {

    /**
     * 主键ID
     */
    @TableId(type = IdType.AUTO)
    private Long id;

    /**
     * 租户ID
     */
    @TableField("tenant_id")
    private Long tenantId;

    /**
     * 角色名称
     */
    @TableField("role_name")
    private String roleName;

    /**
     * 角色编码
     */
    @TableField("role_code")
    private String roleCode;

    /**
     * 角色描述
     */
    @TableField("description")
    private String description;

    /**
     * 是否系统角色
     */
    @TableField("is_system")
    private Boolean isSystem = false;

    /**
     * 角色拥有的权限集合（非数据库字段）
     */
    @TableField(exist = false)
    private Set<Permission> permissions;

    /**
     * 角色关联的用户集合（非数据库字段）
     */
    @TableField(exist = false)
    private Set<User> users;

    /**
     * 角色所属租户（非数据库字段）
     */
    @TableField(exist = false)
    private Tenant tenant;
}
```

#### 示例3：BaseEntity改造

```java
package com.qoobot.openidaas.common.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 基础实体类
 *
 * @author QooBot
 */
@Data
public class BaseEntity implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 主键ID（各子类定义自己的@TableId）
     */
    private Long id;

    /**
     * 创建时间
     */
    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;

    /**
     * 更新时间
     */
    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updateTime;

    /**
     * 创建人ID
     */
    @TableField(fill = FieldFill.INSERT)
    private Long createdBy;

    /**
     * 更新人ID
     */
    @TableField(fill = FieldFill.INSERT_UPDATE)
    private Long updatedBy;

    /**
     * 逻辑删除标记（0-未删除，1-已删除）
     */
    @TableLogic
    @TableField("deleted")
    private Integer deleted = 0;

    /**
     * 版本号（乐观锁）
     */
    @Version
    @TableField("version")
    private Integer version = 0;
}
```

### 3.4 字段填充处理器

创建`MetaObjectHandler`实现类，自动填充创建时间、更新时间等字段：

```java
package com.qoobot.openidaas.core.handler;

import com.baomidou.mybatisplus.core.handlers.MetaObjectHandler;
import org.apache.ibatis.reflection.MetaObject;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;

/**
 * MyBatis-Plus字段自动填充处理器
 *
 * @author QooBot
 */
@Component
public class MyMetaObjectHandler implements MetaObjectHandler {

    /**
     * 插入时自动填充
     */
    @Override
    public void insertFill(MetaObject metaObject) {
        this.strictInsertFill(metaObject, "createTime", LocalDateTime.class, LocalDateTime.now());
        this.strictInsertFill(metaObject, "updateTime", LocalDateTime.class, LocalDateTime.now());
        this.strictInsertFill(metaObject, "deleted", Integer.class, 0);
        this.strictInsertFill(metaObject, "version", Integer.class, 0);
    }

    /**
     * 更新时自动填充
     */
    @Override
    public void updateFill(MetaObject metaObject) {
        this.strictUpdateFill(metaObject, "updateTime", LocalDateTime.class, LocalDateTime.now());
    }
}
```

---

## 数据层接口改造

### 4.1 Repository到Mapper的映射关系

| JPA Repository | MyBatis-Plus Mapper | 说明 |
|----------------|---------------------|------|
| `extends JpaRepository` | `extends BaseMapper` | 基础CRUD接口 |
| 方法名派生查询 | 自定义方法+XML/注解 | 需要手动实现 |
| `@Query`注解 | `@Select/@Insert/@Update/@Delete` | SQL查询 |
| `Specification` | `QueryWrapper/LambdaQueryWrapper` | 动态条件查询 |
| `JpaSpecificationExecutor` | - | 使用Wrapper替代 |

### 4.2 JPA BaseRepository改造

**JPA版本**
```java
package com.qoobot.openidaas.core.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.repository.NoRepositoryBean;

import java.util.Optional;

/**
 * JPA基础Repository接口
 *
 * @param <T> 实体类型
 * @param <ID> 主键类型
 */
@NoRepositoryBean
public interface BaseRepository<T, ID> extends JpaRepository<T, ID>, JpaSpecificationExecutor<T> {

    /**
     * 根据ID和租户ID查询
     */
    Optional<T> findByIdAndTenantId(ID id, Long tenantId);

    /**
     * 判断是否存在
     */
    boolean existsByIdAndTenantId(ID id, Long tenantId);

    /**
     * 根据ID和租户ID删除
     */
    void deleteByIdAndTenantId(ID id, Long tenantId);
}
```

**MyBatis-Plus版本**
```java
package com.qoobot.openidaas.core.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;

/**
 * MyBatis-Plus基础Mapper接口
 *
 * @param <T> 实体类型
 * @author QooBot
 */
@Mapper
public interface BaseMapper<T> extends com.baomidou.mybatisplus.core.mapper.BaseMapper<T> {
    // BaseMapper已提供基础CRUD方法：
    // - insert(T entity)
    // - deleteById(Serializable id)
    // - delete(Wrapper<T> wrapper)
    // - updateById(T entity)
    // - update(T entity, Wrapper<T> wrapper)
    // - selectById(Serializable id)
    // - selectBatchIds(Collection<? extends Serializable> ids)
    // - selectList(Wrapper<T> wrapper)
    // - selectCount(Wrapper<T> wrapper)
    // - selectPage(IPage<T> page, Wrapper<T> wrapper)
}
```

### 4.3 UserRepository完整改造示例

#### JPA版本（修改前）

```java
package com.qoobot.openidaas.core.repository;

import com.qoobot.openidaas.core.domain.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

/**
 * 用户Repository接口
 */
@Repository
public interface UserRepository extends JpaRepository<User, Long>, JpaSpecificationExecutor<User> {

    // ========== 基础查询 ==========

    /**
     * 根据用户名查询
     */
    Optional<User> findByUsername(String username);

    /**
     * 根据邮箱查询
     */
    Optional<User> findByEmail(String email);

    /**
     * 根据手机号查询
     */
    Optional<User> findByMobile(String mobile);

    // ========== 租户隔离查询 ==========

    /**
     * 根据ID和租户ID查询
     */
    Optional<User> findByIdAndTenantId(Long id, Long tenantId);

    /**
     * 根据用户名和租户ID查询
     */
    Optional<User> findByUsernameAndTenantId(String username, Long tenantId);

    /**
     * 判断用户名是否存在于指定租户
     */
    boolean existsByUsernameAndTenantId(String username, Long tenantId);

    // ========== 模糊查询 ==========

    /**
     * 用户名模糊查询
     */
    @Query("SELECT u FROM User u WHERE u.username LIKE %:username% AND u.tenantId = :tenantId")
    Page<User> findByUsernameContainingAndTenantId(@Param("username") String username,
                                                     @Param("tenantId") Long tenantId,
                                                     Pageable pageable);

    // ========== 关联查询 ==========

    /**
     * 根据部门ID查询用户
     */
    @Query("SELECT u FROM User u JOIN u.departments d WHERE d.id = :deptId")
    List<User> findByDepartmentId(@Param("deptId") Long deptId);

    /**
     * 根据角色ID查询用户
     */
    @Query("SELECT u FROM User u JOIN u.roles r WHERE r.id = :roleId")
    List<User> findByRoleId(@Param("roleId") Long roleId);

    // ========== 条件查询 ==========

    /**
     * 查询登录失败次数过多的用户
     */
    @Query("SELECT u FROM User u WHERE u.loginFailCount >= :failCount AND u.tenantId = :tenantId")
    List<User> findUsersWithExcessiveLoginFailures(@Param("failCount") Integer failCount,
                                                    @Param("tenantId") Long tenantId);

    /**
     * 查询锁定中的用户
     */
    @Query("SELECT u FROM User u WHERE u.lockTime IS NOT NULL AND u.lockTime > :currentTime")
    List<User> findLockedUsers(@Param("currentTime") LocalDateTime currentTime);

    // ========== 统计查询 ==========

    /**
     * 统计租户用户数
     */
    long countByTenantId(Long tenantId);

    /**
     * 按状态统计用户数
     */
    @Query("SELECT u.status, COUNT(u) FROM User u WHERE u.tenantId = :tenantId GROUP BY u.status")
    List<Object[]> countByStatusAndTenantId(@Param("tenantId") Long tenantId);
}
```

#### MyBatis-Plus版本（修改后）

```java
package com.qoobot.openidaas.core.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.qoobot.openidaas.core.domain.User;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Delete;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

/**
 * 用户Mapper接口
 *
 * @author QooBot
 */
@Mapper
public interface UserMapper extends BaseMapper<User> {

    // ========== 基础查询（使用BaseMapper方法）==========
    // UserMapper.selectById(id)
    // UserMapper.selectList(wrapper)
    // UserMapper.selectCount(wrapper)

    // ========== 精确查询 ==========

    /**
     * 根据用户名查询
     */
    @Select("SELECT * FROM users WHERE username = #{username} AND deleted = 0 LIMIT 1")
    User selectByUsername(@Param("username") String username);

    /**
     * 根据邮箱查询
     */
    @Select("SELECT * FROM users WHERE email = #{email} AND deleted = 0 LIMIT 1")
    User selectByEmail(@Param("email") String email);

    /**
     * 根据手机号查询
     */
    @Select("SELECT * FROM users WHERE mobile = #{mobile} AND deleted = 0 LIMIT 1")
    User selectByMobile(@Param("mobile") String mobile);

    // ========== 租户隔离查询 ==========

    /**
     * 根据用户名和租户ID查询
     */
    @Select("SELECT * FROM users WHERE username = #{username} AND tenant_id = #{tenantId} AND deleted = 0 LIMIT 1")
    User selectByUsernameAndTenantId(@Param("username") String username,
                                       @Param("tenantId") Long tenantId);

    /**
     * 判断用户名是否存在于指定租户
     */
    @Select("SELECT COUNT(1) FROM users WHERE username = #{username} AND tenant_id = #{tenantId} AND deleted = 0")
    int countByUsernameAndTenantId(@Param("username") String username,
                                     @Param("tenantId") Long tenantId);

    // ========== 模糊查询（分页）==========

    /**
     * 用户名模糊查询（分页）
     */
    @Select("<script>" +
            "SELECT * FROM users " +
            "WHERE deleted = 0 " +
            "<if test='tenantId != null'>AND tenant_id = #{tenantId}</if> " +
            "<if test='username != null and username != \"\"'>AND username LIKE CONCAT('%', #{username}, '%')</if> " +
            "ORDER BY id DESC" +
            "</script>")
    IPage<User> selectByUsernameContainingPage(Page<User> page,
                                               @Param("username") String username,
                                               @Param("tenantId") Long tenantId);

    // ========== 关联查询 ==========

    /**
     * 根据部门ID查询用户
     */
    @Select("SELECT u.* FROM users u " +
            "INNER JOIN user_departments ud ON u.id = ud.user_id " +
            "WHERE ud.dept_id = #{deptId} AND u.deleted = 0")
    List<User> selectByDepartmentId(@Param("deptId") Long deptId);

    /**
     * 根据角色ID查询用户
     */
    @Select("SELECT u.* FROM users u " +
            "INNER JOIN user_roles ur ON u.id = ur.user_id " +
            "WHERE ur.role_id = #{roleId} AND u.deleted = 0")
    List<User> selectByRoleId(@Param("roleId") Long roleId);

    // ========== 条件查询 ==========

    /**
     * 查询登录失败次数过多的用户
     */
    @Select("SELECT * FROM users " +
            "WHERE login_fail_count >= #{failCount} " +
            "AND tenant_id = #{tenantId} " +
            "AND deleted = 0")
    List<User> selectUsersWithExcessiveLoginFailures(@Param("failCount") Integer failCount,
                                                       @Param("tenantId") Long tenantId);

    /**
     * 查询锁定中的用户
     */
    @Select("SELECT * FROM users " +
            "WHERE lock_time IS NOT NULL " +
            "AND lock_time > #{currentTime} " +
            "AND deleted = 0")
    List<User> selectLockedUsers(@Param("currentTime") LocalDateTime currentTime);

    // ========== 统计查询 ==========

    /**
     * 统计租户用户数
     */
    @Select("SELECT COUNT(1) FROM users WHERE tenant_id = #{tenantId} AND deleted = 0")
    long countByTenantId(@Param("tenantId") Long tenantId);

    /**
     * 按状态统计用户数
     */
    @Select("SELECT status, COUNT(1) as count FROM users " +
            "WHERE tenant_id = #{tenantId} AND deleted = 0 " +
            "GROUP BY status")
    List<StatusCountVO> countByStatusAndTenantId(@Param("tenantId") Long tenantId);

    // ========== 删除操作 ==========

    /**
     * 根据ID和租户ID删除（逻辑删除）
     */
    @Delete("UPDATE users SET deleted = 1 WHERE id = #{id} AND tenant_id = #{tenantId}")
    int deleteByIdAndTenantId(@Param("id") Long id, @Param("tenantId") Long tenantId);
}

/**
 * 状态统计结果VO
 */
class StatusCountVO {
    private String status;
    private Long count;
    
    // getter和setter
}
```

### 4.4 XML映射文件方式（复杂查询推荐）

创建`src/main/resources/mapper/UserMapper.xml`：

```xml
<?xml version="1.0" encoding="UTF-8"?>
<!DOCTYPE mapper PUBLIC "-//mybatis.org//DTD Mapper 3.0//EN"
        "http://mybatis.org/dtd/mybatis-3-mapper.dtd">
<mapper namespace="com.qoobot.openidaas.core.mapper.UserMapper">

    <!-- 结果映射 -->
    <resultMap id="BaseResultMap" type="com.qoobot.openidaas.core.domain.User">
        <id column="id" property="id" jdbcType="BIGINT"/>
        <result column="tenant_id" property="tenantId" jdbcType="BIGINT"/>
        <result column="username" property="username" jdbcType="VARCHAR"/>
        <result column="email" property="email" jdbcType="VARCHAR"/>
        <result column="mobile" property="mobile" jdbcType="VARCHAR"/>
        <result column="password" property="password" jdbcType="VARCHAR"/>
        <result column="nickname" property="nickname" jdbcType="VARCHAR"/>
        <result column="real_name" property="realName" jdbcType="VARCHAR"/>
        <result column="avatar" property="avatar" jdbcType="VARCHAR"/>
        <result column="gender" property="gender" jdbcType="INTEGER"/>
        <result column="birthday" property="birthday" jdbcType="TIMESTAMP"/>
        <result column="status" property="status" jdbcType="VARCHAR"/>
        <result column="last_login_time" property="lastLoginTime" jdbcType="TIMESTAMP"/>
        <result column="last_login_ip" property="lastLoginIp" jdbcType="VARCHAR"/>
        <result column="login_fail_count" property="loginFailCount" jdbcType="INTEGER"/>
        <result column="lock_time" property="lockTime" jdbcType="TIMESTAMP"/>
        <result column="pwd_expire_time" property="pwdExpireTime" jdbcType="TIMESTAMP"/>
        <result column="pwd_reset_required" property="pwdResetRequired" jdbcType="BOOLEAN"/>
        <result column="pwd_reset_time" property="pwdResetTime" jdbcType="TIMESTAMP"/>
        <result column="pwd_reset_by" property="pwdResetBy" jdbcType="BIGINT"/>
        <result column="create_time" property="createTime" jdbcType="TIMESTAMP"/>
        <result column="update_time" property="updateTime" jdbcType="TIMESTAMP"/>
        <result column="created_by" property="createdBy" jdbcType="BIGINT"/>
        <result column="updated_by" property="updatedBy" jdbcType="BIGINT"/>
        <result column="deleted" property="deleted" jdbcType="INTEGER"/>
        <result column="version" property="version" jdbcType="INTEGER"/>
    </resultMap>

    <!-- 基础列 -->
    <sql id="Base_Column_List">
        id, tenant_id, username, email, mobile, password, nickname, real_name, avatar,
        gender, birthday, status, last_login_time, last_login_ip, login_fail_count,
        lock_time, pwd_expire_time, pwd_reset_required, pwd_reset_time, pwd_reset_by,
        create_time, update_time, created_by, updated_by, deleted, version
    </sql>

    <!-- 复杂动态查询 -->
    <select id="selectUserByConditions" resultMap="BaseResultMap">
        SELECT
        <include refid="Base_Column_List"/>
        FROM users
        <where>
            deleted = 0
            <if test="tenantId != null">
                AND tenant_id = #{tenantId}
            </if>
            <if test="username != null and username != ''">
                AND username LIKE CONCAT('%', #{username}, '%')
            </if>
            <if test="nickname != null and nickname != ''">
                AND nickname LIKE CONCAT('%', #{nickname}, '%')
            </if>
            <if test="status != null">
                AND status = #{status}
            </if>
            <if test="startTime != null">
                AND create_time &gt;= #{startTime}
            </if>
            <if test="endTime != null">
                AND create_time &lt;= #{endTime}
            </if>
        </where>
        ORDER BY create_time DESC
    </select>

    <!-- 关联查询用户角色权限 -->
    <select id="selectUserWithRolesAndPermissions" resultMap="UserWithRolesResultMap">
        SELECT
            u.*,
            r.id as role_id,
            r.role_name,
            r.role_code,
            p.id as perm_id,
            p.perm_code,
            p.perm_name
        FROM users u
        LEFT JOIN user_roles ur ON u.id = ur.user_id
        LEFT JOIN roles r ON ur.role_id = r.id
        LEFT JOIN role_permissions rp ON r.id = rp.role_id
        LEFT JOIN permissions p ON rp.perm_id = p.id
        WHERE u.id = #{userId} AND u.deleted = 0
    </select>

    <resultMap id="UserWithRolesResultMap" type="com.qoobot.openidaas.core.domain.User">
        <id column="id" property="id"/>
        <!-- 用户字段映射 -->
        <result column="tenant_id" property="tenantId"/>
        <result column="username" property="username"/>
        <!-- ... 其他字段 ... -->
        
        <!-- 角色集合 -->
        <collection property="roles" ofType="com.qoobot.openidaas.core.domain.Role">
            <id column="role_id" property="id"/>
            <result column="role_name" property="roleName"/>
            <result column="role_code" property="roleCode"/>
            
            <!-- 权限集合 -->
            <collection property="permissions" ofType="com.qoobot.openidaas.core.domain.Permission">
                <id column="perm_id" property="id"/>
                <result column="perm_code" property="permCode"/>
                <result column="perm_name" property="permName"/>
            </collection>
        </collection>
    </resultMap>

</mapper>
```

---

## 查询条件构建方式转换

### 5.1 JPA Criteria API vs MyBatis-Plus Wrapper

#### 场景：动态条件查询用户

**JPA Specification方式（修改前）**
```java
@Override
public PageResultVO<UserVO> queryUsers(UserQueryDTO queryDTO) {
    Pageable pageable = PageRequest.of(
        queryDTO.getPage().intValue() - 1,
        queryDTO.getSize().intValue()
    );

    Specification<User> spec = (root, query, criteriaBuilder) -> {
        List<Predicate> predicates = new ArrayList<>();

        if (StringUtils.hasText(queryDTO.getUsername())) {
            predicates.add(criteriaBuilder.like(
                root.get("username"),
                "%" + queryDTO.getUsername() + "%"
            ));
        }
        if (StringUtils.hasText(queryDTO.getNickname())) {
            predicates.add(criteriaBuilder.like(
                root.get("nickname"),
                "%" + queryDTO.getNickname() + "%"
            ));
        }
        if (queryDTO.getStatus() != null) {
            predicates.add(criteriaBuilder.equal(root.get("status"), queryDTO.getStatus()));
        }
        if (queryDTO.getTenantId() != null) {
            predicates.add(criteriaBuilder.equal(root.get("tenantId"), queryDTO.getTenantId()));
        }
        if (queryDTO.getStartTime() != null) {
            predicates.add(criteriaBuilder.greaterThanOrEqualTo(
                root.get("createTime"),
                queryDTO.getStartTime()
            ));
        }
        if (queryDTO.getEndTime() != null) {
            predicates.add(criteriaBuilder.lessThanOrEqualTo(
                root.get("createTime"),
                queryDTO.getEndTime()
            ));
        }

        return criteriaBuilder.and(predicates.toArray(new Predicate[0]));
    };

    Page<User> page = userRepository.findAll(spec, pageable);
    
    // 转换逻辑...
    return new PageResultVO<>(page.getContent(), page.getTotalElements());
}
```

**MyBatis-Plus LambdaQueryWrapper方式（修改后）**
```java
@Override
public PageResultVO<UserVO> queryUsers(UserQueryDTO queryDTO) {
    // 构建查询条件
    LambdaQueryWrapper<User> wrapper = new LambdaQueryWrapper<>();

    // 动态条件
    wrapper.like(StringUtils.hasText(queryDTO.getUsername()),
                User::getUsername,
                queryDTO.getUsername())
          .like(StringUtils.hasText(queryDTO.getNickname()),
                User::getNickname,
                queryDTO.getNickname())
          .eq(queryDTO.getStatus() != null,
              User::getStatus,
              queryDTO.getStatus())
          .eq(queryDTO.getTenantId() != null,
              User::getTenantId,
              queryDTO.getTenantId())
          .ge(queryDTO.getStartTime() != null,
              User::getCreateTime,
              queryDTO.getStartTime())
          .le(queryDTO.getEndTime() != null,
              User::getCreateTime,
              queryDTO.getEndTime())
          .orderByDesc(User::getCreateTime);

    // 分页查询
    Page<User> page = userMapper.selectPage(
        new Page<>(queryDTO.getPage(), queryDTO.getSize()),
        wrapper
    );

    // 转换VO
    List<UserVO> voList = page.getRecords().stream()
        .map(this::convertToVO)
        .collect(Collectors.toList());

    return new PageResultVO<>(voList, page.getTotal());
}
```

### 5.2 常用查询条件对照表

| JPA Criteria API | MyBatis-Plus Wrapper | 说明 |
|------------------|---------------------|------|
| `equal(field, value)` | `eq(实体::getField, value)` | 等于 |
| `notEqual(field, value)` | `ne(实体::getField, value)` | 不等于 |
| `greaterThan(field, value)` | `gt(实体::getField, value)` | 大于 |
| `greaterThanOrEqualTo(field, value)` | `ge(实体::getField, value)` | 大于等于 |
| `lessThan(field, value)` | `lt(实体::getField, value)` | 小于 |
| `lessThanOrEqualTo(field, value)` | `le(实体::getField, value)` | 小于等于 |
| `like(field, pattern)` | `like(实体::getField, pattern)` | 模糊查询 |
| `notLike(field, pattern)` | `notLike(实体::getField, pattern)` | 不包含 |
| `in(field, values)` | `in(实体::getField, values)` | IN查询 |
| `notIn(field, values)` | `notIn(实体::getField, values)` | NOT IN查询 |
| `between(field, start, end)` | `between(实体::getField, start, end)` | BETWEEN |
| `isNull(field)` | `isNull(实体::getField)` | IS NULL |
| `isNotNull(field)` | `isNotNull(实体::getField)` | IS NOT NULL |
| `and(predicates)` | `and(wrapper)` | AND连接 |
| `or(predicates)` | `or(wrapper)` | OR连接 |

### 5.3 复杂查询条件示例

#### 示例1：嵌套OR条件

**JPA方式**
```java
Specification<User> spec = (root, query, cb) -> {
    Predicate active = cb.equal(root.get("status"), UserStatusEnum.ACTIVE);
    Predicate normal = cb.equal(root.get("status"), UserStatusEnum.NORMAL);
    Predicate deleted = cb.equal(root.get("deleted"), 0);
    
    return cb.and(cb.or(active, normal), deleted);
};
```

**MyBatis-Plus方式**
```java
LambdaQueryWrapper<User> wrapper = new LambdaQueryWrapper<>();
wrapper.and(w -> w.eq(User::getStatus, UserStatusEnum.ACTIVE)
                   .or()
                   .eq(User::getStatus, UserStatusEnum.NORMAL))
      .eq(User::getDeleted, 0);
```

#### 示例2：子查询

**JPA方式**
```java
@Query("SELECT u FROM User u WHERE u.id IN " +
       "(SELECT ur.user_id FROM user_roles ur WHERE ur.role_id = :roleId)")
List<User> findUsersByRoleId(@Param("roleId") Long roleId);
```

**MyBatis-Plus方式（XML）**
```xml
<select id="selectUsersByRoleId" resultMap="BaseResultMap">
    SELECT u.* FROM users u
    WHERE u.id IN (
        SELECT ur.user_id FROM user_roles ur WHERE ur.role_id = #{roleId}
    )
    AND u.deleted = 0
</select>
```

或使用子查询构造器：
```java
// 方式1：使用apply
LambdaQueryWrapper<User> wrapper = new LambdaQueryWrapper<>();
wrapper.apply("id IN (SELECT user_id FROM user_roles WHERE role_id = {0})", roleId);

// 方式2：使用exists
wrapper.exists("SELECT 1 FROM user_roles ur WHERE ur.user_id = t.id AND ur.role_id = {0}", roleId);
```

### 5.4 LambdaQueryWrapper完整方法列表

```java
// ============ 等值比较 ============
wrapper.eq(User::getId, 1L);                  // 等于
wrapper.ne(User::getStatus, "DISABLED");      // 不等于

// ============ 范围比较 ============
wrapper.gt(User::getAge, 18);                  // 大于 >
wrapper.ge(User::getAge, 18);                  // 大于等于 >=
wrapper.lt(User::getAge, 60);                  // 小于 <
wrapper.le(User::getAge, 60);                  // 小于等于 <=

// ============ 模糊查询 ============
wrapper.like(User::getUsername, "admin");      // LIKE '%admin%'
wrapper.notLike(User::getUsername, "test");    // NOT LIKE '%test%'
wrapper.likeLeft(User::getUsername, "admin");  // LIKE '%admin'
wrapper.likeRight(User::getUsername, "admin"); // LIKE 'admin%'

// ============ NULL判断 ============
wrapper.isNull(User::getEmail);               // IS NULL
wrapper.isNotNull(User::getEmail);             // IS NOT NULL

// ============ 集合查询 ============
wrapper.in(User::getId, Arrays.asList(1L, 2L, 3L));            // IN
wrapper.notIn(User::getId, Arrays.asList(1L, 2L, 3L));        // NOT IN

// ============ BETWEEN查询 ============
wrapper.between(User::getCreateTime, startTime, endTime);

// ============ 分组与排序 ============
wrapper.groupBy(User::getTenantId);
wrapper.orderByAsc(User::getCreateTime);
wrapper.orderByDesc(User::getCreateTime);
wrapper.orderBy(true, true, User::getCreateTime); // (isAsc, isNullsFirst, column)

// ============ 逻辑条件组合 ============
wrapper.and(w -> w.eq(User::getStatus, "ACTIVE")
                   .or()
                   .eq(User::getStatus, "NORMAL"));
wrapper.or(w -> w.eq(User::getDeleted, 0)
                  .eq(User::getLocked, false));

// ============ 条件（条件为true时才添加）============
wrapper.eq(StringUtils.hasText(username), User::getUsername, username);
wrapper.like(status != null, User::getNickname, nickname);

// ============ 聚合函数 ============
wrapper.select(User::getTenantId, User::getStatus);
wrapper.groupBy(User::getTenantId, User::getStatus);
wrapper.having("COUNT(1) > 0");

// ============ 自定义SQL片段 ============
wrapper.apply("DATE_FORMAT(create_time, '%Y-%m-%d') = {0}", dateStr);

// = EXISTS子查询 ============
wrapper.exists("SELECT 1 FROM user_roles ur WHERE ur.user_id = t.id AND ur.role_id = {0}", roleId);

// = 只查询指定字段 ============
wrapper.select(User::getId, User::getUsername, User::getStatus);

// = 去除重复 ============
wrapper.distinct();
```

---

## 事务管理配置变更

### 6.1 事务注解对比

**好消息**：`@Transactional`注解在JPA和MyBatis-Plus中**完全兼容**，无需修改！

```java
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)  // 类级别默认只读事务
public class UserServiceImpl implements UserService {

    private final UserMapper userMapper;

    /**
     * 只读查询（使用类级别的@Transactional(readOnly = true)）
     */
    @Override
    public User getUserById(Long userId) {
        return userMapper.selectById(userId);
    }

    /**
     * 写操作（需要写事务）
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public User createUser(UserCreateDTO dto) {
        User user = new User();
        // ... 设置属性
        userMapper.insert(user);
        return user;
    }

    /**
     * 批量操作（需要事务）
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public void batchUpdateStatus(List<Long> userIds, UserStatusEnum status) {
        userIds.forEach(userId -> {
            User user = new User();
            user.setId(userId);
            user.setStatus(status);
            userMapper.updateById(user);
        });
    }

    /**
     * 复杂业务逻辑（需要事务）
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public void assignRoles(Long userId, List<Long> roleIds) {
        // 1. 先删除旧角色
        LambdaQueryWrapper<UserRole> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(UserRole::getUserId, userId);
        userRoleMapper.delete(wrapper);

        // 2. 插入新角色
        List<UserRole> userRoles = roleIds.stream()
            .map(roleId -> {
                UserRole ur = new UserRole();
                ur.setUserId(userId);
                ur.setRoleId(roleId);
                return ur;
            })
            .collect(Collectors.toList());
        
        userRoles.forEach(ur -> userRoleMapper.insert(ur));
    }
}
```

### 6.2 移除JPA配置类

**删除`openidaas-core/src/main/java/com/qoobot/openidaas/core/config/JpaConfig.java`**

```java
// ❌ 删除此文件
@Configuration
@EnableJpaRepositories(basePackages = "com.qoobot.openidaas.core.repository")
@EntityScan(basePackages = "com.qoobot.openidaas.core.domain")
@EnableTransactionManagement
public class JpaConfig {
}
```

### 6.3 添加MyBatis-Plus配置类

创建`openidaas-core/src/main/java/com/qoobot/openidaas/core/config/MyBatisPlusConfig.java`：

```java
package com.qoobot.openidaas.core.config;

import com.baomidou.mybatisplus.annotation.DbType;
import com.baomidou.mybatisplus.extension.plugins.MybatisPlusInterceptor;
import com.baomidou.mybatisplus.extension.plugins.inner.BlockAttackInnerInterceptor;
import com.baomidou.mybatisplus.extension.plugins.inner.OptimisticLockerInnerInterceptor;
import com.baomidou.mybatisplus.extension.plugins.inner.PaginationInnerInterceptor;
import com.baomidou.mybatisplus.extension.plugins.inner.PerformanceInnerInterceptor;
import org.mybatis.spring.annotation.MapperScan;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.transaction.annotation.EnableTransactionManagement;

/**
 * MyBatis-Plus配置类
 *
 * @author QooBot
 */
@Configuration
@EnableTransactionManagement  // 启用事务管理（与JPA相同）
@MapperScan("com.qoobot.openidaas.core.mapper")  // 扫描Mapper接口
public class MyBatisPlusConfig {

    /**
     * MyBatis-Plus拦截器链
     */
    @Bean
    public MybatisPlusInterceptor mybatisPlusInterceptor() {
        MybatisPlusInterceptor interceptor = new MybatisPlusInterceptor();

        // 1. 分页插件
        PaginationInnerInterceptor paginationInterceptor = new PaginationInnerInterceptor(DbType.MYSQL);
        paginationInterceptor.setMaxLimit(500L);  // 单页最大条数限制
        paginationInterceptor.setOverflow(false); // 溢出总页数后是否进行处理
        interceptor.addInnerInterceptor(paginationInterceptor);

        // 2. 性能分析插件（开发环境启用）
        PerformanceInnerInterceptor performanceInterceptor = new PerformanceInnerInterceptor();
        performanceInterceptor.setMaxTime(500);  // SQL执行最大时长（毫秒）
        performanceInterceptor.setFormat(true);  // 格式化SQL
        interceptor.addInnerInterceptor(performanceInterceptor);

        // 3. 乐观锁插件
        interceptor.addInnerInterceptor(new OptimisticLockerInnerInterceptor());

        // 4. 防止全表更新和删除插件
        interceptor.addInnerInterceptor(new BlockAttackInnerInterceptor());

        return interceptor;
    }
}
```

### 6.4 application.yml配置调整

**JPA配置（删除）**
```yaml
# ❌ 删除以下配置
spring:
  jpa:
    hibernate:
      ddl-auto: none
    show-sql: true
    properties:
      hibernate:
        dialect: org.hibernate.dialect.MySQLDialect
        format_sql: true
        use_sql_comments: true
```

**MyBatis-Plus配置（添加）**
```yaml
# ✅ 添加MyBatis-Plus配置
mybatis-plus:
  # Mapper XML文件位置
  mapper-locations: classpath*:mapper/**/*.xml
  # 实体类包路径
  type-aliases-package: com.qoobot.openidaas.core.domain
  # 配置
  configuration:
    # 下划线转驼峰
    map-underscore-to-camel-case: true
    # 日志实现
    log-impl: org.apache.ibatis.logging.slf4j.Slf4jImpl
    # 开启二级缓存
    cache-enabled: true
    # 驼峰转下划线
    map-underscore-to-camel-case: true
  # 全局配置
  global-config:
    # 数据库配置
    db-config:
      # 主键类型（AUTO自增）
      id-type: auto
      # 逻辑删除字段名
      logic-delete-field: deleted
      # 逻辑删除值
      logic-delete-value: 1
      # 逻辑未删除值
      logic-not-delete-value: 0
      # 乐观锁字段名
      version-field: version

spring:
  # 数据源配置（保持不变）
  datasource:
    url: jdbc:mysql://localhost:3306/openidaas?useUnicode=true&characterEncoding=utf8&serverTimezone=Asia/Shanghai
    username: root
    password: your_password
    driver-class-name: com.mysql.cj.jdbc.Driver
    # Hikari连接池配置
    hikari:
      minimum-idle: 5
      maximum-pool-size: 20
      idle-timeout: 600000
      max-lifetime: 1800000
      connection-timeout: 30000
```

---

## 多对多关系处理

### 7.1 JPA vs MyBatis-Plus多对多关系对比

| 特性 | JPA | MyBatis-Plus |
|------|-----|--------------|
| **中间表维护** | 自动维护 | 需手动维护 |
| **关联查询** | `@ManyToMany` | 需自定义SQL |
| **懒加载** | 支持（需事务） | 需手动加载 |
| **级联操作** | `CascadeType` | 需手动实现 |

### 7.2 创建中间表实体

#### 示例：用户-角色关系

**创建UserRole中间表实体**
```java
package com.qoobot.openidaas.core.domain;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 用户-角色关联实体
 */
@Data
@TableName("user_roles")
public class UserRole implements Serializable {

    /**
     * 主键ID
     */
    @TableId(type = IdType.AUTO)
    private Long id;

    /**
     * 用户ID
     */
    @TableField("user_id")
    private Long userId;

    /**
     * 角色ID
     */
    @TableField("role_id")
    private Long roleId;

    /**
     * 租户ID
     */
    @TableField("tenant_id")
    private Long tenantId;

    /**
     * 创建时间
     */
    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;
}
```

**创建UserRoleMapper**
```java
package com.qoobot.openidaas.core.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.qoobot.openidaas.core.domain.UserRole;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Delete;
import org.apache.ibatis.annotations.Select;

import java.util.List;

/**
 * 用户-角色Mapper
 */
@Mapper
public interface UserRoleMapper extends BaseMapper<UserRole> {

    /**
     * 查询用户的所有角色ID
     */
    @Select("SELECT role_id FROM user_roles WHERE user_id = #{userId} AND tenant_id = #{tenantId}")
    List<Long> selectRoleIdsByUserId(@Param("userId") Long userId,
                                      @Param("tenantId") Long tenantId);

    /**
     * 删除用户的所有角色
     */
    @Delete("DELETE FROM user_roles WHERE user_id = #{userId} AND tenant_id = #{tenantId}")
    int deleteByUserId(@Param("userId") Long userId,
                       @Param("tenantId") Long tenantId);

    /**
     * 删除角色的所有用户
     */
    @Delete("DELETE FROM user_roles WHERE role_id = #{roleId} AND tenant_id = #{tenantId}")
    int deleteByRoleId(@Param("roleId") Long roleId,
                       @Param("tenantId") Long tenantId);
}
```

### 7.3 Service层多对多关系操作

#### 为用户分配角色

**JPA方式**
```java
@Transactional
public void assignRolesToUser(Long userId, List<Long> roleIds, Long tenantId) {
    User user = userRepository.findByIdAndTenantId(userId, tenantId)
        .orElseThrow(() -> new BusinessException("用户不存在"));

    List<Role> roles = roleRepository.findAllById(roleIds);
    user.setRoles(new HashSet<>(roles));
    userRepository.save(user);
}
```

**MyBatis-Plus方式**
```java
@Transactional(rollbackFor = Exception.class)
public void assignRolesToUser(Long userId, List<Long> roleIds, Long tenantId) {
    // 1. 验证用户存在
    User user = userMapper.selectById(userId);
    if (user == null) {
        throw new BusinessException("用户不存在");
    }

    // 2. 先删除用户的所有角色
    LambdaQueryWrapper<UserRole> deleteWrapper = new LambdaQueryWrapper<>();
    deleteWrapper.eq(UserRole::getUserId, userId)
                 .eq(UserRole::getTenantId, tenantId);
    userRoleMapper.delete(deleteWrapper);

    // 3. 批量插入新角色
    if (roleIds != null && !roleIds.isEmpty()) {
        List<UserRole> userRoles = roleIds.stream()
            .map(roleId -> {
                UserRole ur = new UserRole();
                ur.setUserId(userId);
                ur.setRoleId(roleId);
                ur.setTenantId(tenantId);
                return ur;
            })
            .collect(Collectors.toList());

        // 使用批量插入（推荐）
        userRoles.forEach(ur -> userRoleMapper.insert(ur));
    }
}
```

#### 查询用户的所有角色

**JPA方式**
```java
// 懒加载，需要在事务中
@Transactional(readOnly = true)
public Set<Role> getUserRoles(Long userId) {
    User user = userRepository.findById(userId)
        .orElseThrow(() -> new BusinessException("用户不存在"));
    return user.getRoles();
}
```

**MyBatis-Plus方式**
```java
@Transactional(readOnly = true)
public List<Role> getUserRoles(Long userId, Long tenantId) {
    // 方式1：通过中间表查询
    List<Long> roleIds = userRoleMapper.selectRoleIdsByUserId(userId, tenantId);
    
    if (roleIds.isEmpty()) {
        return Collections.emptyList();
    }
    
    // 批量查询角色
    LambdaQueryWrapper<Role> wrapper = new LambdaQueryWrapper<>();
    wrapper.in(Role::getId, roleIds)
          .eq(Role::getTenantId, tenantId);
    
    return roleMapper.selectList(wrapper);
}

// 方式2：使用关联查询（在XML中定义）
@Transactional(readOnly = true)
public List<Role> getUserRolesWithDetails(Long userId, Long tenantId) {
    return userRoleMapper.selectRolesWithDetails(userId, tenantId);
}
```

#### 查询用户的完整权限集合（多级关联）

**JPA方式**
```java
@Query("SELECT DISTINCT p FROM User u " +
       "JOIN u.roles r " +
       "JOIN r.permissions p " +
       "WHERE u.id = :userId")
List<Permission> findUserPermissions(@Param("userId") Long userId);
```

**MyBatis-Plus方式（XML）**
```xml
<select id="selectUserPermissions" resultMap="PermissionResultMap">
    SELECT DISTINCT p.*
    FROM permissions p
    INNER JOIN role_permissions rp ON p.id = rp.perm_id
    INNER JOIN roles r ON rp.role_id = r.id
    INNER JOIN user_roles ur ON r.id = ur.role_id
    WHERE ur.user_id = #{userId}
    AND r.deleted = 0
    AND p.deleted = 0
    ORDER BY p.sort_order
</select>
```

### 7.4 关联数据批量加载优化

为避免N+1查询问题，使用批量查询优化：

```java
/**
 * 批量加载用户的关联数据
 */
public UserVO getUserWithAssociations(Long userId, Long tenantId) {
    // 1. 查询用户基础信息
    User user = userMapper.selectById(userId);
    if (user == null) {
        throw new BusinessException("用户不存在");
    }

    // 2. 并发查询关联数据
    CompletableFuture<Set<Role>> rolesFuture = CompletableFuture.supplyAsync(
        () -> new HashSet<>(getUserRoles(userId, tenantId))
    );
    
    CompletableFuture<Set<Department>> deptsFuture = CompletableFuture.supplyAsync(
        () -> new HashSet<>(getUserDepartments(userId, tenantId))
    );
    
    CompletableFuture<Set<Permission>> permsFuture = CompletableFuture.supplyAsync(
        () -> new HashSet<>(getUserPermissions(userId, tenantId))
    );
    
    CompletableFuture<Tenant> tenantFuture = CompletableFuture.supplyAsync(
        () -> tenantMapper.selectById(user.getTenantId())
    );

    // 3. 等待所有关联数据加载完成
    CompletableFuture.allOf(rolesFuture, deptsFuture, permsFuture, tenantFuture).join();

    // 4. 设置关联数据
    user.setRoles(rolesFuture.join());
    user.setDepartments(deptsFuture.join());
    user.setPermissions(permsFuture.join());
    user.setTenant(tenantFuture.join());

    return convertToVO(user);
}
```

---

## 分页查询改造

### 8.1 JPA Pageable vs MyBatis-Plus Page

| JPA | MyBatis-Plus |
|-----|--------------|
| `PageRequest.of(page, size)` | `new Page<>(page, size)` |
| `Page<T>` | `IPage<T>` |
| `page.getContent()` | `page.getRecords()` |
| `page.getTotalElements()` | `page.getTotal()` |
| `page.getTotalPages()` | `page.getPages()` |
| `page.getNumber()` | `page.getCurrent()` |

### 8.2 分页查询改造示例

**JPA方式**
```java
@Override
public PageResultVO<UserVO> queryUsers(UserQueryDTO queryDTO) {
    // JPA分页参数（page从0开始）
    Pageable pageable = PageRequest.of(
        queryDTO.getPage() - 1,  // 转换为0-based
        queryDTO.getSize(),
        Sort.by(Sort.Direction.DESC, "createTime")
    );

    // 查询
    Page<User> page = userRepository.findAll(pageable);

    // 转换
    List<UserVO> voList = page.getContent().stream()
        .map(this::convertToVO)
        .collect(Collectors.toList());

    return new PageResultVO<>(
        voList,
        page.getTotalElements(),
        page.getNumber() + 1,  // 转换为1-based
        page.getSize()
    );
}
```

**MyBatis-Plus方式**
```java
@Override
public PageResultVO<UserVO> queryUsers(UserQueryDTO queryDTO) {
    // MyBatis-Plus分页参数（page从1开始）
    Page<User> page = new Page<>(queryDTO.getPage(), queryDTO.getSize());

    // 构建查询条件
    LambdaQueryWrapper<User> wrapper = new LambdaQueryWrapper<>();
    wrapper.eq(queryDTO.getTenantId() != null, User::getTenantId, queryDTO.getTenantId())
          .like(StringUtils.hasText(queryDTO.getUsername()), 
                User::getUsername, queryDTO.getUsername())
          .orderByDesc(User::getCreateTime);

    // 查询
    Page<User> result = userMapper.selectPage(page, wrapper);

    // 转换
    List<UserVO> voList = result.getRecords().stream()
        .map(this::convertToVO)
        .collect(Collectors.toList());

    return new PageResultVO<>(
        voList,
        result.getTotal(),       // 总记录数
        result.getCurrent(),     // 当前页码（1-based）
        result.getSize()         // 每页大小
    );
}
```

### 8.3 自定义分页查询

**在Mapper接口中定义自定义分页查询**
```java
@Mapper
public interface UserMapper extends BaseMapper<User> {

    /**
     * 自定义分页查询（XML方式）
     */
    IPage<UserVO> selectUserPageByCondition(
        Page<UserVO> page,
        @Param("query") UserQueryDTO queryDTO
    );

    /**
     * 关联查询分页（带角色信息）
     */
    IPage<UserRoleVO> selectUserWithRolePage(
        Page<UserRoleVO> page,
        @Param("tenantId") Long tenantId
    );
}
```

**在XML中实现**
```xml
<select id="selectUserPageByCondition" resultType="com.qoobot.openidaas.core.vo.UserVO">
    SELECT
        u.id,
        u.username,
        u.nickname,
        u.email,
        u.mobile,
        u.status,
        u.create_time
    FROM users u
    <where>
        u.deleted = 0
        <if test="query.tenantId != null">
            AND u.tenant_id = #{query.tenantId}
        </if>
        <if test="query.username != null and query.username != ''">
            AND u.username LIKE CONCAT('%', #{query.username}, '%')
        </if>
        <if test="query.status != null">
            AND u.status = #{query.status}
        </if>
    </where>
    ORDER BY u.create_time DESC
</select>

<select id="selectUserWithRolePage" resultType="com.qoobot.openidaas.core.vo.UserRoleVO">
    SELECT
        u.id,
        u.username,
        u.nickname,
        u.status,
        r.id as role_id,
        r.role_name,
        r.role_code
    FROM users u
    LEFT JOIN user_roles ur ON u.id = ur.user_id
    LEFT JOIN roles r ON ur.role_id = r.id
    WHERE u.tenant_id = #{tenantId}
    AND u.deleted = 0
    ORDER BY u.create_time DESC
</select>
```

### 8.4 分页结果对象

**统一分页结果VO**
```java
package com.qoobot.openidaas.common.vo;

import lombok.Data;
import java.util.List;

/**
 * 分页结果VO
 *
 * @param <T> 数据类型
 */
@Data
public class PageResultVO<T> {

    /**
     * 数据列表
     */
    private List<T> records;

    /**
     * 总记录数
     */
    private Long total;

    /**
     * 当前页码（从1开始）
     */
    private Long current;

    /**
     * 每页大小
     */
    private Long size;

    /**
     * 总页数
     */
    private Long pages;

    public PageResultVO(List<T> records, Long total, Long current, Long size) {
        this.records = records;
        this.total = total;
        this.current = current;
        this.size = size;
        this.pages = (total + size - 1) / size;
    }

    /**
     * 空分页结果
     */
    public static <T> PageResultVO<T> empty() {
        return new PageResultVO<>(Collections.emptyList(), 0L, 1L, 10L);
    }
}
```

---

## 动态查询改造

### 9.1 复杂动态查询示例

#### 场景：用户高级查询

```java
package com.qoobot.openidaas.core.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.qoobot.openidaas.core.domain.User;
import com.qoobot.openidaas.core.mapper.UserMapper;
import com.qoobot.openidaas.core.dto.UserQueryDTO;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

/**
 * 用户服务实现
 */
@Service
public class UserServiceImpl {

    private final UserMapper userMapper;

    /**
     * 高级用户查询（多条件动态查询）
     */
    public PageResultVO<UserVO> advancedSearch(UserQueryDTO queryDTO) {
        // 1. 构建查询条件
        LambdaQueryWrapper<User> wrapper = buildQueryWrapper(queryDTO);

        // 2. 分页查询
        Page<User> page = userMapper.selectPage(
            new Page<>(queryDTO.getPage(), queryDTO.getSize()),
            wrapper
        );

        // 3. 转换VO
        List<UserVO> voList = page.getRecords().stream()
            .map(this::convertToVO)
            .collect(Collectors.toList());

        return new PageResultVO<>(voList, page.getTotal(), page.getCurrent(), page.getSize());
    }

    /**
     * 构建查询条件（核心方法）
     */
    private LambdaQueryWrapper<User> buildQueryWrapper(UserQueryDTO queryDTO) {
        LambdaQueryWrapper<User> wrapper = new LambdaQueryWrapper<>();

        // ========== 基础条件 ==========
        wrapper.eq(User::getDeleted, 0);  // 未删除
        wrapper.eq(queryDTO.getTenantId() != null, User::getTenantId, queryDTO.getTenantId());

        // ========== 用户名查询（精确/模糊）==========
        if (queryDTO.getUsernameExact() != null && queryDTO.getUsernameExact()) {
            // 精确查询
            wrapper.eq(StringUtils.hasText(queryDTO.getUsername()), 
                      User::getUsername, queryDTO.getUsername());
        } else {
            // 模糊查询
            wrapper.like(StringUtils.hasText(queryDTO.getUsername()), 
                        User::getUsername, queryDTO.getUsername());
        }

        // ========== 昵称查询 ==========
        wrapper.like(StringUtils.hasText(queryDTO.getNickname()), 
                    User::getNickname, queryDTO.getNickname());

        // ========== 邮箱查询 ==========
        wrapper.like(StringUtils.hasText(queryDTO.getEmail()), 
                    User::getEmail, queryDTO.getEmail());

        // ========== 手机号查询 ==========
        wrapper.like(StringUtils.hasText(queryDTO.getMobile()), 
                    User::getMobile, queryDTO.getMobile());

        // ========== 状态查询（多选）==========
        if (queryDTO.getStatuses() != null && !queryDTO.getStatuses().isEmpty()) {
            wrapper.in(User::getStatus, queryDTO.getStatuses());
        } else if (queryDTO.getStatus() != null) {
            wrapper.eq(User::getStatus, queryDTO.getStatus());
        }

        // ========== 性别查询 ==========
        wrapper.eq(queryDTO.getGender() != null, User::getGender, queryDTO.getGender());

        // ========== 年龄范围查询 ==========
        wrapper.ge(queryDTO.getMinAge() != null, User::getAge, queryDTO.getMinAge());
        wrapper.le(queryDTO.getMaxAge() != null, User::getAge, queryDTO.getMaxAge());

        // ========== 创建时间范围查询 ==========
        wrapper.ge(queryDTO.getStartTime() != null, User::getCreateTime, queryDTO.getStartTime());
        wrapper.le(queryDTO.getEndTime() != null, User::getCreateTime, queryDTO.getEndTime());

        // ========== 登录时间范围查询 ==========
        wrapper.ge(queryDTO.getLoginStartTime() != null, 
                   User::getLastLoginTime, queryDTO.getLoginStartTime());
        wrapper.le(queryDTO.getLoginEndTime() != null, 
                   User::getLastLoginTime, queryDTO.getLoginEndTime());

        // ========== 登录失败次数查询 ==========
        wrapper.ge(queryDTO.getMinLoginFailCount() != null, 
                   User::getLoginFailCount, queryDTO.getMinLoginFailCount());

        // ========== 账户锁定状态查询 ==========
        if (queryDTO.getLocked() != null) {
            if (queryDTO.getLocked()) {
                // 查询已锁定用户
                wrapper.isNotNull(User::getLockTime)
                      .gt(User::getLockTime, LocalDateTime.now());
            } else {
                // 查询未锁定用户
                wrapper.and(w -> w.isNull(User::getLockTime)
                              .or()
                              .lt(User::getLockTime, LocalDateTime.now()));
            }
        }

        // ========== 密码重置状态查询 ==========
        wrapper.eq(queryDTO.getPwdResetRequired() != null, 
                  User::getPwdResetRequired, queryDTO.getPwdResetRequired());

        // ========== 关联部门查询 ==========
        if (queryDTO.getDepartmentId() != null) {
            wrapper.apply("EXISTS (SELECT 1 FROM user_departments ud " +
                         "WHERE ud.user_id = id AND ud.dept_id = {0})", 
                         queryDTO.getDepartmentId());
        }

        // ========== 关联角色查询 ==========
        if (queryDTO.getRoleId() != null) {
            wrapper.apply("EXISTS (SELECT 1 FROM user_roles ur " +
                         "WHERE ur.user_id = id AND ur.role_id = {0})", 
                         queryDTO.getRoleId());
        }

        // ========== 排序 ==========
        if (queryDTO.getSortField() != null) {
            // 动态排序
            boolean isAsc = queryDTO.getSortOrder() == null || 
                           "asc".equalsIgnoreCase(queryDTO.getSortOrder());
            if (isAsc) {
                wrapper.orderByAsc(getSortField(queryDTO.getSortField()));
            } else {
                wrapper.orderByDesc(getSortField(queryDTO.getSortField()));
            }
        } else {
            // 默认按创建时间倒序
            wrapper.orderByDesc(User::getCreateTime);
        }

        return wrapper;
    }

    /**
     * 获取排序字段（字符串转Lambda表达式）
     */
    private SFunction<User, ?> getSortField(String sortField) {
        switch (sortField) {
            case "username":
                return User::getUsername;
            case "createTime":
                return User::getCreateTime;
            case "lastLoginTime":
                return User::getLastLoginTime;
            default:
                return User::getCreateTime;
        }
    }
}
```

### 9.2 使用XML实现复杂动态SQL

```xml
<select id="selectUserByAdvancedConditions" resultMap="BaseResultMap">
    SELECT
        u.*,
        t.tenant_name,
        d.dept_name
    FROM users u
    LEFT JOIN tenants t ON u.tenant_id = t.id
    LEFT JOIN user_departments ud ON u.id = ud.user_id
    LEFT JOIN departments d ON ud.dept_id = d.id
    <where>
        u.deleted = 0
        <if test="query.tenantId != null">
            AND u.tenant_id = #{query.tenantId}
        </if>
        
        <!-- 用户名查询 -->
        <choose>
            <when test="query.usernameExact != null and query.usernameExact">
                <if test="query.username != null and query.username != ''">
                    AND u.username = #{query.username}
                </if>
            </when>
            <otherwise>
                <if test="query.username != null and query.username != ''">
                    AND u.username LIKE CONCAT('%', #{query.username}, '%')
                </if>
            </otherwise>
        </choose>
        
        <!-- 状态查询（支持IN） -->
        <choose>
            <when test="query.statuses != null and query.statuses.size() > 0">
                AND u.status IN
                <foreach collection="query.statuses" item="status" open="(" separator="," close=")">
                    #{status}
                </foreach>
            </when>
            <when test="query.status != null">
                AND u.status = #{query.status}
            </when>
        </choose>
        
        <!-- 时间范围查询 -->
        <if test="query.startTime != null">
            AND u.create_time &gt;= #{query.startTime}
        </if>
        <if test="query.endTime != null">
            AND u.create_time &lt;= #{query.endTime}
        </if>
        
        <!-- 关联部门查询 -->
        <if test="query.departmentId != null">
            AND EXISTS (
                SELECT 1 FROM user_departments ud2 
                WHERE ud2.user_id = u.id AND ud2.dept_id = #{query.departmentId}
            )
        </if>
        
        <!-- 关联角色查询 -->
        <if test="query.roleIds != null and query.roleIds.size() > 0">
            AND EXISTS (
                SELECT 1 FROM user_roles ur
                WHERE ur.user_id = u.id AND ur.role_id IN
                <foreach collection="query.roleIds" item="roleId" open="(" separator="," close=")">
                    #{roleId}
                </foreach>
            )
        </if>
        
        <!-- 登录失败次数查询 -->
        <if test="query.minLoginFailCount != null">
            AND u.login_fail_count &gt;= #{query.minLoginFailCount}
        </if>
        
        <!-- 账户锁定状态 -->
        <choose>
            <when test="query.locked != null and query.locked">
                AND u.lock_time IS NOT NULL AND u.lock_time > NOW()
            </when>
            <when test="query.locked != null and !query.locked">
                AND (u.lock_time IS NULL OR u.lock_time &lt;= NOW())
            </when>
        </choose>
    </where>
    
    <!-- 排序 -->
    <choose>
        <when test="query.sortField != null">
            ORDER BY u.${query.sortField}
            <if test="query.sortOrder != null">
                ${query.sortOrder}
            </if>
        </when>
        <otherwise>
            ORDER BY u.create_time DESC
        </otherwise>
    </choose>
</select>
```

---

## 性能优化建议

### 10.1 批量操作优化

#### 批量插入

```java
// ❌ 低效：循环单条插入
for (User user : users) {
    userMapper.insert(user);  // N次数据库交互
}

// ✅ 高效：批量插入（使用MyBatis-Plus批量插入）
// 方式1：使用saveBatch
userMapper.insertBatchSomeColumn(users);  // 需要配置

// 方式2：自定义批量插入
@Insert("<script>" +
        "INSERT INTO users (username, email, password, tenant_id, create_time) VALUES " +
        "<foreach collection='users' item='user' separator=','>" +
        "(#{user.username}, #{user.email}, #{user.password}, #{user.tenantId}, NOW())" +
        "</foreach>" +
        "</script>")
int batchInsert(@Param("users") List<User> users);
```

#### 批量更新

```java
// ❌ 低效：循环单条更新
for (Long userId : userIds) {
    User user = new User();
    user.setId(userId);
    user.setStatus(UserStatusEnum.ACTIVE);
    userMapper.updateById(user);
}

// ✅ 高效：批量更新
@Update("<script>" +
        "UPDATE users SET status = #{status} " +
        "WHERE id IN " +
        "<foreach collection='userIds' item='id' open='(' separator=',' close=')'>" +
        "#{id}" +
        "</foreach>" +
        "</script>")
int batchUpdateStatus(@Param("userIds") List<Long> userIds, 
                      @Param("status") UserStatusEnum status);
```

### 10.2 查询性能优化

#### 1. 避免N+1查询

**问题代码**
```java
// ❌ 产生N+1查询
List<User> users = userMapper.selectList(wrapper);
for (User user : users) {
    // 每次循环都查询一次角色，产生N次额外查询
    Set<Role> roles = getUserRoles(user.getId(), user.getTenantId());
    user.setRoles(roles);
}
```

**优化代码**
```java
// ✅ 使用批量查询
List<User> users = userMapper.selectList(wrapper);
List<Long> userIds = users.stream().map(User::getId).collect(Collectors.toList());

// 批量查询所有用户角色
List<UserRole> userRoles = userRoleMapper.selectList(
    new LambdaQueryWrapper<UserRole>()
        .in(UserRole::getUserId, userIds)
);

// 分组并设置
Map<Long, List<UserRole>> userRoleMap = userRoles.stream()
    .collect(Collectors.groupingBy(UserRole::getUserId));

users.forEach(user -> {
    List<UserRole> roles = userRoleMap.getOrDefault(user.getId(), Collections.emptyList());
    user.setRoles(roles.stream().map(ur -> roleMapper.selectById(ur.getRoleId()))
                           .collect(Collectors.toSet()));
});
```

#### 2. 使用JOIN查询替代多次查询

**XML实现**
```xml
<!-- 一次查询获取用户及角色信息 -->
<select id="selectUserWithRoles" resultMap="UserWithRolesResultMap">
    SELECT
        u.*,
        r.id as role_id,
        r.role_name,
        r.role_code
    FROM users u
    LEFT JOIN user_roles ur ON u.id = ur.user_id
    LEFT JOIN roles r ON ur.role_id = r.id
    WHERE u.deleted = 0
    AND u.tenant_id = #{tenantId}
    ORDER BY u.id
</select>

<resultMap id="UserWithRolesResultMap" type="com.qoobot.openidaas.core.domain.User">
    <id column="id" property="id"/>
    <result column="username" property="username"/>
    <!-- 其他字段... -->
    
    <collection property="roles" ofType="com.qoobot.openidaas.core.domain.Role">
        <id column="role_id" property="id"/>
        <result column="role_name" property="roleName"/>
        <result column="role_code" property="roleCode"/>
    </collection>
</resultMap>
```

#### 3. 只查询需要的字段

```java
// ❌ 查询所有字段（浪费资源）
User user = userMapper.selectById(userId);

// ✅ 只查询需要的字段
LambdaQueryWrapper<User> wrapper = new LambdaQueryWrapper<>();
wrapper.select(User::getId, User::getUsername, User::getStatus)
      .eq(User::getId, userId);
User user = userMapper.selectOne(wrapper);
```

### 10.3 缓存策略

#### 启用MyBatis二级缓存

**配置类**
```java
@Configuration
public class MyBatisPlusConfig {

    @Bean
    public MybatisPlusInterceptor mybatisPlusInterceptor() {
        MybatisPlusInterceptor interceptor = new MybatisPlusInterceptor();
        // ... 其他配置
        return interceptor;
    }
}
```

**application.yml**
```yaml
mybatis-plus:
  configuration:
    cache-enabled: true  # 启用二级缓存
```

**Mapper上添加缓存注解**
```java
@CacheNamespace  // 启用二级缓存
@Mapper
public interface UserMapper extends BaseMapper<User> {
    // ...
}
```

#### 使用Spring Cache

```java
@Service
public class UserServiceImpl {

    /**
     * 缓存用户信息
     */
    @Cacheable(value = "user", key = "#userId")
    public User getUserById(Long userId) {
        return userMapper.selectById(userId);
    }

    /**
     * 更新用户（清除缓存）
     */
    @CacheEvict(value = "user", key = "#user.id")
    public void updateUser(User user) {
        userMapper.updateById(user);
    }

    /**
     * 查询用户列表（不缓存）
     */
    @Cacheable(value = "user", key = "'list:' + #tenantId")
    public List<User> listUsers(Long tenantId) {
        return userMapper.selectList(
            new LambdaQueryWrapper<User>()
                .eq(User::getTenantId, tenantId)
        );
    }
}
```

### 10.4 索引优化建议

**确保以下字段有索引**：
- 主键：`id`（已有）
- 唯一索引：`username`, `email`, `mobile`
- 租户隔离：`tenant_id`
- 常用查询条件：`status`, `create_time`, `last_login_time`
- 外键：`role_id`, `dept_id`, `permission_id`
- 组合索引：`(tenant_id, status)`, `(tenant_id, username)`

```sql
-- 创建索引
CREATE INDEX idx_users_tenant_id ON users(tenant_id);
CREATE INDEX idx_users_status ON users(status);
CREATE INDEX idx_users_username ON users(username);
CREATE INDEX idx_users_email ON users(email);
CREATE INDEX idx_users_mobile ON users(mobile);
CREATE INDEX idx_users_create_time ON users(create_time);

-- 组合索引
CREATE INDEX idx_users_tenant_status ON users(tenant_id, status);
CREATE INDEX idx_users_tenant_username ON users(tenant_id, username);

-- 中间表索引
CREATE INDEX idx_user_roles_user_id ON user_roles(user_id);
CREATE INDEX idx_user_roles_role_id ON user_roles(role_id);
CREATE INDEX idx_user_roles_tenant_id ON user_roles(tenant_id);
```

---

## 完整迁移清单

### 阶段一：准备工作（1天）

- [ ] 备份当前代码和数据库
- [ ] 创建迁移分支
- [ ] 阅读本文档，理解迁移方案
- [ ] 准备测试环境

### 阶段二：依赖和配置（0.5天）

- [ ] 修改`openidaas-core/pom.xml`
  - [ ] 移除`spring-boot-starter-data-jpa`
  - [ ] 添加`mybatis-plus-boot-starter`
- [ ] 修改`application.yml`
  - [ ] 移除JPA配置
  - [ ] 添加MyBatis-Plus配置
- [ ] 删除`JpaConfig.java`
- [ ] 创建`MyBatisPlusConfig.java`

### 阶段三：实体类改造（2-3天）

- [ ] 改造`BaseEntity`
  - [ ] 替换JPA注解为MyBatis-Plus注解
  - [ ] 添加字段填充配置
- [ ] 改造`User`
  - [ ] 替换注解
  - [ ] 移除关联字段（标记为`@TableField(exist = false)`）
- [ ] 改造`Role`
- [ ] 改造`Permission`
- [ ] 改造`Menu`
- [ ] 改造`Department`
- [ ] 改造`Tenant`
- [ ] 改造`Dict`和`DictItem`
- [ ] 改造`Application`
- [ ] 改造`AuditLog`
- [ ] 改造`DataPermission`

### 阶段四：Mapper接口改造（3-4天）

- [ ] 创建中间表实体（UserRole、UserDepartment等）
- [ ] 创建中间表Mapper
- [ ] 改造`UserRepository` → `UserMapper`
- [ ] 改造`RoleRepository` → `RoleMapper`
- [ ] 改造`PermissionRepository` → `PermissionMapper`
- [ ] 改造`MenuRepository` → `MenuMapper`
- [ ] 改造`TenantRepository` → `TenantMapper`
- [ ] 改造`DepartmentRepository` → `DepartmentMapper`
- [ ] 改造`DictRepository` → `DictMapper`
- [ ] 改造`DictItemRepository` → `DictItemMapper`
- [ ] 改造`ApplicationRepository` → `ApplicationMapper`
- [ ] 改造`AuditLogRepository` → `AuditLogMapper`

### 阶段五：Service层改造（2-3天）

- [ ] 改造`UserServiceImpl`
  - [ ] Repository → Mapper
  - [ ] `Specification` → `LambdaQueryWrapper`
  - [ ] `Pageable` → `Page`
  - [ ] 多对多关系操作
- [ ] 改造`RoleServiceImpl`
- [ ] 改造`PermissionServiceImpl`
- [ ] 改造`MenuServiceImpl`
- [ ] 改造其他Service实现类

### 阶段六：测试验证（2-3天）

- [ ] 单元测试
  - [ ] Mapper测试
  - [ ] Service测试
- [ ] 集成测试
  - [ ] 用户管理功能
  - [ ] 角色管理功能
  - [ ] 权限管理功能
- [ ] 性能测试
  - [ ] 查询性能对比
  - [ ] 批量操作测试

### 阶段七：上线部署（1天）

- [ ] 代码审查
- [ ] 灰度发布
- [ ] 监控观察
- [ ] 问题修复

---

## 常见问题FAQ

### Q1: 如何处理JPA的懒加载？

A: MyBatis-Plus没有自动懒加载，需要手动加载：

```java
// JPA方式（自动懒加载）
@Transactional
public User getUser(Long id) {
    User user = userRepository.findById(id).get();
    Set<Role> roles = user.getRoles();  // 自动加载
    return user;
}

// MyBatis-Plus方式（手动加载）
@Transactional
public User getUser(Long id) {
    User user = userMapper.selectById(id);
    // 手动加载角色
    Set<Role> roles = getUserRoles(id, user.getTenantId());
    user.setRoles(roles);
    return user;
}
```

### Q2: 如何处理JPA的级联操作？

A: 需要在业务层手动实现：

```java
// JPA CascadeType.ALL（自动级联）
user.setRoles(new HashSet<>(roles));
userRepository.save(user);  // 自动保存关联的role

// MyBatis-Plus（手动级联）
@Transactional
public void createUserWithRoles(User user, List<Long> roleIds) {
    // 1. 保存用户
    userMapper.insert(user);
    
    // 2. 保存用户角色关系
    roleIds.forEach(roleId -> {
        UserRole ur = new UserRole();
        ur.setUserId(user.getId());
        ur.setRoleId(roleId);
        userRoleMapper.insert(ur);
    });
}
```

### Q3: 事务是否需要修改？

A: 不需要，`@Transactional`注解完全兼容。

### Q4: 如何处理枚举类型？

A: MyBatis-Plus会自动处理枚举：

```java
// 实体类
@TableName("users")
public class User {
    @TableField("status")
    private UserStatusEnum status;  // 直接使用枚举
}

// 数据库存储字符串
```

### Q5: 性能会有变化吗？

A: MyBatis-Plus性能通常优于JPA：
- SQL更可控
- 减少N+1查询
- 批量操作更高效

---

## 总结

本文档提供了从JPA到MyBatis-Plus的完整迁移方案，包括：

1. ✅ **Maven依赖配置**：详细说明依赖替换和配置变更
2. ✅ **实体类注解调整**：完整的注解映射对照表和示例代码
3. ✅ **数据层接口改造**：Repository到Mapper的转换方案
4. ✅ **查询条件构建**：Specification到Wrapper的转换
5. ✅ **事务管理配置**：事务管理保持不变
6. ✅ **多对多关系处理**：中间表实体和关联操作实现
7. ✅ **分页查询改造**：Pageable到Page的转换
8. ✅ **动态查询改造**：复杂查询条件的构建方式
9. ✅ **性能优化建议**：批量操作、缓存、索引优化
10. ✅ **完整迁移清单**：分阶段迁移计划

**预计总工作量：14-20个工作日**

迁移完成后，项目将拥有：
- 统一的持久层框架
- 更好的SQL可控性
- 更优的性能表现
- 更低的维护成本
