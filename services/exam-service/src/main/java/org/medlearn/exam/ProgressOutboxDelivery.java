package org.medlearn.exam;

import java.sql.ResultSet;
import java.util.List;
import java.util.UUID;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Short claims with SKIP LOCKED; leases recover after worker crashes.
 */
@Service
public class ProgressOutboxDelivery {
    private final JdbcTemplate jdbc;

    public ProgressOutboxDelivery(JdbcTemplate jdbc) {
        this.jdbc = jdbc;
    }

    @Transactional
    public List<Item> claim(int limit) {
        return jdbc.query("""
                UPDATE progress_outbox SET claim_token=gen_random_uuid(),claimed_until=now()+interval '45 seconds',
                  retry_count=retry_count+1
                WHERE id IN (SELECT id FROM progress_outbox
                 WHERE delivered_at IS NULL AND dead_lettered_at IS NULL
                   AND next_attempt_at<=now() AND (claimed_until IS NULL OR claimed_until<now())
                 ORDER BY next_attempt_at,id FOR UPDATE SKIP LOCKED LIMIT ?)
                RETURNING id,attempt_id,chapter_id,user_id,claim_token
                """, (rs, n) -> new Item(rs.getObject("id", UUID.class), rs.getObject("attempt_id", UUID.class),
                rs.getObject("chapter_id", UUID.class), rs.getObject("user_id", UUID.class), rs.getObject("claim_token", UUID.class)), limit);
    }

    @Transactional
    public int succeeded(UUID id, UUID token) {
        return jdbc.update("UPDATE progress_outbox SET delivered_at=now(),claimed_until=NULL,claim_token=NULL,last_error=NULL WHERE id=? AND claim_token=? AND delivered_at IS NULL AND dead_lettered_at IS NULL", id, token);
    }

    @Transactional
    public int failed(UUID id, UUID token, String reason) {
        return jdbc.update("""
                UPDATE progress_outbox SET claimed_until=NULL,claim_token=NULL,last_error=?,
                dead_lettered_at=CASE WHEN retry_count>=12 THEN now() ELSE NULL END,
                next_attempt_at=now()+make_interval(secs => LEAST(3600, CAST(POWER(2,LEAST(retry_count,11)) AS integer)))
                WHERE id=? AND claim_token=? AND delivered_at IS NULL AND dead_lettered_at IS NULL
                """, reason, id, token);
    }

    @Transactional
    public int requeue(UUID id) {
        return jdbc.update("UPDATE progress_outbox SET dead_lettered_at=NULL,claimed_until=NULL,claim_token=NULL,next_attempt_at=now(),retry_count=0,last_error=NULL WHERE id=? AND dead_lettered_at IS NOT NULL", id);
    }

    public long deadLetterCount() {
        return jdbc.queryForObject("SELECT count(*) FROM progress_outbox WHERE dead_lettered_at IS NOT NULL", Long.class);
    }

    public long pendingCount() {
        return jdbc.queryForObject("SELECT count(*) FROM progress_outbox WHERE delivered_at IS NULL AND dead_lettered_at IS NULL", Long.class);
    }

    public record Item(UUID id, UUID attemptId, UUID chapterId, UUID userId, UUID claimToken) {
    }
}
