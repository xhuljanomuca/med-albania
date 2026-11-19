package org.medlearn.exam;

import org.junit.jupiter.api.Test;
import org.springframework.security.oauth2.jwt.Jwt;

import java.time.Instant;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class JwtAudienceValidatorTest {
    private Jwt jwt(List<String> audience) {
        var builder = Jwt.withTokenValue("test").header("alg", "RS256").subject("user")
                .issuedAt(Instant.now()).expiresAt(Instant.now().plusSeconds(300));
        if (audience != null) builder.audience(audience);
        return builder.build();
    }

    @Test
    void acceptsRequiredAudience() {
        assertFalse(JwtAudienceConfig.audienceValidator("medlearn-api").validate(jwt(List.of("medlearn-api", "account"))).hasErrors());
    }

    @Test
    void rejectsOtherAudience() {
        assertTrue(JwtAudienceConfig.audienceValidator("medlearn-api").validate(jwt(List.of("account"))).hasErrors());
    }

    @Test
    void rejectsAbsentAudience() {
        assertTrue(JwtAudienceConfig.audienceValidator("medlearn-api").validate(jwt(null)).hasErrors());
    }
}
