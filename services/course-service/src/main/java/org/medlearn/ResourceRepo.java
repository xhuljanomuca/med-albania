package org.medlearn;import org.springframework.data.jpa.repository.JpaRepository;import java.util.*;
public interface ResourceRepo extends JpaRepository<Resource,UUID>{List<Resource> findByChapterId(UUID chapterId);}
