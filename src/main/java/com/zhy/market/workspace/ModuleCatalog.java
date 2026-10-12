package com.zhy.market.workspace;

import java.util.*;
import org.springframework.context.annotation.Profile;
import org.springframework.core.io.ClassPathResource;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ResponseStatusException;
import tools.jackson.databind.ObjectMapper;

@Component
@Profile("local")
public class ModuleCatalog {
    public record Field(
            String key, String label, String type, Boolean required, List<String> options) {
        public Field {
            required = Boolean.TRUE.equals(required);
        }
    }

    public record Module(
            String key,
            String title,
            String table,
            String idColumn,
            boolean managed,
            String returnPath,
            String ledgerKind,
            List<Field> fields) {}

    private final Map<String, Module> modules = new LinkedHashMap<>();

    public ModuleCatalog(ObjectMapper json) throws Exception {
        try (var input = new ClassPathResource("workspace-modules.json").getInputStream()) {
            for (var module : json.readValue(input, Module[].class)) {
                for (String name : List.of(module.table(), module.idColumn()))
                    checkIdentifier(name);
                var keys = new HashSet<String>();
                for (var field : module.fields()) {
                    checkIdentifier(field.key());
                    if (!keys.add(field.key().toLowerCase(Locale.ROOT)))
                        throw new IllegalArgumentException("Duplicate module field");
                }
                if (modules.put(module.key(), module) != null)
                    throw new IllegalArgumentException("Duplicate module key");
            }
        }
    }

    private static void checkIdentifier(String value) {
        if (!value.matches("[a-zA-Z][a-zA-Z0-9_]*"))
            throw new IllegalArgumentException("Invalid catalog identifier");
    }

    public Module get(String key) {
        var module = modules.get(key);
        if (module == null) throw new ResponseStatusException(HttpStatus.NOT_FOUND, "模块不存在");
        return module;
    }

    public Collection<Module> all() {
        return modules.values();
    }
}
