package org.medlearn;

import java.util.*;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.support.TransactionTemplate;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * Durable, retryable Keycloak enablement. Claims are leased to allow recovery after crashes.
 */
@Component
public class KeycloakEnableWorker {
    private final JdbcTemplate jdbc;
    private final KeycloakAdmin keycloak;
    private TransactionTemplate transactions;

    public KeycloakEnableWorker(JdbcTemplate jdbc, KeycloakAdmin keycloak) {
        this.jdbc = jdbc;
        this.keycloak = keycloak;
    }

    @Autowired
    public void configureTransactions(PlatformTransactionManager manager) {
        this.transactions = new TransactionTemplate(manager);
    }

    @Transactional
    public List<Work> claim() {
        return jdbc.query("""
                UPDATE keycloak_enable_outbox SET claimed_until=now()+interval '60 seconds', claim_token=gen_random_uuid(), retry_count=retry_count+1
                WHERE id IN (SELECT id FROM keycloak_enable_outbox WHERE completed_at IS NULL AND dead_lettered_at IS NULL
                 AND next_attempt_at<=now() AND (claimed_until IS NULL OR claimed_until<now())
                 ORDER BY next_attempt_at,id FOR UPDATE SKIP LOCKED LIMIT 10)
                RETURNING id,profile_id,keycloak_id,claim_token
                """, (rs, n) -> new Work(rs.getObject(1, UUID.class), rs.getObject(2, UUID.class), rs.getObject(3, UUID.class), rs.getObject(4, UUID.class)));
    }

    @Transactional
    public void success(Work work) {
        int changed = jdbc.update("UPDATE keycloak_enable_outbox SET completed_at=now(),claimed_until=NULL,claim_token=NULL,last_error=NULL WHERE id=? AND claim_token=? AND completed_at IS NULL AND dead_lettered_at IS NULL", work.id(), work.claimToken());
        if (changed == 1)
            jdbc.update("UPDATE member_profile SET status='ACTIVE' WHERE id=? AND status='APPROVAL_PENDING'", work.profileId());
    }

    @Transactional
    public void failure(Work work, Exception ex) {
        String error = ex.getClass().getSimpleName() + ": " + Objects.toString(ex.getMessage(), "");
        jdbc.update("""
                UPDATE keycloak_enable_outbox SET claimed_until=NULL,claim_token=NULL,last_error=?,
                dead_lettered_at=CASE WHEN retry_count>=12 THEN now() ELSE NULL END,
                next_attempt_at=now()+make_interval(secs=>LEAST(3600,CAST(POWER(2,LEAST(retry_count,11)) AS integer)))
                WHERE id=? AND claim_token=? AND completed_at IS NULL AND dead_lettered_at IS NULL
                """, error.length() > 500 ? error.substring(0, 500) : error, work.id(), work.claimToken());
    }

    @Transactional
    public int requeue(UUID id) {
        return jdbc.update("UPDATE keycloak_enable_outbox SET dead_lettered_at=NULL,retry_count=0,next_attempt_at=now(),claimed_until=NULL,claim_token=NULL,last_error=NULL WHERE id=? AND dead_lettered_at IS NOT NULL AND completed_at IS NULL", id);
    }

    public long pendingCount() {
        return jdbc.queryForObject("SELECT count(*) FROM keycloak_enable_outbox WHERE completed_at IS NULL AND dead_lettered_at IS NULL", Long.class);
    }

    public long deadLetterCount() {
        return jdbc.queryForObject("SELECT count(*) FROM keycloak_enable_outbox WHERE dead_lettered_at IS NOT NULL", Long.class);
    }

    @Scheduled(fixedDelayString = "${medlearn.approval-sync-interval-ms:5000}")
    public void dispatch() {
        // Scheduled self-invocation bypasses Spring transactional proxies. Explicit
        // templates commit each claim and acknowledgement independently of HTTP calls.
        if (transactions == null) throw new IllegalStateException("Transaction manager not configured");
        List<Work> batch = transactions.execute(status -> claim());
        for (Work work : Objects.requireNonNull(batch)) {
            try {
                keycloak.enable(work.keycloakId());
                transactions.executeWithoutResult(status -> success(work));
            } catch (Exception ex) {
                transactions.executeWithoutResult(status -> failure(work, ex));
            }
        }
    }

    public record Work(UUID id, UUID profileId, UUID keycloakId, UUID claimToken) {
    }
}
