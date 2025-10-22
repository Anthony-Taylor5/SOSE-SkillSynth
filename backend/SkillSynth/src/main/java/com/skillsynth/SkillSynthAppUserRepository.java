package com.skillsynth;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import java.util.Optional;
import java.util.List;

public interface SkillSynthAppUserRepository extends JpaRepository<AppUser, Long> {

    Optional<AppUser> findByUsername(String username);

    List<AppUser> findByLevelGreaterThan(int level);

    @Query("SELECT u FROM AppUser u LEFT JOIN FETCH u.allSkills WHERE u.id = :id")
    Optional<AppUser> findByIdWithSkills(@Param("id") Long id);
}
