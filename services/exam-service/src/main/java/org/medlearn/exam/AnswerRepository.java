package org.medlearn.exam;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.*;

public interface AnswerRepository extends JpaRepository<Answer, AnswerKey> {
}
