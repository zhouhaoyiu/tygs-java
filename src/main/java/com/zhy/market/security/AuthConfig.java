package com.zhy.market.security;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.HandlerInterceptor;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class AuthConfig implements WebMvcConfigurer {
    private final Sessions sessions;

    public AuthConfig(Sessions sessions) {
        this.sessions = sessions;
    }

    public boolean authorize(HttpServletRequest request, HttpServletResponse response)
            throws Exception {
        if ("OPTIONS".equals(request.getMethod())) return true;
        var user = sessions.find(request.getHeader("Authorization"));
        if (user == null) {
            response.sendError(401);
            return false;
        }
        String path = request.getRequestURI().substring(request.getContextPath().length());
        if ((path.startsWith("/admin/") && !path.equals("/admin/logout")
                        || path.equals("/adminLog/getAllAdminLog"))
                && !user.manages()) {
            response.sendError(403);
            return false;
        }
        request.setAttribute("user", user);
        return true;
    }

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(
                        new HandlerInterceptor() {
                            @Override
                            public boolean preHandle(
                                    HttpServletRequest request,
                                    HttpServletResponse response,
                                    Object handler)
                                    throws Exception {
                                return authorize(request, response);
                            }
                        })
                .addPathPatterns("/**")
                .excludePathPatterns("/admin/adminLogin", "/error");
    }
}
