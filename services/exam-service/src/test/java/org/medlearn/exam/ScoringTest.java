package org.medlearn.exam;
import org.junit.jupiter.api.Test;import static org.junit.jupiter.api.Assertions.*;
class ScoringTest {@Test void threshold(){assertEquals(70,ExamController.score(7,10));assertTrue(ExamController.score(7,10)>=70);assertFalse(ExamController.score(6,10)>=70);} @Test void zeroQuestions(){assertEquals(0,ExamController.score(0,0));}}
