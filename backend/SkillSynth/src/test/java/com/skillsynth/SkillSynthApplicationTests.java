package com.skillsynth;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.annotation.Commit;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@Commit
class SkillSynthApplicationTests {

    @Autowired
    private SkillSynthService service;

    @Test
    void testCreateAndRetrieveUser1() {
        Skill skill = service.createSkill("Java", "Java programming");
        AppUser user = service.createUser("alexander", 5, List.of(skill));

        AppUser retrieved = service.getUserByIdWithSkills(user.getId()).orElseThrow();
        assertEquals("alexander", retrieved.getUsername());
        assertEquals(5, retrieved.getLevel());
        assertEquals("Java", retrieved.getAllSkills().get(0).getName());
    }

    @Test
    void testCreateAndRetrieveUser2() {
        Skill skill = service.createSkill("PostgreSQL", "Database Mastery");
        AppUser user = service.createUser("chiago", 3, List.of(skill));

        AppUser retrieved = service.getUserByIdWithSkills(user.getId()).orElseThrow();
        assertEquals("chiago", retrieved.getUsername());
        assertEquals(3, retrieved.getLevel());
        assertEquals("PostgreSQL", retrieved.getAllSkills().get(0).getName());
    }

    @Test
    void testUpdateChiagoToAlexander() {
        Skill skill = service.createSkill("PostgreSQL", "Database Mastery");
        AppUser user = service.createUser("chiago", 3, List.of(skill));

        user.setUsername("alexander");
        user.setLevel(5);

        AppUser updated = service.updateUser(user);

        assertEquals("alexander", updated.getUsername());
        assertEquals(5, updated.getLevel());
        assertEquals("PostgreSQL", updated.getAllSkills().get(0).getName());
    }

    @Test
    void testDeleteUser() {
        AppUser user = service.createUser("temp", 1, List.of());
        boolean deleted = service.deleteUser(user.getId());

        assertTrue(deleted);
        assertFalse(service.getUserById(user.getId()).isPresent());
    }
}
