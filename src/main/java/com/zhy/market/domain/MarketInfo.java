package com.zhy.market.domain;

import lombok.Data;

@Data
public class MarketInfo {
    public String getMarketName() {
        return marketName;
    }

    public void setMarketName(String marketName) {
        this.marketName = marketName;
    }

    public String getMarketRecommend() {
        return marketRecommend;
    }

    public void setMarketRecommend(String marketRecommend) {
        this.marketRecommend = marketRecommend;
    }

    public String getMarketMeta() {
        return marketMeta;
    }

    public void setMarketMeta(String marketMeta) {
        this.marketMeta = marketMeta;
    }

    public String getMarketImages() {
        return marketImages;
    }

    public void setMarketImages(String marketImages) {
        this.marketImages = marketImages;
    }

    public String marketName;
    public String marketRecommend;
    public String marketMeta;
    public String marketImages;
}
