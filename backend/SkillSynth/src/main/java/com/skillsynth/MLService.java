package com.skillsynth;

import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.springframework.http.*;
import java.util.*;

@Service
public class MLService {
    
    private final RestTemplate restTemplate;
    private final String mlApiBaseUrl = "http://localhost:8000";
    
    public MLService() {
        this.restTemplate = new RestTemplate();
    }
    
    // Get relevant skills for a skill
    public Map<String, Object> getRelevantSkills(String mainSkill, int topK) {
        String url = mlApiBaseUrl + "/grab_relevant_skills";
        
        Map<String, Object> requestBody = new HashMap<>();
        requestBody.put("main_skill", mainSkill);
        requestBody.put("top_k", topK);
        
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        HttpEntity<Map<String, Object>> entity = new HttpEntity<>(requestBody, headers);
        
        ResponseEntity<Map> response = restTemplate.postForEntity(url, entity, Map.class);
        return response.getBody();
    }
    
    // Generate a project
    public Map<String, Object> generateProject(List<String> mainSkills, int timeAvailability, int experienceLevel) {
        String url = mlApiBaseUrl + "/get_project";
        
        Map<String, Object> requestBody = new HashMap<>();
        requestBody.put("main_skills", mainSkills);
        requestBody.put("time_availability", timeAvailability);
        requestBody.put("experience_level", experienceLevel);
        
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        HttpEntity<Map<String, Object>> entity = new HttpEntity<>(requestBody, headers);
        
        ResponseEntity<Map> response = restTemplate.postForEntity(url, entity, Map.class);
        return response.getBody();
    }
    
    // Process and upload skills to ML API
    public Map<String, Object> processAndUploadSkills(Map<String, List<String>> skills) {
        String url = mlApiBaseUrl + "/process_and_upload_skills";
        
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        HttpEntity<Map<String, List<String>>> entity = new HttpEntity<>(skills, headers);
        
        ResponseEntity<Map> response = restTemplate.postForEntity(url, entity, Map.class);
        return response.getBody();
    }
    
    // Upload users for teammate matching
    public Map<String, Object> uploadUsers(List<Map<String, Object>> users) {
        String url = mlApiBaseUrl + "/upload_users";
        
        Map<String, Object> requestBody = new HashMap<>();
        requestBody.put("users", users);
        
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        HttpEntity<Map<String, Object>> entity = new HttpEntity<>(requestBody, headers);
        
        ResponseEntity<Map> response = restTemplate.postForEntity(url, entity, Map.class);
        return response.getBody();
    }
    
    // Find teammates for a user
    public Map<String, Object> findTeammates(Map<String, Object> user, int topK) {
        String url = mlApiBaseUrl + "/find_teammates";
        
        Map<String, Object> requestBody = new HashMap<>();
        requestBody.put("user", user);
        requestBody.put("top_k", topK);
        
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        HttpEntity<Map<String, Object>> entity = new HttpEntity<>(requestBody, headers);
        
        ResponseEntity<Map> response = restTemplate.postForEntity(url, entity, Map.class);
        return response.getBody();
    }
}