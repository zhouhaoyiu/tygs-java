package com.zhy.market.mapper;

import com.zhy.market.domain.Weihuzongguan;
import org.apache.ibatis.annotations.Select;
import org.springframework.stereotype.Repository;

import java.util.List;
@Repository
public interface WeihuzongguanMapper {
        // 获得所有的信息
        @Select("select * from weihuzongguan")
        public List<Weihuzongguan> getAllWeihuzongguan();
}
