package com.zhy.market.mapper;


import com.zhy.market.domain.AdminLog;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface AdminLogMapper {

    @Insert("insert into adminLog(adminLogUUid, adminUUid, logTime, info, remark )values ( #{adminLogUUid}, #{adminUUid}, #{logTime},#{info},#{remark}) ")
    Integer addAdminLog(@Param("adminLogUUid") String adminLogUUid, @Param("adminUUid") String adminUUid, @Param("logTime") String logTime, @Param("info") String info, @Param("remark") String remark);

    @Select("select * from adminLog")
    List<AdminLog> getAllAdminLog();

    @Select("select * from adminLog where adminUUid = #{adminUUid}")
    List<AdminLog> getAdminLogByAdminUUid(@Param("adminUUid") String adminUUid);
}
