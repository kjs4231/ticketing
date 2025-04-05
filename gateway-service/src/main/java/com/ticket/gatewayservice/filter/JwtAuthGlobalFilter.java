package com.ticket.gatewayservice.filter;

import com.ticket.gatewayservice.security.JwtProvider;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.core.Ordered;
import org.springframework.http.HttpStatus;
import org.springframework.http.server.reactive.ServerHttpResponse;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

@Component
@Slf4j
public class JwtAuthGlobalFilter implements GlobalFilter, Ordered {

    @Autowired
    private JwtProvider jwtProvider;

    private static final String[] PUBLIC_ROUTES = {
            "/user-service/users/login",
            "/user-service/users/signup",
            "/concert-service/concerts/all",
            "/eureka/**"
    };

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
        String path = exchange.getRequest().getPath().value();
        log.info("Request path: {}", path);

        for (String publicRoute : PUBLIC_ROUTES) {
            if (path.startsWith(publicRoute)) {
                log.info("Public route accessed: {}", path);
                return chain.filter(exchange);
            }
        }

        String authHeader = exchange.getRequest().getHeaders().getFirst("Authorization");
        log.info("Auth header: {}", authHeader);

        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            log.error("No valid authorization header found");
            return unauthorized(exchange);
        }

        String token = authHeader.substring(7);
        log.info("Token: {}", token);

        if (!jwtProvider.validateToken(token)) {
            log.error("Invalid token");
            return unauthorized(exchange);
        }

        String username = jwtProvider.getUsername(token);
        log.info("Username from token: {}", username);

        exchange = exchange.mutate()
                .request(r -> r.headers(headers -> headers.add("X-User", username)))
                .build();

        return chain.filter(exchange);
    }

    private Mono<Void> unauthorized(ServerWebExchange exchange) {
        ServerHttpResponse response = exchange.getResponse();
        response.setStatusCode(HttpStatus.UNAUTHORIZED);
        return response.setComplete();
    }

    @Override
    public int getOrder() {
        return -1;
    }
}