// app/src/main/java/com/example/newtrade/models/HomeSection.java
package com.example.newtrade.models;

import java.util.List;

public class HomeSection {
    public enum SectionType {
        RECENT_PRODUCTS("Recent Products"),
        NEARBY_PRODUCTS("Nearby Products"),
        POPULAR_PRODUCTS("Popular Products"),
        RECOMMENDED_PRODUCTS("Recommended for You"),
        FEATURED_PRODUCTS("Featured Products");

        private final String displayName;

        SectionType(String displayName) {
            this.displayName = displayName;
        }

        public String getDisplayName() {
            return displayName;
        }
    }

    private String title;
    private SectionType type;
    private List<Product> products;
    private String seeAllUrl;
    private boolean showSeeAll;

    // Constructors
    public HomeSection() {}

    public HomeSection(String title, SectionType type, List<Product> products) {
        this.title = title;
        this.type = type;
        this.products = products;
        this.showSeeAll = true;
    }

    public HomeSection(SectionType type, List<Product> products) {
        this.title = type.getDisplayName();
        this.type = type;
        this.products = products;
        this.showSeeAll = true;
    }

    // Helper methods
    public boolean hasProducts() {
        return products != null && !products.isEmpty();
    }

    public int getProductCount() {
        return products != null ? products.size() : 0;
    }

    public String getProductCountText() {
        int count = getProductCount();
        if (count == 0) return "No items";
        if (count == 1) return "1 item";
        return count + " items";
    }

    // Getters and Setters
    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }

    public SectionType getType() { return type; }
    public void setType(SectionType type) { this.type = type; }

    public List<Product> getProducts() { return products; }
    public void setProducts(List<Product> products) { this.products = products; }

    public String getSeeAllUrl() { return seeAllUrl; }
    public void setSeeAllUrl(String seeAllUrl) { this.seeAllUrl = seeAllUrl; }

    public boolean isShowSeeAll() { return showSeeAll; }
    public void setShowSeeAll(boolean showSeeAll) { this.showSeeAll = showSeeAll; }

    @Override
    public String toString() {
        return "HomeSection{" +
                "title='" + title + '\'' +
                ", type=" + type +
                ", products=" + getProductCount() +
                '}';
    }
}