package com.zhy.market.mapper;

import com.zhy.market.domain.ValueWall;
import com.zhy.market.domain.Watermeterwell;
import org.apache.ibatis.annotations.Select;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ValueWallMapper {

    @Select("select * from valuewall")
    List<ValueWall> getAllValueWallInfo();



}
