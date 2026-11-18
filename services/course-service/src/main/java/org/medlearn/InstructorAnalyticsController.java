package org.medlearn;

import java.util.*;
import org.springframework.http.HttpStatus;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

/** Instructor-scoped aggregate reporting; no learner PII is returned. */
@RestController
@RequestMapping("/api/courses/instructor")
public class InstructorAnalyticsController {
 private final JdbcTemplate jdbc;
 public InstructorAnalyticsController(JdbcTemplate jdbc) { this.jdbc=jdbc; }
 @GetMapping("/overview")
 public List<Map<String,Object>> overview(JwtAuthenticationToken auth) {
  UUID instructor=subject(auth);
  return jdbc.queryForList("""
    SELECT c.id AS course_id,c.title_sq,c.title_en,c.status,
      count(DISTINCT ch.id) AS chapter_count,
      count(DISTINCT p.user_id) AS active_learners,
      count(p.id) AS completed_chapter_events
    FROM course c LEFT JOIN chapter ch ON ch.course_id=c.id
    LEFT JOIN chapter_progress p ON p.chapter_id=ch.id
    WHERE c.instructor_id=?
    GROUP BY c.id,c.title_sq,c.title_en,c.status ORDER BY c.title_en
    """,instructor);
 }
 @GetMapping("/{courseId}/statistics")
 public Map<String,Object> statistics(@PathVariable UUID courseId,JwtAuthenticationToken auth) {
  UUID instructor=subject(auth);
  Long owner=jdbc.queryForObject("SELECT count(*) FROM course WHERE id=? AND instructor_id=?",Long.class,courseId,instructor);
  if(owner==null||owner==0)throw new ResponseStatusException(HttpStatus.NOT_FOUND);
  return jdbc.queryForMap("""
    SELECT (SELECT count(*) FROM chapter WHERE course_id=?) AS chapters,
      (SELECT count(DISTINCT p.user_id) FROM chapter_progress p JOIN chapter ch ON ch.id=p.chapter_id WHERE ch.course_id=?) AS learners_with_progress,
      (SELECT count(*) FROM chapter_progress p JOIN chapter ch ON ch.id=p.chapter_id WHERE ch.course_id=?) AS completed_chapters
    """,courseId,courseId,courseId);
 }
 private UUID subject(JwtAuthenticationToken auth) {
  try{return UUID.fromString(auth.getName());}
  catch(IllegalArgumentException ex){throw new ResponseStatusException(HttpStatus.UNAUTHORIZED,"Invalid subject");}
 }
}
