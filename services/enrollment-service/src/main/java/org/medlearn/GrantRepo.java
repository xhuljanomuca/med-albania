package org.medlearn;
import org.springframework.data.jpa.repository.JpaRepository;import java.util.*;
public interface GrantRepo extends JpaRepository<CourseGrant,UUID>{List<CourseGrant> findByUserId(UUID id);boolean existsByUserIdAndCourseId(UUID userId,UUID courseId);}
