package org.medlearn;

import java.sql.*;
import java.util.UUID;
import org.flywaydb.core.Flyway;
import org.junit.jupiter.api.Test;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import static org.junit.jupiter.api.Assertions.*;

@Testcontainers(disabledWithoutDocker=true)
class InstructorAnalyticsPostgresTest {
 @Container static PostgreSQLContainer<?> db=new PostgreSQLContainer<>("postgres:17-alpine");
 @Test void courseOwnershipIsolatedInReportingQuery() throws Exception {
  Flyway.configure().dataSource(db.getJdbcUrl(),db.getUsername(),db.getPassword())
   .locations("classpath:db/migration").load().migrate();
  UUID owner=UUID.randomUUID(),other=UUID.randomUUID();
  try(Connection c=DriverManager.getConnection(db.getJdbcUrl(),db.getUsername(),db.getPassword())) {
   try(PreparedStatement p=c.prepareStatement("INSERT INTO course(id,slug,category,status,academy,instructor_id,title_sq,title_en) VALUES (?,?,'MEDICINE','PUBLISHED','OPEN',?,'Mjekësi','Medicine')")){
    p.setObject(1,UUID.randomUUID());p.setString(2,"instructor-test");p.setObject(3,owner);p.executeUpdate();
   }
   try(PreparedStatement p=c.prepareStatement("SELECT count(*) FROM course WHERE instructor_id=?")){
    p.setObject(1,owner);try(ResultSet rs=p.executeQuery()){assertTrue(rs.next());assertEquals(1,rs.getInt(1));}
    p.setObject(1,other);try(ResultSet rs=p.executeQuery()){assertTrue(rs.next());assertEquals(0,rs.getInt(1));}
   }
  }
 }
}
