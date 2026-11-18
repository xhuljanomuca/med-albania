package org.medlearn.exam;
import static org.junit.jupiter.api.Assertions.*;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.datasource.DriverManagerDataSource;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

@Testcontainers(disabledWithoutDocker = true)
class OutboxLifecyclePostgresTest {
 @Container static PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres:17-alpine");
 @Test void duplicateClaimsAreExcludedAndRetriesAreFenced() {
  var db = new JdbcTemplate(new DriverManagerDataSource(postgres.getJdbcUrl(),postgres.getUsername(),postgres.getPassword()));
  db.execute("CREATE TABLE progress_outbox(id uuid primary key,attempt_id uuid,chapter_id uuid,user_id uuid,created_at timestamptz default now(),delivered_at timestamptz,retry_count int default 0,next_attempt_at timestamptz default now(),claimed_until timestamptz,dead_lettered_at timestamptz,last_error varchar(128),claim_token uuid)");
  UUID id=UUID.randomUUID();
  db.update("INSERT INTO progress_outbox(id,attempt_id,chapter_id,user_id) VALUES (?,?,?,?)",id,UUID.randomUUID(),UUID.randomUUID(),UUID.randomUUID());
  var delivery=new ProgressOutboxDelivery(db);
  var first=delivery.claim(1).get(0);
  assertTrue(delivery.claim(1).isEmpty());
  assertEquals(1,delivery.failed(id,first.claimToken(),"timeout"));
  assertTrue(delivery.claim(1).isEmpty()); // retry backoff
  db.update("UPDATE progress_outbox SET next_attempt_at=now()-interval '1 second' WHERE id=?",id);
  var second=delivery.claim(1).get(0);
  assertNotEquals(first.claimToken(),second.claimToken());
  assertEquals(0,delivery.succeeded(id,first.claimToken()));
  assertEquals(1,delivery.succeeded(id,second.claimToken()));
  assertEquals(0,delivery.succeeded(id,second.claimToken()));
  assertEquals(0L,delivery.pendingCount());
 }
}
