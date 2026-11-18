package org.medlearn.exam;
import org.springframework.data.jpa.repository.*;import org.springframework.data.repository.query.Param;
import jakarta.persistence.LockModeType;import java.util.*;
public interface ProgressOutboxRepository extends JpaRepository<ProgressOutbox,UUID> {
 @Lock(LockModeType.PESSIMISTIC_WRITE)
 @Query("select p from ProgressOutbox p where p.deliveredAt is null order by p.createdAt")
 List<ProgressOutbox> pending();
}
