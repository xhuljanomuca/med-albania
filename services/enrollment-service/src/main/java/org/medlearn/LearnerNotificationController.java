package org.medlearn;

import java.util.*;
import org.springframework.http.ResponseEntity;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.web.bind.annotation.*;

/** All learner notification reads/writes are scoped to the authenticated JWT subject. */
@RestController
@RequestMapping("/api/enrollments/me/notifications")
public class LearnerNotificationController {
 private final JdbcTemplate jdbc;
 public LearnerNotificationController(JdbcTemplate jdbc) { this.jdbc = jdbc; }
 @GetMapping
 public List<Map<String,Object>> list(JwtAuthenticationToken auth, @RequestParam(defaultValue="false") boolean unreadOnly) {
  UUID userId=UUID.fromString(auth.getName());
  String sql="SELECT id,category,title_en,title_sq,body_en,body_sq,created_at,read_at FROM learner_notification WHERE user_id=?" +
   (unreadOnly?" AND read_at IS NULL":"") + " ORDER BY created_at DESC,id DESC LIMIT 100";
  return jdbc.queryForList(sql,userId);
 }
 @GetMapping("/summary")
 public Map<String,Long> summary(JwtAuthenticationToken auth) {
  Long unread=jdbc.queryForObject("SELECT count(*) FROM learner_notification WHERE user_id=? AND read_at IS NULL",Long.class,UUID.fromString(auth.getName()));
  return Map.of("unread",unread==null?0L:unread);
 }
 @PostMapping("/{id}/read")
 public ResponseEntity<Void> markRead(@PathVariable UUID id,JwtAuthenticationToken auth) {
  int count=jdbc.update("UPDATE learner_notification SET read_at=now() WHERE id=? AND user_id=? AND read_at IS NULL",id,UUID.fromString(auth.getName()));
  if(count==0) {
   Long own=jdbc.queryForObject("SELECT count(*) FROM learner_notification WHERE id=? AND user_id=?",Long.class,id,UUID.fromString(auth.getName()));
   if(own==null||own==0)return ResponseEntity.notFound().build();
  }
  return ResponseEntity.noContent().build();
 }
}
