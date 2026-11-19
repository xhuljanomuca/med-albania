package org.medlearn;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import java.util.*;

import org.junit.jupiter.api.Test;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;

class LearnerNotificationControllerTest {
    private JwtAuthenticationToken auth(UUID id) {
        Jwt jwt = Jwt.withTokenValue("test").header("alg", "none").subject(id.toString()).build();
        return new JwtAuthenticationToken(jwt);
    }

    @Test
    void notificationsAreScopedToAuthenticatedUser() {
        JdbcTemplate jdbc = mock(JdbcTemplate.class);
        UUID user = UUID.randomUUID();
        when(jdbc.queryForList(anyString(), eq(user))).thenReturn(List.of());
        assertTrue(new LearnerNotificationController(jdbc).list(auth(user), false).isEmpty());
        verify(jdbc).queryForList(contains("WHERE user_id=?"), eq(user));
    }

    @Test
    void foreignNotificationCannotBeMarkedRead() {
        JdbcTemplate jdbc = mock(JdbcTemplate.class);
        UUID user = UUID.randomUUID(), note = UUID.randomUUID();
        when(jdbc.queryForObject(anyString(), eq(Long.class), eq(note), eq(user))).thenReturn(0L);
        assertEquals(404, new LearnerNotificationController(jdbc).markRead(note, auth(user)).getStatusCode().value());
        verify(jdbc).update(contains("user_id=?"), eq(note), eq(user));
    }
}
