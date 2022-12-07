/*
 * Copyright (c) 2022. Lorem ipsum dolor sit amet, consectetur adipiscing elit.
 * Morbi non lorem porttitor neque feugiat blandit. Ut vitae ipsum eget quam lacinia accumsan.
 * Etiam sed turpis ac ipsum condimentum fringilla. Maecenas magna.
 * Proin dapibus sapien vel ante. Aliquam erat volutpat. Pellentesque sagittis ligula eget metus.
 * Vestibulum commodo. Ut rhoncus gravida arcu.
 */

package com.zhy.market.mapper;

import com.zhy.market.domain.Watermeterwell;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * @author Administrator
 */
@Repository
public interface WaterMeterWellMapper {
    @Select("select * from watermeterwell where status = '1'")
    List<Watermeterwell> getAllWaterMeterWellInfo();

    @Select("select count(*) from watermeterwell where status = '1'")
    Integer getAllWaterMeterWellInfoCount();

    @Update("update watermeterwell set status = '2' where id = #{id}")
    Integer deleteWaterMeterWell(@Param("id")String id);

    @Select("select * from watermeterwell where FilledBy like #{FilledBy}")
    List<Watermeterwell> getWaterMeterWellInfoByFilled();

    @Update("update watermeterwell set repairInfo = #{repairInfo} where id = #{id} ")
    Integer updateWaterMeterWellRepairInfoWithId(@Param("repairInfo") String repairInfo, @Param("id") Integer id);

    @Select("select repairInfo from watermeterwell where id = #{id}")
    String getWaterMeterWellRepairInfoById(@Param("id") Integer id);

    // @Insert("insert into watermeterwell set ")

    // 在watermeterwell表中插入一条数据
    @Insert("insert into watermeterwell set" +
            " filledBy = #{filledBy}," +
            " department = ''," +
            " writtingTime = #{writtingTime}," +
            " userType = ''," +
            " accountIdentifier = #{accountIdentifier}," +
            " accountName = #{accountName}," +
            " accountNumber = #{accountNumber}," +
            " address = #{address}," +
            " coordinates = #{coordinates}," +
            " caliber = #{caliber}," +
            " wellDepth = #{wellDepth}," +
            " includedFacilities = #{includedFacilities}," +
            " waterMeterManufacturer = #{waterMeterManufacturer}," +
            " operatingStatus = #{operatingStatus}," +
            " wellOutside = ''," +
            " wellInside = ''," +
            " waterNature = #{waterNature}," +
            " operatingUsersNumber = ''," +
            " operatingUserDetailsAndDetailedAddress = ''," +
            " specialBankUsersNumber = ''," +
            " specialBankUserDetailsAndDetailedAddress = '',")
    Integer addWaterMeterWell(@Param("filledBy") String filledBy,
                              @Param("accountName") String accountName,
                              @Param("accountNumber") String accountNumber,
                              @Param("address") String address,
                              @Param("coordinates") String coordinates,
                              @Param("caliber") String caliber,
                              @Param("operatingStatus") String operatingStatus,
                              @Param("waterNature") String waterNature,
                              @Param("wellDepth") String wellDepth,
                              @Param("includedFacilities") String includedFacilities,
                              @Param("waterMeterManufacturer") String waterMeterManufacturer,
                              @Param("accountIdentifier") String accountIdentifier,
                              @Param("writtingTime")String writtingTime);
}
