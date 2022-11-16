package com.zhy.market.mapper;

import com.zhy.market.domain.ThreeSupply;
import org.apache.ibatis.annotations.Select;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * @author Administrator
 */
@Repository
public interface ThreeSupplyMapper {

    @Select("select * from ThreeSupply")
    List<ThreeSupply> getAllThreeSupplyInfo();
}
