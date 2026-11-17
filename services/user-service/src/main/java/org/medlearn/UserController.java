package org.medlearn;
import org.springframework.web.bind.annotation.*;
import org.springframework.http.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.beans.factory.annotation.Value;
import java.nio.file.*;
import java.util.*;
import java.io.*;
@RestController @RequestMapping("/api/users") public class UserController {
 private final ProfileRepo repo;private final KeycloakAdmin kc;
 @Value("${UPLOAD_DIR:/app/uploads}") String uploadDir;
 public UserController(ProfileRepo repo,KeycloakAdmin kc){this.repo=repo;this.kc=kc;}
 public record Registration(String email,String password,String academy,String category,String firstName,String fatherName,String surname,String nationalId,String phone,String city,String workplace,String profession,String university,String degree,String discipline,Integer studyYear,String locale){}
 public record Decision(boolean approve,String reason){}
 @GetMapping("/catalog") public Map<String,Object> catalog(){return Map.of("professions",List.of("Physician","Pharmacist","Nurse","Dentist","Physiotherapist"),"disciplines",List.of("Medicine","Pharmacy","Nursing","Dentistry","Physiotherapy"),"degrees",List.of("Bachelor","Master","Integrated Master","PhD"));}
 @PostMapping(value="/register",consumes=MediaType.MULTIPART_FORM_DATA_VALUE) public ResponseEntity<?> register(@RequestPart("payload") String payload,@RequestPart(value="payment",required=false) MultipartFile payment) throws Exception {
  var mapper=new com.fasterxml.jackson.databind.ObjectMapper();Registration r=mapper.readValue(payload,Registration.class);
  if(r.email()==null||!r.email().matches("^[^@\\s]+@[^@\\s]+\\.[^@\\s]+$")||r.password()==null||r.password().length()<12||r.firstName()==null||r.firstName().isBlank()||r.surname()==null||r.surname().isBlank())return ResponseEntity.badRequest().body(Map.of("error","Invalid required fields"));
  if(!List.of("PROFESSIONAL","OPEN").contains(r.academy())||!List.of("STUDENT","PROFESSIONAL").contains(r.category()))return ResponseEntity.badRequest().body(Map.of("error","Invalid academy/category"));
  if(r.academy().equals("PROFESSIONAL")&&!r.category().equals("PROFESSIONAL"))return ResponseEntity.badRequest().body(Map.of("error","Professional Academy requires professional category"));
  if(r.category().equals("STUDENT") && (r.university()==null||r.degree()==null||r.discipline()==null||r.studyYear()==null||r.studyYear()<1||r.studyYear()>8))return ResponseEntity.badRequest().body(Map.of("error","Student academic fields required"));
  if(r.category().equals("PROFESSIONAL")&&(r.profession()==null||!List.of("Physician","Pharmacist","Nurse","Dentist","Physiotherapist").contains(r.profession())))return ResponseEntity.badRequest().body(Map.of("error","Valid profession required"));
  if(r.academy().equals("PROFESSIONAL") && (payment==null||payment.isEmpty()))return ResponseEntity.badRequest().body(Map.of("error","Payment PDF required"));
  if(repo.existsByEmail(r.email().toLowerCase(Locale.ROOT)))return ResponseEntity.status(409).body(Map.of("error","Email already registered"));
  String file=null;UUID kid=null;
  try {
   if(payment!=null&&!payment.isEmpty()){
    if(payment.getSize()>5*1024*1024)throw new IllegalArgumentException("PDF exceeds 5 MiB");
    byte[] data=payment.getBytes();if(data.length<5||!new String(data,0,5,java.nio.charset.StandardCharsets.US_ASCII).equals("%PDF-"))throw new IllegalArgumentException("Invalid PDF signature");
    Path dir=Paths.get(uploadDir);Files.createDirectories(dir);file=UUID.randomUUID()+".pdf";Files.write(dir.resolve(file),data,StandardOpenOption.CREATE_NEW);
   }
   kid=kc.create(r.email().toLowerCase(Locale.ROOT),r.password(),r.firstName(),r.surname(),r.academy().equals("OPEN"),r.category().equals("STUDENT")?"student":"professional");
   Profile p=new Profile();p.id=UUID.randomUUID();p.keycloakId=kid;p.academy=r.academy();p.category=r.category();p.firstName=r.firstName();p.fatherName=r.fatherName();p.surname=r.surname();p.nationalId=r.nationalId();p.email=r.email().toLowerCase(Locale.ROOT);p.phone=r.phone();p.city=r.city();p.workplace=r.workplace();p.profession=r.profession();p.university=r.university();p.degree=r.degree();p.discipline=r.discipline();p.studyYear=r.studyYear();p.locale=List.of("sq","en").contains(r.locale())?r.locale():"sq";p.status=r.academy().equals("OPEN")?"ACTIVE":"PENDING";p.paymentFile=file;p.paymentOriginalName=payment==null?null:payment.getOriginalFilename();repo.save(p);
   return ResponseEntity.status(201).body(Map.of("id",p.id,"status",p.status));
  }catch(Exception e){if(kid!=null)try{kc.delete(kid);}catch(Exception ignored){}if(file!=null)Files.deleteIfExists(Paths.get(uploadDir,file));throw e;}
 }
 @GetMapping("/me") public Profile me(JwtAuthenticationToken auth){return repo.findByKeycloakId(UUID.fromString(auth.getName())).orElseThrow();}
 @PutMapping("/me") public Profile update(@RequestBody Map<String,String> changes,JwtAuthenticationToken auth){Profile p=me(auth);p.phone=changes.getOrDefault("phone",p.phone);p.city=changes.getOrDefault("city",p.city);p.workplace=changes.getOrDefault("workplace",p.workplace);p.locale=changes.getOrDefault("locale",p.locale);return repo.save(p);}
 @GetMapping("/admin/pending") public List<Profile> pending(){return repo.findByStatus("PENDING");}
 @PostMapping("/admin/{id}/decision") public Profile decision(@PathVariable UUID id,@RequestBody Decision d){Profile p=repo.findById(id).orElseThrow();if(!"PENDING".equals(p.status))throw new IllegalStateException("Not pending");if(d.approve()){kc.enable(p.keycloakId);p.status="ACTIVE";}else{p.status="REJECTED";p.rejectionReason=d.reason();}return repo.save(p);}
 @ExceptionHandler(IllegalArgumentException.class) ResponseEntity<?> bad(IllegalArgumentException ex){return ResponseEntity.badRequest().body(Map.of("error",ex.getMessage()));}
}
