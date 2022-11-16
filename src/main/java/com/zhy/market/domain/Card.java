package com.zhy.market.domain;

import lombok.Data;

/**
 * @author Administrator
 */
@Data
public class Card {
    public Integer id;
    public String ssmc; // 设施名称
    public String ggxh; // 规格型号
    public String syzt; // 使用状态
    public String ssgx; // 所属管线
    public String jsbh; // 井室编号
    public String jslx; // 井室类型
    public String jszb; // 井室坐标
    public String yxzt; // 运行状态
    public String jsqk; // 井室情况
    public String kgfx; // 开关方向
    public String xdwz; // 相对位置
    public String js; // 井深
    public String sccj; // 生产厂家
    public String azsj; // 安装时间
}
