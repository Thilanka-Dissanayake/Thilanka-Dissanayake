package com.example.myrestaurant;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.widget.Button;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;

// Fixed: Unified Firebase imports to resolve "Cannot resolve symbol"
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;

import java.util.Locale;

public class OwnerSpentsActivity extends AppCompatActivity {
    private FirebaseFirestore db;
    private TableLayout spentTable;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // 1. SET CONTENT VIEW FIRST
        setContentView(R.layout.activity_owner_spents);

        // 2. Initialize AFTER layout is set
        db = FirebaseFirestore.getInstance();
        spentTable = findViewById(R.id.spentTable);

        Button btnLogout = findViewById(R.id.btnOwnerLogout);
        if (btnLogout != null) {
            btnLogout.setOnClickListener(v -> {
                // Warning: Ensure FirebaseAuth is in build.gradle
                FirebaseAuth.getInstance().signOut();
                startActivity(new Intent(this, MainActivity.class));
                finish();
            });
        }

        listenForSpentUpdates();
    }

    private void listenForSpentUpdates() {
        // Listening to "Items" collection group to see all worker updates
        db.collectionGroup("Items").addSnapshotListener((value, error) -> {
            if (error != null) {
                Toast.makeText(this, "Error: " + error.getMessage(), Toast.LENGTH_SHORT).show();
                return;
            }
            if (value != null) {
                spentTable.removeAllViews();
                addHeader();
                for (DocumentSnapshot doc : value.getDocuments()) {
                    addRow(doc);
                }
            }
        });
    }

    private void addRow(DocumentSnapshot doc) {
        TableRow row = new TableRow(this);

        String name = doc.getString("name") != null ? doc.getString("name") : "Unknown";
        String qty = doc.getString("quantity") != null ? doc.getString("quantity") : "0";
        String price = doc.getString("price") != null ? doc.getString("price") : "0";
        String imageUrl = doc.getString("billImageUrl");

        row.addView(createTextView(name));
        row.addView(createTextView(qty));
        row.addView(createTextView(price));

        try {
            double q = Double.parseDouble(qty);
            double p = Double.parseDouble(price);
            double total = q * p;
            // Fixed: Added Locale for standard formatting
            row.addView(createTextView(String.format(Locale.getDefault(), "%.2f", total)));
        } catch (NumberFormatException e) {
            row.addView(createTextView("0.00"));
        }

        Button btnView = new Button(this);
        btnView.setText("View Bill");
        btnView.setOnClickListener(v -> {
            if (imageUrl != null && !imageUrl.isEmpty()) {
                Intent i = new Intent(Intent.ACTION_VIEW, Uri.parse(imageUrl));
                startActivity(i);
            } else {
                Toast.makeText(this, "No image found", Toast.LENGTH_SHORT).show();
            }
        });
        row.addView(btnView);
        spentTable.addView(row);
    }

    private TextView createTextView(String text) {
        TextView tv = new TextView(this);
        tv.setText(text);
        tv.setPadding(10, 10, 10, 10);
        tv.setTextSize(14);
        tv.setTextColor(android.graphics.Color.BLACK); // Ensure visibility
        return tv;
    }

    private void addHeader() {
        TableRow header = new TableRow(this);
        header.setBackgroundColor(android.graphics.Color.LTGRAY);
        header.addView(createHeaderTextView("Item"));
        header.addView(createHeaderTextView("Qty"));
        header.addView(createHeaderTextView("Price"));
        header.addView(createHeaderTextView("Total"));
        header.addView(createHeaderTextView("Bill"));
        spentTable.addView(header);
    }

    private TextView createHeaderTextView(String text) {
        TextView tv = createTextView(text);
        tv.setTypeface(null, android.graphics.Typeface.BOLD);
        return tv;
    }
}