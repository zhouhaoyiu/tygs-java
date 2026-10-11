package com.zhy.market.security;

import jakarta.servlet.http.HttpServletRequest;
import java.security.SecureRandom;
import java.time.Instant;
import java.util.Base64;
import java.util.concurrent.ConcurrentHashMap;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ResponseStatusException;

@Component
public class Sessions {
    public record User(String id, String name, String role) { public boolean manages() { return "admin".equals(role); } }
    public void revoke(String id) { sessions.entrySet().removeIf(e -> e.getValue().user().id().equals(id)); }
    public record Session(User user, Instant expires) {}
    private final ConcurrentHashMap<String, Session> sessions = new ConcurrentHashMap<>();
    public String create(User user) {
        sessions.entrySet().removeIf(entry -> !entry.getValue().expires().isAfter(Instant.now()));
        if (sessions.size() >= 10000) throw new ResponseStatusException(HttpStatus.SERVICE_UNAVAILABLE);
        byte[] bytes = new byte[32]; new SecureRandom().nextBytes(bytes);
        String token = Base64.getUrlEncoder().withoutPadding().encodeToString(bytes);
        sessions.put(token, new Session(user, Instant.now().plusSeconds(28800)));
        return token;
    }
    public User find(String header) {
        if (header == null || !header.startsWith("Bearer ")) return null;
        String token = header.substring(7);
        Session session = sessions.get(token);
        if (session == null) return null;
        if (!session.expires().isAfter(Instant.now())) { sessions.remove(token); return null; }
        return session.user();
    }
    public void remove(String header) { if (header != null && header.startsWith("Bearer ")) sessions.remove(header.substring(7)); }
    public static User user(HttpServletRequest request) {
        var user = (User) request.getAttribute("user");
        if (user == null) throw new ResponseStatusException(HttpStatus.UNAUTHORIZED);
        return user;
    }
    public static void owner(HttpServletRequest request, String owner) {
        var user = user(request);
        if (!user.manages() && !user.id().equals(owner)) throw new ResponseStatusException(HttpStatus.FORBIDDEN);
    }
}
