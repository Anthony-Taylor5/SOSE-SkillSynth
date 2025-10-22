package com.skillsynth;

import org.springframework.stereotype.Service;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.client.RestTemplate;

import java.util.*;
import java.util.stream.Collectors;

@Service
public class SkillSynthService {

    private final SkillSynthAppUserRepository userRepository;
    private final SkillSynthSkillRepository skillRepository;
    private final SkillSynthProjectRepository projectRepository;

    private final RestTemplate restTemplate = new RestTemplate();

    @Value("${ml.api.base-url:http://localhost:8000}")
    private String mlApiBaseUrl;

    // Constructor Injection
    public SkillSynthService(SkillSynthAppUserRepository userRepository,
                             SkillSynthSkillRepository skillRepository,
                             SkillSynthProjectRepository projectRepository) {
        this.userRepository = userRepository;
        this.skillRepository = skillRepository;
        this.projectRepository = projectRepository;
    }

    /* ==============================
       USER SERVICES
    ============================== */

    public AppUser createUser(String username, int level, List<Skill> skills) {
        AppUser user = new AppUser(username, level, skills);
        
        // Reattach existing skills to the current persistence context
        List<Skill> managedSkills = new ArrayList<>();
        for (Skill skill : skills) {
        if (skill.getId() != null) {
            Skill managed = skillRepository.findById(skill.getId())
                    .orElseThrow(() -> new RuntimeException("Skill not found: " + skill.getId()));
            managedSkills.add(managed);
        } else {
            managedSkills.add(skillRepository.save(skill));
        }
        }
        user.setAllSkills(managedSkills);

        AppUser savedUser = userRepository.save(user);

        try {
            Map<String, Object> mlUser = new HashMap<>();
            mlUser.put("id", savedUser.getId().toString());

            Map<String, Integer> skillsMap = new HashMap<>();
            for (Skill skill : savedUser.getAllSkills()) {
                skillsMap.put(skill.getName(), skill.getLevel());
            }
            mlUser.put("skills", skillsMap);

            int timeAvailability = Math.min(20, Math.max(1, level * 2));
            mlUser.put("time_availability", timeAvailability);

            Map<String, Object> mlRequest = new HashMap<>();
            mlRequest.put("users", List.of(mlUser));

            String mlUploadUrl = mlApiBaseUrl + "/upload_users";
            restTemplate.postForObject(mlUploadUrl, mlRequest, Map.class);
            System.out.println("✅ Uploaded user to ML service: " + username);

        } catch (Exception e) {
            System.err.println("⚠️ Failed to upload user to ML service: " + e.getMessage());
        }

        return savedUser;
    }

    public Optional<AppUser> getUserById(Long id) {
        return userRepository.findById(id);
    }

    public Optional<AppUser> getUserByUsername(String username) {
        return userRepository.findByUsername(username);
    }

    public List<AppUser> getAllUsers() {
        return userRepository.findAll();
    }

    public List<AppUser> getUsersWithLevelGreaterThan(int level) {
        return userRepository.findByLevelGreaterThan(level);
    }

    public List<AppUser> getUsersWithLevelLessThan(int level) {
        return userRepository.findAll().stream()
                .filter(user -> user.getLevel() < level)
                .toList();
    }

    public List<AppUser> getUsersWithLevelEqualTo(int level) {
        return userRepository.findAll().stream()
                .filter(user -> user.getLevel() == level)
                .toList();
    }

    public AppUser updateUser(AppUser user) {
        AppUser updatedUser = userRepository.save(user);

        try {
            Map<String, Object> mlUser = new HashMap<>();
            mlUser.put("id", updatedUser.getId().toString());

            Map<String, Integer> skillsMap = new HashMap<>();
            for (Skill skill : updatedUser.getAllSkills()) {
                skillsMap.put(skill.getName(), skill.getLevel());
            }
            mlUser.put("skills", skillsMap);

            int timeAvailability = Math.min(20, Math.max(1, updatedUser.getLevel() * 2));
            mlUser.put("time_availability", timeAvailability);

            Map<String, Object> mlRequest = new HashMap<>();
            mlRequest.put("users", List.of(mlUser));

            restTemplate.postForObject(mlApiBaseUrl + "/upload_users", mlRequest, Map.class);
            System.out.println("✅ Updated user uploaded to ML service: " + updatedUser.getUsername());
        } catch (Exception e) {
            System.err.println("⚠️ Failed to update user in ML service: " + e.getMessage());
        }

        return updatedUser;
    }

    public Optional<AppUser> updateUserLevel(Long id, int newLevel) {
        return userRepository.findById(id).map(user -> {
            user.setLevel(newLevel);
            return userRepository.save(user);
        });
    }

    public boolean deleteUser(Long id) {
        if (userRepository.existsById(id)) {
            userRepository.deleteById(id);
            return true;
        }
        return false;
    }

    public Optional<AppUser> getUserByIdWithSkills(Long id) {
        return userRepository.findByIdWithSkills(id);
    }

    /* ==============================
       SKILL SERVICES
    ============================== */

    public Skill createSkill(String skillName, String category) {
        Skill skill = new Skill(skillName, category);
        Skill savedSkill = skillRepository.save(skill);

        try {
            Map<String, List<String>> skillPayload = new HashMap<>();
            skillPayload.put(category != null ? category : "General", List.of(skillName));

            restTemplate.postForObject(mlApiBaseUrl + "/process_and_upload_skills", skillPayload, Map.class);
            System.out.println("✅ Sent skill to ML service: " + skillName + " (" + category + ")");
        } catch (Exception e) {
            System.err.println("⚠️ Failed to send skill to ML service: " + e.getMessage());
        }

        return savedSkill;
    }

    public Map<String, Object> getRelevantSkills(String mainSkill, int topK) {
        try {
            Map<String, Object> requestBody = new HashMap<>();
            requestBody.put("main_skill", mainSkill);
            requestBody.put("top_k", topK);

            Map<String, Object> response =
                    restTemplate.postForObject(mlApiBaseUrl + "/grab_relevant_skills", requestBody, Map.class);

            if (response == null || !response.containsKey("relevant_skills")) {
                throw new RuntimeException("Invalid response from ML service for skill: " + mainSkill);
            }
            return response;
        } catch (Exception e) {
            System.err.println("⚠️ Failed to fetch relevant skills for " + mainSkill + ": " + e.getMessage());
            return Map.of("error", e.getMessage());
        }
    }
    public Optional<Skill> getSkillById(Long id) {
        return skillRepository.findById(id);
    }
    public Optional<Skill> getSkillByName(String name) {
        return skillRepository.findByName(name);
    }

    public Optional<Skill> getSkillByDescription(String description) {
        return skillRepository.findByCategory(description);
    }

    public List<Skill> getAllSkills() {
        return skillRepository.findAll();
    }

    public List<Skill> getSkillsByKeyword(String keyword) {
        return skillRepository.findByNameContaining(keyword);
    }

    public Skill updateSkill(Skill skill) {
        Skill updatedSkill = skillRepository.save(skill);

        try {
            Map<String, List<String>> skillPayload = new HashMap<>();
            skillPayload.put(
                    updatedSkill.getCategory() != null ? updatedSkill.getCategory() : "General",
                    List.of(updatedSkill.getName())
            );

            restTemplate.postForObject(mlApiBaseUrl + "/process_and_upload_skills", skillPayload, Map.class);
            System.out.println("✅ Updated skill sent to ML service: " + updatedSkill.getName());
        } catch (Exception e) {
            System.err.println("⚠️ Failed to update skill in ML service: " + e.getMessage());
        }

        return updatedSkill;
    }

    public boolean deleteSkill(Long id) {
        if (skillRepository.existsById(id)) {
            skillRepository.deleteById(id);
            return true;
        }
        return false;
    }

    /* ==============================
       PROJECT SERVICES
    ============================== */

    public Project createProject(String projectName, List<Skill> skills, String dates, String details, int projectLevel) {
        Project project = new Project(projectName, skills, dates, details, projectLevel);
        return projectRepository.save(project);
    }

    public Project createAIProject(String projectName, List<Skill> skills, int timeAvailability, int projectLevel) {
        try {
            List<String> mainSkillNames = skills.stream()
                    .map(Skill::getName)
                    .collect(Collectors.toList());

            Map<String, Object> mlRequest = new HashMap<>();
            mlRequest.put("main_skills", mainSkillNames);
            mlRequest.put("time_availability", timeAvailability);
            mlRequest.put("experience_level", projectLevel);

            Map<String, Object> mlResponse =
                    restTemplate.postForObject(mlApiBaseUrl + "/get_project", mlRequest, Map.class);

            if (mlResponse == null || !mlResponse.containsKey("project")) {
                throw new RuntimeException("Invalid ML API response");
            }

            Map<String, Object> projectData = (Map<String, Object>) mlResponse.get("project");
            String aiProjectName = (String) projectData.getOrDefault("project_name", projectName);
            String aiDescription = (String) projectData.getOrDefault("description", "AI-generated project description");
            List<String> relevantSkillNames =
                    (List<String>) projectData.getOrDefault("relevant_skills", new ArrayList<>());

            List<Skill> recommendedSkills = new ArrayList<>();
            for (String skillName : relevantSkillNames) {
                Skill skill = skillRepository.findByName(skillName)
                        .orElseGet(() -> skillRepository.save(new Skill(skillName, "AI-recommended skill")));
                recommendedSkills.add(skill);
            }

            for (Skill baseSkill : skills) {
                if (recommendedSkills.stream().noneMatch(s -> s.getName().equalsIgnoreCase(baseSkill.getName()))) {
                    recommendedSkills.add(baseSkill);
                }
            }

            Project project = new Project(
                    aiProjectName,
                    recommendedSkills,
                    calculateDateRange(timeAvailability),
                    aiDescription,
                    projectLevel
            );

            Project savedProject = projectRepository.save(project);
            System.out.println("✅ Created AI project from ML service: " + savedProject.getName());
            return savedProject;

        } catch (Exception e) {
            System.err.println("⚠️ Failed to create AI project: " + e.getMessage());
            throw new RuntimeException("AI project creation failed: " + e.getMessage(), e);
        }
    }

    private String calculateDateRange(int timeAvailability) {
        int weeks = Math.max(1, timeAvailability / 5);
        return "Estimated " + weeks + " week" + (weeks > 1 ? "s" : "") + " duration";
    }

    public Optional<Project> getProjectById(Long id) {
        return projectRepository.findById(id);
    }

    public Optional<Project> getProjectByName(String name) {
        return projectRepository.findByName(name);
    }

    public List<Project> getAllProjects() {
        return projectRepository.findAll();
    }

    public List<Project> getProjectsXPGreaterThan(int level) {
        return projectRepository.findByExperienceLevelGreaterThan(level);
    }

    public List<Project> getProjectsXPLessThan(int level) {
        return projectRepository.findAll().stream()
                .filter(project -> project.getExperienceLevel() < level && project.getExperienceLevel() != 0)
                .toList();
    }

    public List<Project> getProjectsXPEqualTo(int level) {
        return projectRepository.findAll().stream()
                .filter(project -> project.getExperienceLevel() == level)
                .toList();
    }

    public Project updateProject(Project project) {
        Project updatedProject = projectRepository.save(project);

        try {
            Map<String, Object> mlRequest = new HashMap<>();
            List<String> skillNames = updatedProject.getRecommendedSkills()
                    .stream().map(Skill::getName).toList();
            mlRequest.put("main_skills", skillNames);
            mlRequest.put("time_availability", 10);
            mlRequest.put("experience_level", updatedProject.getExperienceLevel());
            restTemplate.postForObject(mlApiBaseUrl + "/get_project", mlRequest, Map.class);
        } catch (Exception e) {
            System.err.println("⚠️ Failed to update project in ML: " + e.getMessage());
        }

        return updatedProject;
    }

    public boolean deleteProject(Long id) {
        if (projectRepository.existsById(id)) {
            projectRepository.deleteById(id);
            return true;
        }
        return false;
    }
}
