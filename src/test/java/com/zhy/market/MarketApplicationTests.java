package com.zhy.market;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.contains;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.zhy.market.controller.AdminController;
import com.zhy.market.controller.FireHydrantController;
import com.zhy.market.controller.ValueWellController;
import com.zhy.market.controller.WaterMeterRoomController;
import com.zhy.market.controller.WaterMeterWellController;
import com.zhy.market.domain.Admin;
import com.zhy.market.mapper.AdminLogMapper;
import com.zhy.market.mapper.AdminMapper;
import com.zhy.market.mapper.WaterMeterInfoMapper;
import com.zhy.market.security.AuthConfig;
import com.zhy.market.security.MeterActions;
import com.zhy.market.security.Sessions;
import jakarta.servlet.http.HttpServletRequest;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.server.ResponseStatusException;

class MarketApplicationTests {
    @Test
    void sessionsCannotBeForgedAndCanBeRevoked() {
        Sessions sessions = new Sessions();
        var user = new Sessions.User("id", "test", "staff");
        assertNull(sessions.find("Bearer id"));
        String token = sessions.create(user);
        assertEquals(user, sessions.find("Bearer " + token));
        sessions.revoke("id");
        assertNull(sessions.find("Bearer " + token));
    }

    @Test
    void guardProtectsAllBusinessRoutesAndAdminRoles() throws Exception {
        Sessions sessions = new Sessions();
        AuthConfig config = new AuthConfig(sessions);
        var request = new MockHttpServletRequest("GET", "/WaterMeterWell/getAllWaterMeterWellInfo");
        var response = new MockHttpServletResponse();
        assertFalse(config.authorize(request, response));
        assertEquals(401, response.getStatus());
        request.addHeader(
                "Authorization",
                "Bearer " + sessions.create(new Sessions.User("u", "staff", "staff")));
        assertTrue(config.authorize(request, new MockHttpServletResponse()));
        request.setRequestURI("/admin/getAllAdminsInfo");
        assertFalse(config.authorize(request, new MockHttpServletResponse()));
    }

    @Test
    void loginReturnsASessionOnlyForVerifiedPassword() {
        AdminMapper mapper = mock(AdminMapper.class);
        Sessions sessions = new Sessions();
        Admin user = new Admin();
        user.userName = "test";
        user.adminUUid = "u";
        user.adminId = 1;
        user.adminRole = 1;
        user.passWord = new BCryptPasswordEncoder().encode("test-password-123");
        when(mapper.adminLogin("test")).thenReturn(List.of(user));
        Admin input = new Admin();
        input.userName = "test";
        input.passWord = "bad";
        var controller = new AdminController(mapper, sessions);
        var request = new MockHttpServletRequest();
        assertThrows(ResponseStatusException.class, () -> controller.login(input, request));
        input.passWord = "test-password-123";
        assertNotNull(controller.login(input, request));
    }

    @Test
    void retirementScopesBothOwnerAndFacilityType() {
        var meters = mock(WaterMeterInfoMapper.class);
        var logs = mock(AdminLogMapper.class);
        when(meters.retire("m", 5, "watermeterwell")).thenReturn(1);
        when(logs.addAdminLog(anyString(), anyString(), anyString(), anyString(), anyString()))
                .thenReturn(1);
        new MeterActions(meters, logs)
                .retire(
                        new MeterActions.Retire("m", 5, "更换"),
                        "watermeterwell",
                        new Sessions.User("u", "test", "staff"));
        verify(meters).retire("m", 5, "watermeterwell");
        verify(logs)
                .addAdminLog(
                        anyString(),
                        eq("u"),
                        anyString(),
                        contains("watermeterwell/5/m"),
                        eq("更换"));
    }

    @Test
    void retirementRejectsMissingOwnerAndStaleRecord() {
        var action = new MeterActions(mock(WaterMeterInfoMapper.class), mock(AdminLogMapper.class));
        var user = new Sessions.User("u", "test", "staff");
        assertThrows(
                ResponseStatusException.class,
                () ->
                        action.retire(
                                new MeterActions.Retire("m", null, "原因"), "watermeterwell", user));
        assertThrows(
                ResponseStatusException.class,
                () -> action.retire(new MeterActions.Retire("m", 5, "原因"), "watermeterwell", user));
    }

    @Test
    void mutationsAreNotGetRoutes() throws Exception {
        for (Class<?> type :
                List.of(
                        WaterMeterWellController.class,
                        WaterMeterRoomController.class,
                        ValueWellController.class,
                        FireHydrantController.class)) {
            assertNotNull(
                    type.getMethod(
                                    "deleteWaterMeterInfoById",
                                    MeterActions.Retire.class,
                                    HttpServletRequest.class)
                            .getAnnotation(PostMapping.class));
        }
    }
}
