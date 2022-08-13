package com.zhy.market.mapper;

import com.zhy.market.domain.Goods;
import org.apache.ibatis.annotations.*;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface GoodsMapper {

    @Insert("insert into goods(goodName,goodClassification,goodPrice,goodCount,goodImages,goodDescribeImages) values(#{goodName},#{goodClassification},#{goodPrice},#{goodCount},#{goodImages},#{goodDescribeImages})")
    Integer addGood(@Param("goodName") String goodName, @Param("goodClassification") Integer goodClassification,
                    @Param("goodPrice") Integer goodPrice, @Param("goodCount") Integer goodCount,
                    @Param("goodImages") String goodImages, @Param("goodDescribeImages") String goodDescribeImages);

    @Select("select * from goods where isDeleted = 0")
    List<Goods> getAllGoods();

    @Delete("update goods set isDeleted = 1 where goodId = #{goodId}")
    Integer deleteGood(Integer goodId);

    @Update("update goods set goodVisitCount = goodVisitCount + 1  where goodId = #{goodId}")
    Integer addGoodVisible(@Param("goodId") Integer goodId);

    @Update("update goods set goodSellCount = goodSellCount + #{orderGoodsNumber} where goodId = #{goodId}")
    Integer addGoodSellCount(@Param("goodId") Integer goodId, @Param("orderGoodsNumber") Integer orderGoodsNumber);

    @Update("update goods set goodCount = goodCount -  #{orderGoodsNumber} where goodId = #{goodId}")
    Integer minusGoodCount(@Param("goodId") Integer goodId, @Param("orderGoodsNumber") Integer orderGoodsNumber);

    @Select("select goodCount from goods where goodId = #{goodId} and isDeleted = 0")
    Integer getGoodCountById(@Param("goodId") Integer goodId);
}
