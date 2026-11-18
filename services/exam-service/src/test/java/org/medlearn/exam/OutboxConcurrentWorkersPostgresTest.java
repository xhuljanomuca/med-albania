package org.medlearn.exam;
import static org.junit.jupiter.api.Assertions.*;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.datasource.DriverManagerDataSource;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
@Testcontainers(disabledWithoutDocker=true)
class OutboxConcurrentWorkersPostgresTest {
 @Container static PostgreSQLContainer<?> postgres=new PostgreSQLContainer<>("postgres:17-alpine");
 @Test void differentWorkersDoNotClaimTheSameEvent() {
  var db=new JdbcTemplate(new DriverManagerDataSource(postgres.getJdbcUrl(),postgres.getUsername(),postgres.getPassword()));
  db.execute("CREATE TABLE progress_outbox(id uuid primary key,attempt_id uuid,chapter_id uuid,user_id uuid,created_at timestamptz default now(),delivered_at timestamptz,retry_count int default 0,next_attempt_at timestamptz default now(),claimed_until timestamptz,dead_lettered_at timestamptz,last_error varchar(128),claim_token uuid)");
  UUID first=UUID.randomUUID(), second=UUID.randomUUID();
  for(UUID id:new UUID[]{first,second})db.update("INSERT INTO progress_outbox(id,attempt_id,chapter_id,user_id) VALUES(?,?,?,?)",id,UUID.randomUUID(),UUID.randomUUID(),UUID.randomUUID());
  var workerA=new ProgressOutboxDelivery(db);var workerB=new ProgressOutboxDelivery(db);
  var a=workerA.claim(1);var b=workerB.claim(1);
  assertEquals(1,a.size());assertEquals(1,b.size());assertNotEquals(a.get(0).id(),b.get(0).id());
  assertEquals(0,workerB.succeeded(a.get(0).id(),b.get(0).claimToken()));
  assertEquals(1,workerA.succeeded(a.get(0).id(),a.get(0).claimToken()));
  assertEquals(1,workerB.succeeded(b.get(0).id(),b.get(0).claimToken()));
  assertEquals(0,workerA.pendingCount());
 }
}
