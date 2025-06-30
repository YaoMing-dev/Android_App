// app/src/main/java/com/example/newtrade/utils/ImageUtils.java
package com.example.newtrade.utils;

import android.content.Context;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.media.ExifInterface;
import android.net.Uri;
import android.provider.MediaStore;
import android.util.Log;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;

public class ImageUtils {
    private static final String TAG = "ImageUtils";
    private static final int MAX_WIDTH = 1080;
    private static final int MAX_HEIGHT = 1080;
    private static final int QUALITY = 80;

    /**
     * Get real file path from URI
     */
    public static String getRealPathFromURI(Context context, Uri uri) {
        String realPath = null;

        if (uri == null) {
            return null;
        }

        if (uri.getScheme().equals("content")) {
            try (Cursor cursor = context.getContentResolver().query(uri, null, null, null, null)) {
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
            bitmap = resizeBitmap(bitmap, MAX_WIDTH, MAX_HEIGHT);

            // Save compressed image
            String compressedPath = getCompressedImagePath(imagePath);
            File compressedFile = new File(compressedPath);

            // Create parent directories if needed
            File parentDir = compressedFile.getParentFile();
            if (parentDir != null && !parentDir.exists()) {
                parentDir.mkdirs();
            }

            try (FileOutputStream out = new FileOutputStream(compressedFile)) {
                bitmap.compress(Bitmap.CompressFormat.JPEG, QUALITY, out);
                out.flush();
            }

            // Clean up bitmap
            if (!bitmap.isRecycled()) {
                bitmap.recycle();
            }

            Log.d(TAG, "Image compressed successfully: " + compressedPath);
            return compressedPath;

        } catch (Exception e) {
            Log.e(TAG, "Error compressing image: " + imagePath, e);
            return imagePath;
        }
    }

    /**
     * Calculate sample size for image decoding
     */
    private static int calculateSampleSize(int width, int height) {
        int sampleSize = 1;

        if (height > MAX_HEIGHT || width > MAX_WIDTH) {
            final int halfHeight = height / 2;
            final int halfWidth = width / 2;

            while ((halfHeight / sampleSize) >= MAX_HEIGHT && (halfWidth / sampleSize) >= MAX_WIDTH) {
                sampleSize *= 2;
            }
        }

        return sampleSize;
    }

    /**
     * Rotate image based on EXIF data
     */
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

    /**
     * Rotate bitmap by specified degrees
     */
    private static Bitmap rotateBitmap(Bitmap bitmap, int degrees) {
        Matrix matrix = new Matrix();
        matrix.postRotate(degrees);

        Bitmap rotatedBitmap = Bitmap.createBitmap(bitmap, 0, 0, bitmap.getWidth(), bitmap.getHeight(), matrix, true);

        if (rotatedBitmap != bitmap && !bitmap.isRecycled()) {
            bitmap.recycle();
        }

        return rotatedBitmap;
    }

    /**
     * Resize bitmap to fit within max dimensions
     */
    private static Bitmap resizeBitmap(Bitmap bitmap, int maxWidth, int maxHeight) {
        int width = bitmap.getWidth();
        int height = bitmap.getHeight();

        if (width <= maxWidth && height <= maxHeight) {
            return bitmap;
        }

        float ratioX = (float) maxWidth / width;
        float ratioY = (float) maxHeight / height;
        float ratio = Math.min(ratioX, ratioY);

        int newWidth = (int) (width * ratio);
        int newHeight = (int) (height * ratio);

        Bitmap resizedBitmap = Bitmap.createScaledBitmap(bitmap, newWidth, newHeight, true);

        if (resizedBitmap != bitmap && !bitmap.isRecycled()) {
            bitmap.recycle();
        }

        return resizedBitmap;
    }

    /**
     * Generate compressed image file path
     */
    private static String getCompressedImagePath(String originalPath) {
        String fileName = "compressed_" + System.currentTimeMillis() + ".jpg";
        String directory = originalPath.substring(0, originalPath.lastIndexOf("/"));
        return directory + "/" + fileName;
    }

    /**
     * Create thumbnail from image path
     */
    public static String createThumbnail(String imagePath, int thumbnailSize) {
        try {
            BitmapFactory.Options options = new BitmapFactory.Options();
            options.inJustDecodeBounds = true;
            BitmapFactory.decodeFile(imagePath, options);

            // Calculate sample size for thumbnail
            int sampleSize = calculateThumbnailSampleSize(options.outWidth, options.outHeight, thumbnailSize);

            options.inJustDecodeBounds = false;
            options.inSampleSize = sampleSize;
            Bitmap bitmap = BitmapFactory.decodeFile(imagePath, options);

            if (bitmap == null) {
                return null;
            }

            // Create square thumbnail
            Bitmap thumbnail = createSquareBitmap(bitmap, thumbnailSize);

            // Save thumbnail
            String thumbnailPath = getThumbnailPath(imagePath);
            File thumbnailFile = new File(thumbnailPath);

            try (FileOutputStream out = new FileOutputStream(thumbnailFile)) {
                thumbnail.compress(Bitmap.CompressFormat.JPEG, 85, out);
            }

            // Clean up
            if (!bitmap.isRecycled()) {
                bitmap.recycle();
            }
            if (!thumbnail.isRecycled()) {
                thumbnail.recycle();
            }

            return thumbnailPath;

        } catch (Exception e) {
            Log.e(TAG, "Error creating thumbnail", e);
            return null;
        }
    }

    private static int calculateThumbnailSampleSize(int width, int height, int thumbnailSize) {
        int sampleSize = 1;
        int minDimension = Math.min(width, height);

        while (minDimension / sampleSize > thumbnailSize * 2) {
            sampleSize *= 2;
        }

        return sampleSize;
    }

    private static Bitmap createSquareBitmap(Bitmap bitmap, int size) {
        int width = bitmap.getWidth();
        int height = bitmap.getHeight();

        if (width == height) {
            return Bitmap.createScaledBitmap(bitmap, size, size, true);
        }

        int minDimension = Math.min(width, height);
        int x = (width - minDimension) / 2;
        int y = (height - minDimension) / 2;

        Bitmap squareBitmap = Bitmap.createBitmap(bitmap, x, y, minDimension, minDimension);
        Bitmap scaledBitmap = Bitmap.createScaledBitmap(squareBitmap, size, size, true);

        if (squareBitmap != bitmap && !squareBitmap.isRecycled()) {
            squareBitmap.recycle();
        }

        return scaledBitmap;
    }

    private static String getThumbnailPath(String originalPath) {
        String fileName = "thumb_" + System.currentTimeMillis() + ".jpg";
        String directory = originalPath.substring(0, originalPath.lastIndexOf("/"));
        return directory + "/" + fileName;
    }

    /**
     * Delete image file
     */
    public static boolean deleteImage(String imagePath) {
        try {
            File file = new File(imagePath);
            if (file.exists()) {
                boolean deleted = file.delete();
                Log.d(TAG, "Image deleted: " + imagePath + " (" + deleted + ")");
                return deleted;
            }
        } catch (Exception e) {
            Log.e(TAG, "Error deleting image: " + imagePath, e);
        }
        return false;
    }

    /**
     * Get image file size in bytes
     */
    public static long getImageFileSize(String imagePath) {
        try {
            File file = new File(imagePath);
            if (file.exists()) {
                return file.length();
            }
        } catch (Exception e) {
            Log.e(TAG, "Error getting image file size", e);
        }
        return 0;
    }

    /**
     * Check if file is a valid image
     */
    public static boolean isValidImage(String filePath) {
        try {
            BitmapFactory.Options options = new BitmapFactory.Options();
            options.inJustDecodeBounds = true;
            BitmapFactory.decodeFile(filePath, options);
            return options.outWidth > 0 && options.outHeight > 0;
        } catch (Exception e) {
            Log.e(TAG, "Error validating image", e);
            return false;
        }
    }
}