package com.zhy.market.mapper;

import com.zhy.market.domain.ThreeSupply;
import java.util.List;
import org.apache.ibatis.annotations.Select;
import org.springframework.stereotype.Repository;

/**
 * @author Administrator
 */
@Repository
public interface ThreeSupplyMapper {

    @Select("select * from ThreeSupply")
    List<ThreeSupply> getAllThreeSupplyInfo();
}
