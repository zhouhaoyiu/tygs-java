package com.zhy.market.workspace;

import java.util.List;
import tools.jackson.databind.JsonNode;

public final class LocationModels {
    private LocationModels() {}

    public record LocationInput(
            Double longitude, Double latitude, String crs, String evidence, long revision) {}

    public record Summary(
            long total, long located, long addressMatched, long review, long pending) {}

    public record Records(List<Facility> rows, long total) {}

    public record Facility(
            String id,
            String name,
            String identifier,
            String address,
            String targetTable,
            long targetId,
            Coordinates location,
            GeocodeReference geocode) {}

    public record Coordinates(
            double longitude,
            double latitude,
            String crs,
            String evidence,
            String actor,
            String updatedAt,
            long revision) {}

    public record SavedLocation(
            double longitude, double latitude, String crs, String evidence, long revision) {}

    public record Change(String actor, JsonNode before, JsonNode after, String createdAt) {}

    public record GeocodeReference(
            long id,
            String decision,
            String reason,
            Double longitude,
            Double latitude,
            String createdAt) {}

    public record GeocodeInput(
            String requestId, String sourceAddress, GeocodeRules.ProviderResult result) {}

    public record GeocodeDetails(
            String sourceAddress,
            String queryAddress,
            String city,
            String blocked,
            List<GeocodeHistory> history) {}

    public record GeocodeHistory(
            String sourceAddress,
            String queryAddress,
            String decision,
            String reason,
            String actor,
            String createdAt,
            long id,
            boolean current,
            JsonNode result) {}
}
