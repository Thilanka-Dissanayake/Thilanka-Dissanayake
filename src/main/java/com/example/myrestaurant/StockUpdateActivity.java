package com.example.myrestaurant;

import android.content.Intent;
import android.os.Bundle;
import android.widget.*;
import androidx.appcompat.app.AppCompatActivity;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.*;
import java.text.SimpleDateFormat;
import java.util.*;

public class StockUpdateActivity extends AppCompatActivity {
    private FirebaseFirestore db;
    private TableLayout stockTable;
    private String today = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(new Date());

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // FIX 1: Set Content View FIRST
        setContentView(R.layout.activity_stock_update);

        // FIX 2: Initialize DB
        db = FirebaseFirestore.getInstance();

        stockTable = findViewById(R.id.stockTable);

        // Setup Listeners AFTER setContentView
        findViewById(R.id.fab_switch).setOnClickListener(v -> {
            startActivity(new Intent(this, BillingActivity.class));
            finish();
        });

        findViewById(R.id.btnWorkerLogout).setOnClickListener(v -> {
            FirebaseAuth.getInstance().signOut();
            startActivity(new Intent(this, MainActivity.class));
            finish();
        });

        findViewById(R.id.btnAddStock).setOnClickListener(v -> saveStock());
        listenForStock();
    }

    private void saveStock() {
        String name = ((EditText)findViewById(R.id.etGoodName)).getText().toString();
        if(name.isEmpty()) return;

        Map<String, Object> data = new HashMap<>();
        data.put("name", name);
        data.put("timestamp", FieldValue.serverTimestamp());

        db.collection("DailyStocks").document(today).collection("Items").add(data);
        ((EditText)findViewById(R.id.etGoodName)).setText(""); // Clear input
    }

    private void listenForStock() {
        db.collection("DailyStocks").document(today).collection("Items")
                .orderBy("timestamp", Query.Direction.ASCENDING)
                .addSnapshotListener((value, error) -> {
                    if (value == null) return;
                    stockTable.removeAllViews();
                    for (DocumentSnapshot doc : value.getDocuments()) {
                        TableRow row = new TableRow(this);
                        TextView tv = new TextView(this);
                        tv.setText(doc.getString("name"));
                        tv.setPadding(10,10,10,10);
                        row.addView(tv);
                        stockTable.addView(row);
                    }
                });
    }
}