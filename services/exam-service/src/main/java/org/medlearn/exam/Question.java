package org.medlearn.exam;
import jakarta.persistence.*;import java.util.*;
@Entity @Table(name="question") public class Question {
 @Id public UUID id;@Column(name="quiz_id") public UUID quizId;public int position;
 @Column(name="prompt_sq",columnDefinition="text") public String promptSq;@Column(name="prompt_en",columnDefinition="text") public String promptEn;
 @Column(name="choices_sq",columnDefinition="text") public String choicesSq;@Column(name="choices_en",columnDefinition="text") public String choicesEn;
 @Column(name="correct_index") public int correctIndex;@Column(name="explanation_sq",columnDefinition="text") public String explanationSq;@Column(name="explanation_en",columnDefinition="text") public String explanationEn;
}
