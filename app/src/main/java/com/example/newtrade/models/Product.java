// app/src/main/java/com/example/newtrade/models/Product.java
package com.example.newtrade.models;

import android.os.Parcel;
import android.os.Parcelable;

import com.example.newtrade.utils.PriceFormatter;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class Product implements Parcelable {

    // =============================================
    // ENUMS
    // =============================================
    public enum ProductCondition {
        NEW, LIKE_NEW, GOOD, FAIR, POOR
    }

    public enum ProductStatus {
        AVAILABLE, SOLD, PAUSED, EXPIRED, REPORTED
    }

    // =============================================
    // PROPERTIES
    // =============================================
    private Long id;
    private String title;
    private String description;
    private BigDecimal price;
    private ProductCondition condition;
    private String location;
    private ProductStatus status;
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

    // =============================================
    // CONSTRUCTORS
    // =============================================
    public Product() {
        this.imageUrls = new ArrayList<>();
        this.tags = new ArrayList<>();
        this.price = BigDecimal.ZERO;
        this.condition = ProductCondition.GOOD;
        this.status = ProductStatus.AVAILABLE;
    }

    protected Product(Parcel in) {
        if (in.readByte() == 0) {
            id = null;
        } else {
            id = in.readLong();
        }
        title = in.readString();
        description = in.readString();

        // Read BigDecimal price
        String priceStr = in.readString();
        if (priceStr != null) {
            price = new BigDecimal(priceStr);
        } else {
            price = BigDecimal.ZERO;
        }

        // Read enum condition
        String conditionStr = in.readString();
        if (conditionStr != null) {
            condition = ProductCondition.valueOf(conditionStr);
        } else {
            condition = ProductCondition.GOOD;
        }

        location = in.readString();

        // Read enum status
        String statusStr = in.readString();
        if (statusStr != null) {
            status = ProductStatus.valueOf(statusStr);
        } else {
            status = ProductStatus.AVAILABLE;
        }

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

    // =============================================
    // FACTORY METHODS
    // =============================================
    @SuppressWarnings("unchecked")
    public static Product fromMap(Map<String, Object> productData) {
        Product product = new Product();

        try {
            if (productData.get("id") != null) {
                product.id = ((Number) productData.get("id")).longValue();
            }

            product.title = (String) productData.get("title");
            product.description = (String) productData.get("description");

            // Handle price
            Object priceObj = productData.get("price");
            if (priceObj instanceof Number) {
                product.price = BigDecimal.valueOf(((Number) priceObj).doubleValue());
            } else if (priceObj instanceof String) {
                product.price = new BigDecimal((String) priceObj);
            }

            // Handle condition
            String conditionStr = (String) productData.get("condition");
            if (conditionStr != null) {
                try {
                    product.condition = ProductCondition.valueOf(conditionStr.toUpperCase());
                } catch (IllegalArgumentException e) {
                    product.condition = ProductCondition.GOOD;
                }
            }

            product.location = (String) productData.get("location");

            // Handle status
            String statusStr = (String) productData.get("status");
            if (statusStr != null) {
                try {
                    product.status = ProductStatus.valueOf(statusStr.toUpperCase());
                } catch (IllegalArgumentException e) {
                    product.status = ProductStatus.AVAILABLE;
                }
            }

            Boolean negotiable = (Boolean) productData.get("negotiable");
            if (negotiable != null) {
                product.isNegotiable = negotiable;
            }

            // Handle image URLs
            Object imageUrlsObj = productData.get("imageUrls");
            if (imageUrlsObj instanceof List) {
                product.imageUrls = (List<String>) imageUrlsObj;
            } else if (imageUrlsObj instanceof String) {
                product.imageUrls = new ArrayList<>();
                product.imageUrls.add((String) imageUrlsObj);
            }

            // Handle tags
            Object tagsObj = productData.get("tags");
            if (tagsObj instanceof List) {
                product.tags = (List<String>) tagsObj;
            }

            // Handle counts
            if (productData.get("viewCount") != null) {
                product.viewCount = ((Number) productData.get("viewCount")).intValue();
            }
            if (productData.get("offerCount") != null) {
                product.offerCount = ((Number) productData.get("offerCount")).intValue();
            }

            // Handle location coordinates
            if (productData.get("latitude") != null) {
                product.latitude = ((Number) productData.get("latitude")).doubleValue();
            }
            if (productData.get("longitude") != null) {
                product.longitude = ((Number) productData.get("longitude")).doubleValue();
            }

            product.createdAt = (String) productData.get("createdAt");
            product.updatedAt = (String) productData.get("updatedAt");

            // Handle seller information
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

            // Handle category information
            Map<String, Object> categoryData = (Map<String, Object>) productData.get("category");
            if (categoryData != null) {
                if (categoryData.get("id") != null) {
                    product.categoryId = ((Number) categoryData.get("id")).longValue();
                }
                product.categoryName = (String) categoryData.get("name");
            }

        } catch (Exception e) {
            // Handle parsing errors silently
        }

        return product;
    }

    // =============================================
    // GETTERS AND SETTERS
    // =============================================
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

    public BigDecimal getPrice() {
        return price;
    }

    public void setPrice(BigDecimal price) {
        this.price = price != null ? price : BigDecimal.ZERO;
    }

    // Compatibility method for double price
    public void setPrice(double price) {
        this.price = BigDecimal.valueOf(price);
    }

    public ProductCondition getCondition() {
        return condition;
    }

    public void setCondition(ProductCondition condition) {
        this.condition = condition != null ? condition : ProductCondition.GOOD;
    }

    // Compatibility method for string condition
    public void setCondition(String condition) {
        if (condition != null) {
            try {
                this.condition = ProductCondition.valueOf(condition.toUpperCase());
            } catch (IllegalArgumentException e) {
                this.condition = ProductCondition.GOOD;
            }
        }
    }

    public String getLocation() {
        return location;
    }

    public void setLocation(String location) {
        this.location = location;
    }

    public ProductStatus getStatus() {
        return status;
    }

    public void setStatus(ProductStatus status) {
        this.status = status != null ? status : ProductStatus.AVAILABLE;
    }

    // Compatibility method for string status
    public void setStatus(String status) {
        if (status != null) {
            try {
                this.status = ProductStatus.valueOf(status.toUpperCase());
            } catch (IllegalArgumentException e) {
                this.status = ProductStatus.AVAILABLE;
            }
        }
    }

    public boolean isNegotiable() {
        return isNegotiable;
    }

    public void setNegotiable(boolean negotiable) {
        isNegotiable = negotiable;
    }

    public List<String> getImageUrls() {
        return imageUrls != null ? imageUrls : new ArrayList<>();
    }

    public void setImageUrls(List<String> imageUrls) {
        this.imageUrls = imageUrls != null ? imageUrls : new ArrayList<>();
    }

    // Compatibility method for single image URL
    public void setImageUrl(String imageUrl) {
        if (this.imageUrls == null) {
            this.imageUrls = new ArrayList<>();
        }
        if (imageUrl != null) {
            this.imageUrls.clear();
            this.imageUrls.add(imageUrl);
        }
    }

    public String getPrimaryImageUrl() {
        if (imageUrls != null && !imageUrls.isEmpty()) {
            return imageUrls.get(0);
        }
        return null;
    }

    public void setPrimaryImageUrl(String primaryImageUrl) {
        if (this.imageUrls == null) {
            this.imageUrls = new ArrayList<>();
        }
        if (primaryImageUrl != null) {
            if (this.imageUrls.isEmpty()) {
                this.imageUrls.add(primaryImageUrl);
            } else {
                this.imageUrls.set(0, primaryImageUrl);
            }
        }
    }

    public List<String> getTags() {
        return tags != null ? tags : new ArrayList<>();
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
    public String getFormattedPrice() {
        if (price != null) {
            return PriceFormatter.format(price);
        }
        return "Free";
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

    // =============================================
    // PARCELABLE IMPLEMENTATION
    // =============================================
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
        dest.writeString(price != null ? price.toString() : null);
        dest.writeString(condition != null ? condition.name() : null);
        dest.writeString(location);
        dest.writeString(status != null ? status.name() : null);
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
                ", condition=" + condition +
                ", location='" + location + '\'' +
                ", status=" + status +
                ", isNegotiable=" + isNegotiable +
                ", viewCount=" + viewCount +
                ", sellerName='" + sellerName + '\'' +
                ", categoryName='" + categoryName + '\'' +
                '}';
    }
}