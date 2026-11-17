package org.medlearn.exam;
import java.io.*;import java.util.*;
public class AnswerKey implements Serializable {public UUID attemptId;public UUID questionId;public AnswerKey(){}public boolean equals(Object o){return o instanceof AnswerKey a&&Objects.equals(attemptId,a.attemptId)&&Objects.equals(questionId,a.questionId);}public int hashCode(){return Objects.hash(attemptId,questionId);}}
