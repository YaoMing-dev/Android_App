// app/src/main/java/com/example/newtrade/models/RecommendationSection.java
package com.example.newtrade.models;

import java.util.List;

public class RecommendationSection {
    private String title;
    private List<Product> products;
    private String sectionType; // "personalized", "popular", "nearby", "category"
    private String subtitle;
    private boolean showViewAll;

    public RecommendationSection() {}

    public RecommendationSection(String title, List<Product> products, String sectionType, String subtitle, boolean showViewAll) {
        this.title = title;
        this.products = products;
        this.sectionType = sectionType;
        this.subtitle = subtitle;
        this.showViewAll = showViewAll;
    }

    // Getters and setters
    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }

    public List<Product> getProducts() { return products; }
    public void setProducts(List<Product> products) { this.products = products; }

    public String getSectionType() { return sectionType; }
    public void setSectionType(String sectionType) { this.sectionType = sectionType; }

    public String getSubtitle() { return subtitle; }
    public void setSubtitle(String subtitle) { this.subtitle = subtitle; }

    public boolean isShowViewAll() { return showViewAll; }
    public void setShowViewAll(boolean showViewAll) { this.showViewAll = showViewAll; }
}