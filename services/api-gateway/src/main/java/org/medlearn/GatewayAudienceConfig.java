package org.medlearn;

import org.springframework.context.annotation.*;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.oauth2.jwt.*;
import org.springframework.security.oauth2.core.*;

@Configuration
public class GatewayAudienceConfig {
    @Bean
    ReactiveJwtDecoder jwtDecoder(@Value("${spring.security.oauth2.resourceserver.jwt.jwk-set-uri}") String jwks,
                                  @Value("${spring.security.oauth2.resourceserver.jwt.issuer-uri}") String issuer,
                                  @Value("${medlearn.security.audience}") String audience) {
        NimbusReactiveJwtDecoder decoder = NimbusReactiveJwtDecoder.withJwkSetUri(jwks).build();
        decoder.setJwtValidator(new DelegatingOAuth2TokenValidator<>(JwtValidators.createDefaultWithIssuer(issuer), jwt ->
                jwt.getAudience().contains(audience) ? OAuth2TokenValidatorResult.success() :
                        OAuth2TokenValidatorResult.failure(new OAuth2Error("invalid_token", "Missing audience", null))));
        return decoder;
    }
}
