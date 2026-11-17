package org.medlearn.cert;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.http.MediaType;
import org.springframework.web.client.RestClient;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.http.HttpStatus;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.Map;
@Component
public class ServiceTokenProvider {
 private final RestClient client=RestClient.create();
 @Value("${medlearn.oauth.token-url}") String tokenUrl;
 @Value("${medlearn.oauth.client-id}") String clientId;
 @Value("${medlearn.oauth.client-secret}") String clientSecret;
 public String token(){
  if(clientSecret==null||clientSecret.isBlank())throw new ResponseStatusException(HttpStatus.SERVICE_UNAVAILABLE,"Service credentials not configured");
  String body="grant_type=client_credentials&client_id="+enc(clientId)+"&client_secret="+enc(clientSecret);
  Map<?,?> response=client.post().uri(tokenUrl).contentType(MediaType.APPLICATION_FORM_URLENCODED).body(body).retrieve().body(Map.class);
  if(response==null||!(response.get("access_token") instanceof String token)||token.isBlank())throw new ResponseStatusException(HttpStatus.BAD_GATEWAY,"Service token unavailable");
  return token;
 }
 private static String enc(String s){return URLEncoder.encode(s,StandardCharsets.UTF_8);}
}
