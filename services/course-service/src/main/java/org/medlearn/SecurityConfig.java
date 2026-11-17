
package org.medlearn;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationConverter;
import org.springframework.security.web.SecurityFilterChain;

import java.util.Collection;
import java.util.List;

@Configuration
public class SecurityConfig {

 @Bean
 SecurityFilterChain security(HttpSecurity http) throws Exception {

  JwtAuthenticationConverter converter =
          new JwtAuthenticationConverter();

  converter.setJwtGrantedAuthoritiesConverter(jwt -> {

   var realm = jwt.getClaimAsMap("realm_access");

   if (realm == null ||
           !(realm.get("roles") instanceof Collection<?> roles)) {
    return List.of();
   }

   return roles.stream()
           .filter(String.class::isInstance)
           .map(String.class::cast)
           .map(role -> (GrantedAuthority)
                   new SimpleGrantedAuthority("ROLE_" + role))
           .toList();
  });

  return http
          .csrf(csrf -> csrf.disable())
          .authorizeHttpRequests(auth -> auth

                  // Public course catalog
                  .requestMatchers(
                          HttpMethod.GET,
                          "/api/courses"
                  ).permitAll()

                  // Certificate service access
                  .requestMatchers(
                          "/internal/courses/*/certificate-metadata"
                  ).hasRole("svc_certificate")

                  // Examination service access
                  .requestMatchers(
                          "/api/courses/internal/**"
                  ).hasRole("svc_exam")

                  .requestMatchers(
                          "/internal/chapters/**"
                  ).hasRole("svc_exam")

                  .requestMatchers(
                          "/internal/courses/*/chapters",
                          "/internal/courses/*/completion/**"
                  ).hasRole("svc_exam")

                  // Deny other internal endpoints
                  .requestMatchers("/internal/**").denyAll()

                  // Platform administrator access
                  .requestMatchers(
                          "/api/courses/admin/**"
                  ).hasRole("platform_admin")

                  // All remaining endpoints require authentication
                  .anyRequest().authenticated()
          )
          .oauth2ResourceServer(oauth2 ->
                  oauth2.jwt(jwt ->
                          jwt.jwtAuthenticationConverter(converter)))
          .build();
 }
}