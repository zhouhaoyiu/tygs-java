package com.zhy.market.mapper;

import com.zhy.market.domain.Admin;
import com.zhy.market.domain.AdminInfo;
import org.apache.ibatis.annotations.Delete;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * @author Administrator
 */
@Repository
public interface AdminMapper {
    @Select("select userName,emailAddress,adminUUid,phoneNumber,adminRole from admin")
    List<AdminInfo> getAllAdminsInfo();

    @Delete("delete from admin where adminUUid = #{adminUUid}")
    Integer deleteAdmin(String adminUUid);

    @Select("select * from admin where userName = #{userName}")
    List<Admin> adminLogin(String userName);

    @Insert("insert into admin (userName, passWord, emailAddress, phoneNumber, adminUUid, adminRole) values (#{userName}, #{passWord}, #{emailAddress}, #{phoneNumber}, #{adminUUid}, #{role})")
    Integer adminRegis(@Param("userName") String userName, @Param("passWord") String passWord,
                       @Param("emailAddress") String emailAddress,
                       @Param("phoneNumber") String phoneNumber, @Param("adminUUid") String adminUUid,
                       @Param("role") Integer role);

    @Select("select COUNT(*) from admin where userName = #{userName}")
    Integer checkUserName(String userName);

}
