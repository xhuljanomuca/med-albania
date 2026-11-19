package org.medlearn.exam;

import jakarta.persistence.*;

import java.util.*;
import java.time.*;

@Entity
@Table(name = "progress_outbox")
public class ProgressOutbox {
    @Id
    public UUID id;
    @Column(name = "attempt_id", nullable = false, unique = true)
    public UUID attemptId;
    @Column(name = "chapter_id", nullable = false)
    public UUID chapterId;
    @Column(name = "user_id", nullable = false)
    public UUID userId;
    @Column(name = "created_at", nullable = false)
    public Instant createdAt;
    @Column(name = "delivered_at")
    public Instant deliveredAt;
    @Column(name = "retry_count", nullable = false)
    public int retryCount;
}
