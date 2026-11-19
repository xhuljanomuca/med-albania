package org.medlearn.exam;

import jakarta.persistence.*;

import java.util.*;
import java.time.*;

@Entity
@Table(name = "attempt")
public class Attempt {
    @Id
    public UUID id;
    @Column(name = "quiz_id")
    public UUID quizId;
    @Column(name = "user_id")
    public UUID userId;
    @Column(name = "attempt_number")
    public int attemptNumber;
    @Column(name = "started_at")
    public Instant startedAt;
    @Column(name = "submitted_at")
    public Instant submittedAt;
    @Column(name = "score_percent")
    public Integer scorePercent;
    public Boolean passed;
    @Column(name = "question_order", columnDefinition = "text")
    public String questionOrder;
}
