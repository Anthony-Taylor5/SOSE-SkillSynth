package com.skillsynth;

import jakarta.persistence.*;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "users")
public class AppUser {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String username;
    private int level;

    @ManyToMany(fetch = FetchType.EAGER, cascade = {CascadeType.MERGE})
    @JoinTable(
        name = "user_skills",
        joinColumns = @JoinColumn(name = "user_id"),
        inverseJoinColumns = @JoinColumn(name = "skill_id")
    )
    private List<Skill> allSkills = new ArrayList<>(); // ✅ Initialize to avoid nulls

    // === Default Constructor ===
    public AppUser() {
        this.username = "undefined";
        this.level = 1;
        this.allSkills = new ArrayList<>();
    }

    // === Full Constructor ===
    public AppUser(String username, int level, List<Skill> skills) {
        this.username = username;
        this.level = level;
        this.allSkills = (skills != null) ? skills : new ArrayList<>();
    }

    // === Getters and Setters ===
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public int getLevel() {
        return level;
    }

    public void setLevel(int level) {
        this.level = level;
    }

    public List<Skill> getAllSkills() {
        return allSkills;
    }

    public void setAllSkills(List<Skill> allSkills) {
        this.allSkills = (allSkills != null) ? allSkills : new ArrayList<>();
    }
}
