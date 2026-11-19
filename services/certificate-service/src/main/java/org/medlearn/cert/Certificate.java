package org.medlearn.cert;

import jakarta.persistence.*;

import java.util.*;
import java.time.*;
import java.math.*;

@Entity
@Table(name = "certificate")
public class Certificate {
    @Id
    public UUID id;
    @Column(name = "user_id", nullable = false)
    public UUID userId;
    @Column(name = "course_id", nullable = false)
    public UUID courseId;
    @Column(name = "exam_attempt_id", nullable = false, unique = true)
    public UUID examAttemptId;
    @Column(name = "verification_code", nullable = false, unique = true)
    public String verificationCode;
    @Column(name = "recipient_name", nullable = false)
    public String recipientName;
    @Column(name = "course_title_sq", nullable = false)
    public String courseTitleSq;
    @Column(name = "course_title_en", nullable = false)
    public String courseTitleEn;
    @Column(name = "score_percent", nullable = false)
    public int scorePercent;
    @Column(name = "issued_at", nullable = false)
    public Instant issuedAt;
    @Column(name = "expires_at")
    public Instant expiresAt;
    @Column(name = "revoked_at")
    public Instant revokedAt;
    @Column(nullable = false)
    public String status = "ISSUED";
    @Column(nullable = false)
    public boolean accredited = false;
    @Column(name = "credit_hours")
    public BigDecimal creditHours;
    @Column(name = "accrediting_body")
    public String accreditingBody;

    public String effectiveStatus() {
        return "REVOKED".equals(status) ? "REVOKED" : expiresAt != null && expiresAt.isBefore(Instant.now()) ? "EXPIRED" : "VALID";
    }
}
