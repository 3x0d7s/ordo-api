package com.kyut.ordo.security.csrf;

import com.kyut.ordo.security.jwt.JwtService;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.Arrays;
import java.util.Set;

@Component
@RequiredArgsConstructor
public class CsrfTokenFilter extends OncePerRequestFilter {
    private final JwtService jwtService;
    
    private static final Set<String> STATE_CHANGING_METHODS = 
        Set.of("POST", "PUT", "DELETE", "PATCH");
    
    // Endpoints that don't need CSRF check
    private static final Set<String> EXCLUDED_PATHS = 
        Set.of("/auth/", "/swagger-ui/", "/api-docs");

    @Override
    protected void doFilterInternal(
            HttpServletRequest request,
            HttpServletResponse response,
            FilterChain filterChain
    ) throws ServletException, IOException {
        
        String method = request.getMethod();
        String path = request.getRequestURI();
        
        // Skip CSRF check for safe methods and excluded paths
        if (!STATE_CHANGING_METHODS.contains(method) || 
            EXCLUDED_PATHS.stream().anyMatch(path::startsWith)) {
            filterChain.doFilter(request, response);
            return;
        }

        try {
            // Get JWT from cookie
            String jwt = getJwtFromCookie(request);
            
            // Get CSRF token from header
            String csrfTokenFromHeader = request.getHeader("X-CSRF-Token");
            
            if (!StringUtils.hasText(jwt) || !StringUtils.hasText(csrfTokenFromHeader)) {
                sendErrorResponse(response, HttpServletResponse.SC_FORBIDDEN, "CSRF token missing");
                return;
            }
            
            // Validate JWT and extract CSRF token from it
            if (!jwtService.validateToken(jwt)) {
                sendErrorResponse(response, HttpServletResponse.SC_UNAUTHORIZED, "Invalid token");
                return;
            }
            
            String csrfTokenFromJwt = jwtService.getCsrfTokenFromJwt(jwt);
            
            // Compare CSRF tokens
            if (!csrfTokenFromHeader.equals(csrfTokenFromJwt)) {
                sendErrorResponse(response, HttpServletResponse.SC_FORBIDDEN, "CSRF token mismatch");
                return;
            }
            
        } catch (Exception ex) {
            logger.error("CSRF validation error", ex);
            sendErrorResponse(response, HttpServletResponse.SC_FORBIDDEN, "CSRF validation failed");
            return;
        }

        filterChain.doFilter(request, response);
    }
    
    private String getJwtFromCookie(HttpServletRequest request) {
        Cookie[] cookies = request.getCookies();
        if (cookies == null) return null;
        
        return Arrays.stream(cookies)
                .filter(cookie -> "jwt".equals(cookie.getName()))
                .map(Cookie::getValue)
                .findFirst()
                .orElse(null);
    }
    
    private void sendErrorResponse(HttpServletResponse response, int status, String message) throws IOException {
        response.setStatus(status);
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);
        response.getWriter().write(String.format("{\"error\":\"%s\",\"message\":\"%s\"}", 
            status == HttpServletResponse.SC_FORBIDDEN ? "Forbidden" : "Unauthorized", 
            message));
    }
}

