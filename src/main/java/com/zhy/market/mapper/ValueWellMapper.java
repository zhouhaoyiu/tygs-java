package com.zhy.market.mapper;

import java.util.List;

import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;
import org.springframework.stereotype.Repository;

import com.zhy.market.domain.ValueWell;

@Repository
public interface ValueWellMapper {

  @Select("select * from valuewell")
  List<ValueWell> getAllValueWellInfo();

  @Select("select count(*) from valuewell")
  Integer getAllValueWellInfoCount();

  @Select("select * from valuewell where FilledBy like #{filledBy}")
  List<ValueWell> getValueWellInfoByFilled();

  @Update("update valuewell set repairInfo = #{repairInfo} where id = #{id} ")
  Integer updateValueWellRepairInfoWithId(@Param("repairInfo") String repairInfo, @Param("id") Integer id);

  @Select("select repairInfo from valuewell where id = #{id}")
  String getValueWellRepairInfoById(@Param("id") Integer id);
}
