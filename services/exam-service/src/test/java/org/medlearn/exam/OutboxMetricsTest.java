package org.medlearn.exam;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import io.micrometer.core.instrument.simple.SimpleMeterRegistry;
import org.junit.jupiter.api.Test;

class OutboxMetricsTest {
    @Test
    void recordsDeliveryRetryAndStaleClaim() {
        var registry = new SimpleMeterRegistry();
        var delivery = mock(ProgressOutboxDelivery.class);
        var metrics = new OutboxMetrics(registry, delivery);
        metrics.success(1);
        metrics.failure(1);
        metrics.success(0);
        assertEquals(1.0, registry.get("medlearn.outbox.delivery").tag("result", "success").counter().count());
        assertEquals(1.0, registry.get("medlearn.outbox.delivery").tag("result", "failure").counter().count());
        assertEquals(1.0, registry.get("medlearn.outbox.delivery").tag("result", "stale_claim").counter().count());
    }
}
