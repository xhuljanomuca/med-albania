package org.medlearn;

import org.junit.jupiter.api.Test;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.web.server.ResponseStatusException;
import java.util.UUID;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

class EnrollmentConcurrencyTest {
 @Test void uniqueConstraintConflictReturns409() {
  GrantRepo repo=mock(GrantRepo.class);
  when(repo.saveAndFlush(any(CourseGrant.class))).thenThrow(new DataIntegrityViolationException("duplicate user/course"));
  EnrollmentController controller=new EnrollmentController(repo,mock(org.springframework.jdbc.core.JdbcTemplate.class));
  CourseGrant grant=new CourseGrant();grant.id=UUID.randomUUID();grant.userId=UUID.randomUUID();grant.courseId=UUID.randomUUID();
  ResponseStatusException ex=assertThrows(ResponseStatusException.class,()->controller.saveUnique(grant));
  assertEquals(409,ex.getStatusCode().value());
 }
 @Test void successfulGrantPersistsOnce() {
  GrantRepo repo=mock(GrantRepo.class);
  when(repo.saveAndFlush(any(CourseGrant.class))).thenAnswer(call->call.getArgument(0));
  EnrollmentController controller=new EnrollmentController(repo,mock(org.springframework.jdbc.core.JdbcTemplate.class));
  CourseGrant grant=new CourseGrant();grant.id=UUID.randomUUID();
  assertSame(grant,controller.saveUnique(grant));
  verify(repo,times(1)).saveAndFlush(grant);
 }
}
