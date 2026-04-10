package com.priceradar.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.priceradar.domain.entity.AdminOperationLog;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface AdminOperationLogMapper extends BaseMapper<AdminOperationLog> {
}
