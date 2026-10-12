package com.zhy.market.workspace;

import java.util.Set;
import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;

final class FacilityTables {
    private static final Set<String> TABLES =
            Set.of("watermeterwell", "watermeterroom", "valuewell", "firehydrant");

    static final String INVENTORY =
            "(SELECT 'watermeterwell' targetTable,id targetId,accountIdentifier identifier,accountName name,COALESCE(NULLIF(address,''),coordinates,'') address FROM watermeterwell UNION ALL "
                    + "SELECT 'watermeterroom',id,accountIdentifier,communityName,COALESCE(NULLIF(detailedAddress,''),location,'') FROM watermeterroom UNION ALL "
                    + "SELECT 'valuewell',id,accountIdentifier,streetName,COALESCE(positioningCoordinates,'') FROM valuewell UNION ALL "
                    + "SELECT 'firehydrant',id,accountIdentifier,streetName,COALESCE(positioningCoordinates,'') FROM firehydrant)";

    private FacilityTables() {}

    static void validate(String table) {
        if (!TABLES.contains(table)) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "设施类型不存在");
        }
    }
}
