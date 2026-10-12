package com.zhy.market.controller;

import com.zhy.market.domain.FireHydrant;
import com.zhy.market.domain.ValueWell;
import com.zhy.market.domain.WaterMeterRoom;
import com.zhy.market.domain.Watermeterwell;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import org.springframework.http.HttpStatus;
import org.springframework.jdbc.core.BeanPropertyRowMapper;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

@RestController
@RequestMapping("/facilities")
public class FacilityPageController {
    private record Facility(
            String table, Class<?> rowType, Set<String> fields, boolean ignoreCase) {}

    private static final Map<String, Facility> FACILITIES =
            Map.of(
                    "rooms",
                            new Facility(
                                    "watermeterroom",
                                    WaterMeterRoom.class,
                                    Set.of("accountIdentifier", "communityName", "detailedAddress"),
                                    true),
                    "meters",
                            new Facility(
                                    "watermeterwell",
                                    Watermeterwell.class,
                                    Set.of(
                                            "accountIdentifier",
                                            "accountName",
                                            "accountNumber",
                                            "address"),
                                    true),
                    "hydrants",
                            new Facility(
                                    "firehydrant",
                                    FireHydrant.class,
                                    Set.of("streetName", "manufactor"),
                                    false),
                    "valves",
                            new Facility(
                                    "valuewell",
                                    ValueWell.class,
                                    Set.of("filledBy", "accountIdentifier"),
                                    false));
    private final JdbcTemplate db;

    public FacilityPageController(JdbcTemplate db) {
        this.db = db;
    }

    @GetMapping("/{type}/records")
    public Object records(
            @PathVariable String type,
            @RequestParam(defaultValue = "") String field,
            @RequestParam(defaultValue = "") String q,
            @RequestParam(defaultValue = "") String streetName,
            @RequestParam(defaultValue = "") String manufactor,
            @RequestParam(required = false) List<String> caliber,
            @RequestParam(required = false) List<String> wellChamberType,
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "30") int size) {
        Facility facility = FACILITIES.get(type);
        if (facility == null) throw new ResponseStatusException(HttpStatus.NOT_FOUND, "设施类型不存在");
        if (page < 1
                || page > 100000
                || size < 1
                || size > 500
                || q.length() > 200
                || streetName.length() > 200
                || manufactor.length() > 200
                || (!field.isEmpty() && !facility.fields().contains(field))
                || (!q.isEmpty() && field.isEmpty())) throw invalid();
        caliber = validateOptions(caliber);
        wellChamberType = validateOptions(wellChamberType);
        boolean chamber = type.equals("valves") || type.equals("hydrants");
        if ((!chamber && (!streetName.isEmpty() || !manufactor.isEmpty() || !caliber.isEmpty()))
                || (!type.equals("valves") && !wellChamberType.isEmpty())) throw invalid();

        StringBuilder where = new StringBuilder(" WHERE 1=1");
        List<Object> args = new ArrayList<>();
        contains(where, args, field, facility.ignoreCase() ? q.strip() : q, facility.ignoreCase());
        contains(where, args, "streetName", streetName, false);
        contains(where, args, "manufactor", manufactor, false);
        oneOf(where, args, "caliber", caliber);
        oneOf(where, args, "wellChamberType", wellChamberType);
        String from = " FROM " + facility.table() + where;
        long total = db.queryForObject("SELECT COUNT(*)" + from, Long.class, args.toArray());
        args.add(size);
        args.add((page - 1) * size);
        var rows =
                db.query(
                        "SELECT *" + from + " ORDER BY id LIMIT ? OFFSET ?",
                        BeanPropertyRowMapper.newInstance(facility.rowType()),
                        args.toArray());
        return Map.of("rows", rows, "total", total, "page", page, "size", size);
    }

    private static ResponseStatusException invalid() {
        return new ResponseStatusException(HttpStatus.BAD_REQUEST, "查询参数无效");
    }

    private static List<String> validateOptions(List<String> values) {
        if (values == null) return List.of();
        if (values.size() > 30
                || values.stream().anyMatch(v -> v == null || v.isEmpty() || v.length() > 200))
            throw invalid();
        return values.stream().distinct().toList();
    }

    private static void contains(
            StringBuilder where,
            List<Object> args,
            String field,
            String value,
            boolean ignoreCase) {
        if (value.isEmpty()) return;
        // Field names come only from the per-facility allowlist or fixed filter names.
        where.append(" AND ")
                .append(ignoreCase ? "LOWER(" + field + ")" : field)
                .append(" LIKE ? ESCAPE '!'");
        String term = ignoreCase ? value.toLowerCase(Locale.ROOT) : value;
        args.add("%" + term.replace("!", "!!").replace("%", "!%").replace("_", "!_") + "%");
    }

    private static void oneOf(
            StringBuilder where, List<Object> args, String field, List<String> values) {
        if (values.isEmpty()) return;
        where.append(" AND ")
                .append(field)
                .append(" IN (")
                .append(String.join(",", Collections.nCopies(values.size(), "?")))
                .append(")");
        args.addAll(values);
    }
}
