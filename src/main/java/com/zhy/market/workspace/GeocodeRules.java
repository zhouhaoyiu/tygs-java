package com.zhy.market.workspace;

import static com.zhy.market.workspace.WorkspaceErrors.bad;

import java.text.Normalizer;
import java.util.List;
import java.util.Locale;
import java.util.Objects;
import java.util.Set;
import java.util.regex.Pattern;

public final class GeocodeRules {
    public record Candidate(
            String formattedAddress,
            String province,
            String city,
            String district,
            String adcode,
            String street,
            String number,
            String level,
            Double longitude,
            Double latitude) {}

    public record ProviderResult(
            String status,
            String info,
            int resultCount,
            List<Candidate> candidates,
            Object rawResponse) {}

    public record Decision(String status, String reason, Double longitude, Double latitude) {}

    private static final List<String> DISTRICTS =
            List.of("小店区", "迎泽区", "杏花岭区", "尖草坪区", "万柏林区", "晋源区", "清徐县", "阳曲县", "娄烦县", "古交市");
    private static final Pattern NUMBERED = Pattern.compile(".+(?:路|街|巷|道|弄)\\d+(?:-\\d+)?号");

    private GeocodeRules() {}

    static String compact(String value) {
        return Normalizer.normalize(Objects.toString(value, ""), Normalizer.Form.NFKC)
                .replaceAll("[\\s/／]", "");
    }

    public static String query(String source) {
        String address = compact(source);
        if (address.isBlank() || address.length() > 300) throw bad("地址为空或过长，需先核对原始资料");
        if (address.toLowerCase(Locale.ROOT).contains("http:")
                || address.toLowerCase(Locale.ROOT).contains("https:")
                || address.contains("@")
                || Pattern.compile("\\d{11}").matcher(address).find())
            throw bad("地址包含链接、邮箱或疑似电话号码，未发送外部服务");
        return address;
    }

    private static String localPart(String value) {
        String address =
                compact(value)
                        .replaceFirst("^中国", "")
                        .replaceFirst("^山西省", "")
                        .replaceFirst("^太原市", "");
        for (String district : DISTRICTS)
            if (address.startsWith(district)) return address.substring(district.length());
        return address;
    }

    private static Decision review(String reason) {
        return new Decision("review", reason, null, null);
    }

    public static Decision assess(String source, ProviderResult result) {
        if (!"complete".equals(result.status()) || !"OK".equals(result.info()))
            return new Decision(
                    "unavailable",
                    "服务未返回有效结果：" + Objects.toString(result.info(), "未知错误"),
                    null,
                    null);
        if (result.candidates() == null || result.candidates().isEmpty())
            return review("未找到地址候选，保留待核实");
        if (result.resultCount() != 1 || result.candidates().size() != 1)
            return review("存在多个或不完整的候选结果，不能自动选取");
        Candidate c = result.candidates().get(0);
        if (c == null
                || c.longitude() == null
                || c.latitude() == null
                || !Double.isFinite(c.longitude())
                || !Double.isFinite(c.latitude())
                || Math.abs(c.longitude()) > 180
                || Math.abs(c.latitude()) > 90) return review("候选坐标无效");
        if (!"太原市".equals(c.city()) || !Objects.toString(c.adcode(), "").startsWith("1401"))
            return review("候选不在指定的太原市范围，需核对城市");
        if (c.longitude() < 110 || c.longitude() > 115 || c.latitude() < 35 || c.latitude() > 40)
            return review("候选坐标远离太原，需人工核实");
        for (String district : DISTRICTS)
            if (compact(source).contains(district) && !district.equals(c.district()))
                return review("原始地址与候选区县不一致");
        if (!Set.of("门牌号", "门址").contains(Objects.toString(c.level(), "")))
            return review("返回精度为" + Objects.toString(c.level(), "未知") + "，不足以确定设施门牌位置");
        String local = localPart(source);
        if (!NUMBERED.matcher(local).matches()) return review("地址含楼栋、单元、方位或缺少明确门牌，需人工核对");
        if (!local.equals(localPart(c.formattedAddress()))
                || !local.equals(compact(c.street()) + compact(c.number())))
            return review("返回地址、道路或门牌与原文不完全一致");
        return new Decision(
                "address_matched",
                "唯一门牌候选，城市、区县、道路和门牌一致；仅为地址参考点，非设施实测点",
                c.longitude(),
                c.latitude());
    }
}
