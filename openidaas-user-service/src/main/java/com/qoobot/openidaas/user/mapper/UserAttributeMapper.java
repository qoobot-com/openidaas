package com.qoobot.openidaas.user.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.qoobot.openidaas.user.entity.UserAttribute;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;

/**
 * 用户扩展属性Mapper接口
 *
 * @author QooBot
 */
@Mapper
public interface UserAttributeMapper extends BaseMapper<UserAttribute> {

    /**
     * 查询用户的所有扩展属性
     *
     * @param userId 用户ID
     * @return 属性列表
     */
    List<UserAttribute> selectByUserId(Long userId);

    /**
     * 查询用户的指定属性
     *
     * @param userId 用户ID
     * @param attrKey 属性键
     * @return 属性信息
     */
    UserAttribute selectByKey(@org.apache.ibatis.annotations.Param("userId") Long userId,
                               @org.apache.ibatis.annotations.Param("attrKey") String attrKey);

    /**
     * 删除用户的所有扩展属性
     *
     * @param userId 用户ID
     * @return 影响行数
     */
    int deleteByUserId(Long userId);
}
