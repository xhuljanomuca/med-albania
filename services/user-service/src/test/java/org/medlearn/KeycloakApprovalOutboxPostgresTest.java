package org.medlearn;

import org.junit.jupiter.api.Test;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.flywaydb.core.Flyway;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.datasource.DriverManagerDataSource;

import java.util.*;
import java.util.concurrent.*;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Exercises the actual V1-V5 User Service Flyway schema. Requires Docker.
 */
@Testcontainers(disabledWithoutDocker = true)
class KeycloakApprovalOutboxPostgresTest {
    @Container
    static PostgreSQLContainer<?> db = new PostgreSQLContainer<>("postgres:17-alpine");

    JdbcTemplate database() {
        var ds = new DriverManagerDataSource(db.getJdbcUrl(), db.getUsername(), db.getPassword());
        Flyway.configure().dataSource(ds).locations("classpath:db/migration").load().migrate();
        return new JdbcTemplate(ds);
    }

    @Test
    void claimFencingDeadLetterAndManualRecovery() {
        JdbcTemplate jdbc = database();
        UUID profile = UUID.randomUUID(), keycloak = UUID.randomUUID(), id = UUID.randomUUID();
        jdbc.update("INSERT INTO member_profile(id,keycloak_id,academy,category,first_name,surname,email,status) VALUES (?,?,?,?,?,?,?,?)", profile, keycloak, "PROFESSIONAL", "PROFESSIONAL", "Test", "Learner", id + "@example.org", "APPROVAL_PENDING");
        jdbc.update("INSERT INTO keycloak_enable_outbox(id,profile_id,keycloak_id) VALUES (?,?,?)", id, profile, keycloak);
        KeycloakEnableWorker worker = new KeycloakEnableWorker(jdbc, org.mockito.Mockito.mock(KeycloakAdmin.class));
        var first = worker.claim();
        assertEquals(1, first.size());
        assertTrue(worker.claim().isEmpty(), "leased item must not be double-claimed");
        worker.failure(first.get(0), new IllegalStateException("temporary outage"));
        assertTrue(worker.claim().isEmpty(), "retry backoff must prevent immediate claim");
        jdbc.update("UPDATE keycloak_enable_outbox SET next_attempt_at=now()-interval '1 second' WHERE id=?", id);
        var second = worker.claim();
        assertEquals(1, second.size());
        assertNotEquals(first.get(0).claimToken(), second.get(0).claimToken());
        worker.success(first.get(0));
        assertEquals("APPROVAL_PENDING", jdbc.queryForObject("SELECT status FROM member_profile WHERE id=?", String.class, profile), "stale claim must not activate user");
        worker.success(second.get(0));
        assertEquals("ACTIVE", jdbc.queryForObject("SELECT status FROM member_profile WHERE id=?", String.class, profile));
        assertEquals(0, worker.requeue(id), "completed delivery cannot be requeued");
        UUID id2 = UUID.randomUUID(), profile2 = UUID.randomUUID(), kc2 = UUID.randomUUID();
        jdbc.update("INSERT INTO member_profile(id,keycloak_id,academy,category,first_name,surname,email,status) VALUES (?,?,?,?,?,?,?,?)", profile2, kc2, "PROFESSIONAL", "PROFESSIONAL", "Test", "Second", id2 + "@example.org", "APPROVAL_PENDING");
        jdbc.update("INSERT INTO keycloak_enable_outbox(id,profile_id,keycloak_id,retry_count) VALUES (?,?,?,11)", id2, profile2, kc2);
        var finalAttempt = worker.claim();
        assertEquals(1, finalAttempt.size());
        worker.failure(finalAttempt.get(0), new IllegalStateException("still unavailable"));
        assertEquals(1L, worker.deadLetterCount());
        assertEquals(1, worker.requeue(id2));
        assertEquals(0L, worker.deadLetterCount());
        assertEquals(0, worker.requeue(id2));
        assertEquals(1, worker.claim().size(), "requeued delivery must become claimable");
    }
}
