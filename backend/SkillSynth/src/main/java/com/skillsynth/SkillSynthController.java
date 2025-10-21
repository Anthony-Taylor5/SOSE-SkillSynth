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
        AppUser createdUser = skillSynthService.createUser(user.getUsername(), user.getLevel(), user.getAllSkills());
        
        // Upload user to ML service for teammate matching
        try {
            uploadUserToMLService(createdUser);
        } catch (Exception e) {
            // Log error but don't fail user creation
            System.err.println("Failed to upload user to ML service: " + e.getMessage());
        }
        
        return createdUser;
    }

    @PutMapping("/users")
    public AppUser updateUser(@RequestBody AppUser user) {
        AppUser updatedUser = skillSynthService.updateUser(user);
        
        // Re-upload user to ML service with updated information
        try {
            uploadUserToMLService(updatedUser);
        } catch (Exception e) {
            // Log error but don't fail user update
            System.err.println("Failed to update user in ML service: " + e.getMessage());
        }
        
        return updatedUser;
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

    @PostMapping("/projects/ai-generate")
    public ResponseEntity<Map<String, Object>> generateAIGeneratedProject(@RequestBody Map<String, Object> request) {
        try {
            @SuppressWarnings("unchecked")
            List<String> mainSkills = (List<String>) request.get("main_skills");
            int timeAvailability = (Integer) request.get("time_availability");
            int experienceLevel = (Integer) request.get("experience_level");
            
            // Generate project using ML service
            Map<String, Object> aiProject = mlService.generateProject(mainSkills, timeAvailability, experienceLevel);
            
            return ResponseEntity.ok(aiProject);
        } catch (Exception e) {
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("error", "Failed to generate AI project: " + e.getMessage());
            return ResponseEntity.status(500).body(errorResponse);
        }
    }

    @PostMapping("/projects/ai-generate-and-save")
    public ResponseEntity<Project> generateAndSaveAIGeneratedProject(@RequestBody Map<String, Object> request) {
        try {
            @SuppressWarnings("unchecked")
            List<String> mainSkills = (List<String>) request.get("main_skills");
            int timeAvailability = (Integer) request.get("time_availability");
            int experienceLevel = (Integer) request.get("experience_level");
            
            // Generate project using ML service
            Map<String, Object> aiProjectResponse = mlService.generateProject(mainSkills, timeAvailability, experienceLevel);
            
            // Extract project data from AI response
            @SuppressWarnings("unchecked")
            Map<String, Object> projectData = (Map<String, Object>) aiProjectResponse.get("project");
            
            // Convert AI project to our Project entity
            Project project = convertAIToProject(projectData, mainSkills, timeAvailability, experienceLevel);
            
            // Save to database
            Project savedProject = skillSynthService.createProject(
                    project.getName(),
                    project.getRecommendedSkills(),
                    project.getDateRange(),
                    project.getProjectDescription(),
                    project.getExperienceLevel()
            );
            
            return ResponseEntity.ok(savedProject);
        } catch (Exception e) {
            System.err.println("Failed to generate and save AI project: " + e.getMessage());
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

    // -------------------- HELPER METHODS --------------------

    private void uploadUserToMLService(AppUser user) {
        // Convert AppUser to ML service format
        Map<String, Object> mlUser = new HashMap<>();
        mlUser.put("id", user.getId().toString());
        
        // Convert skills to skill level mapping (assuming all skills are level 3 for now)
        // In a real implementation, you might want to store skill levels in the database
        Map<String, Integer> skillsMap = new HashMap<>();
        for (Skill skill : user.getAllSkills()) {
            skillsMap.put(skill.getSkillName(), 3); // Default level 3
        }
        mlUser.put("skills", skillsMap);
        
        // Set time availability based on user level (1-20 scale)
        // Higher level users might have more time availability
        int timeAvailability = Math.min(20, Math.max(1, user.getLevel() * 2));
        mlUser.put("time_availability", timeAvailability);
        
        // Upload single user to ML service
        List<Map<String, Object>> usersList = new ArrayList<>();
        usersList.add(mlUser);
        
        mlService.uploadUsers(usersList);
    }

    private Project convertAIToProject(Map<String, Object> aiProjectData, List<String> mainSkills, int timeAvailability, int experienceLevel) {
        Project project = new Project();
        
        // Set basic project information
        project.setName((String) aiProjectData.get("project_name"));
        project.setProjectDescription((String) aiProjectData.get("description"));
        project.setExperienceLevel(experienceLevel);
        
        // Set date range based on time availability
        String dateRange = calculateDateRange(timeAvailability);
        project.setDateRange(dateRange);
        
        // Convert relevant skills to Skill objects
        @SuppressWarnings("unchecked")
        List<String> relevantSkills = (List<String>) aiProjectData.get("relevant_skills");
        List<Skill> skillObjects = new ArrayList<>();
        
        // Add main skills
        for (String skillName : mainSkills) {
            Skill skill = skillSynthService.getSkillByName(skillName).orElse(null);
            if (skill == null) {
                // Create skill if it doesn't exist
                skill = skillSynthService.createSkill(skillName, "AI-generated skill for project");
            }
            skillObjects.add(skill);
        }
        
        // Add relevant skills
        if (relevantSkills != null) {
            for (String skillName : relevantSkills) {
                if (!mainSkills.contains(skillName)) { // Avoid duplicates
                    Skill skill = skillSynthService.getSkillByName(skillName).orElse(null);
                    if (skill == null) {
                        skill = skillSynthService.createSkill(skillName, "AI-recommended skill for project");
                    }
                    skillObjects.add(skill);
                }
            }
        }
        
        project.setRecommendedSkills(skillObjects);
        
        return project;
    }

    private String calculateDateRange(int timeAvailability) {
        // Calculate estimated duration based on time availability
        int weeks = Math.max(1, timeAvailability / 5); // Rough estimate: 5 hours per week
        return "Estimated " + weeks + " week" + (weeks > 1 ? "s" : "") + " duration";
    }
}
