package com.zhy.market.mapper;

import com.zhy.market.domain.Sheet1;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface Sheet1Mapper {

    @Select("select * from sheet1 limit #{start}, #{size}")
    List<Sheet1> selectInfoByPage(@Param("start") Integer start, @Param("size") Integer size);

    @Select("select count(*) from sheet1 where 1=1")
    Integer selectInfoCount();

    @Select("select * from sheet1 where mc like concat('%', #{name}, '%') limit #{start}, #{size}")
    List<Sheet1> selectInfoByPageAndName(@Param("start") Integer start, @Param("size") Integer size,
                                         @Param("name") String name);

    // 获得通过名字查询的记录数量
    @Select("select count(*) from sheet1 where mc like concat('%', #{name}, '%')")
    Integer selectInfoByPageAndNameCount(@Param("name") String name);

    @Select("select * from sheet1 where jfh like concat('%', #{jfh}, '%') limit #{start}, #{size}")
    List<Sheet1> selectInfoByPageAndJfh(@Param("start") Integer start, @Param("size") Integer size,
                                        @Param("jfh") String jfh);

    @Select("select count(*) from sheet1 where jfh like concat('%', #{jfh}, '%')")
    Integer selectInfoByPageAndJfhCount(@Param("jfh") String jfh);

    @Select("select * from sheet1 where hh like concat('%', #{hh}, '%') limit #{start}, #{size}")
    List<Sheet1> selectInfoByPageAndHh(@Param("start") Integer start, @Param("size") Integer size,
                                       @Param("hh") String hh);

    @Select("select count(*) from sheet1 where hh like concat('%', #{hh}, '%')")
    Integer selectInfoByPageAndHhCount(@Param("hh") String hh);

    @Select("select * from sheet1 where dz like concat('%', #{dz}, '%') limit #{start}, #{size}")
    List<Sheet1> selectInfoByPageAndDz(@Param("start") Integer start, @Param("size") Integer size,
                                       @Param("dz") String dz);

    @Select("select count(*) from sheet1 where dz like concat('%', #{dz}, '%')")
    Integer selectInfoByPageAndDzCount(@Param("dz") String dz);
}
