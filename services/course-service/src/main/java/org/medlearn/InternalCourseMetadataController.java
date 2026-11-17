package org.medlearn;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.util.*;
@RestController @RequestMapping("/internal/courses")
public class InternalCourseMetadataController {
 private final CourseRepo courses;
 public InternalCourseMetadataController(CourseRepo courses){this.courses=courses;}
 public record Metadata(UUID courseId,String titleSq,String titleEn,String status){}
 @GetMapping("/{id}/certificate-metadata")
 public Metadata metadata(@PathVariable UUID id){
  Course c=courses.findById(id).orElseThrow(()->new ResponseStatusException(HttpStatus.NOT_FOUND));
  if(!"PUBLISHED".equals(c.status))throw new ResponseStatusException(HttpStatus.CONFLICT,"Course not published");
  return new Metadata(id,c.titleSq,c.titleEn,c.status);
 }
}
