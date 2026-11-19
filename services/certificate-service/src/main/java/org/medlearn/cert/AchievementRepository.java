package org.medlearn.cert;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.*;

public interface AchievementRepository extends JpaRepository<Achievement, UUID> {
    List<Achievement> findByUserIdOrderByAwardedAtDesc(UUID userId);
}
