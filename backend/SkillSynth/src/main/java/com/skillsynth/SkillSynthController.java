package com.skillsynth;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.List;
import java.util.Optional;
import java.util.Map;
import java.util.HashMap;
import java.util.ArrayList;

@CrossOrigin(origins = "http://localhost:5173/")
@RestController
@RequestMapping("/api")
public class SkillSynthController {

    @Autowired
    private SkillSynthService skillSynthService;

    @Autowired
    private MLService mlService;

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
        // Correct argument order and method names
        return skillSynthService.createUser(
                user.getUsername(),
                user.getLevel(),
                user.getAllSkills()
        );
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
        return skillSynthService.createSkill(skill.getName(), skill.getCategory());
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

    @PostMapping("/projects/ai-generate")
    public ResponseEntity<Project> generateAIProject(@RequestBody Map<String, Object> request) {
        try {
            @SuppressWarnings("unchecked")
            List<Map<String, Object>> skillMaps = (List<Map<String, Object>>) request.get("skills");
            List<Skill> skills = new ArrayList<>();

            for (Map<String, Object> skillMap : skillMaps) {
                String skillName = (String) skillMap.get("skillName");
                String category = (String) skillMap.getOrDefault("category", "General");
                Skill skill = skillSynthService.getSkillByName(skillName)
                        .orElseGet(() -> skillSynthService.createSkill(skillName, category));
                skills.add(skill);
            }

            int timeAvailability = (Integer) request.get("time_availability");
            int experienceLevel = (Integer) request.get("experience_level");

            Project aiProject = skillSynthService.createAIProject(
                    "AI Suggested Project",
                    skills,
                    timeAvailability,
                    experienceLevel
            );

            return ResponseEntity.ok(aiProject);

        } catch (Exception e) {
            System.err.println("⚠️ Failed to generate AI project: " + e.getMessage());
            return ResponseEntity.status(500).build();
        }
    }

    @PostMapping("/projects/ai-generate-and-save")
    public ResponseEntity<Project> generateAndSaveAIProject(@RequestBody Map<String, Object> request) {
        try {
            @SuppressWarnings("unchecked")
            List<Map<String, Object>> skillMaps = (List<Map<String, Object>>) request.get("skills");
            List<Skill> skills = new ArrayList<>();

            for (Map<String, Object> skillMap : skillMaps) {
                String skillName = (String) skillMap.get("skillName");
                String category = (String) skillMap.getOrDefault("category", "General");
                Skill skill = skillSynthService.getSkillByName(skillName)
                        .orElseGet(() -> skillSynthService.createSkill(skillName, category));
                skills.add(skill);
            }

            int timeAvailability = (Integer) request.get("time_availability");
            int experienceLevel = (Integer) request.get("experience_level");

            Project aiProject = skillSynthService.createAIProject(
                    "AI Suggested Project",
                    skills,
                    timeAvailability,
                    experienceLevel
            );

            return ResponseEntity.ok(aiProject);

        } catch (Exception e) {
            System.err.println("⚠️ Failed to generate and save AI project: " + e.getMessage());
            return ResponseEntity.status(500).build();
        }
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

    // -------------------- ML SERVICE ENDPOINTS --------------------

    @PostMapping("/ml/relevant-skills")
    public Map<String, Object> getRelevantSkills(@RequestParam String mainSkill, @RequestParam(defaultValue = "3") int topK) {
        return mlService.getRelevantSkills(mainSkill, topK);
    }

    @PostMapping("/ml/generate-project")
    public Map<String, Object> generateProject(@RequestBody Map<String, Object> request) {
        @SuppressWarnings("unchecked")
        List<String> mainSkills = (List<String>) request.get("main_skills");
        int timeAvailability = (Integer) request.get("time_availability");
        int experienceLevel = (Integer) request.get("experience_level");

        return mlService.generateProject(mainSkills, timeAvailability, experienceLevel);
    }

    @PostMapping("/ml/process-skills")
    public Map<String, Object> processAndUploadSkills(@RequestBody Map<String, List<String>> skills) {
        return mlService.processAndUploadSkills(skills);
    }

    @PostMapping("/ml/upload-users")
    public Map<String, Object> uploadUsers(@RequestBody List<Map<String, Object>> users) {
        return mlService.uploadUsers(users);
    }

    @PostMapping("/ml/find-teammates")
    public Map<String, Object> findTeammates(@RequestBody Map<String, Object> request) {
        @SuppressWarnings("unchecked")
        Map<String, Object> user = (Map<String, Object>) request.get("user");
        int topK = (Integer) request.getOrDefault("top_k", 15);

        return mlService.findTeammates(user, topK);
    }
}
