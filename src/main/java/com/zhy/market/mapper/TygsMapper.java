/*
 * Copyright (c) 2022. Lorem ipsum dolor sit amet, consectetur adipiscing elit.
 * Morbi non lorem porttitor neque feugiat blandit. Ut vitae ipsum eget quam lacinia accumsan.
 * Etiam sed turpis ac ipsum condimentum fringilla. Maecenas magna.
 * Proin dapibus sapien vel ante. Aliquam erat volutpat. Pellentesque sagittis ligula eget metus.
 * Vestibulum commodo. Ut rhoncus gravida arcu.
 */

package com.zhy.market.mapper;

import com.zhy.market.domain.Tygs;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface TygsMapper {
    @Select("select * from tygs")
    List<Tygs> getAllInfo();

    @Select("select * from tygs where FilledBy like #{FilledBy}")
    List<Tygs> getInfoByFilled();

    @Update("update tygs set repairInfo = #{repairInfo} where id = #{id} ")
    Integer updateRepairInfoWithId(@Param("repairInfo") String repairInfo, @Param("id") Integer id);

    @Select("select repairInfo from tygs where id = #{id}")
    String getRepairInfoById(@Param("id") Integer id);
}

