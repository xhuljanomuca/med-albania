package org.medlearn;
import org.springframework.web.bind.annotation.*;import org.springframework.http.*;import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;import org.springframework.web.client.RestClient;import org.springframework.beans.factory.annotation.Value;import java.util.*;import java.util.stream.*;
@RestController @RequestMapping("/api/courses") public class CourseController {
 private final CourseRepo courses;private final ChapterRepo chapters;private final ProgressRepo progress;private final RestClient http=RestClient.create();
 @Value("${services.enrollment}") String enrollment;@Value("${services.users}") String users;
 public CourseController(CourseRepo c,ChapterRepo ch,ProgressRepo p){courses=c;chapters=ch;progress=p;}
 public record CourseInput(String slug,String category,String specialty,String academy,String eligibleProfessions,String eligibleDisciplines,String eligibleDegrees,String eligibleYears,UUID instructorId,String titleSq,String titleEn,String descriptionSq,String descriptionEn){}
 public record ChapterInput(int position,String titleSq,String titleEn,String bodySq,String bodyEn){}
 public record ChapterView(UUID id,int position,String title,boolean completed,boolean locked){}
 public record CatalogItem(UUID id,String slug,String category,String specialty,String academy,String title,String description){}
 static String language(String l){return "en".equalsIgnoreCase(l)?"en":"sq";}
 static boolean contains(String csv,Object value){return csv==null||csv.isBlank()||"*".equals(csv)||value!=null&&Arrays.stream(csv.split(",")).map(String::trim).anyMatch(s->s.equalsIgnoreCase(value.toString()));}
 boolean eligible(Course c,JwtAuthenticationToken auth){if(auth==null)return false;
  try{Map<?,?> p=http.get().uri(users+"/api/users/me").header("Authorization","Bearer "+auth.getToken().getTokenValue()).retrieve().body(Map.class);
   if(p==null||!"ACTIVE".equals(p.get("status")))return false;
   if("PROFESSIONAL".equals(c.academy))return "PROFESSIONAL".equals(p.get("category"))&&contains(c.eligibleProfessions,p.get("profession"));
   if("STUDENT".equals(p.get("category")))return contains(c.eligibleDisciplines,p.get("discipline"))&&contains(c.eligibleDegrees,p.get("degree"))&&contains(c.eligibleYears,p.get("studyYear"));
   return contains(c.eligibleProfessions,p.get("profession"));
  }catch(Exception e){return false;}}
 boolean allowed(Course c,JwtAuthenticationToken auth){if(!"PUBLISHED".equals(c.status)||!eligible(c,auth))return false;
  try{Map<?,?> grant=http.get().uri(enrollment+"/api/enrollments/access/"+c.id).header("Authorization","Bearer "+auth.getToken().getTokenValue()).retrieve().body(Map.class);return grant!=null&&Boolean.TRUE.equals(grant.get("allowed"));}catch(Exception e){return false;}}
 @GetMapping public List<CatalogItem> catalog(@RequestHeader(value="Accept-Language",required=false) String lang){String l=language(lang);return courses.findByStatus("PUBLISHED").stream().map(c->new CatalogItem(c.id,c.slug,c.category,c.specialty,c.academy,c.title(l),"en".equals(l)?c.descriptionEn:c.descriptionSq)).toList();}
 @GetMapping("/{id}") public Map<String,Object> detail(@PathVariable UUID id,@RequestHeader(value="Accept-Language",required=false) String lang){Course c=courses.findById(id).orElseThrow();if(!"PUBLISHED".equals(c.status))throw new org.springframework.web.server.ResponseStatusException(HttpStatus.NOT_FOUND);String l=language(lang);return Map.of("id",c.id,"title",c.title(l),"academy",c.academy,"category",c.category,"chapters",chapters.findByCourseIdOrderByPositionAsc(id).stream().map(ch->Map.of("id",ch.id,"title",ch.title(l),"position",ch.position)).toList());}
 @GetMapping("/{id}/eligibility") public Map<String,Object> eligibility(@PathVariable UUID id,JwtAuthenticationToken auth){Course c=courses.findById(id).orElseThrow();return Map.of("eligible","PUBLISHED".equals(c.status)&&eligible(c,auth),"academy",c.academy);}
 @GetMapping("/admin/all") public List<Course> adminAll(){return courses.findAll();}
 @GetMapping("/{id}/learn") public Map<String,Object> learn(@PathVariable UUID id,@RequestHeader(value="Accept-Language",required=false) String lang,JwtAuthenticationToken auth){Course c=courses.findById(id).orElseThrow();if(!allowed(c,auth))throw new org.springframework.web.server.ResponseStatusException(HttpStatus.FORBIDDEN,"Course access not granted");String l=language(lang);List<Chapter> list=chapters.findByCourseIdOrderByPositionAsc(id);UUID user=UUID.fromString(auth.getName());Set<UUID> done=progress.findByUserIdAndChapterIdIn(user,list.stream().map(ch->ch.id).toList()).stream().map(pr->pr.chapterId).collect(Collectors.toSet());List<ChapterView> views=new ArrayList<>();boolean locked=false;for(Chapter ch:list){views.add(new ChapterView(ch.id,ch.position,ch.title(l),done.contains(ch.id),locked));if(!done.contains(ch.id))locked=true;}return Map.of("title",c.title(l),"chapters",views,"percent",list.isEmpty()?0:(int)Math.round(100.0*done.size()/list.size()));}
 @GetMapping("/chapters/{chapterId}") public Map<String,Object> chapter(@PathVariable UUID chapterId,@RequestHeader(value="Accept-Language",required=false) String lang,JwtAuthenticationToken auth){Chapter ch=chapters.findById(chapterId).orElseThrow();Course c=courses.findById(ch.courseId).orElseThrow();if(!allowed(c,auth))throw new org.springframework.web.server.ResponseStatusException(HttpStatus.FORBIDDEN);List<Chapter> ordered=chapters.findByCourseIdOrderByPositionAsc(c.id);UUID user=UUID.fromString(auth.getName());for(Chapter prev:ordered){if(prev.id.equals(ch.id))break;if(!progress.existsByUserIdAndChapterId(user,prev.id))throw new org.springframework.web.server.ResponseStatusException(HttpStatus.FORBIDDEN,"Previous chapter incomplete");}String l=language(lang);return Map.of("id",ch.id,"title",ch.title(l),"body",Objects.toString(ch.body(l),""),"resources",List.of());}
 
 @PostMapping("/internal/chapters/{chapterId}/complete") public ResponseEntity<?> verifiedComplete(@PathVariable UUID chapterId,@RequestBody Map<String,String> body){
  Chapter ch=chapters.findById(chapterId).orElseThrow();UUID user=UUID.fromString(body.get("userId"));
  if(!progress.existsByUserIdAndChapterId(user,chapterId)){Progress p=new Progress();p.id=UUID.randomUUID();p.userId=user;p.chapterId=chapterId;progress.save(p);}return ResponseEntity.noContent().build();
 }
 @GetMapping("/internal/courses/{id}/chapters") public List<Map<String,Object>> internalChapters(@PathVariable UUID id){
  return chapters.findByCourseIdOrderByPositionAsc(id).stream().map(ch->Map.<String,Object>of("id",ch.id,"position",ch.position)).toList();
 }
 @GetMapping("/internal/courses/{id}/completion/{userId}") public Map<String,Object> internalCompletion(@PathVariable UUID id,@PathVariable UUID userId){
  var list=chapters.findByCourseIdOrderByPositionAsc(id);return Map.of("ready",!list.isEmpty()&&list.stream().allMatch(ch->progress.existsByUserIdAndChapterId(userId,ch.id)));
 }
 @PostMapping("/admin") public Course create(@RequestBody CourseInput i){Course c=new Course();c.id=UUID.randomUUID();copy(c,i);c.status="DRAFT";return courses.save(c);}
 @PutMapping("/admin/{id}") public Course edit(@PathVariable UUID id,@RequestBody CourseInput i){Course c=courses.findById(id).orElseThrow();copy(c,i);return courses.save(c);}
 private void copy(Course c,CourseInput i){c.slug=i.slug();c.category=i.category();c.specialty=i.specialty();c.academy=i.academy();c.eligibleProfessions=i.eligibleProfessions();c.eligibleDisciplines=i.eligibleDisciplines();c.eligibleDegrees=i.eligibleDegrees();c.eligibleYears=i.eligibleYears();c.instructorId=i.instructorId();c.titleSq=i.titleSq();c.titleEn=i.titleEn();c.descriptionSq=i.descriptionSq();c.descriptionEn=i.descriptionEn();}
 @PostMapping("/admin/{id}/chapters") public Chapter addChapter(@PathVariable UUID id,@RequestBody ChapterInput i){courses.findById(id).orElseThrow();Chapter ch=new Chapter();ch.id=UUID.randomUUID();ch.courseId=id;ch.position=i.position();ch.titleSq=i.titleSq();ch.titleEn=i.titleEn();ch.bodySq=i.bodySq();ch.bodyEn=i.bodyEn();return chapters.save(ch);}
 @PutMapping("/admin/chapters/{id}") public Chapter editChapter(@PathVariable UUID id,@RequestBody ChapterInput i){Chapter ch=chapters.findById(id).orElseThrow();ch.position=i.position();ch.titleSq=i.titleSq();ch.titleEn=i.titleEn();ch.bodySq=i.bodySq();ch.bodyEn=i.bodyEn();return chapters.save(ch);}
 @PostMapping("/admin/{id}/status/{status}") public Course status(@PathVariable UUID id,@PathVariable String status){if(!List.of("DRAFT","PUBLISHED","ARCHIVED").contains(status))throw new org.springframework.web.server.ResponseStatusException(HttpStatus.BAD_REQUEST);Course c=courses.findById(id).orElseThrow();c.status=status;return courses.save(c);}
}
