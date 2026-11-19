package org.medlearn.cert;

import jakarta.persistence.*;

import java.util.*;
import java.time.*;

@Entity
@Table(name = "achievement")
public class Achievement {
    @Id
    public UUID id;
    @Column(name = "user_id")
    public UUID userId;
    @Column(name = "course_id")
    public UUID courseId;
    @Column(name = "certificate_id")
    public UUID certificateId;
    @Column(name = "label_sq")
    public String labelSq;
    @Column(name = "label_en")
    public String labelEn;
    @Column(name = "awarded_at")
    public Instant awardedAt;
}
