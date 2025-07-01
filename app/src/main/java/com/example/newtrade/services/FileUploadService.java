// app/src/main/java/com/example/newtrade/services/FileUploadService.java
package com.example.newtrade.services;

import android.util.Log;
import com.example.newtrade.api.ApiClient;
import com.example.newtrade.api.FileUploadApiService;
import com.example.newtrade.models.StandardResponse;
import java.io.File;
import java.util.Map;
import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.RequestBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class FileUploadService {
    private static final String TAG = "FileUploadService";

    public interface UploadCallback {
        void onSuccess(String imageUrl);
        void onError(String error);
        void onProgress(int progress);
    }

    /**
     * Upload single product image
     */
    public static void uploadProductImage(File imageFile, UploadCallback callback) {
        try {
            if (!isValidImageFile(imageFile)) {
                callback.onError("Invalid image file");
                return;
            }

            RequestBody requestFile = RequestBody.create(
                    MediaType.parse("multipart/form-data"), imageFile);
            MultipartBody.Part body = MultipartBody.Part.createFormData(
                    "file", imageFile.getName(), requestFile);
            RequestBody description = RequestBody.create(
                    MediaType.parse("multipart/form-data"), "Product image");

            FileUploadApiService service = ApiClient.getFileUploadService();
            Call<StandardResponse<Map<String, String>>> call =
                    service.uploadProductImage(body, description);

            call.enqueue(new Callback<StandardResponse<Map<String, String>>>() {
                @Override
                public void onResponse(Call<StandardResponse<Map<String, String>>> call,
                                       Response<StandardResponse<Map<String, String>>> response) {
                    if (response.isSuccessful() && response.body() != null) {
                        StandardResponse<Map<String, String>> apiResponse = response.body();
                        if (apiResponse.isSuccess()) {
                            String imageUrl = apiResponse.getData().get("imageUrl");
                            Log.d(TAG, "✅ Product image uploaded: " + imageUrl);
                            callback.onSuccess(imageUrl);
                        } else {
                            callback.onError(apiResponse.getMessage());
                        }
                    } else {
                        callback.onError("Upload failed: " + response.message());
                    }
                }

                @Override
                public void onFailure(Call<StandardResponse<Map<String, String>>> call, Throwable t) {
                    Log.e(TAG, "❌ Upload failed", t);
                    callback.onError("Network error: " + t.getMessage());
                }
            });

        } catch (Exception e) {
            Log.e(TAG, "❌ Upload error", e);
            callback.onError("Upload error: " + e.getMessage());
        }
    }

    /**
     * Upload avatar image
     */
    public static void uploadAvatar(File imageFile, UploadCallback callback) {
        try {
            if (!isValidImageFile(imageFile)) {
                callback.onError("Invalid image file");
                return;
            }

            RequestBody requestFile = RequestBody.create(
                    MediaType.parse("multipart/form-data"), imageFile);
            MultipartBody.Part body = MultipartBody.Part.createFormData(
                    "file", imageFile.getName(), requestFile);
            RequestBody description = RequestBody.create(
                    MediaType.parse("multipart/form-data"), "Avatar image");

            FileUploadApiService service = ApiClient.getFileUploadService();
            Call<StandardResponse<Map<String, String>>> call =
                    service.uploadAvatar(body, description);

            call.enqueue(new Callback<StandardResponse<Map<String, String>>>() {
                @Override
                public void onResponse(Call<StandardResponse<Map<String, String>>> call,
                                       Response<StandardResponse<Map<String, String>>> response) {
                    if (response.isSuccessful() && response.body() != null) {
                        StandardResponse<Map<String, String>> apiResponse = response.body();
                        if (apiResponse.isSuccess()) {
                            String imageUrl = apiResponse.getData().get("imageUrl");
                            Log.d(TAG, "✅ Avatar uploaded: " + imageUrl);
                            callback.onSuccess(imageUrl);
                        } else {
                            callback.onError(apiResponse.getMessage());
                        }
                    } else {
                        callback.onError("Upload failed: " + response.message());
                    }
                }

                @Override
                public void onFailure(Call<StandardResponse<Map<String, String>>> call, Throwable t) {
                    Log.e(TAG, "❌ Avatar upload failed", t);
                    callback.onError("Network error: " + t.getMessage());
                }
            });

        } catch (Exception e) {
            Log.e(TAG, "❌ Avatar upload error", e);
            callback.onError("Upload error: " + e.getMessage());
        }
    }

    /**
     * Validate image file
     */
    private static boolean isValidImageFile(File file) {
        if (file == null || !file.exists()) {
            return false;
        }

        String fileName = file.getName().toLowerCase();
        return fileName.endsWith(".jpg") || fileName.endsWith(".jpeg") ||
                fileName.endsWith(".png") || fileName.endsWith(".gif") ||
                fileName.endsWith(".webp");
    }
}