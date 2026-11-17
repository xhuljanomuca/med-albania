package org.medlearn;import org.springframework.data.jpa.repository.JpaRepository;import java.util.*;
public interface ProgressRepo extends JpaRepository<Progress,UUID>{List<Progress> findByUserIdAndChapterIdIn(UUID userId,Collection<UUID> chapterIds);boolean existsByUserIdAndChapterId(UUID userId,UUID chapterId);}
