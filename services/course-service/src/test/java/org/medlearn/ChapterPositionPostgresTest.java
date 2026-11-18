package org.medlearn;
import org.junit.jupiter.api.Test;
import org.flywaydb.core.Flyway;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import java.sql.*;
import java.util.*;
import java.util.concurrent.*;
import static org.junit.jupiter.api.Assertions.*;
@Testcontainers(disabledWithoutDocker=true)
class ChapterPositionPostgresTest {
 @Container static PostgreSQLContainer<?> db=new PostgreSQLContainer<>("postgres:17-alpine");
 @Test void concurrentChapterPositionIsUnique() throws Exception {
  // Validate the actual production migration history.
  Flyway.configure().dataSource(db.getJdbcUrl(),db.getUsername(),db.getPassword())
    .locations("classpath:db/migration").load().migrate();
  UUID course=UUID.randomUUID();
  try(Connection c=DriverManager.getConnection(db.getJdbcUrl(),db.getUsername(),db.getPassword());PreparedStatement p=c.prepareStatement("INSERT INTO course(id,slug,category,status,academy,title_sq,title_en) VALUES (?,'concurrent-course','MEDICINE','DRAFT','OPEN','Test','Test')")){p.setObject(1,course);p.executeUpdate();}
  ExecutorService pool=Executors.newFixedThreadPool(2);
  CountDownLatch start=new CountDownLatch(1);
  Callable<Boolean> insert=()->{start.await();try(Connection c=DriverManager.getConnection(db.getJdbcUrl(),db.getUsername(),db.getPassword());PreparedStatement p=c.prepareStatement("INSERT INTO chapter(id,course_id,position,title_sq,title_en) VALUES (?,?,1,'Test','Test')")){p.setObject(1,UUID.randomUUID());p.setObject(2,course);return p.executeUpdate()==1;}catch(SQLException ex){if("23505".equals(ex.getSQLState()))return false;throw ex;}};
  try {Future<Boolean> a=pool.submit(insert),b=pool.submit(insert);start.countDown();assertNotEquals(a.get(20,TimeUnit.SECONDS),b.get(20,TimeUnit.SECONDS));}
  finally{pool.shutdownNow();}
 }
}
