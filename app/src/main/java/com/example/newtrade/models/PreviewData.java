// app/src/main/java/com/example/newtrade/models/PreviewData.java
package com.example.newtrade.models;

import android.net.Uri;
import android.os.Parcel;
import android.os.Parcelable;

import java.util.ArrayList;
import java.util.List;

public class PreviewData implements Parcelable {
    private String title;
    private String description;
    private String price;
    private String category;
    private String condition;
    private String location;
    private String tags;
    private boolean negotiable;
    private List<Uri> imageUris;

    public PreviewData() {
        imageUris = new ArrayList<>();
    }

    protected PreviewData(Parcel in) {
        title = in.readString();
        description = in.readString();
        price = in.readString();
        category = in.readString();
        condition = in.readString();
        location = in.readString();
        tags = in.readString();
        negotiable = in.readByte() != 0;
        imageUris = in.createTypedArrayList(Uri.CREATOR);
    }

    public static final Creator<PreviewData> CREATOR = new Creator<PreviewData>() {
        @Override
        public PreviewData createFromParcel(Parcel in) {
            return new PreviewData(in);
        }

        @Override
        public PreviewData[] newArray(int size) {
            return new PreviewData[size];
        }
    };

    // Getters and Setters
    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public String getPrice() { return price; }
    public void setPrice(String price) { this.price = price; }

    public String getCategory() { return category; }
    public void setCategory(String category) { this.category = category; }

    public String getCondition() { return condition; }
    public void setCondition(String condition) { this.condition = condition; }

    public String getLocation() { return location; }
    public void setLocation(String location) { this.location = location; }

    public String getTags() { return tags; }
    public void setTags(String tags) { this.tags = tags; }

    public boolean isNegotiable() { return negotiable; }
    public void setNegotiable(boolean negotiable) { this.negotiable = negotiable; }

    public List<Uri> getImageUris() { return imageUris; }
    public void setImageUris(List<Uri> imageUris) { this.imageUris = imageUris; }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(title);
        dest.writeString(description);
        dest.writeString(price);
        dest.writeString(category);
        dest.writeString(condition);
        dest.writeString(location);
        dest.writeString(tags);
        dest.writeByte((byte) (negotiable ? 1 : 0));
        dest.writeTypedList(imageUris);
    }
}