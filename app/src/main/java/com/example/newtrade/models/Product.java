// app/src/main/java/com/example/newtrade/models/Product.java
package com.example.newtrade.models;

import android.os.Parcel;
import android.os.Parcelable;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public class Product implements Parcelable {
    private Long id;
    private String title;
    private String description;
    private double price;
    private String condition;
    private String location;
    private String status;
    private boolean isNegotiable;
    private List<String> imageUrls;
    private List<String> tags;
    private int viewCount;
    private int offerCount;
    private double latitude;
    private double longitude;
    private String createdAt;
    private String updatedAt;

    // Seller information
    private Long sellerId;
    private String sellerName;
    private String sellerAvatar;
    private double sellerRating;

    // Category information
    private Long categoryId;
    private String categoryName;

    // UI state
    private boolean isSaved;
    private boolean isOwn;

    // Constructors
    public Product() {
        this.imageUrls = new ArrayList<>();
        this.tags = new ArrayList<>();
    }

    public Product(Long id, String title, String description, double price, String condition,
                   String location, String status, boolean isNegotiable) {
        this();
        this.id = id;
        this.title = title;
        this.description = description;
        this.price = price;
        this.condition = condition;
        this.location = location;
        this.status = status;
        this.isNegotiable = isNegotiable;
    }

    protected Product(Parcel in) {
        if (in.readByte() == 0) {
            id = null;
        } else {
            id = in.readLong();
        }
        title = in.readString();
        description = in.readString();
        price = in.readDouble();
        condition = in.readString();
        location = in.readString();
        status = in.readString();
        isNegotiable = in.readByte() != 0;
        imageUrls = in.createStringArrayList();
        tags = in.createStringArrayList();
        viewCount = in.readInt();
        offerCount = in.readInt();
        latitude = in.readDouble();
        longitude = in.readDouble();
        createdAt = in.readString();
        updatedAt = in.readString();
        if (in.readByte() == 0) {
            sellerId = null;
        } else {
            sellerId = in.readLong();
        }
        sellerName = in.readString();
        sellerAvatar = in.readString();
        sellerRating = in.readDouble();
        if (in.readByte() == 0) {
            categoryId = null;
        } else {
            categoryId = in.readLong();
        }
        categoryName = in.readString();
        isSaved = in.readByte() != 0;
        isOwn = in.readByte() != 0;
    }

    public static final Creator<Product> CREATOR = new Creator<Product>() {
        @Override
        public Product createFromParcel(Parcel in) {
            return new Product(in);
        }

        @Override
        public Product[] newArray(int size) {
            return new Product[size];
        }
    };

    // Factory method from Map
    @SuppressWarnings("unchecked")
    public static Product fromMap(Map<String, Object> productData) {
        Product product = new Product();

        try {
            if (productData.get("id") != null) {
                product.id = ((Number) productData.get("id")).longValue();
            }
            product.title = (String) productData.get("title");
            product.description = (String) productData.get("description");
            if (productData.get("price") != null) {
                product.price = ((Number) productData.get("price")).doubleValue();
            }
            product.condition = (String) productData.get("condition");
            product.location = (String) productData.get("location");
            product.status = (String) productData.get("status");
            product.isNegotiable = Boolean.TRUE.equals(productData.get("isNegotiable"));

            // Handle image URLs
            Object imageUrlsObj = productData.get("imageUrls");
            if (imageUrlsObj instanceof List) {
                product.imageUrls = new ArrayList<>((List<String>) imageUrlsObj);
            }

            // Handle tags
            Object tagsObj = productData.get("tags");
            if (tagsObj instanceof List) {
                product.tags = new ArrayList<>((List<String>) tagsObj);
            }

            if (productData.get("viewCount") != null) {
                product.viewCount = ((Number) productData.get("viewCount")).intValue();
            }
            if (productData.get("offerCount") != null) {
                product.offerCount = ((Number) productData.get("offerCount")).intValue();
            }
            if (productData.get("latitude") != null) {
                product.latitude = ((Number) productData.get("latitude")).doubleValue();
            }
            if (productData.get("longitude") != null) {
                product.longitude = ((Number) productData.get("longitude")).doubleValue();
            }
            product.createdAt = (String) productData.get("createdAt");
            product.updatedAt = (String) productData.get("updatedAt");

            // Seller information
            Map<String, Object> sellerData = (Map<String, Object>) productData.get("seller");
            if (sellerData != null) {
                if (sellerData.get("id") != null) {
                    product.sellerId = ((Number) sellerData.get("id")).longValue();
                }
                product.sellerName = (String) sellerData.get("displayName");
                product.sellerAvatar = (String) sellerData.get("profilePicture");
                if (sellerData.get("rating") != null) {
                    product.sellerRating = ((Number) sellerData.get("rating")).doubleValue();
                }
            }

            // Category information
            Map<String, Object> categoryData = (Map<String, Object>) productData.get("category");
            if (categoryData != null) {
                if (categoryData.get("id") != null) {
                    product.categoryId = ((Number) categoryData.get("id")).longValue();
                }
                product.categoryName = (String) categoryData.get("name");
            }

        } catch (Exception e) {
            // Handle parsing errors
        }

        return product;
    }

    // Getters and Setters
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public double getPrice() {
        return price;
    }

    public void setPrice(double price) {
        this.price = price;
    }

    public String getCondition() {
        return condition;
    }

    public void setCondition(String condition) {
        this.condition = condition;
    }

    public String getLocation() {
        return location;
    }

    public void setLocation(String location) {
        this.location = location;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public boolean isNegotiable() {
        return isNegotiable;
    }

    public void setNegotiable(boolean negotiable) {
        isNegotiable = negotiable;
    }

    public List<String> getImageUrls() {
        return imageUrls;
    }

    public void setImageUrls(List<String> imageUrls) {
        this.imageUrls = imageUrls != null ? imageUrls : new ArrayList<>();
    }

    public List<String> getTags() {
        return tags;
    }

    public void setTags(List<String> tags) {
        this.tags = tags != null ? tags : new ArrayList<>();
    }

    public int getViewCount() {
        return viewCount;
    }

    public void setViewCount(int viewCount) {
        this.viewCount = viewCount;
    }

    public int getOfferCount() {
        return offerCount;
    }

    public void setOfferCount(int offerCount) {
        this.offerCount = offerCount;
    }

    public double getLatitude() {
        return latitude;
    }

    public void setLatitude(double latitude) {
        this.latitude = latitude;
    }

    public double getLongitude() {
        return longitude;
    }

    public void setLongitude(double longitude) {
        this.longitude = longitude;
    }

    public String getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(String createdAt) {
        this.createdAt = createdAt;
    }

    public String getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(String updatedAt) {
        this.updatedAt = updatedAt;
    }

    public Long getSellerId() {
        return sellerId;
    }

    public void setSellerId(Long sellerId) {
        this.sellerId = sellerId;
    }

    public String getSellerName() {
        return sellerName;
    }

    public void setSellerName(String sellerName) {
        this.sellerName = sellerName;
    }

    public String getSellerAvatar() {
        return sellerAvatar;
    }

    public void setSellerAvatar(String sellerAvatar) {
        this.sellerAvatar = sellerAvatar;
    }

    public double getSellerRating() {
        return sellerRating;
    }

    public void setSellerRating(double sellerRating) {
        this.sellerRating = sellerRating;
    }

    public Long getCategoryId() {
        return categoryId;
    }

    public void setCategoryId(Long categoryId) {
        this.categoryId = categoryId;
    }

    public String getCategoryName() {
        return categoryName;
    }

    public void setCategoryName(String categoryName) {
        this.categoryName = categoryName;
    }

    public boolean isSaved() {
        return isSaved;
    }

    public void setSaved(boolean saved) {
        isSaved = saved;
    }

    public boolean isOwn() {
        return isOwn;
    }

    public void setOwn(boolean own) {
        isOwn = own;
    }

    // Utility methods
    public String getFormattedPrice() {
        return String.format(Locale.getDefault(), "$%.2f", price);
    }

    public String getConditionDisplay() {
        if (condition == null) return "Unknown";

        switch (condition.toLowerCase()) {
            case "new":
                return "New";
            case "like_new":
                return "Like New";
            case "good":
                return "Good";
            case "fair":
                return "Fair";
            case "poor":
                return "Poor";
            default:
                return condition;
        }
    }

    public String getStatusDisplay() {
        if (status == null) return "Unknown";

        switch (status.toLowerCase()) {
            case "available":
                return "Available";
            case "sold":
                return "Sold";
            case "paused":
                return "Paused";
            case "expired":
                return "Expired";
            default:
                return status;
        }
    }

    public String getMainImageUrl() {
        if (imageUrls != null && !imageUrls.isEmpty()) {
            return imageUrls.get(0);
        }
        return null;
    }

    public String getFormattedCreatedAt() {
        if (createdAt == null) return "";

        try {
            SimpleDateFormat inputFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss", Locale.getDefault());
            SimpleDateFormat outputFormat = new SimpleDateFormat("MMM d, yyyy", Locale.getDefault());
            Date date = inputFormat.parse(createdAt);
            return date != null ? outputFormat.format(date) : createdAt;
        } catch (Exception e) {
            return createdAt;
        }
    }

    public String getTimeAgo() {
        if (createdAt == null) return "";

        try {
            SimpleDateFormat inputFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss", Locale.getDefault());
            Date date = inputFormat.parse(createdAt);
            if (date != null) {
                long diff = System.currentTimeMillis() - date.getTime();
                long seconds = diff / 1000;
                long minutes = seconds / 60;
                long hours = minutes / 60;
                long days = hours / 24;

                if (days > 0) {
                    return days + (days == 1 ? " day ago" : " days ago");
                } else if (hours > 0) {
                    return hours + (hours == 1 ? " hour ago" : " hours ago");
                } else if (minutes > 0) {
                    return minutes + (minutes == 1 ? " minute ago" : " minutes ago");
                } else {
                    return "Just now";
                }
            }
        } catch (Exception e) {
            return createdAt;
        }

        return "";
    }

    public boolean isAvailable() {
        return "AVAILABLE".equalsIgnoreCase(status);
    }

    public boolean isSold() {
        return "SOLD".equalsIgnoreCase(status);
    }

    public boolean isPaused() {
        return "PAUSED".equalsIgnoreCase(status);
    }

    public boolean hasLocation() {
        return latitude != 0.0 && longitude != 0.0;
    }

    public boolean hasImages() {
        return imageUrls != null && !imageUrls.isEmpty();
    }

    public boolean hasTags() {
        return tags != null && !tags.isEmpty();
    }

    public String getTagsString() {
        if (tags == null || tags.isEmpty()) return "";
        return String.join(", ", tags);
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
        dest.writeString(title);
        dest.writeString(description);
        dest.writeDouble(price);
        dest.writeString(condition);
        dest.writeString(location);
        dest.writeString(status);
        dest.writeByte((byte) (isNegotiable ? 1 : 0));
        dest.writeStringList(imageUrls);
        dest.writeStringList(tags);
        dest.writeInt(viewCount);
        dest.writeInt(offerCount);
        dest.writeDouble(latitude);
        dest.writeDouble(longitude);
        dest.writeString(createdAt);
        dest.writeString(updatedAt);
        if (sellerId == null) {
            dest.writeByte((byte) 0);
        } else {
            dest.writeByte((byte) 1);
            dest.writeLong(sellerId);
        }
        dest.writeString(sellerName);
        dest.writeString(sellerAvatar);
        dest.writeDouble(sellerRating);
        if (categoryId == null) {
            dest.writeByte((byte) 0);
        } else {
            dest.writeByte((byte) 1);
            dest.writeLong(categoryId);
        }
        dest.writeString(categoryName);
        dest.writeByte((byte) (isSaved ? 1 : 0));
        dest.writeByte((byte) (isOwn ? 1 : 0));
    }

    @Override
    public String toString() {
        return "Product{" +
                "id=" + id +
                ", title='" + title + '\'' +
                ", price=" + price +
                ", condition='" + condition + '\'' +
                ", location='" + location + '\'' +
                ", status='" + status + '\'' +
                ", isNegotiable=" + isNegotiable +
                ", viewCount=" + viewCount +
                ", sellerName='" + sellerName + '\'' +
                ", categoryName='" + categoryName + '\'' +
                '}';
    }
}