package com.zhy.market.controller;

import com.zhy.market.domain.Admin;
import com.zhy.market.mapper.AdminMapper;
import com.zhy.market.security.Sessions;
import jakarta.servlet.http.HttpServletRequest;
import java.nio.charset.StandardCharsets;
import java.util.*;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

@RestController
@RequestMapping("/admin")
public class AdminController {
    private final AdminMapper admins;
    private final Sessions sessions;
    private static final BCryptPasswordEncoder HASH = new BCryptPasswordEncoder(12);
    private final Map<String, long[]> attempts = new HashMap<>();

    public AdminController(AdminMapper admins, Sessions sessions) {
        this.admins = admins;
        this.sessions = sessions;
    }

    @PostMapping("/adminLogin")
    public synchronized Object login(@RequestBody Admin input, HttpServletRequest request) {
        long now = System.currentTimeMillis();
        attempts.entrySet().removeIf(e -> now - e.getValue()[0] > 60000);
        if (attempts.size() > 10000)
            throw new ResponseStatusException(HttpStatus.TOO_MANY_REQUESTS);
        long[] limit =
                attempts.computeIfAbsent(request.getRemoteAddr(), key -> new long[] {now, 0});
        if (++limit[1] > 10) throw new ResponseStatusException(HttpStatus.TOO_MANY_REQUESTS);
        if (input.userName == null
                || input.passWord == null
                || input.userName.length() > 128
                || input.passWord.length() > 72)
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED);
        var rows = admins.adminLogin(input.userName);
        Admin user = rows.isEmpty() ? null : rows.get(0);
        boolean valid = false;
        try {
            valid =
                    user != null
                            && user.passWord != null
                            && user.passWord.startsWith("$2")
                            && HASH.matches(input.passWord, user.passWord);
        } catch (IllegalArgumentException ignored) {
        }
        if (!valid) throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "账号或密码错误");
        var principal =
                new Sessions.User(
                        user.adminUUid,
                        user.userName,
                        Integer.valueOf(0).equals(user.adminRole) ? "admin" : "staff");
        String token = sessions.create(principal);
        return Map.of(
                "code",
                "0",
                "token",
                token,
                "data",
                Map.of(
                        "userName",
                        user.userName,
                        "adminUUid",
                        user.adminUUid,
                        "adminRole",
                        user.adminRole,
                        "adminId",
                        user.adminId));
    }

    @PostMapping("/logout")
    public void logout(HttpServletRequest request) {
        sessions.remove(request.getHeader("Authorization"));
    }

    @GetMapping("/getAllAdminsInfo")
    public Object users() {
        return Map.of("code", "0", "data", admins.getAllAdminsInfo());
    }

    @PostMapping("/adminRegis")
    public Object create(@RequestBody Admin input) {
        if (input.userName == null
                || input.userName.isBlank()
                || input.userName.length() > 128
                || input.passWord == null
                || input.passWord.length() < 12
                || input.passWord.getBytes(StandardCharsets.UTF_8).length > 72)
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST);
        if (admins.checkUserName(input.userName) > 0)
            throw new ResponseStatusException(HttpStatus.CONFLICT);
        int result =
                admins.adminRegis(
                        input.userName,
                        HASH.encode(input.passWord),
                        Objects.toString(input.emailAddress, ""),
                        Objects.toString(input.phoneNumber, ""),
                        UUID.randomUUID().toString(),
                        1);
        if (result != 1) throw new ResponseStatusException(HttpStatus.CONFLICT);
        return Map.of("code", "0", "msg", "账号已创建");
    }

    @PostMapping("/deleteAdmin")
    public Object delete(@RequestBody Map<String, String> input, HttpServletRequest request) {
        String id = input.get("adminUUid");
        if (id == null || id.equals(Sessions.user(request).id()))
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST);
        if (admins.deleteAdmin(id) != 1) throw new ResponseStatusException(HttpStatus.NOT_FOUND);
        sessions.revoke(id);
        return Map.of("code", "0");
    }
}
