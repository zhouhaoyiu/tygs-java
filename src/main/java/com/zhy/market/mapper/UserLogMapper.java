package com.zhy.market.mapper;


import com.zhy.market.domain.UserLog;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface UserLogMapper {
    @Insert("insert into userLog(loguuid,useruuid,type,gid,logtime) values (#{loguuid},#{useruuid},#{type},#{gid},#{logtime})")
    Integer addUserLog(@Param("loguuid") String loguuid, @Param("useruuid") String useruuid, @Param("type") String type, @Param("gid") Integer gid, @Param("logtime") String logtime);

    @Select("select * from userLog")
    List<UserLog> getAllUserLog();
}
