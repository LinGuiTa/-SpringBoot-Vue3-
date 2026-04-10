package com.priceradar.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.priceradar.domain.entity.PriceRecord;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

@Mapper
public interface PriceRecordMapper extends BaseMapper<PriceRecord> {

    @Select("SELECT DATE(recorded_at) as stat_date, MIN(price) as min_price " +
            "FROM t_price_record " +
            "WHERE product_id = #{productId} AND platform_id = #{platformId} " +
            "AND recorded_at >= #{startDate} " +
            "GROUP BY DATE(recorded_at) " +
            "ORDER BY stat_date ASC")
    List<Map<String, Object>> selectDailyMinPrice(@Param("productId") Long productId,
                                                   @Param("platformId") Long platformId,
                                                   @Param("startDate") LocalDateTime startDate);
}
