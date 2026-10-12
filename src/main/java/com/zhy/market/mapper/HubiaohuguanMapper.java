package com.zhy.market.mapper;

import com.zhy.market.domain.Hubiaohuguan;
import java.util.List;
import org.apache.ibatis.annotations.Select;
import org.springframework.stereotype.Repository;

@Repository
public interface HubiaohuguanMapper {

    // 获得所有的信息
    @Select("select * from hubiaohuguan")
    public List<Hubiaohuguan> getAllHubiaohuguan();
}
