package org.medlearn.cert;
import org.springframework.context.annotation.*;import org.springframework.security.config.annotation.web.builders.HttpSecurity;import org.springframework.security.web.SecurityFilterChain;import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationConverter;import org.springframework.security.core.authority.SimpleGrantedAuthority;import java.util.*;
@Configuration public class CertificateSecurity {
 @Bean SecurityFilterChain filter(HttpSecurity http)throws Exception {
  var conv=new JwtAuthenticationConverter();conv.setJwtGrantedAuthoritiesConverter(jwt->{Map<String,Object> realm=jwt.getClaimAsMap("realm_access");if(realm==null||!(realm.get("roles") instanceof Collection<?> roles))return List.of();return roles.stream().map(x->new SimpleGrantedAuthority("ROLE_"+x)).toList();});
  return http.csrf(c->c.disable()).authorizeHttpRequests(a->a.requestMatchers(org.springframework.http.HttpMethod.GET,"/api/certificates/verify/**").permitAll().requestMatchers("/api/certificates/admin/**").hasRole("platform_admin").anyRequest().authenticated()).oauth2ResourceServer(o->o.jwt(j->j.jwtAuthenticationConverter(conv))).build();
 }
}
