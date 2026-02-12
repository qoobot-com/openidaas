package com.qoobot.openidaas.user.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.qoobot.openidaas.user.entity.UserProfile;
import org.apache.ibatis.annotations.Mapper;

/**
 * 用户档案Mapper接口
 *
 * @author QooBot
 */
@Mapper
public interface UserProfileMapper extends BaseMapper<UserProfile> {
}
