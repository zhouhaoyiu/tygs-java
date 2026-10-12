package com.zhy.market.config;

import com.zhy.market.domain.Admin;
import com.zhy.market.domain.AdminLog;
import com.zhy.market.domain.Card;
import com.zhy.market.domain.FireHydrant;
import com.zhy.market.domain.Hubiaohuguan;
import com.zhy.market.domain.Hubiaozongguan;
import com.zhy.market.domain.RsaKey;
import com.zhy.market.domain.Sheet1;
import com.zhy.market.domain.ThreeSupply;
import com.zhy.market.domain.ValueWell;
import com.zhy.market.domain.WaterMeterInfo;
import com.zhy.market.domain.WaterMeterRoom;
import com.zhy.market.domain.Watermeterwell;
import com.zhy.market.domain.Weihuzongguan;
import com.zhy.market.ledger.LedgerImport;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.UUID;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.context.annotation.Profile;
import org.springframework.core.io.ClassPathResource;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.datasource.init.ResourceDatabasePopulator;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Component;
import tools.jackson.databind.ObjectMapper;

/** Initializes a separate local database from the current Java contract, never production. */
@Component
@Profile("local")
public class LocalDatabase implements ApplicationRunner {
    private final JdbcTemplate db;
    private final String password;
    private final ObjectMapper json;

    public LocalDatabase(
            JdbcTemplate db, @Value("${LOCAL_ADMIN_PASSWORD}") String password, ObjectMapper json) {
        this.db = db;
        this.password = password;
        this.json = json;
    }

    @Override
    public void run(ApplicationArguments args) {
        Map<String, Class<?>> tables = new LinkedHashMap<>();
        tables.put("admin", Admin.class);
        tables.put("adminLog", AdminLog.class);
        tables.put("watermeterwell", Watermeterwell.class);
        tables.put("watermeterroom", WaterMeterRoom.class);
        tables.put("valuewell", ValueWell.class);
        tables.put("firehydrant", FireHydrant.class);
        tables.put("watermeterinfo", WaterMeterInfo.class);
        tables.put("ThreeSupply", ThreeSupply.class);
        tables.put("sheet1", Sheet1.class);
        tables.put("hubiaohuguan", Hubiaohuguan.class);
        tables.put("hubiaozongguan", Hubiaozongguan.class);
        tables.put("weihuzongguan", Weihuzongguan.class);
        tables.put("card", Card.class);
        tables.put("rsakey", RsaKey.class);
        for (var entry : tables.entrySet()) {
            List<String> columns = new ArrayList<>();
            for (var field : entry.getValue().getDeclaredFields()) {
                if (Modifier.isStatic(field.getModifiers()) || field.isSynthetic()) continue;
                String name = field.getName();
                boolean identity = Set.of("id", "adminId", "adminLogid", "infoId").contains(name);
                String type =
                        identity
                                ? "INT AUTO_INCREMENT PRIMARY KEY"
                                : field.getType() == Integer.class ? "INT" : "TEXT";
                String defaults =
                        name.equals("repairInfo")
                                ? " DEFAULT '[]'"
                                : name.equals("status") ? " DEFAULT '1'" : "";
                columns.add("`" + name + "` " + type + defaults);
            }
            db.execute(
                    "CREATE TABLE IF NOT EXISTS `"
                            + entry.getKey()
                            + "` ("
                            + String.join(",", columns)
                            + ")");
        }
        new ResourceDatabasePopulator(new ClassPathResource("ledger-schema.sql"))
                .execute(Objects.requireNonNull(db.getDataSource()));
        db.execute(
                "ALTER TABLE sheet1 ADD COLUMN IF NOT EXISTS id BIGINT AUTO_INCREMENT PRIMARY KEY");
        db.execute("ALTER TABLE card ADD COLUMN IF NOT EXISTS remarks TEXT");
        new ResourceDatabasePopulator(new ClassPathResource("workspace-schema.sql"))
                .execute(Objects.requireNonNull(db.getDataSource()));
        db.execute("ALTER TABLE ledger_record ADD COLUMN IF NOT EXISTS addressKey VARCHAR(2048)");
        db.execute("ALTER TABLE ledger_record ADD COLUMN IF NOT EXISTS positionKey VARCHAR(2048)");
        for (var row :
                db.queryForList(
                        "SELECT id,mappedJson FROM ledger_record WHERE addressKey IS NULL OR positionKey IS NULL")) {
            var fields = json.readTree((String) row.get("mappedJson"));
            String address =
                    fields.path("address").asText(fields.path("detailedAddress").asText(""));
            String position =
                    fields.path("coordinates")
                            .asText(
                                    fields.path("positioningCoordinates")
                                            .asText(fields.path("location").asText("")));
            db.update(
                    "UPDATE ledger_record SET addressKey=?,positionKey=? WHERE id=?",
                    LedgerImport.boundedKey(address, 2048),
                    LedgerImport.boundedKey(position, 2048),
                    row.get("id"));
        }
        db.execute("CREATE INDEX IF NOT EXISTS ledger_address_idx ON ledger_record(addressKey)");
        db.execute("CREATE INDEX IF NOT EXISTS ledger_position_idx ON ledger_record(positionKey)");
        if (db.queryForObject("SELECT COUNT(*) FROM admin", Long.class) == 0) {
            if (password.length() < 12 || password.length() > 72)
                throw new IllegalArgumentException("Local password must contain 12-72 characters");
            db.update(
                    "INSERT INTO admin(userName,passWord,adminUUid,adminRole) VALUES(?,?,?,0)",
                    "local-admin",
                    new BCryptPasswordEncoder(12).encode(password),
                    UUID.randomUUID().toString());
        }
    }
}
