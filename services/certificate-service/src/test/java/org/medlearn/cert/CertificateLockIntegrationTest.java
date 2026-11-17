package org.medlearn.cert;

import org.junit.jupiter.api.Test;
import org.testcontainers.containers.PostgreSQLContainer;
import java.sql.*;
import java.util.concurrent.*;
import static org.junit.jupiter.api.Assertions.*;

/** Tests the same transaction-scoped advisory lock used by certificate issuance. */
class CertificateLockIntegrationTest {
 @Test void concurrentRequestsForSameAttemptAreSerialized() throws Exception {
  try (var postgres = new PostgreSQLContainer<>("postgres:17-alpine")) {
   postgres.start();
   var attempt = "3f6a142a-1ac5-4784-ae19-724ce94b8e99";
   var pool = Executors.newFixedThreadPool(2);
   var firstLocked = new CountDownLatch(1);
   var releaseFirst = new CountDownLatch(1);
   try {
    var first = pool.submit(() -> {
     try (Connection c = DriverManager.getConnection(postgres.getJdbcUrl(), postgres.getUsername(), postgres.getPassword())) {
      c.setAutoCommit(false);
      try (PreparedStatement ps = c.prepareStatement("SELECT pg_advisory_xact_lock(hashtextextended(?::text, 0))")) {
       ps.setString(1,attempt); ps.execute(); firstLocked.countDown();
       if (!releaseFirst.await(10,TimeUnit.SECONDS)) throw new AssertionError("Timed out waiting for release");
      }
      c.commit();
     }
     return true;
    });
    assertTrue(firstLocked.await(10,TimeUnit.SECONDS));
    var second = pool.submit(() -> {
     try (Connection c = DriverManager.getConnection(postgres.getJdbcUrl(), postgres.getUsername(), postgres.getPassword())) {
      c.setAutoCommit(false);
      try (PreparedStatement ps = c.prepareStatement("SELECT pg_try_advisory_xact_lock(hashtextextended(?::text, 0))")) {
       ps.setString(1,attempt);
       try (ResultSet rs = ps.executeQuery()) {rs.next(); boolean acquired=rs.getBoolean(1);c.rollback();return acquired;}
      }
     }
    });
    assertFalse(second.get(10,TimeUnit.SECONDS),"Second transaction must not acquire the lock while first holds it");
    releaseFirst.countDown();assertTrue(first.get(10,TimeUnit.SECONDS));
   } finally {releaseFirst.countDown();pool.shutdownNow();}
  }
 }
}
