// app/src/main/java/com/example/newtrade/utils/ImageUtils.java
package com.example.newtrade.utils;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.media.ExifInterface;
import android.net.Uri;
import android.util.Log;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;

public class ImageUtils {
    private static final String TAG = "ImageUtils";
    private static final int MAX_IMAGE_SIZE = 1920;
    private static final int JPEG_QUALITY = 85;
    public static String generateImageFileName() {
        return "IMG_" + System.currentTimeMillis() + ".jpg";
    }
    public static String generateImageFileName(String extension) {
        if (extension == null || !extension.startsWith(".")) {
            extension = ".jpg";
        }
        return "IMG_" + System.currentTimeMillis() + extension;
    }

    public static String compressImage(Context context, Uri imageUri, String outputPath) {
        try {
            // Get input stream from URI
            InputStream inputStream = context.getContentResolver().openInputStream(imageUri);
            if (inputStream == null) {
                Log.e(TAG, "Failed to open input stream");
                return null;
            }

            // Decode bitmap
            Bitmap originalBitmap = BitmapFactory.decodeStream(inputStream);
            inputStream.close();

            if (originalBitmap == null) {
                Log.e(TAG, "Failed to decode bitmap");
                return null;
            }

            // Get image orientation
            int orientation = getImageOrientation(context, imageUri);

            // Rotate if needed
            Bitmap rotatedBitmap = rotateImageIfRequired(originalBitmap, orientation);

            // Compress and resize
            Bitmap compressedBitmap = resizeImage(rotatedBitmap, MAX_IMAGE_SIZE);

            // Save to file
            String savedPath = saveBitmapToFile(compressedBitmap, outputPath);

            // Clean up
            if (originalBitmap != rotatedBitmap) {
                originalBitmap.recycle();
            }
            if (rotatedBitmap != compressedBitmap) {
                rotatedBitmap.recycle();
            }
            compressedBitmap.recycle();

            return savedPath;

        } catch (Exception e) {
            Log.e(TAG, "Error compressing image", e);
            return null;
        }
    }

    private static int getImageOrientation(Context context, Uri imageUri) {
        try {
            InputStream inputStream = context.getContentResolver().openInputStream(imageUri);
            if (inputStream != null) {
                ExifInterface exif = new ExifInterface(inputStream);
                inputStream.close();
                return exif.getAttributeInt(ExifInterface.TAG_ORIENTATION, ExifInterface.ORIENTATION_NORMAL);
            }
        } catch (IOException e) {
            Log.e(TAG, "Error reading EXIF data", e);
        }
        return ExifInterface.ORIENTATION_NORMAL;
    }

    private static Bitmap rotateImageIfRequired(Bitmap bitmap, int orientation) {
        switch (orientation) {
            case ExifInterface.ORIENTATION_ROTATE_90:
                return rotateImage(bitmap, 90);
            case ExifInterface.ORIENTATION_ROTATE_180:
                return rotateImage(bitmap, 180);
            case ExifInterface.ORIENTATION_ROTATE_270:
                return rotateImage(bitmap, 270);
            default:
                return bitmap;
        }
    }

    private static Bitmap rotateImage(Bitmap bitmap, float angle) {
        Matrix matrix = new Matrix();
        matrix.postRotate(angle);
        return Bitmap.createBitmap(bitmap, 0, 0, bitmap.getWidth(), bitmap.getHeight(), matrix, true);
    }

    private static Bitmap resizeImage(Bitmap bitmap, int maxSize) {
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

    private static String saveBitmapToFile(Bitmap bitmap, String outputPath) throws IOException {
        File outputFile = new File(outputPath);
        File parentDir = outputFile.getParentFile();
        if (parentDir != null && !parentDir.exists()) {
            parentDir.mkdirs();
        }

        FileOutputStream out = new FileOutputStream(outputFile);
        bitmap.compress(Bitmap.CompressFormat.JPEG, JPEG_QUALITY, out);
        out.flush();
        out.close();

        return outputPath;
    }

    public static byte[] bitmapToByteArray(Bitmap bitmap) {
        ByteArrayOutputStream stream = new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.JPEG, JPEG_QUALITY, stream);
        return stream.toByteArray();
    }
}