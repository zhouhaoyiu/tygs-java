/*
 * Copyright (c) 2022. Lorem ipsum dolor sit amet, consectetur adipiscing elit.
 * Morbi non lorem porttitor neque feugiat blandit. Ut vitae ipsum eget quam lacinia accumsan.
 * Etiam sed turpis ac ipsum condimentum fringilla. Maecenas magna.
 * Proin dapibus sapien vel ante. Aliquam erat volutpat. Pellentesque sagittis ligula eget metus.
 * Vestibulum commodo. Ut rhoncus gravida arcu.
 */

package com.zhy.market.mapper;

import com.zhy.market.domain.Watermeterwell;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface WaterMeterWellMapper {
    @Select("select * from watermeterwell")
    List<Watermeterwell> getAllWaterMeterWellInfo();

    @Select("select count(*) from watermeterwell")
    Integer getAllWaterMeterWellInfoCount();

    @Select("select * from watermeterwell where FilledBy like #{FilledBy}")
    List<Watermeterwell> getWaterMeterWellInfoByFilled();

    @Update("update watermeterwell set repairInfo = #{repairInfo} where id = #{id} ")
    Integer updateWaterMeterWellRepairInfoWithId(@Param("repairInfo") String repairInfo, @Param("id") Integer id);

    @Select("select repairInfo from watermeterwell where id = #{id}")
    String getWaterMeterWellRepairInfoById(@Param("id") Integer id);
}

