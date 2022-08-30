package com.zhy.market.mapper;

import com.zhy.market.domain.WaterMeterRoom;

import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface WaterMeterRoomMapper {

    @Select("select * from watermeterroom")
    List<WaterMeterRoom> getAllWaterMeterRoomInfo();

    @Select("select count(*) from watermeterroom")
    Integer getAllWaterMeterRoomInfoCount();

    @Select("select * from watermeterwell where FilledBy like #{filledBy}")
    List<WaterMeterRoom> getWaterMeterRoomInfoByFilled();

    @Update("update watermeterroom set repairInfo = #{repairInfo} where id = #{id} ")
    Integer updateWaterMeterRoomRepairInfoWithId(@Param("repairInfo") String repairInfo, @Param("id") Integer id);

    @Select("select repairInfo from watermeterroom where id = #{id}")
    String getWaterMeterRoomRepairInfoById(@Param("id") Integer id);
}
