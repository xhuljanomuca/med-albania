package org.medlearn;
import org.springframework.web.bind.annotation.*;import org.springframework.web.multipart.MultipartFile;import org.springframework.http.*;import org.springframework.beans.factory.annotation.Value;import org.springframework.core.io.*;import java.util.*;import java.nio.file.*;import java.io.*;
@RestController @RequestMapping("/api/courses") public class MediaController {
 private final ResourceRepo resources;private final ChapterRepo chapters;private final CourseRepo courses;private final CourseController access;
 @Value("${MEDIA_DIR:/app/media}") String mediaDir;
 public MediaController(ResourceRepo r,ChapterRepo ch,CourseRepo c,CourseController access){resources=r;chapters=ch;courses=c;this.access=access;}
 @PostMapping(value="/admin/chapters/{chapterId}/resources",consumes=MediaType.MULTIPART_FORM_DATA_VALUE)
 public Resource upload(@PathVariable UUID chapterId,@RequestPart("file") MultipartFile file,@RequestParam(defaultValue="false") boolean downloadable)throws IOException{
  chapters.findById(chapterId).orElseThrow();if(file.isEmpty()||file.getSize()>30*1024*1024)throw new org.springframework.web.server.ResponseStatusException(HttpStatus.BAD_REQUEST,"File must be 1..30 MiB");
  String filename=Objects.toString(file.getOriginalFilename(),"resource");String lower=filename.toLowerCase(Locale.ROOT);
  byte[] bytes=file.getBytes();String mime;
  if(lower.endsWith(".pdf")&&bytes.length>=5&&new String(bytes,0,5,java.nio.charset.StandardCharsets.US_ASCII).equals("%PDF-"))mime="application/pdf";
  else if(lower.endsWith(".mp4")&&bytes.length>=12&&new String(bytes,4,4,java.nio.charset.StandardCharsets.US_ASCII).equals("ftyp"))mime="video/mp4";
  else if(lower.endsWith(".png")&&bytes.length>=8&&bytes[0]==(byte)137&&bytes[1]==80&&bytes[2]==78&&bytes[3]==71)mime="image/png";
  else throw new org.springframework.web.server.ResponseStatusException(HttpStatus.BAD_REQUEST,"Unsupported file format");
  Path dir=Path.of(mediaDir).toAbsolutePath().normalize();Files.createDirectories(dir);String key=UUID.randomUUID().toString();Files.write(dir.resolve(key),bytes,StandardOpenOption.CREATE_NEW);
  Resource r=new Resource();r.id=UUID.randomUUID();r.chapterId=chapterId;r.filename=filename.replaceAll("[\\r\\n]","_");r.mediaType=mime;r.storageKey=key;r.byteSize=bytes.length;r.downloadAllowed=downloadable;return resources.save(r);
 }
 @GetMapping("/resources/{id}") public ResponseEntity<org.springframework.core.io.Resource> download(@PathVariable UUID id,org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken auth)throws IOException{
  Resource r=resources.findById(id).orElseThrow();Chapter ch=chapters.findById(r.chapterId).orElseThrow();Course c=courses.findById(ch.courseId).orElseThrow();
  if(!access.allowed(c,auth))throw new org.springframework.web.server.ResponseStatusException(HttpStatus.FORBIDDEN);
  Path path=Path.of(mediaDir).toAbsolutePath().normalize().resolve(r.storageKey);if(!Files.isRegularFile(path))throw new org.springframework.web.server.ResponseStatusException(HttpStatus.NOT_FOUND);
  return ResponseEntity.ok().contentType(MediaType.parseMediaType(r.mediaType)).header(HttpHeaders.CONTENT_DISPOSITION,(r.downloadAllowed?"attachment":"inline")+"; filename=resource").body(new FileSystemResource(path));
 }
}
