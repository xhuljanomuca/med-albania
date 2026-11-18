package org.medlearn.exam;

import java.time.Duration;
import java.util.Map;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

/** HTTP calls run outside database claim transactions. */
@Component
public class ProgressOutboxWorker {
 private final ProgressOutboxDelivery delivery;
 private final ServiceTokenProvider tokens;
 private final OutboxMetrics metrics;
 private final RestClient http;
 @Value("${services.courses}") String courses;
 public ProgressOutboxWorker(ProgressOutboxDelivery delivery, ServiceTokenProvider tokens, OutboxMetrics metrics,
     @Value("${medlearn.outbox.connect-timeout-ms:2000}") int connectTimeout,
     @Value("${medlearn.outbox.read-timeout-ms:5000}") int readTimeout) {
  this.delivery=delivery; this.tokens=tokens; this.metrics=metrics;
  var factory=new SimpleClientHttpRequestFactory();
  factory.setConnectTimeout(Duration.ofMillis(connectTimeout));
  factory.setReadTimeout(Duration.ofMillis(readTimeout));
  this.http=RestClient.builder().requestFactory(factory).build();
 }
 @Scheduled(fixedDelayString="${medlearn.outbox.poll-ms:5000}")
 public void deliver() {
  for (var item:delivery.claim(25)) {
   try {
    http.post().uri(courses+"/api/courses/internal/chapters/"+item.chapterId()+"/complete")
      .headers(h->h.setBearerAuth(tokens.token()))
      .header("Idempotency-Key",item.attemptId().toString())
      .body(Map.of("userId",item.userId().toString())).retrieve().toBodilessEntity();
    metrics.success(delivery.succeeded(item.id(),item.claimToken()));
   } catch (Exception ex) {
    metrics.failure(delivery.failed(item.id(),item.claimToken(),ex.getClass().getSimpleName()));
   }
  }
 }
}
