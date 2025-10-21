package com.skillsynth;

import org.springframework.stereotype.Service;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Service
public class SkillSynthService {

    private final SkillSynthAppUserRepository userRepository; // Renamed for clarity - handles User operations

    private final SkillSynthSkillRepository skillRepository;

    private final SkillSynthProjectRepository projectRepository;

    // Constructor injection
    public SkillSynthService(SkillSynthAppUserRepository userRepository,
                             SkillSynthSkillRepository skillRepository,
                             SkillSynthProjectRepository projectRepository) {
        this.userRepository = userRepository;
        this.skillRepository = skillRepository;
        this.projectRepository = projectRepository;
    }

    /*
        USER SERVICES || This code block contains all services related to Users
     */
    // Creates a new User
    public AppUser createUser(String username, int level, List<Skill> skills) {
        AppUser user = new AppUser(username, level, skills);
        return userRepository.save(user);
    }

    // Retrieves a User by ID
    public Optional<AppUser> getUserById(Long id) {
        return userRepository.findById(id);
    }

    // Retrieves a User by username
    public Optional<AppUser> getUserByUsername(String username) {
        return userRepository.findByUsername(username);
    }

    // Retrieves All Users
    public List<AppUser> getAllUsers() {
        return userRepository.findAll();
    }

    // Retrieves Users with level greater than specified level
    public List<AppUser> getUsersWithLevelGreaterThan(int level) {
        return userRepository.findByLevelGreaterThan(level);
    }

    // Retrieves Users with level less than specified level
    public List<AppUser> getUsersWithLevelLessThan(int level) {
        return userRepository.findAll().stream()
                .filter(user -> user.getLevel() < level)
                .toList();
    }

    // Retrieves Users with level equal to specified level
    public List<AppUser> getUsersWithLevelEqualTo(int level) {
        return userRepository.findAll().stream()
                .filter(user -> user.getLevel() == level)
                .toList();
    }

    // Update user
    public AppUser updateUser(AppUser user) {
        return userRepository.save(user); // Save will update if ID exists
    }

    // Update user level specifically
    public Optional<AppUser> updateUserLevel(Long id, int newLevel) {
        Optional<AppUser> userOptional = userRepository.findById(id);
        if (userOptional.isPresent()) {
            AppUser user = userOptional.get();
            user.setLevel(newLevel);
            return Optional.of(userRepository.save(user));
        }
        return Optional.empty();
    }

    // Delete user
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


    // Creates a new Skill
    public Skill createSkill(String skillName, String description) {
        Skill skill = new Skill(skillName, description);
        return skillRepository.save(skill);
    }

    // Retrieves a Skill by name
    public Optional<Skill> getSkillByName(String name) {
        return skillRepository.findBySkillName(name);
    }

    // Retrieves a Skill by ID
    public Optional<Skill> getSkillById(Long id) {
        return skillRepository.findById(id);
    }

    // Retrieves a Skill by description
    public Optional<Skill> getSkillByDescription(String description) {
        return skillRepository.findByDescription(description);
    }

    // Retrieves All Skills
    public List<Skill> getAllSkills() {
        return skillRepository.findAll();
    }

    // Retrieves Skills containing keyword
    public List<Skill> getSkillsByKeyword(String keyword) {
        return skillRepository.findBySkillNameContaining(keyword);
    }

    // Update Skill
    public Skill updateSkill(Skill skill) {
        return skillRepository.save(skill); // Save will update if ID exists
    }

    // Delete Skill
    public boolean deleteSkill(Long id) {
        if (skillRepository.existsById(id)) {
            skillRepository.deleteById(id);
            return true;
        }
        return false;
    }

    /*
        PROJECT SERVICES || This code block contains all services related to Projects
     */

    // Creates a new Project
    public Project createProject(String projectName, List<Skill> skills, String dates, String details, int projectLevel) {
        Project project = new Project(projectName, skills, dates, details, projectLevel);
        return projectRepository.save(project);
    }

    // Retrieves a Project by ID
    public Optional<Project> getProjectById(Long id) {
        return projectRepository.findById(id);
    }

    // Retrieves a project by name
    public Optional<Project> getProjectByName(String name){
        return projectRepository.findByName(name);
    }

    // Retrieve All Projects
    public List<Project> getAllProjects(){
        return projectRepository.findAll();
    }

    // Retrieves Projects with experience level greater than specified level
    public List<Project> getProjectsXPGreaterThan(int level){
        return projectRepository.findByExperienceLevelGreaterThan(level);
    }

    // Retrieves Projects with experience level less than specified level, cannot equal 0
    public List<Project> getProjectsXPLessThan(int level){
        return projectRepository.findAll().stream()
                .filter(project -> project.getExperienceLevel() < level && project.getExperienceLevel() != 0)
                .toList();
    }

    // Retrieves Projects with experience level equal to specified level
    public List<Project> getProjectsXPEqualTo(int level){
        return projectRepository.findAll().stream()
                .filter(project -> project.getExperienceLevel() == level)
                .toList();
    }

    //Update Project
    public Project updateProject(Project project){
        return projectRepository.save(project); // Save will update if ID exists
    }

    // Delete Project
    public boolean deleteProject(Long id){
        if(projectRepository.existsById(id)){
            projectRepository.deleteById(id);
            return true;
        }
        return false;
    }

    public AppUser createUser(String username, Integer level, List<Skill> allSkills) {
        if (level == null) level = 1;           // default level
        if (allSkills == null) allSkills = new ArrayList<>();
        AppUser user = new AppUser(username, level, allSkills);
        return userRepository.save(user);
    }


}