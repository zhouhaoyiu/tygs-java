package com.zhy.market.mapper;

import com.zhy.market.domain.Weihuzongguan;
import java.util.List;
import org.apache.ibatis.annotations.Select;
import org.springframework.stereotype.Repository;

@Repository
public interface WeihuzongguanMapper {
    // 获得所有的信息
    @Select("select * from weihuzongguan")
    public List<Weihuzongguan> getAllWeihuzongguan();
}
