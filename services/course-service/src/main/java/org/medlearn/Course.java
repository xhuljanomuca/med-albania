package org.medlearn;

import jakarta.persistence.*;

import java.util.*;

@Entity
@Table(name = "course")
public class Course {
    @Id
    public UUID id;
    public String slug, category, specialty, status, academy;
    @Column(name = "eligible_professions")
    public String eligibleProfessions;
    @Column(name = "eligible_disciplines")
    public String eligibleDisciplines;
    @Column(name = "eligible_degrees")
    public String eligibleDegrees;
    @Column(name = "eligible_years")
    public String eligibleYears;
    @Column(name = "instructor_id")
    public UUID instructorId;
    @Column(name = "title_sq")
    public String titleSq;
    @Column(name = "title_en")
    public String titleEn;
    @Column(name = "description_sq", columnDefinition = "text")
    public String descriptionSq;
    @Column(name = "description_en", columnDefinition = "text")
    public String descriptionEn;

    public String title(String locale) {
        return "en".equals(locale) ? titleEn : titleSq;
    }
}
