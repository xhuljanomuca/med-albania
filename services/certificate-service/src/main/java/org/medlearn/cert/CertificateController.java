package org.medlearn.cert;

import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.http.*;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.RestClient;

import java.time.*;
import java.util.*;
import java.math.*;

import jakarta.persistence.EntityManager;

@RestController
@RequestMapping("/api/certificates")
public class CertificateController {
    private final EntityManager entityManager;
    private final ServiceTokenProvider serviceTokens;
    private final CertificateRepository certificates;
    private final AchievementRepository achievements;
    private final RestClient http = RestClient.create();
    private final CertificatePdf pdf;
    @Value("${medlearn.exam-url}")
    String examUrl;
    @Value("${medlearn.user-url}")
    String userUrl;
    @Value("${medlearn.course-url}")
    String courseUrl;

    public CertificateController(CertificateRepository c, AchievementRepository a, CertificatePdf pdf, ServiceTokenProvider serviceTokens, EntityManager entityManager) {
        this.entityManager = entityManager;
        this.serviceTokens = serviceTokens;
        this.certificates = c;
        this.achievements = a;
        this.pdf = pdf;
    }

    private static ResponseStatusException invalid(String msg) {
        return new ResponseStatusException(HttpStatus.BAD_REQUEST, msg);
    }

    private static UUID user(JwtAuthenticationToken auth) {
        return UUID.fromString(auth.getName());
    }

    private Certificate get(UUID id) {
        return certificates.findById(id).orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND));
    }

    @PostMapping("/issue")
    @Transactional
    public Certificate issue(@RequestBody IssueRequest req, JwtAuthenticationToken auth) {
        if (req == null || req.attemptId() == null) throw invalid("Missing examination attempt");
        // Serialize requests for this examination attempt until transaction commit.
        // Unique database constraint remains the final duplicate-issuance defense.
        entityManager.createNativeQuery("SELECT pg_advisory_xact_lock(hashtextextended(CAST(:attempt AS text), 0))")
                .setParameter("attempt", req.attemptId().toString()).getSingleResult();
        var existing = certificates.findByExamAttemptId(req.attemptId());
        if (existing.isPresent()) {
            if (!existing.get().userId.equals(user(auth))) throw new ResponseStatusException(HttpStatus.FORBIDDEN);
            return existing.get();
        }
        Map<?, ?> eligibility;
        try {
            eligibility = http.get().uri(examUrl + "/internal/exams/attempts/" + req.attemptId() + "/certificate-eligibility").headers(headers -> headers.setBearerAuth(serviceTokens.token())).retrieve().body(Map.class);
        } catch (Exception ex) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Unable to verify final examination eligibility");
        }
        if (eligibility == null || !Boolean.TRUE.equals(eligibility.get("passed")) || !user(auth).toString().equals(eligibility.get("userId")))
            throw new ResponseStatusException(HttpStatus.FORBIDDEN);
        int score = ((Number) eligibility.get("scorePercent")).intValue(), threshold = ((Number) eligibility.get("passingPercent")).intValue();
        if (!CertificatePolicy.eligible(true, true, true, score, threshold))
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Passing threshold not reached");
        Map<?, ?> recipient;
        Map<?, ?> course;
        try {
            recipient = http.get().uri(userUrl + "/internal/profiles/" + user(auth) + "/certificate-recipient").headers(headers -> headers.setBearerAuth(serviceTokens.token())).retrieve().body(Map.class);
            course = http.get().uri(courseUrl + "/internal/courses/" + eligibility.get("courseId") + "/certificate-metadata").headers(headers -> headers.setBearerAuth(serviceTokens.token())).retrieve().body(Map.class);
        } catch (Exception ex) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Unable to verify recipient and course metadata");
        }
        if (recipient == null || course == null || !"ACTIVE".equals(recipient.get("status")) || !"PUBLISHED".equals(course.get("status")) || !user(auth).toString().equals(recipient.get("userId")) || !eligibility.get("courseId").toString().equals(course.get("courseId")))
            throw new ResponseStatusException(HttpStatus.FORBIDDEN);
        String trustedName = Objects.toString(recipient.get("fullName"), "").trim();
        String trustedSq = Objects.toString(course.get("titleSq"), "").trim();
        String trustedEn = Objects.toString(course.get("titleEn"), "").trim();
        if (trustedName.isBlank() || trustedSq.isBlank() || trustedEn.isBlank())
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Missing verified metadata");
        Certificate c = new Certificate();
        c.id = UUID.randomUUID();
        c.userId = user(auth);
        c.courseId = UUID.fromString(eligibility.get("courseId").toString());
        c.examAttemptId = req.attemptId();
        c.verificationCode = CertificatePolicy.code();
        c.recipientName = trustedName;
        c.courseTitleSq = trustedSq;
        c.courseTitleEn = trustedEn;
        c.scorePercent = score;
        c.issuedAt = Instant.now();
        certificates.save(c);
        Achievement a = new Achievement();
        a.id = UUID.randomUUID();
        a.userId = c.userId;
        a.courseId = c.courseId;
        a.certificateId = c.id;
        a.labelSq = "Kurs i përfunduar";
        a.labelEn = "Course completed";
        a.awardedAt = c.issuedAt;
        achievements.save(a);
        return c;
    }

    @GetMapping("/me")
    public List<Certificate> mine(JwtAuthenticationToken auth) {
        return certificates.findByUserIdOrderByIssuedAtDesc(user(auth));
    }

    @GetMapping("/me/achievements")
    public List<Achievement> myAchievements(JwtAuthenticationToken auth) {
        return achievements.findByUserIdOrderByAwardedAtDesc(user(auth));
    }

    @GetMapping("/verify/{code}")
    public Verification verify(@PathVariable String code) {
        Certificate c = certificates.findByVerificationCode(code).orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Certificate not found"));
        return new Verification(c.effectiveStatus(), c.verificationCode, c.recipientName, c.courseTitleSq, c.courseTitleEn, c.scorePercent, c.issuedAt, c.expiresAt, c.accredited ? c.creditHours : null, c.accredited ? c.accreditingBody : null);
    }

    @GetMapping(value = "/{id}/pdf", produces = "application/pdf")
    public ResponseEntity<byte[]> download(@PathVariable UUID id, JwtAuthenticationToken auth) {
        Certificate c = get(id);
        if (!c.userId.equals(user(auth))) throw new ResponseStatusException(HttpStatus.FORBIDDEN);
        return ResponseEntity.ok().header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=medlearn-certificate.pdf").body(pdf.render(c));
    }

    @GetMapping("/admin")
    public List<Certificate> all() {
        return certificates.findAll();
    }

    @PostMapping("/admin/{id}/revoke")
    public Certificate revoke(@PathVariable UUID id) {
        Certificate c = get(id);
        c.status = "REVOKED";
        c.revokedAt = Instant.now();
        return certificates.save(c);
    }

    @PostMapping("/admin/{id}/accreditation")
    public ResponseEntity<?> accreditation(@PathVariable UUID id, @RequestBody Accreditation a) {
        throw new ResponseStatusException(HttpStatus.NOT_IMPLEMENTED, "Accreditation requires verified authoritative evidence and approval workflow");
    }

    public record IssueRequest(UUID attemptId) {
    }

    public record Verification(String status, String verificationCode, String recipientName, String courseTitleSq,
                               String courseTitleEn, int scorePercent, Instant issuedAt, Instant expiresAt,
                               BigDecimal creditHours, String accreditingBody) {
    }

    public record Accreditation(boolean accredited, BigDecimal creditHours, String accreditingBody) {
    }
}
