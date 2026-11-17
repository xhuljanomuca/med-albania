package org.medlearn.exam;
import jakarta.persistence.*;import java.util.*;
@Entity @Table(name="answer") @IdClass(AnswerKey.class) public class Answer {
 @Id @Column(name="attempt_id") public UUID attemptId;@Id @Column(name="question_id") public UUID questionId;
 @Column(name="selected_index") public int selectedIndex;public boolean correct;
}
