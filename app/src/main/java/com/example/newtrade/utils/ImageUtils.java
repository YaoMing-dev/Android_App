// app/src/main/java/com/example/newtrade/utils/ImageUtils.java
package com.example.newtrade.utils;

import android.content.Context;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.media.ExifInterface;
import android.net.Uri;
import android.os.Environment;
import android.provider.MediaStore;
import android.util.Log;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class ImageUtils {
    private static final String TAG = "ImageUtils";
    private static final int MAX_IMAGE_SIZE = 1024; // Max width/height in pixels
    private static final int JPEG_QUALITY = 85;

    /**
     * Create a File for saving an image
     */
    public static File createImageFile(Context context) throws IOException {
        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(new Date());
        String imageFileName = "JPEG_" + timeStamp + "_";
        File storageDir = context.getExternalFilesDir(Environment.DIRECTORY_PICTURES);

        if (storageDir != null && !storageDir.exists()) {
            storageDir.mkdirs();
        }

        return File.createTempFile(imageFileName, ".jpg", storageDir);
    }

    /**
     * Get real path from URI
     */
    public static String getRealPathFromUri(Context context, Uri uri) {
        String realPath = null;

        if (uri.getScheme().equals("content")) {
            String[] projection = {MediaStore.Images.Media.DATA};
            try (Cursor cursor = context.getContentResolver().query(uri, projection, null, null, null)) {
                if (cursor != null && cursor.moveToFirst()) {
                    int columnIndex = cursor.getColumnIndexOrThrow(MediaStore.Images.Media.DATA);
                    realPath = cursor.getString(columnIndex);
                }
            } catch (Exception e) {
                Log.e(TAG, "Error getting real path from URI", e);
            }
        } else if (uri.getScheme().equals("file")) {
            realPath = uri.getPath();
        }

        return realPath;
    }

    /**
     * Compress and resize image
     */
    public static String compressImage(String imagePath) {
        try {
            File originalFile = new File(imagePath);
            if (!originalFile.exists()) {
                Log.e(TAG, "Original file does not exist: " + imagePath);
                return imagePath;
            }

            // Decode image dimensions
            BitmapFactory.Options options = new BitmapFactory.Options();
            options.inJustDecodeBounds = true;
            BitmapFactory.decodeFile(imagePath, options);

            // Calculate sample size
            int sampleSize = calculateSampleSize(options.outWidth, options.outHeight);

            // Decode with sample size
            options.inJustDecodeBounds = false;
            options.inSampleSize = sampleSize;
            Bitmap bitmap = BitmapFactory.decodeFile(imagePath, options);

            if (bitmap == null) {
                Log.e(TAG, "Failed to decode bitmap from: " + imagePath);
                return imagePath;
            }

            // Handle rotation
            bitmap = rotateImageIfRequired(bitmap, imagePath);

            // Resize if still too large
            bitmap = resizeBitmap(bitmap, MAX_IMAGE_SIZE);

            // Save compressed image
            String compressedPath = imagePath.replace(".jpg", "_compressed.jpg");
            File compressedFile = new File(compressedPath);

            try (FileOutputStream out = new FileOutputStream(compressedFile)) {
                bitmap.compress(Bitmap.CompressFormat.JPEG, JPEG_QUALITY, out);
            }

            bitmap.recycle();

            Log.d(TAG, "Image compressed: " + originalFile.length() + " -> " + compressedFile.length());
            return compressedPath;

        } catch (Exception e) {
            Log.e(TAG, "Error compressing image", e);
            return imagePath;
        }
    }

    private static int calculateSampleSize(int width, int height) {
        int sampleSize = 1;

        if (height > MAX_IMAGE_SIZE || width > MAX_IMAGE_SIZE) {
            final int halfHeight = height / 2;
            final int halfWidth = width / 2;

            while ((halfHeight / sampleSize) >= MAX_IMAGE_SIZE
                    && (halfWidth / sampleSize) >= MAX_IMAGE_SIZE) {
                sampleSize *= 2;
            }
        }

        return sampleSize;
    }

    private static Bitmap rotateImageIfRequired(Bitmap bitmap, String imagePath) {
        try {
            ExifInterface exif = new ExifInterface(imagePath);
            int orientation = exif.getAttributeInt(ExifInterface.TAG_ORIENTATION, ExifInterface.ORIENTATION_NORMAL);

            switch (orientation) {
                case ExifInterface.ORIENTATION_ROTATE_90:
                    return rotateBitmap(bitmap, 90);
                case ExifInterface.ORIENTATION_ROTATE_180:
                    return rotateBitmap(bitmap, 180);
                case ExifInterface.ORIENTATION_ROTATE_270:
                    return rotateBitmap(bitmap, 270);
                default:
                    return bitmap;
            }
        } catch (IOException e) {
            Log.e(TAG, "Error reading EXIF data", e);
            return bitmap;
        }
    }

    private static Bitmap rotateBitmap(Bitmap bitmap, float degrees) {
        Matrix matrix = new Matrix();
        matrix.postRotate(degrees);
        return Bitmap.createBitmap(bitmap, 0, 0, bitmap.getWidth(), bitmap.getHeight(), matrix, true);
    }

    private static Bitmap resizeBitmap(Bitmap bitmap, int maxSize) {
        int width = bitmap.getWidth();
        int height = bitmap.getHeight();

        if (width <= maxSize && height <= maxSize) {
            return bitmap;
        }

        float ratio = Math.min((float) maxSize / width, (float) maxSize / height);
        int newWidth = Math.round(width * ratio);
        int newHeight = Math.round(height * ratio);

        return Bitmap.createScaledBitmap(bitmap, newWidth, newHeight, true);
    }

    /**
     * Delete file
     */
    public static boolean deleteFile(String filePath) {
        try {
            File file = new File(filePath);
            return file.delete();
        } catch (Exception e) {
            Log.e(TAG, "Error deleting file: " + filePath, e);
            return false;
        }
    }

    /**
     * Get file size in MB
     */
    public static double getFileSizeMB(String filePath) {
        try {
            File file = new File(filePath);
            return file.length() / (1024.0 * 1024.0);
        } catch (Exception e) {
            Log.e(TAG, "Error getting file size: " + filePath, e);
            return 0;
        }
    }
}