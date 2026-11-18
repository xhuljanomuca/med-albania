
package org.medlearn;

import org.junit.jupiter.api.Test;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;

import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

class GrantManagementTest {

 @Test
 void adminSearchRequiresFilter() {
  var controller = new EnrollmentController(
          mock(GrantRepo.class),
          mock(JdbcTemplate.class)
  );

  var exception = assertThrows(
          ResponseStatusException.class,
          () -> controller.grants(null, null)
  );

  assertEquals(400, exception.getStatusCode().value());
 }

 @Test
 void adminCanListUserGrants() {
  var repo = mock(GrantRepo.class);
  var id = UUID.randomUUID();

  when(repo.findByUserId(id))
          .thenReturn(List.of(new CourseGrant()));

  var controller = new EnrollmentController(
          repo,
          mock(JdbcTemplate.class)
  );

  assertEquals(1, controller.grants(id, null).size());
 }

 @Test
 void revokeExistingGrantRecordsAudit() {
  var repo = mock(GrantRepo.class);
  var jdbc = mock(JdbcTemplate.class);

  var id = UUID.randomUUID();
  var grant = new CourseGrant();

  grant.id = id;
  grant.userId = UUID.randomUUID();
  grant.courseId = UUID.randomUUID();

  when(repo.findById(id))
          .thenReturn(Optional.of(grant));

  var auth = mock(JwtAuthenticationToken.class);

  when(auth.getName())
          .thenReturn(UUID.randomUUID().toString());

  var controller = new EnrollmentController(repo, jdbc);

  assertEquals(
          204,
          controller.revoke(id, auth).getStatusCode().value()
  );

  verify(repo).delete(grant);
  verify(repo).flush();

  verify(jdbc).update(
          startsWith("INSERT INTO course_grant_audit"),
          any(UUID.class),
          eq(id),
          eq(grant.userId),
          eq(grant.courseId),
          eq("REVOKED"),
          any(UUID.class)
  );
 }

 @Test
 void revokeUnknownGrantReturns404() {
  var controller = new EnrollmentController(
          mock(GrantRepo.class),
          mock(JdbcTemplate.class)
  );

  var exception = assertThrows(
          ResponseStatusException.class,
          () -> controller.revoke(
                  UUID.randomUUID(),
                  mock(JwtAuthenticationToken.class)
          )
  );

  assertEquals(404, exception.getStatusCode().value());
 }
}
