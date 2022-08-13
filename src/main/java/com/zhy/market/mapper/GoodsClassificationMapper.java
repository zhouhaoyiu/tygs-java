package com.zhy.market.mapper;


import com.zhy.market.domain.GoodsClassification;
import org.apache.ibatis.annotations.Delete;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface GoodsClassificationMapper {

    @Insert("INSERT into goodsclassification (classificationName, parentId) values (#{classificationName}, #{parentId})")
    Integer addClassification(@Param("classificationName")String classificationName,@Param("parentId")Integer parentId);

    @Select("select * from goodsclassification")
    List<GoodsClassification> getAllGoodsClassification();

    @Select("select count(*) from goodsclassification where classificationId = #{parentId}")
    Integer checkIdExist(@Param("parentId")Integer parentId);

    @Delete("delete")
    Integer deleteGoodsClassification(@Param("id")Integer id);
}
