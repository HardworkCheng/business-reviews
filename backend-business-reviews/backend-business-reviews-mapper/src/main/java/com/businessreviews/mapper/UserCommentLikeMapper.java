package com.businessreviews.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.businessreviews.model.dataobject.UserCommentLikeDO;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface UserCommentLikeMapper extends BaseMapper<UserCommentLikeDO> {
}
