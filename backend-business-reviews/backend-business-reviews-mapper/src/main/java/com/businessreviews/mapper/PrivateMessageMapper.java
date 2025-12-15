package com.businessreviews.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.businessreviews.entity.PrivateMessage;
import org.apache.ibatis.annotations.Mapper;

/**
 * 私信消息Mapper
 */
@Mapper
public interface PrivateMessageMapper extends BaseMapper<PrivateMessage> {
}
