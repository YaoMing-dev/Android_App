// app/src/main/java/com/example/newtrade/models/StandardResponse.java
package com.example.newtrade.models;

public class StandardResponse<T> {
    private boolean success;
    private String message;
    private T data;
    private long timestamp;

    // Constructors
    public StandardResponse() {}

    public StandardResponse(boolean success, String message, T data) {
        this.success = success;
        this.message = message;
        this.data = data;
        this.timestamp = System.currentTimeMillis();
    }

    // Static factory methods
    public static <T> StandardResponse<T> success(T data, String message) {
        return new StandardResponse<>(true, message, data);
    }

    public static <T> StandardResponse<T> error(String message) {
        return new StandardResponse<>(false, message, null);
    }

    // Getters and Setters
    public boolean isSuccess() { return success; }
    public void setSuccess(boolean success) { this.success = success; }

    public String getMessage() { return message; }
    public void setMessage(String message) { this.message = message; }

    public T getData() { return data; }
    public void setData(T data) { this.data = data; }

    public long getTimestamp() { return timestamp; }
    public void setTimestamp(long timestamp) { this.timestamp = timestamp; }

    @Override
    public String toString() {
        return "StandardResponse{" +
                "success=" + success +
                ", message='" + message + '\'' +
                ", data=" + data +
                ", timestamp=" + timestamp +
                '}';
    }
}