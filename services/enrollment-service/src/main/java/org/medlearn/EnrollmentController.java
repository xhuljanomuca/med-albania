package org.medlearn;
import org.springframework.web.bind.annotation.*;import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;import java.util.*;
@RestController @RequestMapping("/api/enrollments") public class EnrollmentController {
 @org.springframework.beans.factory.annotation.Value("${COURSE_SERVICE:http://course-service:8084}") String courseService;private final org.springframework.web.client.RestClient http=org.springframework.web.client.RestClient.create();private final GrantRepo repo;private final org.springframework.jdbc.core.JdbcTemplate jdbc;public EnrollmentController(GrantRepo repo,org.springframework.jdbc.core.JdbcTemplate jdbc){this.repo=repo;this.jdbc=jdbc;}
 public record GrantRequest(UUID userId,UUID courseId){}
 @GetMapping("/me") public List<CourseGrant> me(JwtAuthenticationToken auth){return repo.findByUserId(UUID.fromString(auth.getName()));}
 @GetMapping("/access/{courseId}") public Map<String,Boolean> access(@PathVariable UUID courseId,JwtAuthenticationToken auth){return Map.of("allowed",repo.existsByUserIdAndCourseId(UUID.fromString(auth.getName()),courseId));}
 @PostMapping("/open/{courseId}") public CourseGrant enrollOpen(@PathVariable UUID courseId,JwtAuthenticationToken auth){
  Map<?,?> check=http.get().uri(courseService+"/api/courses/"+courseId+"/eligibility").header("Authorization","Bearer "+auth.getToken().getTokenValue()).retrieve().body(Map.class);
  if(check==null||!Boolean.TRUE.equals(check.get("eligible"))||!"OPEN".equals(check.get("academy")))throw new org.springframework.web.server.ResponseStatusException(org.springframework.http.HttpStatus.FORBIDDEN,"Not eligible for open enrollment");
  UUID user=UUID.fromString(auth.getName());if(repo.existsByUserIdAndCourseId(user,courseId))throw new org.springframework.web.server.ResponseStatusException(org.springframework.http.HttpStatus.CONFLICT,"Already enrolled");
  CourseGrant g=new CourseGrant();g.id=UUID.randomUUID();g.userId=user;g.courseId=courseId;return saveUnique(g);
 }
 @GetMapping("/admin/grants") public List<CourseGrant> grants(@RequestParam(required=false) UUID userId,@RequestParam(required=false) UUID courseId){
  if(userId!=null)return repo.findByUserId(userId);
  if(courseId!=null)return repo.findByCourseId(courseId);
  throw new org.springframework.web.server.ResponseStatusException(org.springframework.http.HttpStatus.BAD_REQUEST,"Specify userId or courseId");
 }
 @org.springframework.transaction.annotation.Transactional
 @DeleteMapping("/admin/grants/{id}") public org.springframework.http.ResponseEntity<Void> revoke(@PathVariable UUID id,JwtAuthenticationToken auth){
  CourseGrant g=repo.findById(id).orElseThrow(()->new org.springframework.web.server.ResponseStatusException(org.springframework.http.HttpStatus.NOT_FOUND));
  repo.delete(g);repo.flush();audit(g,"REVOKED",auth);return org.springframework.http.ResponseEntity.noContent().build();
 }
 @org.springframework.transaction.annotation.Transactional
 @PostMapping("/admin/grants") public CourseGrant grant(@RequestBody GrantRequest r,JwtAuthenticationToken auth){if(r==null||r.userId()==null||r.courseId()==null)throw new org.springframework.web.server.ResponseStatusException(org.springframework.http.HttpStatus.BAD_REQUEST,"User and course required");if(repo.existsByUserIdAndCourseId(r.userId(),r.courseId()))throw new org.springframework.web.server.ResponseStatusException(org.springframework.http.HttpStatus.CONFLICT,"Already granted");CourseGrant g=new CourseGrant();g.id=UUID.randomUUID();g.userId=r.userId();g.courseId=r.courseId();CourseGrant saved=saveUnique(g);audit(saved,"GRANTED",auth);return saved;}
 /** Learners may inspect only their own course-access history. */
 @GetMapping("/me/history") public List<Map<String,Object>> myHistory(JwtAuthenticationToken auth){
  UUID learner=UUID.fromString(auth.getName());
  return jdbc.queryForList("SELECT grant_id,course_id,action,occurred_at FROM course_grant_audit WHERE user_id=? ORDER BY occurred_at DESC,id DESC LIMIT 100",learner);
 }
 @GetMapping("/me/summary") public Map<String,Object> mySummary(JwtAuthenticationToken auth){
  UUID learner=UUID.fromString(auth.getName());
  long active=repo.findByUserId(learner).size();
  Long changes=jdbc.queryForObject("SELECT count(*) FROM course_grant_audit WHERE user_id=?",Long.class,learner);
  return Map.of("activeCourses",active,"accessChanges",changes==null?0L:changes);
 }
 @GetMapping("/admin/audit") public List<Map<String,Object>> auditHistory(@RequestParam(required=false) UUID userId,@RequestParam(required=false) UUID courseId,@RequestParam(required=false) String action){
  if(userId==null&&courseId==null)throw new org.springframework.web.server.ResponseStatusException(org.springframework.http.HttpStatus.BAD_REQUEST,"Specify userId or courseId");
  if(action!=null&&!List.of("GRANTED","REVOKED").contains(action))throw new org.springframework.web.server.ResponseStatusException(org.springframework.http.HttpStatus.BAD_REQUEST,"Invalid action");
  StringBuilder sql=new StringBuilder("SELECT id,grant_id,user_id,course_id,action,actor_id,occurred_at FROM course_grant_audit WHERE 1=1");List<Object> args=new ArrayList<>();
  if(userId!=null){sql.append(" AND user_id=?");args.add(userId);}if(courseId!=null){sql.append(" AND course_id=?");args.add(courseId);}if(action!=null){sql.append(" AND action=?");args.add(action);}
  sql.append(" ORDER BY occurred_at DESC LIMIT 100");return jdbc.queryForList(sql.toString(),args.toArray());
 }
 void audit(CourseGrant g,String action,JwtAuthenticationToken auth){jdbc.update("INSERT INTO course_grant_audit(id,grant_id,user_id,course_id,action,actor_id) VALUES (?,?,?,?,?,?)",UUID.randomUUID(),g.id,g.userId,g.courseId,action,UUID.fromString(auth.getName()));
  String granted="GRANTED".equals(action)?"COURSE_ACCESS":"COURSE_REVOKED";
  jdbc.update("INSERT INTO learner_notification(id,user_id,category,title_en,title_sq,body_en,body_sq) VALUES (?,?,?,?,?,?,?)",UUID.randomUUID(),g.userId,granted,
    "GRANTED".equals(action)?"Course access granted":"Course access revoked",
    "GRANTED".equals(action)?"Aksesi në kurs u miratua":"Aksesi në kurs u revokua",
    "GRANTED".equals(action)?"You have been granted access to a course.":"Your access to a course was revoked.",
    "GRANTED".equals(action)?"Ju është dhënë akses në një kurs.":"Aksesi juaj në një kurs është revokuar.");
 }
 CourseGrant saveUnique(CourseGrant g){
  try{return repo.saveAndFlush(g);}catch(org.springframework.dao.DataIntegrityViolationException ex){
   throw new org.springframework.web.server.ResponseStatusException(org.springframework.http.HttpStatus.CONFLICT,"Course already granted",ex);
  }
 }

}
