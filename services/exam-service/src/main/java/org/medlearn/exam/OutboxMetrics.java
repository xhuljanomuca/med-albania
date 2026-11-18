package org.medlearn.exam;
import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.MeterRegistry;
import org.springframework.stereotype.Component;
/** Database-backed gauges and process-local counters for operational visibility. */
@Component
public class OutboxMetrics {
 private final Counter delivered, failed, stale;
 public OutboxMetrics(MeterRegistry registry, ProgressOutboxDelivery delivery) {
  registry.gauge("medlearn.outbox.pending", delivery, d -> d.pendingCount());
  registry.gauge("medlearn.outbox.dead_letter", delivery, d -> d.deadLetterCount());
  delivered=registry.counter("medlearn.outbox.delivery", "result", "success");
  failed=registry.counter("medlearn.outbox.delivery", "result", "failure");
  stale=registry.counter("medlearn.outbox.delivery", "result", "stale_claim");
 }
 public void success(int updated){if(updated>0) delivered.increment(); else stale.increment();}
 public void failure(int updated){if(updated>0) failed.increment(); else stale.increment();}
}
