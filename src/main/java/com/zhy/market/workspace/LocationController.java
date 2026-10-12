package com.zhy.market.workspace;

import com.zhy.market.security.Sessions;
import com.zhy.market.workspace.LocationModels.Change;
import com.zhy.market.workspace.LocationModels.LocationInput;
import com.zhy.market.workspace.LocationModels.Records;
import com.zhy.market.workspace.LocationModels.SavedLocation;
import com.zhy.market.workspace.LocationModels.Summary;
import jakarta.servlet.http.HttpServletRequest;
import java.util.List;
import org.springframework.context.annotation.Profile;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@Profile("local")
@RequestMapping("/locations")
public class LocationController {
    private final LocationService service;

    public LocationController(LocationService service) {
        this.service = service;
    }

    @GetMapping("/summary")
    public Summary summary() {
        return service.summary();
    }

    @GetMapping("/records")
    public Records records(
            @RequestParam(defaultValue = "") String q,
            @RequestParam(defaultValue = "all") String state,
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "30") int size) {
        return service.records(q, state, page, size);
    }

    @GetMapping("/{table}/{id}/history")
    public List<Change> history(@PathVariable String table, @PathVariable long id) {
        return service.history(table, id);
    }

    @PutMapping("/{table}/{id}")
    public SavedLocation save(
            @PathVariable String table,
            @PathVariable long id,
            @RequestBody LocationInput input,
            HttpServletRequest request) {
        return service.save(table, id, input, Sessions.user(request).name());
    }
}
