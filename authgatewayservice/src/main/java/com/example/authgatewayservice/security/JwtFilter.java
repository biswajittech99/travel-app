package com.example.authgatewayservice.security;



import org.example.common.dto.JwtUtil;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;


@Component
public class JwtFilter implements GlobalFilter {

    private final JwtUtil jwtUtil;

    public JwtFilter(JwtUtil jwtUtil) {
        this.jwtUtil = jwtUtil;
    }

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {

        String path = exchange.getRequest().getURI().getPath();

        System.out.println("Incoming PATH "+ path);

        // ✅ PUBLIC ENDPOINTS
        if (isPublicEndpoint(path)) {
            return chain.filter(exchange);
        }

        String authHeader = exchange.getRequest()
                .getHeaders()
                .getFirst(HttpHeaders.AUTHORIZATION);

        // ❌ No token
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            return unauthorized(exchange);
        }

        try {
            String token = authHeader.substring(7);

            // ✅ Validate token
            if (!jwtUtil.validateToken(token)) {
                return unauthorized(exchange);
            }

            String email = jwtUtil.extractEmail(token);
            String role = jwtUtil.extractRole(token);

            // ✅ ROLE-BASED ACCESS
            if (!isAuthorized(path, role)) {
                return forbidden(exchange);
            }

            // ✅ Forward headers to services
            ServerHttpRequest mutatedRequest = exchange.getRequest()
                    .mutate()
                    .header("X-User-Email", email)
                    .header("X-User-Role", role)
                    .build();

            return chain.filter(exchange.mutate()
                    .request(mutatedRequest)
                    .build());

        } catch (Exception e) {
            return unauthorized(exchange);
        }
    }

    // ✅ PUBLIC API CHECK
    private boolean isPublicEndpoint(String path) {
        System.out.println("PUBLIC API HIT");
        return path.startsWith("/api/users/auth/");

    }

    // ✅ ROLE AUTHORIZATION LOGIC
    private boolean isAuthorized(String path, String role) {

        // USER APIs
        if (path.startsWith("/api/bookings")) {
            return role.equals("USER");
        }

        if (path.startsWith("/api/users/profile")) {
            return role.equals("USER");
        }

        // ADMIN APIs
        if (path.startsWith("/api/admin")) {
            return role.equals("ADMIN");
        }

        // Default allow
        return true;
    }

    // ❌ 401
    private Mono<Void> unauthorized(ServerWebExchange exchange) {
        exchange.getResponse().setStatusCode(HttpStatus.UNAUTHORIZED);
        return exchange.getResponse().setComplete();
    }

    // ❌ 403
    private Mono<Void> forbidden(ServerWebExchange exchange) {
        exchange.getResponse().setStatusCode(HttpStatus.FORBIDDEN);
        return exchange.getResponse().setComplete();
    }
}
