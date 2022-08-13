package com.zhy.market.mapper;

import com.zhy.market.domain.User;
import com.zhy.market.domain.UserInfo;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface UserMapper {

    @Select("select * from user where username=#{username} and password=#{password}")
    List<UserInfo> userLogin(@Param("username") String username, @Param("password") String password);

    @Insert("insert into user(username,password,address,phonenumber,useruuid,gender) values(#{username},#{password},#{address},#{phonenumber},#{useruuid},#{gender} )")
    Integer userRegis(@Param("username") String username, @Param("password") String password, @Param("gender") Boolean gender, @Param("address") String address, @Param("phonenumber") String phonenumber, @Param("useruuid") String useruuid);

    @Select("select userid,useruuid,username,address,phonenumber,gender from user")
    List<User> getAllUser();
}
