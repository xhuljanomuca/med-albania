package org.medlearn;
import org.junit.jupiter.api.Test;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import java.util.*;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;
class GrantManagementTest {
 @Test void adminSearchRequiresFilter(){var c=new EnrollmentController(mock(GrantRepo.class),mock(JdbcTemplate.class));var e=assertThrows(ResponseStatusException.class,()->c.grants(null,null));assertEquals(400,e.getStatusCode().value());}
 @Test void adminCanListUserGrants(){var repo=mock(GrantRepo.class);var id=UUID.randomUUID();when(repo.findByUserId(id)).thenReturn(List.of(new CourseGrant()));assertEquals(1,new EnrollmentController(repo,mock(JdbcTemplate.class)).grants(id,null).size());}
 @Test void revokeExistingGrantRecordsAudit(){var repo=mock(GrantRepo.class);var jdbc=mock(JdbcTemplate.class);var id=UUID.randomUUID();var g=new CourseGrant();g.id=id;g.userId=UUID.randomUUID();g.courseId=UUID.randomUUID();when(repo.findById(id)).thenReturn(Optional.of(g));var auth=mock(JwtAuthenticationToken.class);when(auth.getName()).thenReturn(UUID.randomUUID().toString());assertEquals(204,new EnrollmentController(repo,jdbc).revoke(id,auth).getStatusCode().value());verify(repo).delete(g);verify(repo).flush();verify(jdbc).update(startsWith("INSERT INTO course_grant_audit"),any(UUID.class),eq(id),eq(g.userId),eq(g.courseId),eq("REVOKED"),any(UUID.class));}
 @Test void revokeUnknownGrantReturns404(){var c=new EnrollmentController(mock(GrantRepo.class),mock(JdbcTemplate.class));var e=assertThrows(ResponseStatusException.class,()->c.revoke(UUID.randomUUID(),mock(JwtAuthenticationToken.class)));assertEquals(404,e.getStatusCode().value());}
 @Test void auditHistoryUsesBoundParameter(){var jdbc=mock(JdbcTemplate.class);var id=UUID.randomUUID();new EnrollmentController(mock(GrantRepo.class),jdbc).auditHistory(id);verify(jdbc).queryForList(contains("WHERE user_id=?"),eq(id));}
}
