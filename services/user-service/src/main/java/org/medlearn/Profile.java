package org.medlearn;

import jakarta.persistence.*;

import java.util.*;

@Entity
@Table(name = "member_profile")
public class Profile {
    @Id
    public UUID id;
    @Column(name = "keycloak_id")
    public UUID keycloakId;
    public String academy, category;
    @Column(name = "first_name")
    public String firstName;
    @Column(name = "father_name")
    public String fatherName;
    public String surname;
    @Column(name = "national_id")
    public String nationalId;
    public String email, phone, city, workplace, profession, university, degree, discipline;
    @Column(name = "study_year")
    public Integer studyYear;
    public String locale, status;
    @Column(name = "payment_file")
    public String paymentFile;
    @Column(name = "payment_original_name")
    public String paymentOriginalName;
    @Column(name = "rejection_reason")
    public String rejectionReason;
    @Column(name = "created_at", insertable = false, updatable = false)
    public java.time.Instant createdAt;
}
