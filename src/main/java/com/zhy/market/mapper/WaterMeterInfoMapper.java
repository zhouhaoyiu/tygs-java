package com.zhy.market.mapper;

import com.zhy.market.domain.WaterMeterInfo;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface WaterMeterInfoMapper {

    @Select("select * from watermeterinfo where wallId = #{wallId} and waterMeterFrom = 'watermeterwell' and status = 1")
    List<WaterMeterInfo> getWaterMeterWellWaterMeterInfoByWallId(@Param("wallId") Integer wallId);

    @Select("select * from watermeterinfo where wallId = #{wallId} and waterMeterFrom = 'watermeterroom' and status = 1")
    List<WaterMeterInfo> getWatermeterRoomWaterMeterInfoByWallId(@Param("wallId") Integer wallId);

    @Select("select * from watermeterinfo where wallId = #{wallId} and waterMeterFrom = 'valuewell' and status = 1")
    List<WaterMeterInfo> getValueWellWaterMeterInfoByWallId(@Param("wallId") Integer wallId);

    @Select("select * from watermeterinfo where wallId = #{wallId} and waterMeterFrom = 'firehydrant' and status = 1")
    List<WaterMeterInfo> getFireHydrantWaterMeterInfoByWallId(@Param("wallId") Integer wallId);

    @Insert("insert into watermeterinfo(waterMeterId,wallId,paymentNumber,accountNumber,accountName,status,nature,updateTime,waterMeterFrom) values(#{waterMeterId},#{wallId},#{paymentNumber},#{accountNumber},#{accountName},'1',#{nature},#{updateTime},'watermeterwell')")
    Integer insertWaterMeterWellWaterMeterInfo(String waterMeterId, Integer wallId, String paymentNumber,
            String accountNumber, String accountName, String nature, String updateTime);

    @Insert("insert into watermeterinfo(waterMeterId,wallId,paymentNumber,accountNumber,accountName,status,nature,updateTime,waterMeterFrom) values(#{waterMeterId},#{wallId},#{paymentNumber},#{accountNumber},#{accountName},'1',#{nature},#{updateTime},'watermeterroom')")
    Integer insertWaterMeterRoomInfoWaterMeterInfo(String waterMeterId, Integer wallId, String paymentNumber,
            String accountNumber, String accountName, String nature, String updateTime);

    @Insert("insert into watermeterinfo(waterMeterId,wallId,paymentNumber,accountNumber,accountName,status,nature,updateTime,waterMeterFrom) values(#{waterMeterId},#{wallId},#{paymentNumber},#{accountNumber},#{accountName},'1',#{nature},#{updateTime},'valuewell')")
    Integer insertValueWellWaterMeterInfo(String waterMeterId, Integer wallId, String paymentNumber,
            String accountNumber, String accountName, String nature, String updateTime);

    @Insert("insert into watermeterinfo(waterMeterId,wallId,paymentNumber,accountNumber,accountName,status,nature,updateTime,waterMeterFrom) values(#{waterMeterId},#{wallId},#{paymentNumber},#{accountNumber},#{accountName},'1',#{nature},#{updateTime},'firehydrant')")
    Integer insertFireHydrantWaterMeterInfo(String waterMeterId, Integer wallId, String paymentNumber,
            String accountNumber, String accountName, String nature, String updateTime);

    @Update("update watermeterinfo set status = #{status} where waterMeterId = #{waterMeterId}")
    Integer updateWaterMeterInfoStatus(String waterMeterId, String status);
}
