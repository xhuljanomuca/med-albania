package org.medlearn.cert;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.oauth2.jwt.*;
import org.springframework.security.oauth2.core.*;
import java.util.*;
@Configuration public class JwtAudienceConfig {
 @Bean JwtDecoder jwtDecoder(@Value("${spring.security.oauth2.resourceserver.jwt.jwk-set-uri}") String jwks,
 @Value("${spring.security.oauth2.resourceserver.jwt.issuer-uri}") String issuer,
 @Value("${medlearn.security.audience}") String audience) {
  NimbusJwtDecoder decoder=NimbusJwtDecoder.withJwkSetUri(jwks).build();
  OAuth2TokenValidator<Jwt> issuerValidator=JwtValidators.createDefaultWithIssuer(issuer);
  OAuth2TokenValidator<Jwt> audienceValidator=jwt->jwt.getAudience().contains(audience)
    ? OAuth2TokenValidatorResult.success()
    : OAuth2TokenValidatorResult.failure(new OAuth2Error("invalid_token","Missing required audience: "+audience,null));
  decoder.setJwtValidator(new DelegatingOAuth2TokenValidator<>(issuerValidator,audienceValidator));
  return decoder;
 }
}
