package com.businessreviews.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.businessreviews.model.dataobject.PrivateMessageDO;
import org.apache.ibatis.annotations.Mapper;

/**
 * 私信消息Mapper
 */
@Mapper
public interface PrivateMessageMapper extends BaseMapper<PrivateMessageDO> {
}
