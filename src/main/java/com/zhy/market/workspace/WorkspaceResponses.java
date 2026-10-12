package com.zhy.market.workspace;

import java.util.List;
import java.util.Map;
import tools.jackson.databind.JsonNode;

public final class WorkspaceResponses {
    private WorkspaceResponses() {}

    public record RecordView(
            long id, Map<String, String> values, boolean archived, String revision) {}

    public record RecordPage(List<RecordView> rows, long total, int page, int size) {}

    public record RecordDetail(
            long id,
            Map<String, String> values,
            boolean archived,
            String revision,
            List<Change> history,
            List<Source> sources) {}

    public record Change(
            String action, String actor, JsonNode before, JsonNode after, String createdAt) {}

    public record Source(
            String sha256, String fileName, String sheetName, int rowNumber, JsonNode raw) {}

    public record ImportResult(int inserted, int skipped) {}
}
