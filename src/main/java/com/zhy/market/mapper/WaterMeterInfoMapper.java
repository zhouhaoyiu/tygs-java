/*
 * Copyright (c) 2022. Lorem ipsum dolor sit amet, consectetur adipiscing elit.
 * Morbi non lorem porttitor neque feugiat blandit. Ut vitae ipsum eget quam lacinia accumsan.
 * Etiam sed turpis ac ipsum condimentum fringilla. Maecenas magna.
 * Proin dapibus sapien vel ante. Aliquam erat volutpat. Pellentesque sagittis ligula eget metus.
 * Vestibulum commodo. Ut rhoncus gravida arcu.
 */

package com.zhy.market.mapper;

import com.zhy.market.domain.WaterMeterInfo;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface WaterMeterInfoMapper {

    @Select("select * from watermeterinfo where wallId = #{wallId} and waterMeterFrom = 'watermeterwell' ")
    List<WaterMeterInfo> getWatermeterwellWaterMeterInfoByWallId(@Param("wallId") Integer wallId);

    @Insert("insert into watermeterinfo(waterMeterId,wallId,paymentNumber,accountNumber,accountName,status,updateTime,waterMeterFrom) values(#{waterMeterId},#{wallId},#{paymentNumber},#{accountNumber},#{accountName},#{status},#{updateTime},'watermeterwell')")
    Integer insertWatermeterwellWaterMeterInfo(String waterMeterId, Integer wallId, String paymentNumber, String accountNumber, String accountName, String status, String updateTime);

    @Insert("insert into watermeterinfo(waterMeterId,wallId,paymentNumber,accountNumber,accountName,status,updateTime,waterMeterFrom) values(#{waterMeterId},#{wallId},#{paymentNumber},#{accountNumber},#{accountName},#{status},#{updateTime},'valuewell')")
    Integer insertValuewellWaterMeterInfo(String waterMeterId, Integer wallId, String paymentNumber, String accountNumber, String accountName, String status, String updateTime);
}
