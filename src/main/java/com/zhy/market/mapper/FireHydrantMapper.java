package com.zhy.market.mapper;

import java.util.List;

import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;
import org.springframework.stereotype.Repository;

import com.zhy.market.domain.FireHydrant;

/**
 * @author Administrator
 */
@Repository
public interface FireHydrantMapper {

  @Select("select * from firehydrant")
  List<FireHydrant> getAllFireHydrantInfo();

  @Select("select count(*) from firehydrant")
  Integer getAllFireHydrantInfoCount();

  @Select("select * from firehydrant where FilledBy like #{FilledBy}")
  List<FireHydrant> getFireHydrantInfoByFilled();

  @Select("select repairInfo from firehydrant where id = #{id}")
  String getFireHydrantRepairInfoById(@Param("id") Integer id);

  @Update("update firehydrant set repairInfo = #{repairInfo} where id = #{id} ")
  Integer updateFireHydrantRepairInfoWithId(@Param("repairInfo") String repairInfo, @Param("id") Integer id);
}
