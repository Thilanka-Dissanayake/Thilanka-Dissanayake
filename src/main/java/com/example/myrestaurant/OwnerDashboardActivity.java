package com.example.myrestaurant;

import android.content.Intent;
import android.os.Bundle;
import androidx.appcompat.app.AppCompatActivity;

public class OwnerDashboardActivity extends AppCompatActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_owner_dashboard);

        // Listener for Sales & Income button
        findViewById(R.id.btnViewSales).setOnClickListener(v ->
                startActivity(new Intent(this, OwnerSalesActivity.class)));

        // Listener for Stock & Spents button
        findViewById(R.id.btnViewSpents).setOnClickListener(v ->
                startActivity(new Intent(this, OwnerSpentsActivity.class)));
    }
}