package org.medlearn;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.web.server.ServerHttpSecurity;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.oauth2.server.resource.authentication.ReactiveJwtAuthenticationConverter;
import org.springframework.security.web.server.SecurityWebFilterChain;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.reactive.CorsWebFilter;
import org.springframework.web.cors.reactive.UrlBasedCorsConfigurationSource;
import reactor.core.publisher.Flux;

import java.util.Collection;
import java.util.List;
import java.util.Map;

@Configuration
public class GatewaySecurity {

    @Bean
    SecurityWebFilterChain security(
            ServerHttpSecurity http,
            ReactiveJwtAuthenticationConverter jwtConverter
    ) {

        return http
                .csrf(ServerHttpSecurity.CsrfSpec::disable)
                .authorizeExchange(auth -> auth

                        // Public certificate verification
                        .pathMatchers(
                                HttpMethod.GET,
                                "/api/certificates/verify/**"
                        ).permitAll()

                        // Public user registration
                        .pathMatchers(
                                HttpMethod.POST,
                                "/api/users/register"
                        ).permitAll()

                        // Public user catalog
                        .pathMatchers(
                                HttpMethod.GET,
                                "/api/users/catalog"
                        ).permitAll()

                        // Public course catalog
                        .pathMatchers(
                                HttpMethod.GET,
                                "/api/courses"
                        ).permitAll()

                        // Block direct access to internal APIs
                        .pathMatchers(
                                "/api/**/internal/**",
                                "/internal/**",
                                "/api/courses/internal/**"
                        ).denyAll()

                        // Administrator endpoints
                        .pathMatchers(
                                "/api/users/admin/**",
                                "/api/enrollments/admin/**",
                                "/api/courses/admin/**",
                                "/api/exams/admin/**",
                                "/api/certificates/admin/**"
                        ).hasRole("platform_admin")

                        // Require authentication elsewhere
                        .anyExchange().authenticated()
                )
                .oauth2ResourceServer(oauth2 ->
                        oauth2.jwt(jwt ->
                                jwt.jwtAuthenticationConverter(jwtConverter)
                        )
                )
                .build();
    }

    @Bean
    ReactiveJwtAuthenticationConverter jwtConverter() {

        ReactiveJwtAuthenticationConverter converter =
                new ReactiveJwtAuthenticationConverter();

        converter.setJwtGrantedAuthoritiesConverter(jwt -> {

            Map<String, Object> realm =
                    jwt.getClaimAsMap("realm_access");

            if (realm == null ||
                    !(realm.get("roles") instanceof Collection<?> roles)) {
                return Flux.empty();
            }

            return Flux.fromIterable(roles)
                    .filter(String.class::isInstance)
                    .cast(String.class)
                    .map(role -> (GrantedAuthority)
                            new SimpleGrantedAuthority("ROLE_" + role));
        });

        return converter;
    }

    @Bean
    CorsWebFilter corsFilter() {

        CorsConfiguration config = new CorsConfiguration();

        config.setAllowedOrigins(
                List.of("http://localhost:3000")
        );

        config.setAllowedMethods(
                List.of(
                        "GET",
                        "POST",
                        "PUT",
                        "DELETE",
                        "OPTIONS"
                )
        );

        config.setAllowedHeaders(
                List.of(
                        "Authorization",
                        "Content-Type",
                        "Accept-Language"
                )
        );

        UrlBasedCorsConfigurationSource source =
                new UrlBasedCorsConfigurationSource();

        source.registerCorsConfiguration("/**", config);

        return new CorsWebFilter(source);
    }
}