// app/src/main/java/com/example/newtrade/utils/PagedResponseHelper.java
package com.example.newtrade.utils;

import com.example.newtrade.models.PagedResponse;
import java.util.List;

public class PagedResponseHelper {

    /**
     * Tạo empty PagedResponse
     */
    public static <T> PagedResponse<T> createEmpty() {
        return new PagedResponse<>(null, 0, 0, 0, 0, true, true);
    }

    /**
     * Kiểm tra có thể load more không
     */
    public static <T> boolean canLoadMore(PagedResponse<T> pagedResponse) {
        return pagedResponse != null && pagedResponse.hasNext();
    }

    /**
     * Merge hai PagedResponse (dùng cho pagination)
     */
    public static <T> void mergeContent(PagedResponse<T> currentPage, PagedResponse<T> newPage) {
        if (currentPage == null || newPage == null) return;

        List<T> currentContent = currentPage.getContent();
        List<T> newContent = newPage.getContent();

        if (currentContent != null && newContent != null) {
            currentContent.addAll(newContent);
            currentPage.setContent(currentContent);

            // Update pagination info
            currentPage.setPage(newPage.getPage());
            currentPage.setLast(newPage.isLast());
            currentPage.setTotalElements(newPage.getTotalElements());
            currentPage.setTotalPages(newPage.getTotalPages());
        }
    }

    /**
     * Log pagination info for debugging
     */
    public static <T> void logPaginationInfo(String tag, PagedResponse<T> pagedResponse) {
        if (pagedResponse == null) {
            android.util.Log.d(tag, "PagedResponse is null");
            return;
        }

        android.util.Log.d(tag, "Pagination Info: " +
                "Page=" + pagedResponse.getPage() +
                ", Size=" + pagedResponse.getSize() +
                ", Total=" + pagedResponse.getTotalElements() +
                ", HasNext=" + pagedResponse.hasNext() +
                ", Content=" + (pagedResponse.hasContent() ? pagedResponse.getContent().size() : 0));
    }
}