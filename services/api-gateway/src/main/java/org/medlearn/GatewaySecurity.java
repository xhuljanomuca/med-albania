package org.medlearn;
import org.springframework.context.annotation.*;
import org.springframework.security.config.web.server.ServerHttpSecurity;
import org.springframework.security.web.server.SecurityWebFilterChain;
import org.springframework.web.cors.*;
import org.springframework.web.cors.reactive.*;
import java.util.List;
@Configuration public class GatewaySecurity {
 @Bean SecurityWebFilterChain security(ServerHttpSecurity http){return http.csrf(c->c.disable()).cors(c->{}).authorizeExchange(a->a.pathMatchers(org.springframework.http.HttpMethod.GET,"/api/certificates/verify/**").permitAll().pathMatchers(org.springframework.http.HttpMethod.POST,"/api/users/register").permitAll().pathMatchers(org.springframework.http.HttpMethod.GET,"/api/users/catalog").permitAll().pathMatchers(org.springframework.http.HttpMethod.GET,"/api/courses").permitAll().pathMatchers("/api/**/internal/**","/internal/**","/api/courses/internal/**").denyAll().pathMatchers("/api/users/admin/**","/api/enrollments/admin/**","/api/courses/admin/**","/api/exams/admin/**","/api/certificates/admin/**") .hasRole("platform_admin").anyExchange().authenticated()).oauth2ResourceServer(o->o.jwt(j->j.jwtAuthenticationConverter(jwtConverter()))).build();}

 @Bean org.springframework.security.oauth2.server.resource.authentication.ReactiveJwtAuthenticationConverter jwtConverter(){
  var converter=new org.springframework.security.oauth2.server.resource.authentication.ReactiveJwtAuthenticationConverter();
  converter.setJwtGrantedAuthoritiesConverter(jwt->{
   java.util.Map<String,Object> realm=jwt.getClaimAsMap("realm_access");
   if(realm==null||!(realm.get("roles") instanceof java.util.Collection<?> roles))return reactor.core.publisher.Flux.empty();
   return reactor.core.publisher.Flux.fromIterable(roles).map(role->(org.springframework.security.core.GrantedAuthority)new org.springframework.security.core.authority.SimpleGrantedAuthority("ROLE_"+role));
  });return converter;
 }
 @Bean CorsWebFilter corsFilter(){var c=new CorsConfiguration();c.setAllowedOrigins(List.of("http://localhost:3000"));c.setAllowedMethods(List.of("GET","POST","PUT","DELETE","OPTIONS"));c.setAllowedHeaders(List.of("Authorization","Content-Type","Accept-Language"));var s=new UrlBasedCorsConfigurationSource();s.registerCorsConfiguration("/**",c);return new CorsWebFilter(s);}
}
