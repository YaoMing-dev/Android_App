// app/src/main/java/com/example/newtrade/utils/ImageUtils.java
package com.example.newtrade.utils;

import android.content.Context;
import android.text.TextUtils;
import android.util.Log;
import android.widget.ImageView;
import androidx.annotation.Nullable;
import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.bumptech.glide.request.RequestListener;
import com.bumptech.glide.request.target.Target;
import com.example.newtrade.R;

public class ImageUtils {

    private static final String TAG = "ImageUtils";

    /**
     * Build full image URL from relative path
     */
    public static String buildFullImageUrl(String imageUrl) {
        if (TextUtils.isEmpty(imageUrl)) {
            return null;
        }

        // Already full URL
        if (imageUrl.startsWith("http")) {
            return imageUrl;
        }

        // Remove leading slash and add base URL
        if (imageUrl.startsWith("/")) {
            return Constants.BASE_URL + imageUrl.substring(1);
        }

        // Add base URL
        return Constants.BASE_URL + imageUrl;
    }

    /**
     * Load product image with proper error handling
     */
    public static void loadProductImage(Context context, String imageUrl, ImageView imageView) {
        if (imageView == null) return;

        try {
            String fullUrl = buildFullImageUrl(imageUrl);

            if (!TextUtils.isEmpty(fullUrl)) {
                Log.d(TAG, "Loading product image: " + fullUrl);

                Glide.with(context)
                        .load(fullUrl)
                        .placeholder(R.drawable.ic_image_placeholder)
                        .error(R.drawable.ic_image_placeholder)
                        .diskCacheStrategy(DiskCacheStrategy.ALL)
                        .centerCrop()
                        .into(imageView);
            } else {
                imageView.setImageResource(R.drawable.ic_image_placeholder);
            }
        } catch (Exception e) {
            Log.e(TAG, "Error loading product image", e);
            imageView.setImageResource(R.drawable.ic_image_placeholder);
        }
    }

    /**
     * ✅ FIXED: Load avatar với debug chi tiết
     */
    public static void loadAvatarImage(Context context, String imageUrl, ImageView imageView) {
        if (imageView == null) return;

        try {
            String fullUrl = buildFullImageUrl(imageUrl);

            Log.d(TAG, "🔍 Avatar loading attempt:");
            Log.d(TAG, "  - Original URL: " + imageUrl);
            Log.d(TAG, "  - Full URL: " + fullUrl);

            if (!TextUtils.isEmpty(fullUrl)) {
                Glide.with(context)
                        .load(fullUrl)
                        .placeholder(R.drawable.placeholder_avatar)
                        .error(R.drawable.placeholder_avatar)
                        .diskCacheStrategy(DiskCacheStrategy.NONE)
                        .skipMemoryCache(true)
                        .circleCrop()
                        .listener(new RequestListener<android.graphics.drawable.Drawable>() {
                            @Override
                            public boolean onLoadFailed(@Nullable com.bumptech.glide.load.engine.GlideException e, Object model, Target<android.graphics.drawable.Drawable> target, boolean isFirstResource) {
                                Log.e(TAG, "❌ Avatar load FAILED for: " + fullUrl);
                                if (e != null) {
                                    Log.e(TAG, "❌ Glide error: " + e.getMessage());
                                    e.logRootCauses(TAG);
                                }
                                return false; // Let Glide handle the error drawable
                            }

                            @Override
                            public boolean onResourceReady(android.graphics.drawable.Drawable resource, Object model, Target<android.graphics.drawable.Drawable> target, com.bumptech.glide.load.DataSource dataSource, boolean isFirstResource) {
                                Log.d(TAG, "✅ Avatar load SUCCESS for: " + fullUrl);
                                Log.d(TAG, "✅ Data source: " + dataSource);
                                return false; // Let Glide handle the resource
                            }
                        })
                        .into(imageView);

            } else {
                Log.w(TAG, "⚠️ Empty avatar URL, using placeholder");
                imageView.setImageResource(R.drawable.placeholder_avatar);
            }
        } catch (Exception e) {
            Log.e(TAG, "❌ Exception loading avatar image", e);
            imageView.setImageResource(R.drawable.placeholder_avatar);
        }
    }

    /**
     * Load image with custom placeholder
     */
    public static void loadImageWithPlaceholder(Context context, String imageUrl,
                                                ImageView imageView, int placeholderResId) {
        if (imageView == null) return;

        try {
            String fullUrl = buildFullImageUrl(imageUrl);

            Glide.with(context)
                    .load(fullUrl)
                    .placeholder(placeholderResId)
                    .error(placeholderResId)
                    .diskCacheStrategy(DiskCacheStrategy.ALL)
                    .into(imageView);
        } catch (Exception e) {
            Log.e(TAG, "Error loading image", e);
            imageView.setImageResource(placeholderResId);
        }
    }
}