package org.medlearn;
import org.springframework.context.annotation.*;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationConverter;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import java.util.*;
@Configuration public class SecurityConfig {
 @Bean SecurityFilterChain security(HttpSecurity http) throws Exception {
  var conv=new JwtAuthenticationConverter();
  conv.setJwtGrantedAuthoritiesConverter(jwt -> {
   var realm=jwt.getClaimAsMap("realm_access");
   if(realm==null || !(realm.get("roles") instanceof Collection<?> roles)) return List.of();
   return roles.stream().map(r -> new SimpleGrantedAuthority("ROLE_"+r)).toList();
  });
  return http.csrf(c->c.disable()).authorizeHttpRequests(a->a
   .requestMatchers(org.springframework.http.HttpMethod.GET,"/api/courses").permitAll()
   .requestMatchers("/internal/courses/*/certificate-metadata").hasRole("svc_certificate")
   .requestMatchers("/api/courses/internal/**").hasRole("svc_exam")
   .requestMatchers("/internal/chapters/**").hasRole("svc_exam")
   .requestMatchers("/internal/courses/*/chapters","/internal/courses/*/completion/**").hasRole("svc_exam")
   .requestMatchers("/internal/**").denyAll()
   .requestMatchers("/api/courses/admin/**").hasRole("platform_admin")
   .anyRequest().authenticated())
   .oauth2ResourceServer(o->o.jwt(j->j.jwtAuthenticationConverter(conv))).build();
 }
}
