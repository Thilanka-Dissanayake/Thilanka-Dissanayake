package com.example.myrestaurant;

import android.content.Intent;
import android.os.Bundle;
import androidx.appcompat.app.AppCompatActivity;

public class WorkerChoiceActivity extends AppCompatActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_worker_choice);

        findViewById(R.id.btnStockTask).setOnClickListener(v ->
                startActivity(new Intent(this, StockUpdateActivity.class)));

        findViewById(R.id.btnBillingTask).setOnClickListener(v ->
                startActivity(new Intent(this, BillingActivity.class)));
    }
}