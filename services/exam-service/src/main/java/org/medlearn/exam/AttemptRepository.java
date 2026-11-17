package org.medlearn.exam;
import org.springframework.data.jpa.repository.JpaRepository;import java.util.*;
public interface AttemptRepository extends JpaRepository<Attempt,UUID>{List<Attempt> findByUserIdOrderByStartedAtDesc(UUID userId);long countByQuizIdAndUserId(UUID quizId,UUID userId);boolean existsByQuizIdAndUserIdAndPassedTrue(UUID quizId,UUID userId);}
