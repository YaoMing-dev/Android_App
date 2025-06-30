// app/src/main/java/com/example/newtrade/models/ProductImage.java
package com.example.newtrade.models;

public class ProductImage {
    private Long id;
    private String imageUrl;
    private String thumbnailUrl;
    private Integer displayOrder;
    private String alt;
    private Long fileSize;
    private String createdAt;

    // Constructors
    public ProductImage() {}

    public ProductImage(String imageUrl, Integer displayOrder) {
        this.imageUrl = imageUrl;
        this.displayOrder = displayOrder;
    }

    // Getters and Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getImageUrl() { return imageUrl; }
    public void setImageUrl(String imageUrl) { this.imageUrl = imageUrl; }

    public String getThumbnailUrl() { return thumbnailUrl; }
    public void setThumbnailUrl(String thumbnailUrl) { this.thumbnailUrl = thumbnailUrl; }

    public Integer getDisplayOrder() { return displayOrder; }
    public void setDisplayOrder(Integer displayOrder) { this.displayOrder = displayOrder; }

    public String getAlt() { return alt; }
    public void setAlt(String alt) { this.alt = alt; }

    public Long getFileSize() { return fileSize; }
    public void setFileSize(Long fileSize) { this.fileSize = fileSize; }

    public String getCreatedAt() { return createdAt; }
    public void setCreatedAt(String createdAt) { this.createdAt = createdAt; }

    // Helper methods
    public String getFormattedFileSize() {
        if (fileSize == null) return "Unknown size";

        if (fileSize < 1024) return fileSize + " B";
        if (fileSize < 1024 * 1024) return String.format("%.1f KB", fileSize / 1024.0);
        return String.format("%.1f MB", fileSize / (1024.0 * 1024.0));
    }
}