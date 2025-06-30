// app/src/main/java/com/example/newtrade/ui/product/AddProductFragment.java
package com.example.newtrade.ui.product;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.example.newtrade.R;

public class AddProductFragment extends Fragment {
    private static final String TAG = "AddProductFragment";

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_add_product, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        // For now, just redirect to AddProductActivity
        Intent intent = new Intent(getContext(), AddProductActivity.class);
        startActivity(intent);

        // Go back to previous fragment
        if (getActivity() != null) {
            getActivity().onBackPressed();
        }
    }
}