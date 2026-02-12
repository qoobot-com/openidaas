package com.qoobot.openidaas.auth.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.qoobot.openidaas.auth.entity.MFABackupCode;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;

import java.util.List;

/**
 * MFA备用码Mapper
 *
 * @author QooBot
 */
@Mapper
public interface MFABackupCodeMapper extends BaseMapper<MFABackupCode> {

    /**
     * 根据用户ID和因子ID查询所有备用码
     *
     * @param userId 用户ID
     * @param factorId 因子ID
     * @return 备用码列表
     */
    @Select("SELECT * FROM mfa_backup_codes WHERE user_id = #{userId} AND factor_id = #{factorId} AND is_used = 0 ORDER BY id ASC")
    List<MFABackupCode> selectUnusedByUserIdAndFactorId(@Param("userId") Long userId, @Param("factorId") Long factorId);

    /**
     * 查询未使用的备用码数量
     *
     * @param userId 用户ID
     * @param factorId 因子ID
     * @return 数量
     */
    @Select("SELECT COUNT(*) FROM mfa_backup_codes WHERE user_id = #{userId} AND factor_id = #{factorId} AND is_used = 0")
    int countUnusedByUserIdAndFactorId(@Param("userId") Long userId, @Param("factorId") Long factorId);

    /**
     * 删除用户的所有备用码
     *
     * @param userId 用户ID
     * @param factorId 因子ID
     * @return 删除数量
     */
    int deleteByUserIdAndFactorId(@Param("userId") Long userId, @Param("factorId") Long factorId);
}
