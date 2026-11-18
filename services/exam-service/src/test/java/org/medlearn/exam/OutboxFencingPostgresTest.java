package org.medlearn.exam;
import static org.junit.jupiter.api.Assertions.*;
import java.sql.DriverManager;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.datasource.DriverManagerDataSource;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

@Testcontainers(disabledWithoutDocker = true)
class OutboxFencingPostgresTest {
 @Container static PostgreSQLContainer<?> postgres=new PostgreSQLContainer<>("postgres:17-alpine");
 @Test void staleWorkerCannotAcknowledgeNewClaimAndRequeueWorks() throws Exception {
  var ds=new DriverManagerDataSource(postgres.getJdbcUrl(),postgres.getUsername(),postgres.getPassword());
  var db=new JdbcTemplate(ds);
  db.execute("CREATE TABLE progress_outbox(id uuid primary key,attempt_id uuid,chapter_id uuid,user_id uuid,claimed_until timestamptz,claim_token uuid,retry_count int default 0,delivered_at timestamptz,dead_lettered_at timestamptz,next_attempt_at timestamptz default now(),last_error varchar(128))");
  UUID id=UUID.randomUUID();
  db.update("INSERT INTO progress_outbox(id,attempt_id,chapter_id,user_id) VALUES(?,?,?,?)",id,UUID.randomUUID(),UUID.randomUUID(),UUID.randomUUID());
  var delivery=new ProgressOutboxDelivery(db);
  var first=delivery.claim(1).get(0);
  db.update("UPDATE progress_outbox SET claimed_until=now()-interval '1 second' WHERE id=?",id);
  var second=delivery.claim(1).get(0);
  assertNotEquals(first.claimToken(),second.claimToken());
  delivery.succeeded(id,first.claimToken());
  assertEquals(0L,db.queryForObject("SELECT count(*) FROM progress_outbox WHERE delivered_at IS NOT NULL",Long.class));
  delivery.failed(id,second.claimToken(),"transient");
  db.update("UPDATE progress_outbox SET dead_lettered_at=now() WHERE id=?",id);
  assertEquals(1L,delivery.deadLetterCount());
  assertEquals(1,delivery.requeue(id));
  assertEquals(0L,delivery.deadLetterCount());
 }
}
