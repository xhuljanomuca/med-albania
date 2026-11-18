package org.medlearn.exam;

import static org.junit.jupiter.api.Assertions.*;
import java.sql.*;
import java.util.UUID;
import java.util.concurrent.*;
import org.junit.jupiter.api.Test;
import org.testcontainers.containers.PostgreSQLContainer;

/** Real PostgreSQL advisory lock regression; requires Docker. */
class ConcurrentAttemptCreationPostgresTest {
 @Test void secondTransactionWaitsForSameQuizAndLearner() throws Exception {
  try (var postgres=new PostgreSQLContainer<>("postgres:17-alpine")) {
   postgres.start();
   String key=UUID.randomUUID()+":"+UUID.randomUUID();
   try (Connection first=DriverManager.getConnection(postgres.getJdbcUrl(),postgres.getUsername(),postgres.getPassword());
        Connection second=DriverManager.getConnection(postgres.getJdbcUrl(),postgres.getUsername(),postgres.getPassword())) {
    first.setAutoCommit(false); second.setAutoCommit(false);
    try (PreparedStatement stmt=first.prepareStatement("SELECT pg_advisory_xact_lock(hashtextextended(?::text,0))")) {
     stmt.setString(1,key); stmt.executeQuery();
    }
    try (PreparedStatement stmt=second.prepareStatement("SELECT pg_try_advisory_xact_lock(hashtextextended(?::text,0))")) {
     stmt.setString(1,key);
     try (ResultSet rs=stmt.executeQuery()) {assertTrue(rs.next());assertFalse(rs.getBoolean(1));}
    }
    first.commit();
    try (PreparedStatement stmt=second.prepareStatement("SELECT pg_try_advisory_xact_lock(hashtextextended(?::text,0))")) {
     stmt.setString(1,key);
     try (ResultSet rs=stmt.executeQuery()) {assertTrue(rs.next());assertTrue(rs.getBoolean(1));}
    }
    second.commit();
   }
  }
 }
}
