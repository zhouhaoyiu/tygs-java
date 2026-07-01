package com.zhy.market.mapper;

import com.zhy.market.domain.Hubiaohuguan;
import org.apache.ibatis.annotations.Select;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface HubiaozongguanMapper {

    // 获得所有的信息
    @Select("select * from hubiaozongguan")
    public List<Hubiaohuguan> getAllHubiaozongguan();


}
