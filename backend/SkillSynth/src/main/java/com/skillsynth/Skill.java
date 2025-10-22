package com.skillsynth;

import jakarta.persistence.*;

@Entity
@Table(name = "skills")
public class Skill {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    private String name;

    @Column(nullable = false)
    private String category;

    @Column(nullable = false)
    private int level;

    @Column(nullable = false)
    private int xp;

    // === Constructors ===
    public Skill() {
        this.name = "undefined";
        this.category = "General";
        this.level = 1;
        this.xp = 0;
    }

    public Skill(String name, String category) {
        this.name = name;
        this.category = category;
        this.level = 1;
        this.xp = 0;
    }

    public Skill(String name, int level) {
        this.name = name;
        this.level = level;
        this.xp = 0;
        this.category = "General";
    }

    // === Getters and Setters ===
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getCategory() {
        return category;
    }

    public void setCategory(String category) {
        this.category = category;
    }

    public int getLevel() {
        return level;
    }

    public void setLevel(int level) {
        this.level = level;
    }

    public int getXp() {
        return xp;
    }

    public void setXp(int xp) {
        this.xp = xp;
    }

    // === Helper Methods ===
    public void addXP(int xp) {
        this.xp += xp;
        updateLevel();
    }

    private void updateLevel() {
        if (level < 5 && xp >= xpToNextLevel()) {
            level++;
        }
    }

    private int xpToNextLevel() {
        return 100 * level;
    }

    public float getProgressPercentage() {
        return (float) level / 5 * 100f;
    }
}
