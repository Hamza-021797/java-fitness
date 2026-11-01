package com.fitness.gateway;

import java.util.Optional;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.core.Ordered;
import org.springframework.security.core.context.ReactiveSecurityContextHolder;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

/** Replaces any client-supplied identity headers with values taken from the validated JWT. */
@Component
public class UserHeaderFilter implements GlobalFilter, Ordered {
    @Override
    public Mono<Void> filter(ServerWebExchange ex, GatewayFilterChain chain) {
        return ReactiveSecurityContextHolder.getContext()
            .map(c -> c.getAuthentication())
            .filter(a -> a instanceof JwtAuthenticationToken)
            .cast(JwtAuthenticationToken.class)
            .map(t -> {
                Jwt j = t.getToken();
                return ex.mutate().request(r -> r.headers(h -> {
                    h.set("X-User-Id", j.getSubject());
                    h.set("X-User-Email", Optional.ofNullable(j.getClaimAsString("email")).orElse(""));
                    h.set("X-User-Name", Optional.ofNullable(j.getClaimAsString("name")).orElse(""));
                })).build();
            })
            .defaultIfEmpty(ex)
            .flatMap(chain::filter);
    }
    @Override public int getOrder() { return 10; }
}
