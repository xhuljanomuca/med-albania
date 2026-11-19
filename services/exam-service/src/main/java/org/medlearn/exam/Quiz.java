package org.medlearn.exam;

import jakarta.persistence.*;

import java.util.*;

@Entity
@Table(name = "quiz")
public class Quiz {
    @Id
    public UUID id;
    @Column(name = "course_id")
    public UUID courseId;
    @Column(name = "chapter_id")
    public UUID chapterId;
    @Column(name = "title_sq")
    public String titleSq;
    @Column(name = "title_en")
    public String titleEn;
    public String status = "DRAFT";
    @Column(name = "passing_percent")
    public int passingPercent = 70;
    @Column(name = "time_limit_minutes")
    public int timeLimitMinutes = 30;
    @Column(name = "max_attempts")
    public int maxAttempts = 3;
    public boolean randomize = false;
}
