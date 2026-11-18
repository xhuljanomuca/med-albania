package org.medlearn;

import org.junit.jupiter.api.Test;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import java.time.Instant;
import java.util.*;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;
import static org.mockito.ArgumentMatchers.*;

class EnrollmentHistoryTest {
 @Test void learnerHistoryIsScopedToJwtSubject() {
  UUID user=UUID.randomUUID();
  GrantRepo grants=mock(GrantRepo.class);
  JdbcTemplate jdbc=mock(JdbcTemplate.class);
  Jwt jwt=Jwt.withTokenValue("test-token").header("alg","none").subject(user.toString()).issuedAt(Instant.now()).expiresAt(Instant.now().plusSeconds(60)).build();
  JwtAuthenticationToken auth=new JwtAuthenticationToken(jwt);
  when(jdbc.queryForList(anyString(),eq(user))).thenReturn(List.of(Map.of("action","GRANTED")));
  var result=new EnrollmentController(grants,jdbc).myHistory(auth);
  assertEquals(1,result.size());
  verify(jdbc).queryForList(contains("WHERE user_id=?"),eq(user));
 }
 @Test void learnerSummaryDoesNotReadAnotherUsersGrants() {
  UUID user=UUID.randomUUID();
  GrantRepo grants=mock(GrantRepo.class);
  JdbcTemplate jdbc=mock(JdbcTemplate.class);
  Jwt jwt=Jwt.withTokenValue("test-token").header("alg","none").subject(user.toString()).issuedAt(Instant.now()).expiresAt(Instant.now().plusSeconds(60)).build();
  when(grants.findByUserId(user)).thenReturn(List.of());
  when(jdbc.queryForObject(anyString(),eq(Long.class),eq(user))).thenReturn(3L);
  var summary=new EnrollmentController(grants,jdbc).mySummary(new JwtAuthenticationToken(jwt));
  assertEquals(0L,summary.get("activeCourses"));
  assertEquals(3L,summary.get("accessChanges"));
  verify(grants).findByUserId(user);
 }
}
