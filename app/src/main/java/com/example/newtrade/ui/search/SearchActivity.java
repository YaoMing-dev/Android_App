// app/src/main/java/com/example/newtrade/ui/search/SearchActivity.java
package com.example.newtrade.ui.search;

import android.os.Bundle;
import android.view.MenuItem;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.fragment.app.Fragment;

import com.example.newtrade.R;
import com.example.newtrade.utils.Constants;

public class SearchActivity extends AppCompatActivity {
    private static final String TAG = "SearchActivity";

    private Toolbar toolbar;
    private SearchFragment searchFragment;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_search);

        initViews();
        setupToolbar();
        setupFragment();
    }

    private void initViews() {
        toolbar = findViewById(R.id.toolbar);
    }

    private void setupToolbar() {
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setTitle("Search");
        }
    }

    private void setupFragment() {
        searchFragment = new SearchFragment();

        // Pass intent extras to fragment
        Bundle args = new Bundle();
        if (getIntent().getExtras() != null) {
            args.putAll(getIntent().getExtras());
        }
        searchFragment.setArguments(args);

        getSupportFragmentManager().beginTransaction()
                .replace(R.id.fragment_container, searchFragment)
                .commit();
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            finish();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }
}