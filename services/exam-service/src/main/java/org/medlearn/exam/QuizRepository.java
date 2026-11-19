package org.medlearn.exam;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.*;

public interface QuizRepository extends JpaRepository<Quiz, UUID> {
    List<Quiz> findByCourseId(UUID courseId);
}
