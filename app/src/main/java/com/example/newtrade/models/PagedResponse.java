// app/src/main/java/com/example/newtrade/models/PagedResponse.java
package com.example.newtrade.models;

import java.util.List;

public class PagedResponse<T> {
    private List<T> content;
    private int page;
    private int size;
    private long totalElements;
    private int totalPages;
    private boolean first;
    private boolean last;

    // Constructors
    public PagedResponse() {}

    public PagedResponse(List<T> content, int page, int size, long totalElements,
                         int totalPages, boolean first, boolean last) {
        this.content = content;
        this.page = page;
        this.size = size;
        this.totalElements = totalElements;
        this.totalPages = totalPages;
        this.first = first;
        this.last = last;
    }

    // Helper methods
    public boolean hasNext() {
        return !last;
    }

    public boolean hasPrevious() {
        return !first;
    }

    public boolean isEmpty() {
        return content == null || content.isEmpty();
    }

    public int getContentSize() {
        return content != null ? content.size() : 0;
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

    @Override
    public String toString() {
        return "PagedResponse{" +
                "content=" + (content != null ? content.size() + " items" : "null") +
                ", page=" + page +
                ", size=" + size +
                ", totalElements=" + totalElements +
                ", totalPages=" + totalPages +
                ", first=" + first +
                ", last=" + last +
                '}';
    }
}