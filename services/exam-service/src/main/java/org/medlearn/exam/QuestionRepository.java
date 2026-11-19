package org.medlearn.exam;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.*;

public interface QuestionRepository extends JpaRepository<Question, UUID> {
    List<Question> findByQuizIdOrderByPositionAsc(UUID quizId);
}
