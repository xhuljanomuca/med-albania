package org.medlearn.cert;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.*;

public interface CertificateRepository extends JpaRepository<Certificate, UUID> {
    Optional<Certificate> findByVerificationCode(String code);

    Optional<Certificate> findByExamAttemptId(UUID id);

    List<Certificate> findByUserIdOrderByIssuedAtDesc(UUID userId);
}
