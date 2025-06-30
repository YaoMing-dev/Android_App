// app/src/main/java/com/example/newtrade/ui/profile/HelpActivity.java
package com.example.newtrade.ui.profile;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.MenuItem;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.newtrade.R;
import com.example.newtrade.ui.profile.adapter.HelpCategoryAdapter;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class HelpActivity extends AppCompatActivity implements HelpCategoryAdapter.OnHelpCategoryClickListener {
    private static final String TAG = "HelpActivity";

    // UI Components
    private Toolbar toolbar;
    private RecyclerView rvHelpCategories;

    // Data
    private HelpCategoryAdapter adapter;
    private List<HelpCategory> helpCategories = new ArrayList<>();

    public static class HelpCategory {
        public String title;
        public String description;
        public int iconRes;
        public List<HelpItem> items;

        public HelpCategory(String title, String description, int iconRes, List<HelpItem> items) {
            this.title = title;
            this.description = description;
            this.iconRes = iconRes;
            this.items = items;
        }
    }

    public static class HelpItem {
        public String question;
        public String answer;

        public HelpItem(String question, String answer) {
            this.question = question;
            this.answer = answer;
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_help);

        initViews();
        setupToolbar();
        setupRecyclerView();
        loadHelpCategories();
    }

    private void initViews() {
        toolbar = findViewById(R.id.toolbar);
        rvHelpCategories = findViewById(R.id.rv_help_categories);
    }

    private void setupToolbar() {
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setTitle("Help & Support");
        }
    }

    private void setupRecyclerView() {
        adapter = new HelpCategoryAdapter(helpCategories, this);
        rvHelpCategories.setLayoutManager(new LinearLayoutManager(this));
        rvHelpCategories.setAdapter(adapter);
    }

    private void loadHelpCategories() {
        // Getting Started
        List<HelpItem> gettingStartedItems = Arrays.asList(
                new HelpItem("How do I create an account?", "Tap 'Sign Up' on the login screen and fill in your details. You'll need to verify your email address."),
                new HelpItem("How do I verify my account?", "Check your email for a verification code and enter it in the app."),
                new HelpItem("How do I reset my password?", "Tap 'Forgot Password' on the login screen and follow the instructions sent to your email.")
        );

        // Buying
        List<HelpItem> buyingItems = Arrays.asList(
                new HelpItem("How do I buy an item?", "Browse products, tap on one you like, and contact the seller through the chat feature."),
                new HelpItem("How do I make an offer?", "On the product page, tap 'Make Offer' and enter your proposed price."),
                new HelpItem("Is it safe to buy on NewTrade?", "We recommend meeting in public places and inspecting items before payment.")
        );

        // Selling
        List<HelpItem> sellingItems = Arrays.asList(
                new HelpItem("How do I list an item?", "Tap the '+' button, add photos, title, description, and price."),
                new HelpItem("How do I edit my listing?", "Go to 'My Products' and tap the edit button on your item."),
                new HelpItem("When should I mark an item as sold?", "Mark as sold once you've completed the transaction with the buyer.")
        );

        // Safety
        List<HelpItem> safetyItems = Arrays.asList(
                new HelpItem("Safety tips for meeting buyers/sellers", "Always meet in public places, bring a friend if possible, and trust your instincts."),
                new HelpItem("How do I report inappropriate content?", "Use the report button on any item or user profile to flag inappropriate content."),
                new HelpItem("What should I do if I encounter fraud?", "Report the user immediately and contact our support team.")
        );

        helpCategories.add(new HelpCategory("Getting Started", "Account setup and basics", R.drawable.ic_help_getting_started, gettingStartedItems));
        helpCategories.add(new HelpCategory("Buying", "How to purchase items", R.drawable.ic_help_buying, buyingItems));
        helpCategories.add(new HelpCategory("Selling", "List and manage your items", R.drawable.ic_help_selling, sellingItems));
        helpCategories.add(new HelpCategory("Safety", "Stay safe while trading", R.drawable.ic_help_safety, safetyItems));

        adapter.notifyDataSetChanged();
    }

    @Override
    public void onHelpCategoryClick(HelpCategory category) {
        Intent intent = new Intent(this, HelpDetailActivity.class);
        intent.putExtra("category_title", category.title);
        // TODO: Pass help items to detail activity
        startActivity(intent);
    }

    @Override
    public void onContactSupportClick() {
        showContactOptions();
    }

    private void showContactOptions() {
        // Email support
        Intent emailIntent = new Intent(Intent.ACTION_SENDTO);
        emailIntent.setData(Uri.parse("mailto:support@newtrade.com"));
        emailIntent.putExtra(Intent.EXTRA_SUBJECT, "NewTrade Support Request");
        emailIntent.putExtra(Intent.EXTRA_TEXT, "Describe your issue here...");

        try {
            startActivity(Intent.createChooser(emailIntent, "Contact Support"));
        } catch (Exception e) {
            Toast.makeText(this, "No email app found", Toast.LENGTH_SHORT).show();
        }
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