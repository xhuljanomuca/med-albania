package org.medlearn.exam;

import static org.junit.jupiter.api.Assertions.*;

import java.util.UUID;

import org.junit.jupiter.api.Test;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import jakarta.persistence.LockModeType;

class AttemptRepositoryLockTest {
    @Test
    void submissionLookupUsesDatabasePessimisticLock() throws Exception {
        var method = AttemptRepository.class.getMethod("findByIdForUpdate", UUID.class);
        assertEquals(LockModeType.PESSIMISTIC_WRITE, method.getAnnotation(Lock.class).value());
        assertTrue(method.getAnnotation(Query.class).value().contains("a.id"));
    }
}
