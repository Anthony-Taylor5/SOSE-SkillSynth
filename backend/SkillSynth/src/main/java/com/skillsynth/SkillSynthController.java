package com.skillsynth;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.List;
import java.util.Optional;

@CrossOrigin(origins = "http://localhost:5173/")
@RestController
@RequestMapping("/api")

public class SkillSynthController {

    @Autowired
    private SkillSynthService skillSynthService;

    // -------------------- USER ENDPOINTS --------------------

    @GetMapping("/users")
    public List<AppUser> getAllUsers() {
        return skillSynthService.getAllUsers();
    }

    @GetMapping("/users/{id}")
    public ResponseEntity<AppUser> getUserById(@PathVariable Long id) {
        return skillSynthService.getUserById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @GetMapping("/users/{id}/with-skills")
    public ResponseEntity<AppUser> getUserByIdWithSkills(@PathVariable Long id) {
        return skillSynthService.getUserByIdWithSkills(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @GetMapping("/users/username/{username}")
    public ResponseEntity<AppUser> getUserByUsername(@PathVariable String username) {
        return skillSynthService.getUserByUsername(username)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @GetMapping("/users/level/greater-than/{level}")
    public List<AppUser> getUsersWithLevelGreaterThan(@PathVariable int level) {
        return skillSynthService.getUsersWithLevelGreaterThan(level);
    }

    @GetMapping("/users/level/less-than/{level}")
    public List<AppUser> getUsersWithLevelLessThan(@PathVariable int level) {
        return skillSynthService.getUsersWithLevelLessThan(level);
    }

    @GetMapping("/users/level/equal-to/{level}")
    public List<AppUser> getUsersWithLevelEqualTo(@PathVariable int level) {
        return skillSynthService.getUsersWithLevelEqualTo(level);
    }

    @PostMapping("/users")
    public AppUser createUser(@RequestBody AppUser user) {
        return skillSynthService.createUser(user.getUsername(), user.getLevel(), user.getAllSkills());
    }

    @PutMapping("/users")
    public AppUser updateUser(@RequestBody AppUser user) {
        return skillSynthService.updateUser(user);
    }

    @DeleteMapping("/users/{id}")
    public ResponseEntity<Void> deleteUser(@PathVariable Long id) {
        return skillSynthService.deleteUser(id)
                ? ResponseEntity.noContent().build()
                : ResponseEntity.notFound().build();
    }

    @PatchMapping("/users/{id}/level")
    public ResponseEntity<AppUser> updateUserLevel(@PathVariable Long id, @RequestBody int newLevel) {
        return skillSynthService.updateUserLevel(id, newLevel)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    // -------------------- SKILL ENDPOINTS --------------------

    @GetMapping("/skills")
    public List<Skill> getAllSkills() {
        return skillSynthService.getAllSkills();
    }

    @GetMapping("/skills/{id}")
    public ResponseEntity<Skill> getSkillById(@PathVariable Long id) {
        return skillSynthService.getSkillById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @GetMapping("/skills/name/{name}")
    public ResponseEntity<Skill> getSkillByName(@PathVariable String name) {
        return skillSynthService.getSkillByName(name)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @GetMapping("/skills/description")
    public ResponseEntity<Skill> getSkillByDescription(@RequestParam String description) {
        return skillSynthService.getSkillByDescription(description)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @GetMapping("/skills/search")
    public List<Skill> searchSkillsByKeyword(@RequestParam String keyword) {
        return skillSynthService.getSkillsByKeyword(keyword);
    }

    @PostMapping("/skills")
    public Skill createSkill(@RequestBody Skill skill) {
        return skillSynthService.createSkill(skill.getSkillName(), skill.getDescription());
    }

    @PutMapping("/skills")
    public Skill updateSkill(@RequestBody Skill skill) {
        return skillSynthService.updateSkill(skill);
    }

    @DeleteMapping("/skills/{id}")
    public ResponseEntity<Void> deleteSkill(@PathVariable Long id) {
        return skillSynthService.deleteSkill(id)
                ? ResponseEntity.noContent().build()
                : ResponseEntity.notFound().build();
    }

    // -------------------- PROJECT ENDPOINTS --------------------

    @GetMapping("/projects")
    public List<Project> getAllProjects() {
        return skillSynthService.getAllProjects();
    }

    @GetMapping("/projects/{id}")
    public ResponseEntity<Project> getProjectById(@PathVariable Long id) {
        return skillSynthService.getProjectById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @GetMapping("/projects/name/{name}")
    public ResponseEntity<Project> getProjectByName(@PathVariable String name) {
        return skillSynthService.getProjectByName(name)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @GetMapping("/projects/level/greater-than/{level}")
    public List<Project> getProjectsLevelGreaterThan(@PathVariable int level) {
        return skillSynthService.getProjectsXPGreaterThan(level);
    }

    @GetMapping("/projects/level/less-than/{level}")
    public List<Project> getProjectsLevelLessThan(@PathVariable int level) {
        return skillSynthService.getProjectsXPLessThan(level);
    }

    @GetMapping("/projects/level/equal-to/{level}")
    public List<Project> getProjectsLevelEqualTo(@PathVariable int level) {
        return skillSynthService.getProjectsXPEqualTo(level);
    }

    @PostMapping("/projects")
    public Project createProject(@RequestBody Project project) {
        return skillSynthService.createProject(
                project.getName(),
                project.getRecommendedSkills(),
                project.getDateRange(),
                project.getProjectDescription(),
                project.getExperienceLevel()
        );
    }

    @PutMapping("/projects")
    public Project updateProject(@RequestBody Project project) {
        return skillSynthService.updateProject(project);
    }

    @DeleteMapping("/projects/{id}")
    public ResponseEntity<Void> deleteProject(@PathVariable Long id) {
        return skillSynthService.deleteProject(id)
                ? ResponseEntity.noContent().build()
                : ResponseEntity.notFound().build();
    }
}
