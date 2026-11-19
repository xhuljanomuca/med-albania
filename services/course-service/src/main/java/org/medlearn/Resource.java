package org.medlearn;

import jakarta.persistence.*;

import java.util.*;

@Entity
@Table(name = "chapter_resource")
public class Resource {
    @Id
    public UUID id;
    @Column(name = "chapter_id")
    public UUID chapterId;
    public String filename, mediaType;
    @Column(name = "storage_key")
    public String storageKey;
    @Column(name = "byte_size")
    public long byteSize;
    @Column(name = "download_allowed")
    public boolean downloadAllowed;
}
