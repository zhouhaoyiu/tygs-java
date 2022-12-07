package com.zhy.market.mapper;

import org.apache.ibatis.annotations.Select;
import org.springframework.stereotype.Repository;

/**
 * @author Administrator
 */
@Repository
public interface RsaKeyMapper {
    @Select("select publicKey from rsakey limit 1")
    String getPublicKey();

    @Select("select privateKey from rsakey limit 1")
    String getPrivateKey();
}
