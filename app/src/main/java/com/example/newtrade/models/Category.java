// app/src/main/java/com/example/newtrade/models/Category.java
package com.example.newtrade.models;

public class Category {
    private Long id;
    private String name;
    private String description;
    private String icon;
    private boolean isActive;

    // Default constructor
    public Category() {}

    // Constructor để sử dụng trong HomeFragment
    public Category(Long id, String name, String description, String icon, boolean isActive) {
        this.id = id;
        this.name = name;
        this.description = description;
        this.icon = icon;
        this.isActive = isActive;
    }

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

    // Alias for backward compatibility
    public boolean getIsActive() { return isActive; }
    public void setIsActive(boolean active) { this.isActive = active; }
}