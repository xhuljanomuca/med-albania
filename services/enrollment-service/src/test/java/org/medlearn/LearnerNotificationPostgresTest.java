package org.medlearn;

import java.sql.*;
import java.util.UUID;
import org.flywaydb.core.Flyway;
import org.junit.jupiter.api.Test;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import static org.junit.jupiter.api.Assertions.*;

/** Exercises notification ownership and read/unread constraints against real Flyway migrations. */
@Testcontainers(disabledWithoutDocker=true)
class LearnerNotificationPostgresTest {
 @Container static PostgreSQLContainer<?> db=new PostgreSQLContainer<>("postgres:17-alpine");
 @Test void learnerCanMarkOnlyOwnNotificationRead() throws Exception {
  Flyway.configure().dataSource(db.getJdbcUrl(),db.getUsername(),db.getPassword())
   .locations("classpath:db/migration").load().migrate();
  UUID owner=UUID.randomUUID(),other=UUID.randomUUID(),id=UUID.randomUUID();
  try(Connection c=DriverManager.getConnection(db.getJdbcUrl(),db.getUsername(),db.getPassword())) {
   try(PreparedStatement p=c.prepareStatement("INSERT INTO learner_notification(id,user_id,category,title_en,title_sq,body_en,body_sq) VALUES (?,?,'GENERAL','Hello','Përshëndetje','Message','Mesazh')")){
    p.setObject(1,id);p.setObject(2,owner);assertEquals(1,p.executeUpdate());
   }
   try(PreparedStatement p=c.prepareStatement("UPDATE learner_notification SET read_at=now() WHERE id=? AND user_id=? AND read_at IS NULL")){
    p.setObject(1,id);p.setObject(2,other);assertEquals(0,p.executeUpdate());
    p.setObject(2,owner);assertEquals(1,p.executeUpdate());
    assertEquals(0,p.executeUpdate());
   }
  }
 }
}
