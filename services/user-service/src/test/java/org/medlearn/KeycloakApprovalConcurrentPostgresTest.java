package org.medlearn;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import java.util.*;
import java.util.concurrent.*;

import org.flywaydb.core.Flyway;
import org.junit.jupiter.api.Test;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.datasource.DriverManagerDataSource;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

/**
 * Real V1-V5 schema; two independent JDBC connections competing for one claim.
 */
@Testcontainers(disabledWithoutDocker = true)
class KeycloakApprovalConcurrentPostgresTest {
    @Container
    static PostgreSQLContainer<?> db = new PostgreSQLContainer<>("postgres:17-alpine");

    private JdbcTemplate jdbc() {
        var ds = new DriverManagerDataSource(db.getJdbcUrl(), db.getUsername(), db.getPassword());
        return new JdbcTemplate(ds);
    }

    @Test
    void concurrentClaimLeaseRecoveryAndIdempotentAcknowledgement() throws Exception {
        Flyway.configure().dataSource(db.getJdbcUrl(), db.getUsername(), db.getPassword())
                .locations("classpath:db/migration").load().migrate();
        var sql = jdbc();
        UUID profile = UUID.randomUUID(), kc = UUID.randomUUID(), event = UUID.randomUUID();
        sql.update("INSERT INTO member_profile(id,keycloak_id,academy,category,first_name,surname,email,status) VALUES (?,?,?,?,?,?,?,?)",
                profile, kc, "PROFESSIONAL", "PROFESSIONAL", "Test", "Concurrent", event + "@example.org", "APPROVAL_PENDING");
        sql.update("INSERT INTO keycloak_enable_outbox(id,profile_id,keycloak_id) VALUES (?,?,?)", event, profile, kc);
        var first = new KeycloakEnableWorker(jdbc(), mock(KeycloakAdmin.class));
        var second = new KeycloakEnableWorker(jdbc(), mock(KeycloakAdmin.class));
        ExecutorService pool = Executors.newFixedThreadPool(2);
        CountDownLatch start = new CountDownLatch(1);
        try {
            Future<List<KeycloakEnableWorker.Work>> a = pool.submit(() -> {
                start.await();
                return first.claim();
            });
            Future<List<KeycloakEnableWorker.Work>> b = pool.submit(() -> {
                start.await();
                return second.claim();
            });
            start.countDown();
            var all = new ArrayList<KeycloakEnableWorker.Work>();
            all.addAll(a.get(15, TimeUnit.SECONDS));
            all.addAll(b.get(15, TimeUnit.SECONDS));
            assertEquals(1, all.size(), "exactly one worker must claim the event");
            var stale = all.get(0);
            sql.update("UPDATE keycloak_enable_outbox SET claimed_until=now()-interval '1 second' WHERE id=?", event);
            var recovered = second.claim();
            assertEquals(1, recovered.size(), "expired lease must be reclaimable");
            assertNotEquals(stale.claimToken(), recovered.get(0).claimToken());
            first.success(stale);
            assertEquals("APPROVAL_PENDING", sql.queryForObject("SELECT status FROM member_profile WHERE id=?", String.class, profile));
            second.success(recovered.get(0));
            second.success(recovered.get(0)); // duplicate acknowledgement must be harmless
            assertEquals("ACTIVE", sql.queryForObject("SELECT status FROM member_profile WHERE id=?", String.class, profile));
            assertEquals(1, sql.queryForObject("SELECT count(*) FROM keycloak_enable_outbox WHERE completed_at IS NOT NULL AND id=?", Integer.class, event));
        } finally {
            pool.shutdownNow();
        }
    }
}
