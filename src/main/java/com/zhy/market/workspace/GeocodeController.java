package com.zhy.market.workspace;

import com.zhy.market.security.Sessions;
import com.zhy.market.workspace.LocationModels.GeocodeDetails;
import com.zhy.market.workspace.LocationModels.GeocodeInput;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.context.annotation.Profile;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

@RestController
@Profile("local")
@RequestMapping("/locations/{table}/{id}/geocode")
public class GeocodeController {
    private final GeocodeService service;

    public GeocodeController(GeocodeService service) {
        this.service = service;
    }

    @GetMapping
    public GeocodeDetails detail(@PathVariable String table, @PathVariable long id) {
        return service.detail(table, id);
    }

    @PostMapping
    public GeocodeDetails save(
            @PathVariable String table,
            @PathVariable long id,
            @RequestBody GeocodeInput input,
            HttpServletRequest request) {
        var user = Sessions.user(request);
        if (!user.manages()) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "地址试定位需要管理员权限");
        }
        return service.save(table, id, input, user.name());
    }
}
