// app/src/main/java/com/example/newtrade/models/Category.java
package com.example.newtrade.models;

public class Category {
    private Long id;
    private String name;
    private String description;
    private String icon;
    private boolean isActive;

    // Getters and Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public String getIcon() { return icon; }
    public void setIcon(String icon) { this.icon = icon; }

    public boolean isActive() { return isActive; }
    public void setActive(boolean active) { isActive = active; }
}