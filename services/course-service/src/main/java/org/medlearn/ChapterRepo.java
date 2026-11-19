package org.medlearn;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.*;

public interface ChapterRepo extends JpaRepository<Chapter, UUID> {
    List<Chapter> findByCourseIdOrderByPositionAsc(UUID courseId);
}
