package org.medlearn;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.springframework.jdbc.core.JdbcTemplate;

class KeycloakEnableWorkerTest {
 @Test void successfulRemoteEnablementAcknowledgesClaim() {
  JdbcTemplate jdbc=mock(JdbcTemplate.class);KeycloakAdmin kc=mock(KeycloakAdmin.class);
  var worker=new KeycloakEnableWorker(jdbc,kc);
  var w=new KeycloakEnableWorker.Work(UUID.randomUUID(),UUID.randomUUID(),UUID.randomUUID(),UUID.randomUUID());
  when(jdbc.update(contains("SET completed_at=now()"),eq(w.id()),eq(w.claimToken()))).thenReturn(1);
  worker.success(w);
  verify(jdbc).update(contains("SET status='ACTIVE'"),eq(w.profileId()));
 }
 @Test void staleClaimCannotActivateProfile() {
  JdbcTemplate jdbc=mock(JdbcTemplate.class);var worker=new KeycloakEnableWorker(jdbc,mock(KeycloakAdmin.class));
  var w=new KeycloakEnableWorker.Work(UUID.randomUUID(),UUID.randomUUID(),UUID.randomUUID(),UUID.randomUUID());
  when(jdbc.update(contains("SET completed_at=now()"),eq(w.id()),eq(w.claimToken()))).thenReturn(0);
  worker.success(w);
  verify(jdbc,never()).update(contains("SET status='ACTIVE'"),any());
 }
 @Test void failedRemoteEnablementRetainsRetryableRecord() {
  JdbcTemplate jdbc=mock(JdbcTemplate.class);var worker=new KeycloakEnableWorker(jdbc,mock(KeycloakAdmin.class));
  var w=new KeycloakEnableWorker.Work(UUID.randomUUID(),UUID.randomUUID(),UUID.randomUUID(),UUID.randomUUID());
  worker.failure(w,new IllegalStateException("temporarily unavailable"));
  verify(jdbc).update(contains("next_attempt_at=now()"),contains("temporarily unavailable"),eq(w.id()),eq(w.claimToken()));
 }
}
