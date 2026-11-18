package org.medlearn.exam;
import static org.junit.jupiter.api.Assertions.*;
import org.junit.jupiter.api.Test;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import java.sql.DriverManager;
@Testcontainers(disabledWithoutDocker = true)
class OutboxPostgresIntegrationTest {
 @Container static PostgreSQLContainer<?> postgres=new PostgreSQLContainer<>("postgres:17-alpine");
 @Test void skipLockedPreventsConcurrentClaim() throws Exception {
  try(var a=DriverManager.getConnection(postgres.getJdbcUrl(),postgres.getUsername(),postgres.getPassword());
      var b=DriverManager.getConnection(postgres.getJdbcUrl(),postgres.getUsername(),postgres.getPassword())) {
   try(var s=a.createStatement()){s.execute("CREATE TABLE queue_test(id integer PRIMARY KEY)");s.execute("INSERT INTO queue_test VALUES(1)");}
   a.setAutoCommit(false);b.setAutoCommit(false);
   try(var lock=a.createStatement();var waiting=b.createStatement()){
    try(var rs=lock.executeQuery("SELECT id FROM queue_test FOR UPDATE SKIP LOCKED")){assertTrue(rs.next());}
    try(var rs=waiting.executeQuery("SELECT id FROM queue_test FOR UPDATE SKIP LOCKED")){assertFalse(rs.next());}
   } finally {a.rollback();b.rollback();}
  }
 }
}
