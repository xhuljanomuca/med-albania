package org.medlearn;

import jakarta.persistence.*;

import java.util.*;

@Entity
@Table(name = "course_grant")
public class CourseGrant {
    @Id
    public UUID id;
    @Column(name = "user_id")
    public UUID userId;
    @Column(name = "course_id")
    public UUID courseId;
    @Column(name = "granted_at", insertable = false, updatable = false)
    public java.time.Instant grantedAt;
}
