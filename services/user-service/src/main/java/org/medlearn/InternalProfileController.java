package org.medlearn;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.util.*;
@RestController @RequestMapping("/internal/profiles")
public class InternalProfileController {
 private final ProfileRepo profiles;
 public InternalProfileController(ProfileRepo profiles){this.profiles=profiles;}
 public record Recipient(UUID userId,String fullName,String status){}
 @GetMapping("/{keycloakId}/certificate-recipient")
 public Recipient recipient(@PathVariable UUID keycloakId){

  Profile p=profiles.findByKeycloakId(keycloakId).orElseThrow(()->new ResponseStatusException(HttpStatus.NOT_FOUND));
  if(!"ACTIVE".equals(p.status))throw new ResponseStatusException(HttpStatus.CONFLICT,"Recipient is not active");
  return new Recipient(keycloakId,(p.firstName+" "+p.surname).trim(),p.status);
 }
}
