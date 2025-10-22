package com.skillsynth;

import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
import java.util.Optional;

public interface SkillSynthSkillRepository extends JpaRepository<Skill, Long> {
    Optional<Skill> findByName(String name);
    List<Skill> findByNameContaining(String keyword);
    Optional<Skill> findByCategory(String category);
}
