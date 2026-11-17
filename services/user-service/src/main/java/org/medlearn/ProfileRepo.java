package org.medlearn;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.*;
public interface ProfileRepo extends JpaRepository<Profile,UUID>{Optional<Profile> findByKeycloakId(UUID id); List<Profile> findByStatus(String status); boolean existsByEmail(String email);}
