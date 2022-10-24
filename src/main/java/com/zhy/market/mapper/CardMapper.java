package com.zhy.market.mapper;

import java.util.List;

import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.springframework.stereotype.Repository;

import com.zhy.market.domain.Card;

@Repository
public interface CardMapper {

  @Insert("insert into card (ssmc,ggxh,syzt,ssgx,jsbh,jslx,jszb,yxzt,jsqk,kgfx,xdwz,js,sccj,azsj)" +
      "values (#{ssmc},#{ggxh},#{syzt},#{ssgx},#{jsbh},#{jslx},#{jszb},#{yxzt},#{jsqk},#{kgfx},#{xdwz},#{js},#{sccj},#{azsj})")
  public Integer addCard(@Param("ssmc") String ssmc, @Param("ggxh") String ggxh,
      @Param("syzt") String syzt, @Param("ssgx") String ssgx, @Param("jsbh") String jsbh,
      @Param("jslx") String jslx, @Param("jszb") String jszb, @Param("yxzt") String yxzt,
      @Param("jsqk") String jsqk, @Param("kgfx") String kgfx, @Param("xdwz") String xdwz,
      @Param("js") String js, @Param("sccj") String sccj, @Param("azsj") String azsj);

      // 获得所有的卡片信息
    @Select("select * from card")
    public List<Card> getAllCard();

}
