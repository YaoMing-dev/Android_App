// app/src/main/java/com/example/newtrade/interfaces/RecommendationCallback.java
package com.example.newtrade.interfaces;

import com.example.newtrade.models.Product;
import java.util.List;

public interface RecommendationCallback {
    void onSuccess(List<Product> products, String title);
    void onError(String error);
}