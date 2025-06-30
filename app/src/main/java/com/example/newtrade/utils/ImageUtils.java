// app/src/main/java/com/example/newtrade/utils/ImageUtils.java
package com.example.newtrade.utils;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.net.Uri;
import android.util.Log;
import androidx.exifinterface.media.ExifInterface;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;

public class ImageUtils {
    private static final String TAG = "ImageUtils";
    private static final int MAX_IMAGE_SIZE = 1024;
    private static final int JPEG_QUALITY = 80;

    // FR-2.1.4: Image upload supports JPEG/PNG
    public static String compressImage(Context context, Uri imageUri, String fileName) {
        try {
            InputStream inputStream = context.getContentResolver().openInputStream(imageUri);
            Bitmap bitmap = BitmapFactory.decodeStream(inputStream);

            if (bitmap == null) {
                Log.e(TAG, "Failed to decode bitmap from URI");
                return null;
            }

            // Handle image orientation
            Bitmap rotatedBitmap = handleImageOrientation(context, imageUri, bitmap);

            // Resize if too large
            Bitmap resizedBitmap = resizeImage(rotatedBitmap, MAX_IMAGE_SIZE);

            // Save compressed image
            File outputDir = new File(context.getCacheDir(), "temp_images");
            if (!outputDir.exists()) {
                outputDir.mkdirs();
            }

            File outputFile = new File(outputDir, fileName);
            FileOutputStream fos = new FileOutputStream(outputFile);

            resizedBitmap.compress(Bitmap.CompressFormat.JPEG, JPEG_QUALITY, fos);
            fos.close();

            // Clean up bitmaps
            if (bitmap != rotatedBitmap) {
                bitmap.recycle();
            }
            if (rotatedBitmap != resizedBitmap) {
                rotatedBitmap.recycle();
            }
            resizedBitmap.recycle();

            return outputFile.getAbsolutePath();

        } catch (IOException e) {
            Log.e(TAG, "Error compressing image", e);
            return null;
        }
    }

    private static Bitmap handleImageOrientation(Context context, Uri imageUri, Bitmap bitmap) {
        try {
            InputStream inputStream = context.getContentResolver().openInputStream(imageUri);
            ExifInterface exif = new ExifInterface(inputStream);
            int orientation = exif.getAttributeInt(ExifInterface.TAG_ORIENTATION, ExifInterface.ORIENTATION_NORMAL);

            Matrix matrix = new Matrix();
            switch (orientation) {
                case ExifInterface.ORIENTATION_ROTATE_90:
                    matrix.postRotate(90);
                    break;
                case ExifInterface.ORIENTATION_ROTATE_180:
                    matrix.postRotate(180);
                    break;
                case ExifInterface.ORIENTATION_ROTATE_270:
                    matrix.postRotate(270);
                    break;
                default:
                    return bitmap;
            }

            return Bitmap.createBitmap(bitmap, 0, 0, bitmap.getWidth(), bitmap.getHeight(), matrix, true);

        } catch (IOException e) {
            Log.e(TAG, "Error handling image orientation", e);
            return bitmap;
        }
    }

    private static Bitmap resizeImage(Bitmap bitmap, int maxSize) {
        int width = bitmap.getWidth();
        int height = bitmap.getHeight();

        if (width <= maxSize && height <= maxSize) {
            return bitmap;
        }

        float scale = Math.min((float) maxSize / width, (float) maxSize / height);
        int newWidth = Math.round(width * scale);
        int newHeight = Math.round(height * scale);

        return Bitmap.createScaledBitmap(bitmap, newWidth, newHeight, true);
    }

    public static boolean isImageFile(String fileName) {
        if (fileName == null) return false;
        String extension = fileName.toLowerCase();
        return extension.endsWith(".jpg") || extension.endsWith(".jpeg") || extension.endsWith(".png");
    }

    public static String generateImageFileName() {
        return "IMG_" + System.currentTimeMillis() + ".jpg";
    }
}