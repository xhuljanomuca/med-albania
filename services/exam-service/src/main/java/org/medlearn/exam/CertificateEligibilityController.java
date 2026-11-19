package org.medlearn.exam;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.util.*;

@RestController
@RequestMapping("/internal/exams")
public class CertificateEligibilityController {
    private final QuizRepository quizzes;
    private final AttemptRepository attempts;

    public CertificateEligibilityController(QuizRepository q, AttemptRepository a) {
        quizzes = q;
        attempts = a;
    }

    @GetMapping("/attempts/{id}/certificate-eligibility")
    public Eligibility eligibility(@PathVariable UUID id) {
        Attempt a = attempts.findById(id).orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND));
        Quiz q = quizzes.findById(a.quizId).orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND));
        if (q.chapterId != null || a.submittedAt == null || !Boolean.TRUE.equals(a.passed) || a.scorePercent == null || a.scorePercent < q.passingPercent)
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Final examination not passed");
        return new Eligibility(a.id, a.userId, q.courseId, a.scorePercent, q.passingPercent, true);
    }

    public record Eligibility(UUID attemptId, UUID userId, UUID courseId, int scorePercent, int passingPercent,
                              boolean passed) {
    }
}
