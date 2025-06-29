package com.example.newtrade.utils;

import android.app.Activity;
import android.content.ClipData;
import android.content.Intent;
import android.net.Uri;
import android.provider.MediaStore;

import java.util.ArrayList;
import java.util.List;

public class ImagePickerHelper {
    private Activity activity;

    public ImagePickerHelper(Activity activity) {
        this.activity = activity;
    }

    public void openImagePicker(int requestCode) {
        Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
        intent.setType("image/*");
        intent.putExtra(Intent.EXTRA_ALLOW_MULTIPLE, true);
        intent.addCategory(Intent.CATEGORY_OPENABLE);

        // Create chooser intent
        Intent chooser = Intent.createChooser(intent, "Select Images");

        // Add camera option
        Intent cameraIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        Intent[] extraIntents = {cameraIntent};
        chooser.putExtra(Intent.EXTRA_INITIAL_INTENTS, extraIntents);

        activity.startActivityForResult(chooser, requestCode);
    }

    public List<Uri> handleImagePickerResult(Intent data) {
        List<Uri> imageUris = new ArrayList<>();

        if (data != null) {
            // Check if multiple images were selected
            if (data.getClipData() != null) {
                ClipData clipData = data.getClipData();
                for (int i = 0; i < clipData.getItemCount(); i++) {
                    Uri imageUri = clipData.getItemAt(i).getUri();
                    if (imageUri != null) {
                        imageUris.add(imageUri);
                    }
                }
            }
            // Single image selected
            else if (data.getData() != null) {
                Uri imageUri = data.getData();
                imageUris.add(imageUri);
            }
        }

        return imageUris;
    }
}
