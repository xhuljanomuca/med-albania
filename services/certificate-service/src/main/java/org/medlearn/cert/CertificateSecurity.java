
package org.medlearn.cert;

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
import java.util.Map;

@Configuration
public class CertificateSecurity {

 @Bean
 SecurityFilterChain filter(HttpSecurity http) throws Exception {

  JwtAuthenticationConverter converter =
          new JwtAuthenticationConverter();

  converter.setJwtGrantedAuthoritiesConverter(jwt -> {

   Map<String, Object> realm =
           jwt.getClaimAsMap("realm_access");

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

                  // Public certificate verification
                  .requestMatchers(
                          HttpMethod.GET,
                          "/api/certificates/verify/**"
                  ).permitAll()

                  // Platform administrator access
                  .requestMatchers(
                          "/api/certificates/admin/**"
                  ).hasRole("platform_admin")

                  // All other endpoints require authentication
                  .anyRequest().authenticated()
          )
          .oauth2ResourceServer(oauth2 ->
                  oauth2.jwt(jwt ->
                          jwt.jwtAuthenticationConverter(converter)))
          .build();
 }
}