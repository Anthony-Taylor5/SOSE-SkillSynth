package com.skillsynth;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.annotation.Commit;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

//@ExtendWith(MockitoExtension.class)
@SpringBootTest
@Commit
class SkillSynthApplicationTests {

    @Autowired
    private SkillSynthService service;

    @Test
    void testCreateAndRetrieveUser1() {
        Skill skill = service.createSkill("Java", "Java programming");
        AppUser user = service.createUser("alexander", 5, List.of(skill));

        AppUser retrieved = service.getUserByIdWithSkills(user.getId()).orElseThrow(); // ✅ uses fetch-join query
        assertEquals("alexander", retrieved.getUsername());
        assertEquals(5, retrieved.getLevel());
        assertEquals("Java", retrieved.getAllSkills().get(0).getSkillName());
    }

    @Test
    void testCreateAndRetrieveUser2() {
        Skill skill = service.createSkill("PostgreSQL", "Database Mastery");
        AppUser user = service.createUser("chiago", 3, List.of(skill));

        AppUser retrieved = service.getUserByIdWithSkills(user.getId()).orElseThrow(); // ✅ uses fetch-join query
        assertEquals("chiago", retrieved.getUsername());
        assertEquals(3, retrieved.getLevel());
        assertEquals("PostgreSQL", retrieved.getAllSkills().get(0).getSkillName());
    }

    @Test
    void testUpdateChiagoToAlexander() {
        Skill skill = service.createSkill("PostgreSQL", "Database Mastery");
        AppUser user = service.createUser("chiago", 3, List.of(skill));

        // Modify the user object
        user.setUsername("alexander");
        user.setLevel(5);

        // Call update
        AppUser updated = service.updateUser(user);

        // Assertions
        assertEquals("alexander", updated.getUsername());
        assertEquals(5, updated.getLevel());
        assertEquals("PostgreSQL", updated.getAllSkills().get(0).getSkillName());
    }


    @Test
    void testDeleteUser() {
        AppUser user = service.createUser("temp", 1, List.of());
        boolean deleted = service.deleteUser(user.getId());

        assertTrue(deleted);
        assertFalse(service.getUserById(user.getId()).isPresent());
    }
}
