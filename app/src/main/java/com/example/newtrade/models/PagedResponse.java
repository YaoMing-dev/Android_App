// app/src/main/java/com/example/newtrade/models/PagedResponse.java
package com.example.newtrade.models;

import com.google.gson.annotations.SerializedName;
import java.util.List;

public class PagedResponse<T> {

    @SerializedName("content")
    private List<T> content;

    @SerializedName("page")
    private int page;

    @SerializedName("size")
    private int size;

    @SerializedName("totalElements")
    private long totalElements;

    @SerializedName("totalPages")
    private int totalPages;

    @SerializedName("first")
    private boolean first;

    @SerializedName("last")
    private boolean last;

    // Constructors
    public PagedResponse() {}

    public PagedResponse(List<T> content, int page, int size, long totalElements, int totalPages, boolean first, boolean last) {
        this.content = content;
        this.page = page;
        this.size = size;
        this.totalElements = totalElements;
        this.totalPages = totalPages;
        this.first = first;
        this.last = last;
    }

    // Getters and Setters
    public List<T> getContent() { return content; }
    public void setContent(List<T> content) { this.content = content; }

    public int getPage() { return page; }
    public void setPage(int page) { this.page = page; }

    public int getSize() { return size; }
    public void setSize(int size) { this.size = size; }

    public long getTotalElements() { return totalElements; }
    public void setTotalElements(long totalElements) { this.totalElements = totalElements; }

    public int getTotalPages() { return totalPages; }
    public void setTotalPages(int totalPages) { this.totalPages = totalPages; }

    public boolean isFirst() { return first; }
    public void setFirst(boolean first) { this.first = first; }

    public boolean isLast() { return last; }
    public void setLast(boolean last) { this.last = last; }

    // Utility methods
    public boolean hasContent() {
        return content != null && !content.isEmpty();
    }

    public boolean hasNext() {
        return !last;
    }

    public boolean hasPrevious() {
        return !first;
    }

    public int getNextPage() {
        return hasNext() ? page + 1 : page;
    }

    public int getPreviousPage() {
        return hasPrevious() ? page - 1 : page;
    }

    @Override
    public String toString() {
        return "PagedResponse{" +
                "page=" + page +
                ", size=" + size +
                ", totalElements=" + totalElements +
                ", totalPages=" + totalPages +
                ", contentSize=" + (content != null ? content.size() : 0) +
                '}';
    }
}