// app/src/main/java/com/example/newtrade/utils/NotificationResponseHelper.java
package com.example.newtrade.utils;

import com.example.newtrade.models.NotificationResponse;
import com.example.newtrade.models.PagedResponse;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class NotificationResponseHelper {

    private static final String TAG = "NotificationHelper";

    /**
     * Convert Map response to PagedResponse<NotificationResponse>
     */
    @SuppressWarnings("unchecked")
    public static PagedResponse<NotificationResponse> parsePagedResponse(Map<String, Object> responseMap) {
        if (responseMap == null) {
            return new PagedResponse<>();
        }

        try {
            // Parse content list
            List<NotificationResponse> notifications = new ArrayList<>();
            List<Map<String, Object>> contentList = (List<Map<String, Object>>) responseMap.get("content");

            if (contentList != null) {
                for (Map<String, Object> notificationMap : contentList) {
                    NotificationResponse notification = parseNotificationResponse(notificationMap);
                    if (notification != null) {
                        notifications.add(notification);
                    }
                }
            }

            // Parse pagination info
            Integer page = getIntegerValue(responseMap, "page", 0);
            Integer size = getIntegerValue(responseMap, "size", 20);
            Long totalElements = getLongValue(responseMap, "totalElements", 0L);
            Integer totalPages = getIntegerValue(responseMap, "totalPages", 0);
            Boolean first = getBooleanValue(responseMap, "first", true);
            Boolean last = getBooleanValue(responseMap, "last", true);

            return new PagedResponse<>(notifications, page, size, totalElements, totalPages, first, last);

        } catch (Exception e) {
            android.util.Log.e(TAG, "Error parsing PagedResponse", e);
            return new PagedResponse<>();
        }
    }

    /**
     * Convert single Map to NotificationResponse
     */
    @SuppressWarnings("unchecked")
    public static NotificationResponse parseNotificationResponse(Map<String, Object> notificationMap) {
        if (notificationMap == null) {
            return null;
        }

        try {
            NotificationResponse notification = new NotificationResponse();

            // Parse basic fields
            notification.setId(getLongValue(notificationMap, "id", null));
            notification.setTitle(getStringValue(notificationMap, "title", ""));
            notification.setMessage(getStringValue(notificationMap, "message", ""));
            notification.setReferenceId(getLongValue(notificationMap, "referenceId", null));
            notification.setIsRead(getBooleanValue(notificationMap, "isRead", false));
            notification.setCreatedAt(getStringValue(notificationMap, "createdAt", ""));

            // Parse notification type
            String typeStr = getStringValue(notificationMap, "type", "GENERAL");
            notification.setType(parseNotificationType(typeStr));

            return notification;

        } catch (Exception e) {
            android.util.Log.e(TAG, "Error parsing NotificationResponse", e);
            return null;
        }
    }

    /**
     * Parse notification type from string
     */
    private static NotificationResponse.NotificationType parseNotificationType(String typeStr) {
        if (typeStr == null || typeStr.isEmpty()) {
            return NotificationResponse.NotificationType.GENERAL;
        }

        try {
            return NotificationResponse.NotificationType.valueOf(typeStr.toUpperCase());
        } catch (IllegalArgumentException e) {
            android.util.Log.w(TAG, "Unknown notification type: " + typeStr);
            return NotificationResponse.NotificationType.GENERAL;
        }
    }

    /**
     * Parse unread count from Map response
     */
    public static long parseUnreadCount(Map<String, Object> responseMap) {
        if (responseMap == null) {
            return 0L;
        }

        // Try different possible keys
        Object unreadCount = responseMap.get("unreadCount");
        if (unreadCount == null) {
            unreadCount = responseMap.get("count");
        }
        if (unreadCount == null) {
            unreadCount = responseMap.get("total");
        }

        return getLongValue(responseMap, "unreadCount", 0L);
    }

    // ===== UTILITY METHODS =====

    private static String getStringValue(Map<String, Object> map, String key, String defaultValue) {
        Object value = map.get(key);
        return value instanceof String ? (String) value : defaultValue;
    }

    private static Integer getIntegerValue(Map<String, Object> map, String key, Integer defaultValue) {
        Object value = map.get(key);
        if (value instanceof Integer) {
            return (Integer) value;
        } else if (value instanceof Number) {
            return ((Number) value).intValue();
        }
        return defaultValue;
    }

    private static Long getLongValue(Map<String, Object> map, String key, Long defaultValue) {
        Object value = map.get(key);
        if (value instanceof Long) {
            return (Long) value;
        } else if (value instanceof Number) {
            return ((Number) value).longValue();
        }
        return defaultValue;
    }

    private static Boolean getBooleanValue(Map<String, Object> map, String key, Boolean defaultValue) {
        Object value = map.get(key);
        return value instanceof Boolean ? (Boolean) value : defaultValue;
    }
}