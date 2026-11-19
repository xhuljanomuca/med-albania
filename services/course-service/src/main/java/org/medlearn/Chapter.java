package org.medlearn;

import jakarta.persistence.*;

import java.util.*;

@Entity
@Table(name = "chapter")
public class Chapter {
    @Id
    public UUID id;
    @Column(name = "course_id")
    public UUID courseId;
    @Column(name = "position")
    public int position;
    @Column(name = "title_sq")
    public String titleSq;
    @Column(name = "title_en")
    public String titleEn;
    @Column(name = "body_sq", columnDefinition = "text")
    public String bodySq;
    @Column(name = "body_en", columnDefinition = "text")
    public String bodyEn;

    public String title(String locale) {
        return "en".equals(locale) ? titleEn : titleSq;
    }

    public String body(String locale) {
        return "en".equals(locale) ? bodyEn : bodySq;
    }
}
