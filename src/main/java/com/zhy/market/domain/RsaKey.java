package com.zhy.market.domain;

import lombok.Data;

@Data
public class RsaKey {
    private String publicKey;
    private String privateKey;


    public String getPublicKey() {
        return publicKey;
    }

    public void setPublicKey(String publicKey) {
        this.publicKey = publicKey;
    }

    public String getPrivateKey() {
        return privateKey;
    }

    public void setPrivateKey(String privateKey) {
        this.privateKey = privateKey;
    }


}
