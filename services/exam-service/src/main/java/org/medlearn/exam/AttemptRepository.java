package org.medlearn.exam;
import org.springframework.data.jpa.repository.*;
import org.springframework.data.repository.query.Param;
import jakarta.persistence.LockModeType;
import java.util.*;
public interface AttemptRepository extends JpaRepository<Attempt,UUID> {
 List<Attempt> findByUserIdOrderByStartedAtDesc(UUID userId);
 long countByQuizIdAndUserId(UUID quizId,UUID userId);
 boolean existsByQuizIdAndUserIdAndPassedTrue(UUID quizId,UUID userId);
 /** Serializes concurrent submissions of the same attempt to prevent duplicate answers/outbox events. */
 @Lock(LockModeType.PESSIMISTIC_WRITE)
 @Query("select a from Attempt a where a.id = :id")
 Optional<Attempt> findByIdForUpdate(@Param("id") UUID id);
}
