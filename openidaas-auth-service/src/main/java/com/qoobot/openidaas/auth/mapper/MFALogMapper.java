package com.qoobot.openidaas.auth.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.qoobot.openidaas.auth.entity.MFALog;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.time.LocalDateTime;
import java.util.List;

/**
 * MFA日志Mapper
 *
 * @author QooBot
 */
@Mapper
public interface MFALogMapper extends BaseMapper<MFALog> {

    /**
     * 查询用户最近的MFA验证日志
     *
     * @param userId 用户ID
     * @param limit 数量
     * @return 日志列表
     */
    List<MFALog> selectRecentByUserId(@Param("userId") Long userId, @Param("limit") int limit);

    /**
     * 统计用户最近的失败验证次数
     *
     * @param userId 用户ID
     * @param since 起始时间
     * @return 失败次数
     */
    @Select("SELECT COUNT(*) FROM mfa_logs WHERE user_id = #{userId} AND result = 'FAILURE' AND verified_at >= #{since}")
    int countFailedSince(@Param("userId") Long userId, @Param("since") LocalDateTime since);
}
