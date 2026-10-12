package com.zhy.market;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
import tools.jackson.databind.ObjectMapper;
import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT, properties = {
    "spring.datasource.url=jdbc:h2:mem:facility-startup;DB_CLOSE_DELAY=-1",
    "spring.datasource.driver-class-name=org.h2.Driver",
    "spring.datasource.username=sa",
    "spring.datasource.password=",
    "spring.sql.init.mode=never",
    "server.address=127.0.0.1",
    "server.ssl.enabled=false"
})
class StartupTests {
    @LocalServerPort int port;
    @Autowired ObjectMapper json;

    @Test void applicationStartsWithTheNewContainerAndRejectsAnonymousBusinessRequests() throws Exception {
        var request = HttpRequest.newBuilder(URI.create("http://127.0.0.1:" + port
            + "/WaterMeterWell/getAllWaterMeterWellInfo")).GET().build();
        var client = HttpClient.newHttpClient();
        assertEquals(401, client.send(request, HttpResponse.BodyHandlers.ofString()).statusCode());
    }

    @Test void configuredJsonMapperPreservesRepairHistoryAndTextIdentifiers() {
        var value = json.readTree("{\"accountNumber\":\"001234\",\"repairInfo\":[{\"text\":\"repair\",\"repairTime\":\"2026-10-12\"}]}");
        var roundTrip = json.readTree(json.writeValueAsString(value));
        assertEquals(value, roundTrip);
        assertEquals("001234", roundTrip.path("accountNumber").asText());
    }
}
