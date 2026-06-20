package com.example.myrestaurant;

import android.content.Intent;
import android.os.Bundle;
import android.widget.*;
import androidx.appcompat.app.AppCompatActivity;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

public class BillingActivity extends AppCompatActivity {
    private TableLayout cartTable;
    private Spinner itemSpinner, qtySpinner;
    private TextView tvTotalPrice;
    private FirebaseFirestore db;

    private double grandTotal = 0.0;
    private int itemCounter = 0;
    private ArrayList<BillItem> billList = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_billing);

        db = FirebaseFirestore.getInstance();
        cartTable = findViewById(R.id.cartTable);
        itemSpinner = findViewById(R.id.itemSpinner);
        qtySpinner = findViewById(R.id.qtySpinner);
        tvTotalPrice = findViewById(R.id.tvTotalPrice);

        setupSpinners();

        findViewById(R.id.btnAddToCart).setOnClickListener(v -> addToCart());
        findViewById(R.id.btnClearBill).setOnClickListener(v -> clearBill());

        findViewById(R.id.btnPrintBill).setOnClickListener(v -> {
            if (billList.isEmpty()) {
                Toast.makeText(this, "Cart is empty!", Toast.LENGTH_SHORT).show();
                return;
            }

            // 1. Prepare detailed items list for Firestore sync
            ArrayList<Map<String, Object>> salesItems = new ArrayList<>();
            for (BillItem item : billList) {
                Map<String, Object> itemMap = new HashMap<>();
                itemMap.put("itemName", item.getItemName());
                itemMap.put("qty", item.getQty());
                itemMap.put("linePrice", item.getLinePrice());
                salesItems.add(itemMap);
            }

            // 2. Prepare the main sale document
            Map<String, Object> sale = new HashMap<>();
            sale.put("totalAmount", grandTotal);
            sale.put("timestamp", FieldValue.serverTimestamp());
            sale.put("itemsSold", salesItems); // Syncing the full list of items

            db.collection("Sales").add(sale)
                    .addOnSuccessListener(documentReference -> {
                        Intent intent = new Intent(this, PrintActivity.class);
                        intent.putExtra("BILL_LIST", billList);
                        intent.putExtra("GRAND_TOTAL", grandTotal);
                        startActivity(intent);
                    })
                    .addOnFailureListener(e -> {
                        Toast.makeText(this, "Sync Failed: " + e.getMessage(), Toast.LENGTH_LONG).show();
                    });
        });
    }

    private void setupSpinners() {
        String[] menuItems = {"Lemon Tea - 50.00", "Milk Tea - 100.00", "Zara Cafe - 100.00", "Masala Tea - 120.00", "Hot Chocolate - 130.00", "Iced Coffee - 250.00", "Iced Choco - 250.00", "Thambili Mojito - 250.00", "Crispy Chicken Sub - 250.00", "Crispy Chicken Burger - 250.00", "Devilled Chicken Sub - 200.00", "Fried Chicken Sub - 250.00", "Drumstick Burger - 300.00", "Butter Mushroom Burger - 200.00", "Butter Mushroom Sub - 200.00", "Chinese Roll - 80.00", "Dunhill - 170.00", "Gold Leaf - 160.00", "Gold Leaf Half - 105.00"};
        itemSpinner.setAdapter(new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, menuItems));

        String[] quantities = {"1", "2", "3", "4", "5", "6", "7", "8", "9", "10"};
        ArrayAdapter<String> qtyAdapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, quantities);
        qtyAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        qtySpinner.setAdapter(qtyAdapter);
    }

    private void addToCart() {
        try {
            String selected = itemSpinner.getSelectedItem().toString();
            int qty = Integer.parseInt(qtySpinner.getSelectedItem().toString());

            String[] parts = selected.split(" - ");
            String itemName = parts[0];
            double unitPrice = Double.parseDouble(parts[1]);
            double linePrice = unitPrice * qty;

            billList.add(new BillItem(itemName, qty, unitPrice, linePrice));
            itemCounter++;
            grandTotal += linePrice;

            TableRow row = new TableRow(this);
            row.addView(makeCell(String.valueOf(itemCounter)));
            row.addView(makeCell(itemName));
            row.addView(makeCell(String.valueOf(qty)));
            row.addView(makeCell(String.format(Locale.getDefault(), "%.2f", unitPrice)));
            row.addView(makeCell(String.format(Locale.getDefault(), "%.2f", linePrice)));

            cartTable.addView(row);
            tvTotalPrice.setText(String.format(Locale.getDefault(), "%.2f", grandTotal));
        } catch (Exception e) {
            Toast.makeText(this, "Calculation Error!", Toast.LENGTH_SHORT).show();
        }
    }

    private void clearBill() {
        itemCounter = 0;
        grandTotal = 0.0;
        billList.clear();
        int childCount = cartTable.getChildCount();
        if (childCount > 1) {
            cartTable.removeViews(1, childCount - 1);
        }
        tvTotalPrice.setText("0.00");
    }

    private TextView makeCell(String text) {
        TextView tv = new TextView(this);
        tv.setText(text);
        tv.setPadding(15, 10, 15, 10);
        tv.setTextSize(14);
        tv.setTextColor(android.graphics.Color.BLACK);
        return tv;
    }
}