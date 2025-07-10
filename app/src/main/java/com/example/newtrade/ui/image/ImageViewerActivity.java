// app/src/main/java/com/example/newtrade/ui/image/ImageViewerActivity.java
package com.example.newtrade.ui.image;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.target.CustomTarget;
import com.bumptech.glide.request.transition.Transition;
import com.example.newtrade.R;
import com.github.chrisbanes.photoview.PhotoView;
import com.google.android.material.appbar.MaterialToolbar;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;

public class ImageViewerActivity extends AppCompatActivity {

    private static final String TAG = "ImageViewerActivity";
    public static final String EXTRA_IMAGE_URL = "image_url";
    public static final String EXTRA_IMAGE_TITLE = "image_title";

    private PhotoView photoView;
    private MaterialToolbar toolbar;
    private String imageUrl;
    private String imageTitle;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_image_viewer);

        initViews();
        getIntentData();
        setupToolbar();
        loadImage();
    }

    private void initViews() {
        photoView = findViewById(R.id.photo_view);
        toolbar = findViewById(R.id.toolbar);
    }

    private void getIntentData() {
        Intent intent = getIntent();
        if (intent != null) {
            imageUrl = intent.getStringExtra(EXTRA_IMAGE_URL);
            imageTitle = intent.getStringExtra(EXTRA_IMAGE_TITLE);

            if (imageTitle == null || imageTitle.isEmpty()) {
                imageTitle = "Image";
            }
        }

        Log.d(TAG, "Viewing image: " + imageUrl);
    }

    private void setupToolbar() {
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setDisplayShowHomeEnabled(true);
            getSupportActionBar().setTitle(imageTitle);
        }
    }

    private void loadImage() {
        if (imageUrl == null || imageUrl.isEmpty()) {
            Toast.makeText(this, "No image to display", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        // Handle both local and remote URLs
        String fullUrl = imageUrl;
        if (imageUrl.startsWith("/")) {
            fullUrl = "http://10.0.2.2:8080" + imageUrl;
        } else if (imageUrl.startsWith("content://")) {
            // Local URI
            photoView.setImageURI(Uri.parse(imageUrl));
            return;
        }

        // Load remote image with Glide
        Glide.with(this)
                .load(fullUrl)
                .placeholder(R.drawable.ic_image_placeholder)
                .error(R.drawable.ic_image_error)
                .into(photoView);

        Log.d(TAG, "Loading image: " + fullUrl);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_image_viewer, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        int id = item.getItemId();

        if (id == android.R.id.home) {
            finish();
            return true;
        } else if (id == R.id.action_save) {
            saveImageToGallery();
            return true;
        } else if (id == R.id.action_share) {
            shareImage();
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    private void saveImageToGallery() {
        if (imageUrl == null) return;

        String fullUrl = imageUrl.startsWith("/") ? "http://10.0.2.2:8080" + imageUrl : imageUrl;

        Glide.with(this)
                .asBitmap()
                .load(fullUrl)
                .into(new CustomTarget<Bitmap>() {
                    @Override
                    public void onResourceReady(@NonNull Bitmap resource, @Nullable Transition<? super Bitmap> transition) {
                        saveBitmapToGallery(resource);
                    }

                    @Override
                    public void onLoadCleared(@Nullable Drawable placeholder) {}
                });
    }

    private void saveBitmapToGallery(Bitmap bitmap) {
        try {
            String savedImageURL = MediaStore.Images.Media.insertImage(
                    getContentResolver(),
                    bitmap,
                    "ChatImage_" + System.currentTimeMillis(),
                    "Image from chat"
            );

            if (savedImageURL != null) {
                Toast.makeText(this, "Image saved to gallery", Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(this, "Failed to save image", Toast.LENGTH_SHORT).show();
            }
        } catch (Exception e) {
            Log.e(TAG, "Error saving image", e);
            Toast.makeText(this, "Error saving image", Toast.LENGTH_SHORT).show();
        }
    }

    private void shareImage() {
        if (imageUrl == null) return;

        String fullUrl = imageUrl.startsWith("/") ? "http://10.0.2.2:8080" + imageUrl : imageUrl;

        Glide.with(this)
                .asBitmap()
                .load(fullUrl)
                .into(new CustomTarget<Bitmap>() {
                    @Override
                    public void onResourceReady(@NonNull Bitmap resource, @Nullable Transition<? super Bitmap> transition) {
                        shareImageBitmap(resource);
                    }

                    @Override
                    public void onLoadCleared(@Nullable Drawable placeholder) {}
                });
    }

    private void shareImageBitmap(Bitmap bitmap) {
        try {
            File cachePath = new File(getCacheDir(), "images");
            cachePath.mkdirs();

            File file = new File(cachePath, "shared_image.jpg");
            FileOutputStream stream = new FileOutputStream(file);
            bitmap.compress(Bitmap.CompressFormat.JPEG, 100, stream);
            stream.close();

            Uri imageUri = androidx.core.content.FileProvider.getUriForFile(
                    this,
                    getPackageName() + ".fileprovider",
                    file
            );

            Intent shareIntent = new Intent(Intent.ACTION_SEND);
            shareIntent.setType("image/jpeg");
            shareIntent.putExtra(Intent.EXTRA_STREAM, imageUri);
            shareIntent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);

            startActivity(Intent.createChooser(shareIntent, "Share Image"));

        } catch (IOException e) {
            Log.e(TAG, "Error sharing image", e);
            Toast.makeText(this, "Error sharing image", Toast.LENGTH_SHORT).show();
        }
    }
}