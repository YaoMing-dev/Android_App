// app/src/main/java/com/example/newtrade/models/Category.java
package com.example.newtrade.models;

import android.os.Parcel;
import android.os.Parcelable;

import java.util.Map;

public class Category implements Parcelable {
    private Long id;
    private String name;
    private String description;
    private String icon;
    private int productCount;
    private String createdAt;

    // Constructors
    public Category() {}

    public Category(Long id, String name, String description, String icon, int productCount, String createdAt) {
        this.id = id;
        this.name = name;
        this.description = description;
        this.icon = icon;
        this.productCount = productCount;
        this.createdAt = createdAt;
    }

    protected Category(Parcel in) {
        if (in.readByte() == 0) {
            id = null;
        } else {
            id = in.readLong();
        }
        name = in.readString();
        description = in.readString();
        icon = in.readString();
        productCount = in.readInt();
        createdAt = in.readString();
    }

    public static final Creator<Category> CREATOR = new Creator<Category>() {
        @Override
        public Category createFromParcel(Parcel in) {
            return new Category(in);
        }

        @Override
        public Category[] newArray(int size) {
            return new Category[size];
        }
    };

    // Factory method from Map
    public static Category fromMap(Map<String, Object> categoryData) {
        Category category = new Category();

        try {
            if (categoryData.get("id") != null) {
                category.id = ((Number) categoryData.get("id")).longValue();
            }
            category.name = (String) categoryData.get("name");
            category.description = (String) categoryData.get("description");
            category.icon = (String) categoryData.get("icon");
            if (categoryData.get("productCount") != null) {
                category.productCount = ((Number) categoryData.get("productCount")).intValue();
            }
            category.createdAt = (String) categoryData.get("createdAt");
        } catch (Exception e) {
            // Handle parsing errors
        }

        return category;
    }

    // Getters and Setters
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

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getIcon() {
        return icon;
    }

    public void setIcon(String icon) {
        this.icon = icon;
    }

    public int getProductCount() {
        return productCount;
    }

    public void setProductCount(int productCount) {
        this.productCount = productCount;
    }

    public String getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(String createdAt) {
        this.createdAt = createdAt;
    }

    // Utility methods
    public String getDisplayName() {
        return name != null ? name : "Unknown Category";
    }

    public String getIconResourceName() {
        if (icon != null) {
            return "ic_category_" + icon.toLowerCase();
        }
        return "ic_category_default";
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        if (id == null) {
            dest.writeByte((byte) 0);
        } else {
            dest.writeByte((byte) 1);
            dest.writeLong(id);
        }
        dest.writeString(name);
        dest.writeString(description);
        dest.writeString(icon);
        dest.writeInt(productCount);
        dest.writeString(createdAt);
    }

    @Override
    public String toString() {
        return "Category{" +
                "id=" + id +
                ", name='" + name + '\'' +
                ", description='" + description + '\'' +
                ", icon='" + icon + '\'' +
                ", productCount=" + productCount +
                ", createdAt='" + createdAt + '\'' +
                '}';
    }
}