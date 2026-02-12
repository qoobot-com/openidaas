package com.qoobot.openidaas.auth.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.qoobot.openidaas.auth.entity.UserMFAFactor;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

/**
 * 用户MFA因子Mapper
 *
 * @author QooBot
 */
@Mapper
public interface UserMFAFactorMapper extends BaseMapper<UserMFAFactor> {

    /**
     * 根据用户ID查询所有MFA因子
     *
     * @param userId 用户ID
     * @return MFA因子列表
     */
    List<UserMFAFactor> selectByUserId(@Param("userId") Long userId);

    /**
     * 根据用户ID和类型查询MFA因子
     *
     * @param userId 用户ID
     * @param factorType MFA类型
     * @return MFA因子
     */
    UserMFAFactor selectByUserIdAndType(@Param("userId") Long userId, @Param("factorType") String factorType);

    /**
     * 查询用户的主MFA因子
     *
     * @param userId 用户ID
     * @return 主MFA因子
     */
    @Select("SELECT * FROM user_mfa_factors WHERE user_id = #{userId} AND is_primary = 1 AND status = 1 LIMIT 1")
    UserMFAFactor selectPrimaryByUserId(@Param("userId") Long userId);

    /**
     * 查询用户的激活MFA因子数量
     *
     * @param userId 用户ID
     * @return 数量
     */
    @Select("SELECT COUNT(*) FROM user_mfa_factors WHERE user_id = #{userId} AND status = 1")
    int countActiveByUserId(@Param("userId") Long userId);
}
