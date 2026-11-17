package org.medlearn;
import org.springframework.web.bind.annotation.*;import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;import java.util.*;
@RestController @RequestMapping("/api/enrollments") public class EnrollmentController {
 @org.springframework.beans.factory.annotation.Value("${COURSE_SERVICE:http://course-service:8084}") String courseService;private final org.springframework.web.client.RestClient http=org.springframework.web.client.RestClient.create();private final GrantRepo repo;public EnrollmentController(GrantRepo repo){this.repo=repo;}
 public record GrantRequest(UUID userId,UUID courseId){}
 @GetMapping("/me") public List<CourseGrant> me(JwtAuthenticationToken auth){return repo.findByUserId(UUID.fromString(auth.getName()));}
 @GetMapping("/access/{courseId}") public Map<String,Boolean> access(@PathVariable UUID courseId,JwtAuthenticationToken auth){return Map.of("allowed",repo.existsByUserIdAndCourseId(UUID.fromString(auth.getName()),courseId));}
 @PostMapping("/open/{courseId}") public CourseGrant enrollOpen(@PathVariable UUID courseId,JwtAuthenticationToken auth){
  Map<?,?> check=http.get().uri(courseService+"/api/courses/"+courseId+"/eligibility").header("Authorization","Bearer "+auth.getToken().getTokenValue()).retrieve().body(Map.class);
  if(check==null||!Boolean.TRUE.equals(check.get("eligible"))||!"OPEN".equals(check.get("academy")))throw new org.springframework.web.server.ResponseStatusException(org.springframework.http.HttpStatus.FORBIDDEN,"Not eligible for open enrollment");
  UUID user=UUID.fromString(auth.getName());if(repo.existsByUserIdAndCourseId(user,courseId))throw new org.springframework.web.server.ResponseStatusException(org.springframework.http.HttpStatus.CONFLICT,"Already enrolled");
  CourseGrant g=new CourseGrant();g.id=UUID.randomUUID();g.userId=user;g.courseId=courseId;return repo.save(g);
 }
 @PostMapping("/admin/grants") public CourseGrant grant(@RequestBody GrantRequest r){if(repo.existsByUserIdAndCourseId(r.userId(),r.courseId()))throw new IllegalStateException("Already granted");CourseGrant g=new CourseGrant();g.id=UUID.randomUUID();g.userId=r.userId();g.courseId=r.courseId();return repo.save(g);}
}
