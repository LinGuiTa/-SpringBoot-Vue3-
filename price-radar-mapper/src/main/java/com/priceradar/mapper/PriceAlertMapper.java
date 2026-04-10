package com.priceradar.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.priceradar.domain.entity.PriceAlert;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface PriceAlertMapper extends BaseMapper<PriceAlert> {
}
