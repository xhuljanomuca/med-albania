package org.medlearn.exam;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.springframework.jdbc.core.JdbcTemplate;
class OutboxDeliveryTest {
 @Test void failureRecordsRetryWithoutDeletingEvent(){
  JdbcTemplate db=mock(JdbcTemplate.class);var delivery=new ProgressOutboxDelivery(db);
  UUID id=UUID.randomUUID();UUID token=UUID.randomUUID();delivery.failed(id,token,"ResourceAccessException");
  verify(db).update(contains("dead_lettered_at"),eq("ResourceAccessException"),eq(id),eq(token));
 }
 @Test void staleAcknowledgementReturnsZero(){
  JdbcTemplate db=mock(JdbcTemplate.class);var delivery=new ProgressOutboxDelivery(db);
  UUID id=UUID.randomUUID();UUID oldToken=UUID.randomUUID();
  assertEquals(0,delivery.succeeded(id,oldToken));
  assertEquals(0,delivery.failed(id,oldToken,"expired"));
 }
 @Test void successMarksDelivered(){
  JdbcTemplate db=mock(JdbcTemplate.class);var delivery=new ProgressOutboxDelivery(db);
  UUID id=UUID.randomUUID();UUID token=UUID.randomUUID();delivery.succeeded(id,token);
  verify(db).update(contains("delivered_at=now()"),eq(id),eq(token));
 }
}
