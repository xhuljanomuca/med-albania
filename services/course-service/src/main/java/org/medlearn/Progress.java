package org.medlearn;import jakarta.persistence.*;import java.util.*;
@Entity @Table(name="chapter_progress") public class Progress {
 @Id public UUID id;@Column(name="user_id") public UUID userId;@Column(name="chapter_id") public UUID chapterId;
 @Column(name="completed_at") public java.time.Instant completedAt;
}
