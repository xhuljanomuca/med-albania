package org.medlearn;import org.springframework.data.jpa.repository.JpaRepository;import java.util.*;
public interface CourseRepo extends JpaRepository<Course,UUID>{List<Course> findByStatus(String status);Optional<Course> findBySlug(String slug);}
