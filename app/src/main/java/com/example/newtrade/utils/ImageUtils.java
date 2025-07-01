// app/src/main/java/com/example/newtrade/utils/ImageUtils.java
package com.example.newtrade.utils;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.media.ExifInterface;
import android.net.Uri;
import android.os.Environment;
import android.util.Log;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class ImageUtils {
    private static final String TAG = "ImageUtils";

    // Image quality settings
    private static final int MAX_IMAGE_WIDTH = 1024;
    private static final int MAX_IMAGE_HEIGHT = 1024;
    private static final int COMPRESSION_QUALITY = 85;
    private static final long MAX_FILE_SIZE = 5 * 1024 * 1024; // 5MB

    // File naming
    private static final String IMAGE_PREFIX = "IMG_";
    private static final String IMAGE_EXTENSION = ".jpg";

    /**
     * Create image file in external storage
     */
    public static File createImageFile(Context context) throws IOException {
        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(new Date());
        String imageFileName = IMAGE_PREFIX + timeStamp;

        File storageDir = context.getExternalFilesDir(Environment.DIRECTORY_PICTURES);
        if (storageDir != null && !storageDir.exists()) {
            storageDir.mkdirs();
        }

        File image = File.createTempFile(imageFileName, IMAGE_EXTENSION, storageDir);
        Log.d(TAG, "✅ Image file created: " + image.getAbsolutePath());
        return image;
    }

    /**
     * Compress and resize image from URI
     */
    public static File compressImage(Context context, Uri imageUri) throws IOException {
        Log.d(TAG, "Starting image compression for URI: " + imageUri);

        // Get input stream from URI
        InputStream inputStream = context.getContentResolver().openInputStream(imageUri);
        if (inputStream == null) {
            throw new IOException("Cannot open input stream from URI");
        }

        // Decode bitmap with options
        BitmapFactory.Options options = new BitmapFactory.Options();
        options.inJustDecodeBounds = true;
        BitmapFactory.decodeStream(inputStream, null, options);
        inputStream.close();

        // Calculate sample size
        options.inSampleSize = calculateInSampleSize(options, MAX_IMAGE_WIDTH, MAX_IMAGE_HEIGHT);
        options.inJustDecodeBounds = false;

        // Decode actual bitmap
        inputStream = context.getContentResolver().openInputStream(imageUri);
        Bitmap bitmap = BitmapFactory.decodeStream(inputStream, null, options);
        inputStream.close();

        if (bitmap == null) {
            throw new IOException("Failed to decode bitmap");
        }

        // Rotate if needed
        bitmap = rotateImageIfRequired(context, bitmap, imageUri);

        // Resize if still too large
        bitmap = resizeBitmap(bitmap, MAX_IMAGE_WIDTH, MAX_IMAGE_HEIGHT);

        // Save compressed image
        File compressedFile = createImageFile(context);
        FileOutputStream outputStream = new FileOutputStream(compressedFile);

        bitmap.compress(Bitmap.CompressFormat.JPEG, COMPRESSION_QUALITY, outputStream);
        outputStream.close();
        bitmap.recycle();

        // Validate file size
        if (compressedFile.length() > MAX_FILE_SIZE) {
            Log.w(TAG, "⚠️ Compressed file still too large: " + compressedFile.length() + " bytes");
        }

        Log.d(TAG, "✅ Image compressed successfully. Size: " + compressedFile.length() + " bytes");
        return compressedFile;
    }

    /**
     * Calculate sample size for bitmap decoding
     */
    private static int calculateInSampleSize(BitmapFactory.Options options, int reqWidth, int reqHeight) {
        final int height = options.outHeight;
        final int width = options.outWidth;
        int inSampleSize = 1;

        if (height > reqHeight || width > reqWidth) {
            final int halfHeight = height / 2;
            final int halfWidth = width / 2;

            while ((halfHeight / inSampleSize) >= reqHeight && (halfWidth / inSampleSize) >= reqWidth) {
                inSampleSize *= 2;
            }
        }

        return inSampleSize;
    }

    /**
     * Rotate image based on EXIF data
     */
    private static Bitmap rotateImageIfRequired(Context context, Bitmap img, Uri selectedImage) {
        try {
            InputStream input = context.getContentResolver().openInputStream(selectedImage);
            ExifInterface ei = new ExifInterface(input);
            int orientation = ei.getAttributeInt(ExifInterface.TAG_ORIENTATION, ExifInterface.ORIENTATION_NORMAL);

            switch (orientation) {
                case ExifInterface.ORIENTATION_ROTATE_90:
                    return rotateImage(img, 90);
                case ExifInterface.ORIENTATION_ROTATE_180:
                    return rotateImage(img, 180);
                case ExifInterface.ORIENTATION_ROTATE_270:
                    return rotateImage(img, 270);
                default:
                    return img;
            }
        } catch (IOException e) {
            Log.w(TAG, "Failed to read EXIF data", e);
            return img;
        }
    }

    /**
     * Rotate bitmap by specified degrees
     */
    private static Bitmap rotateImage(Bitmap img, int degree) {
        Matrix matrix = new Matrix();
        matrix.postRotate(degree);
        Bitmap rotatedImg = Bitmap.createBitmap(img, 0, 0, img.getWidth(), img.getHeight(), matrix, true);
        img.recycle();
        return rotatedImg;
    }

    /**
     * Resize bitmap to fit within max dimensions
     */
    private static Bitmap resizeBitmap(Bitmap bitmap, int maxWidth, int maxHeight) {
        int width = bitmap.getWidth();
        int height = bitmap.getHeight();

        // Calculate scale ratio
        float scaleWidth = ((float) maxWidth) / width;
        float scaleHeight = ((float) maxHeight) / height;
        float scale = Math.min(scaleWidth, scaleHeight);

        // Only resize if image is larger than max dimensions
        if (scale < 1.0f) {
            int newWidth = Math.round(width * scale);
            int newHeight = Math.round(height * scale);

            Bitmap resizedBitmap = Bitmap.createScaledBitmap(bitmap, newWidth, newHeight, true);
            bitmap.recycle();
            return resizedBitmap;
        }

        return bitmap;
    }

    /**
     * Convert bitmap to byte array
     */
    public static byte[] bitmapToByteArray(Bitmap bitmap, int quality) {
        ByteArrayOutputStream stream = new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.JPEG, quality, stream);
        return stream.toByteArray();
    }

    /**
     * Get file size in MB
     */
    public static double getFileSizeMB(File file) {
        return file.length() / (1024.0 * 1024.0);
    }

    /**
     * Validate image file
     */
    public static boolean isValidImageFile(File file) {
        if (file == null || !file.exists()) {
            return false;
        }

        // Check file size
        if (file.length() > MAX_FILE_SIZE) {
            Log.w(TAG, "File too large: " + file.length() + " bytes");
            return false;
        }

        // Try to decode bitmap to validate
        BitmapFactory.Options options = new BitmapFactory.Options();
        options.inJustDecodeBounds = true;
        BitmapFactory.decodeFile(file.getAbsolutePath(), options);

        return options.outWidth > 0 && options.outHeight > 0;
    }

    /**
     * Clean up temporary files
     */
    public static void cleanupTempFiles(Context context) {
        try {
            File storageDir = context.getExternalFilesDir(Environment.DIRECTORY_PICTURES);
            if (storageDir != null && storageDir.exists()) {
                File[] files = storageDir.listFiles();
                if (files != null) {
                    long currentTime = System.currentTimeMillis();
                    int deletedCount = 0;

                    for (File file : files) {
                        // Delete files older than 24 hours
                        if (currentTime - file.lastModified() > 24 * 60 * 60 * 1000) {
                            if (file.delete()) {
                                deletedCount++;
                            }
                        }
                    }

                    if (deletedCount > 0) {
                        Log.d(TAG, "✅ Cleaned up " + deletedCount + " temporary files");
                    }
                }
            }
        } catch (Exception e) {
            Log.e(TAG, "Failed to cleanup temp files", e);
        }
    }

    /**
     * Get image dimensions without loading full bitmap
     */
    public static int[] getImageDimensions(Context context, Uri imageUri) {
        try {
            InputStream inputStream = context.getContentResolver().openInputStream(imageUri);
            BitmapFactory.Options options = new BitmapFactory.Options();
            options.inJustDecodeBounds = true;
            BitmapFactory.decodeStream(inputStream, null, options);
            inputStream.close();
            return new int[]{options.outWidth, options.outHeight};
        } catch (IOException e) {
            Log.e(TAG, "Failed to get image dimensions", e);
            return new int[]{0, 0};
        }
    }
}