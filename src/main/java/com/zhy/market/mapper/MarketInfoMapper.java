package com.zhy.market.mapper;

import com.zhy.market.domain.MarketInfo;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface MarketInfoMapper {

    @Select("select * from marketInfo limit 0,1")
    List<MarketInfo> getMarketInfo();

    @Update("update marketInfo set marketName = #{marketName},marketRecommend = #{marketRecommend},marketMeta = #{marketMeta},marketImages=#{marketImages} ")
    int setMarketInfo(@Param("marketName") String marketName, @Param("marketRecommend") String marketRecommend, @Param("marketMeta") String marketMeta, @Param("marketImages") String marketImages);
}
