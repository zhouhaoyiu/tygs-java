package com.zhy.market.workspace;

import com.zhy.market.security.Sessions;
import com.zhy.market.workspace.WorkspaceResponses.ImportResult;
import com.zhy.market.workspace.WorkspaceResponses.RecordDetail;
import com.zhy.market.workspace.WorkspaceResponses.RecordPage;
import com.zhy.market.workspace.WorkspaceResponses.RecordView;
import jakarta.servlet.http.HttpServletRequest;
import java.util.Collection;
import org.springframework.context.annotation.Profile;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

@RestController
@RequestMapping("/workspace")
@Profile("local")
public class WorkspaceController {
    private final WorkspaceService service;
    private final ModuleCatalog catalog;

    public WorkspaceController(WorkspaceService service, ModuleCatalog catalog) {
        this.service = service;
        this.catalog = catalog;
    }

    @GetMapping("/modules")
    public Collection<ModuleCatalog.Module> modules() {
        return catalog.all();
    }

    @GetMapping("/{module}/schema")
    public ModuleCatalog.Module schema(@PathVariable String module) {
        return catalog.get(module);
    }

    @GetMapping("/{module}/records")
    public RecordPage records(
            @PathVariable String module,
            @RequestParam(defaultValue = "") String q,
            @RequestParam(defaultValue = "") String field,
            @RequestParam(defaultValue = "active") String state,
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "30") int size) {
        return service.list(module, q, field, state, page, size);
    }

    @GetMapping("/{module}/records/{id}")
    public RecordDetail detail(@PathVariable String module, @PathVariable long id) {
        return service.detail(module, id);
    }

    @PostMapping("/{module}/records")
    public RecordView create(
            @PathVariable String module,
            @RequestBody WorkspaceService.SaveRequest input,
            HttpServletRequest request) {
        return service.create(module, input, Sessions.user(request).name());
    }

    @PutMapping("/{module}/records/{id}")
    public RecordView update(
            @PathVariable String module,
            @PathVariable long id,
            @RequestBody WorkspaceService.SaveRequest input,
            HttpServletRequest request) {
        return service.update(module, id, input, Sessions.user(request).name());
    }

    @PostMapping("/{module}/records/{id}/archive")
    public RecordView archive(
            @PathVariable String module,
            @PathVariable long id,
            @RequestBody WorkspaceService.ArchiveRequest input,
            HttpServletRequest request) {
        admin(request);
        return service.archive(module, id, input, Sessions.user(request).name());
    }

    @PostMapping("/{module}/import")
    public ImportResult importRows(
            @PathVariable String module,
            @RequestBody WorkspaceService.ImportRequest input,
            HttpServletRequest request) {
        admin(request);
        return service.importRows(module, input, Sessions.user(request).name());
    }

    private void admin(HttpServletRequest request) {
        if (!Sessions.user(request).manages())
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "需要管理员权限");
    }
}
