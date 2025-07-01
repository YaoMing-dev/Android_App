// app/src/main/java/com/example/newtrade/utils/ImagePickerHelper.java
package com.example.newtrade.utils;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.provider.MediaStore;
import android.util.Log;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.Fragment;

import com.karumi.dexter.Dexter;
import com.karumi.dexter.MultiplePermissionsReport;
import com.karumi.dexter.PermissionToken;
import com.karumi.dexter.listener.PermissionRequest;
import com.karumi.dexter.listener.multi.MultiplePermissionsListener;
import com.karumi.dexter.listener.single.PermissionListener;
import com.karumi.dexter.PermissionGrantedResponse;
import com.karumi.dexter.PermissionDeniedResponse;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.util.List;

public class ImagePickerHelper {
    private static final String TAG = "ImagePickerHelper";

    // Static variables to store callbacks
    private static ImagePickerListener currentListener;

    public interface ImagePickerListener {
        void onImageSelected(File imageFile);
        void onError(String error);
    }

    /**
     * Show image picker dialog for Activity
     */
    public static void showImagePickerDialog(Activity activity, ImagePickerListener listener) {
        currentListener = listener;

        String[] options = {"Camera", "Gallery", "Cancel"};

        AlertDialog.Builder builder = new AlertDialog.Builder(activity);
        builder.setTitle("Select Image Source")
                .setItems(options, (dialog, which) -> {
                    switch (which) {
                        case 0:
                            checkCameraPermissionAndOpen(activity);
                            break;
                        case 1:
                            checkGalleryPermissionAndOpen(activity);
                            break;
                        case 2:
                            dialog.dismiss();
                            currentListener = null;
                            break;
                    }
                });
        builder.show();
    }

    /**
     * Show image picker dialog for Fragment
     */
    public static void showImagePickerDialog(Fragment fragment, ImagePickerListener listener) {
        showImagePickerDialog(fragment.requireActivity(), listener);
    }

    /**
     * Check camera permissions and open camera
     */
    private static void checkCameraPermissionAndOpen(Activity activity) {
        Dexter.withContext(activity)
                .withPermissions(
                        android.Manifest.permission.CAMERA,
                        android.Manifest.permission.WRITE_EXTERNAL_STORAGE
                )
                .withListener(new MultiplePermissionsListener() {
                    @Override
                    public void onPermissionsChecked(MultiplePermissionsReport report) {
                        if (report.areAllPermissionsGranted()) {
                            openCamera(activity);
                        } else {
                            if (currentListener != null) {
                                currentListener.onError("Camera permission required");
                            }
                        }
                    }

                    @Override
                    public void onPermissionRationaleShouldBeShown(List<PermissionRequest> permissions, PermissionToken token) {
                        token.continuePermissionRequest();
                    }
                })
                .check();
    }

    /**
     * Check gallery permissions and open gallery
     */
    private static void checkGalleryPermissionAndOpen(Activity activity) {
        Dexter.withContext(activity)
                .withPermission(android.Manifest.permission.READ_EXTERNAL_STORAGE)
                .withListener(new PermissionListener() {
                    @Override
                    public void onPermissionGranted(PermissionGrantedResponse response) {
                        openGallery(activity);
                    }

                    @Override
                    public void onPermissionDenied(PermissionDeniedResponse response) {
                        if (currentListener != null) {
                            currentListener.onError("Gallery permission required");
                        }
                    }

                    @Override
                    public void onPermissionRationaleShouldBeShown(PermissionRequest permission, PermissionToken token) {
                        token.continuePermissionRequest();
                    }
                })
                .check();
    }

    /**
     * Open camera
     */
    private static void openCamera(Activity activity) {
        Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        if (intent.resolveActivity(activity.getPackageManager()) != null) {
            activity.startActivityForResult(intent, Constants.RC_TAKE_PHOTO);
        } else {
            if (currentListener != null) {
                currentListener.onError("Camera not available");
            }
        }
    }

    /**
     * Open gallery
     */
    private static void openGallery(Activity activity) {
        Intent intent = new Intent(Intent.ACTION_PICK);
        intent.setType("image/*");
        activity.startActivityForResult(intent, Constants.RC_PICK_IMAGE);
    }

    /**
     * Handle activity result - call this from Activity's onActivityResult
     */
    public static void handleActivityResult(int requestCode, int resultCode, Intent data, Context context) {
        if (resultCode != Activity.RESULT_OK || currentListener == null) {
            currentListener = null;
            return;
        }

        try {
            switch (requestCode) {
                case Constants.RC_TAKE_PHOTO:
                    handleCameraResult(data, context);
                    break;
                case Constants.RC_PICK_IMAGE:
                    handleGalleryResult(data, context);
                    break;
            }
        } catch (Exception e) {
            Log.e(TAG, "❌ Error handling activity result", e);
            if (currentListener != null) {
                currentListener.onError("Failed to process image: " + e.getMessage());
            }
        } finally {
            currentListener = null;
        }
    }

    /**
     * Handle camera result
     */
    private static void handleCameraResult(Intent data, Context context) {
        try {
            Bitmap bitmap = (Bitmap) data.getExtras().get("data");
            if (bitmap != null) {
                File imageFile = saveBitmapToFile(bitmap, context);
                currentListener.onImageSelected(imageFile);
            } else {
                currentListener.onError("Failed to capture image");
            }
        } catch (Exception e) {
            Log.e(TAG, "❌ Camera result error", e);
            currentListener.onError("Failed to process camera image: " + e.getMessage());
        }
    }

    /**
     * Handle gallery result
     */
    private static void handleGalleryResult(Intent data, Context context) {
        try {
            Uri selectedImageUri = data.getData();
            if (selectedImageUri != null) {
                File imageFile = getFileFromUri(selectedImageUri, context);
                currentListener.onImageSelected(imageFile);
            } else {
                currentListener.onError("Failed to select image");
            }
        } catch (Exception e) {
            Log.e(TAG, "❌ Gallery result error", e);
            currentListener.onError("Failed to process gallery image: " + e.getMessage());
        }
    }

    /**
     * Save bitmap to temporary file
     */
    private static File saveBitmapToFile(Bitmap bitmap, Context context) throws Exception {
        File tempFile = new File(context.getCacheDir(), "temp_image_" + System.currentTimeMillis() + ".jpg");

        FileOutputStream out = new FileOutputStream(tempFile);
        bitmap.compress(Bitmap.CompressFormat.JPEG, Constants.IMAGE_COMPRESSION_QUALITY, out);
        out.flush();
        out.close();

        return tempFile;
    }

    /**
     * Get file from URI
     */
    private static File getFileFromUri(Uri uri, Context context) throws Exception {
        File tempFile = new File(context.getCacheDir(), "temp_image_" + System.currentTimeMillis() + ".jpg");

        InputStream inputStream = context.getContentResolver().openInputStream(uri);
        FileOutputStream outputStream = new FileOutputStream(tempFile);

        byte[] buffer = new byte[1024];
        int length;
        while ((length = inputStream.read(buffer)) > 0) {
            outputStream.write(buffer, 0, length);
        }

        outputStream.close();
        inputStream.close();

        return tempFile;
    }
}