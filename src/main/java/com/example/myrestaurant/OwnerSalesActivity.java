package com.example.myrestaurant;

import android.os.Bundle;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.Timestamp;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;
import java.util.Map;

public class OwnerSalesActivity extends AppCompatActivity {

    private FirebaseFirestore db;
    private TableLayout salesTable;
    // Ensure these IDs match your activity_owner_sales.xml
    private TextView tvDailyIncome, tvWeeklyIncome, tvMonthlyIncome, tvTotalIncome;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_owner_sales);

        db = FirebaseFirestore.getInstance();
        salesTable = findViewById(R.id.salesTable);
        tvDailyIncome = findViewById(R.id.tvDailyIncome);
        tvWeeklyIncome = findViewById(R.id.tvWeeklyIncome);
        tvMonthlyIncome = findViewById(R.id.tvMonthlyIncome);
        tvTotalIncome = findViewById(R.id.tvTotalIncome);

        loadSalesData();
    }

    private void loadSalesData() {
        db.collection("Sales").orderBy("timestamp", Query.Direction.DESCENDING)
                .addSnapshotListener((value, error) -> {
                    if (value != null) {
                        salesTable.removeAllViews();

                        double dailyTotal = 0, weeklyTotal = 0, monthlyTotal = 0, grandTotal = 0;

                        // Calculate reset thresholds
                        Date dailyReset = getResetTimestamp(Calendar.DATE, 0); // Today 9AM
                        Date weeklyReset = getWeeklyResetTimestamp(); // Monday 9AM
                        Date monthlyReset = getResetTimestamp(Calendar.DAY_OF_MONTH, 1); // 1st of Month 9AM

                        for (DocumentSnapshot doc : value.getDocuments()) {
                            Double amt = doc.getDouble("totalAmount");
                            Timestamp ts = doc.getTimestamp("timestamp");

                            if (amt != null && ts != null) {
                                Date saleDate = ts.toDate();
                                grandTotal += amt;

                                // Filter into correct time buckets
                                if (saleDate.after(monthlyReset)) monthlyTotal += amt;
                                if (saleDate.after(weeklyReset)) weeklyTotal += amt;
                                if (saleDate.after(dailyReset)) dailyTotal += amt;

                                ArrayList<Map<String, Object>> itemsSold = (ArrayList<Map<String, Object>>) doc.get("itemsSold");
                                addDetailedTableRow(doc, itemsSold);
                            }
                        }

                        // Update UI with Locale safety
                        tvDailyIncome.setText(String.format(Locale.US, "LKR %.2f", dailyTotal));
                        tvWeeklyIncome.setText(String.format(Locale.US, "LKR %.2f", weeklyTotal));
                        tvMonthlyIncome.setText(String.format(Locale.US, "LKR %.2f", monthlyTotal));
                        tvTotalIncome.setText(String.format(Locale.US, "Total Income: LKR %.2f", grandTotal));
                    }
                });
    }

    // Generic reset helper for Daily and Monthly
    private Date getResetTimestamp(int field, int value) {
        Calendar cal = Calendar.getInstance();
        if (value != 0) cal.set(field, value);
        cal.set(Calendar.HOUR_OF_DAY, 9);
        cal.set(Calendar.MINUTE, 0);
        cal.set(Calendar.SECOND, 0);
        cal.set(Calendar.MILLISECOND, 0);

        // If it's currently before 9 AM, the reset happened yesterday
        if (new Date().before(cal.getTime()) && field == Calendar.DATE) {
            cal.add(Calendar.DATE, -1);
        }
        return cal.getTime();
    }

    // Specific logic for Monday 9:00 AM
    private Date getWeeklyResetTimestamp() {
        Calendar cal = Calendar.getInstance();
        cal.set(Calendar.DAY_OF_WEEK, Calendar.MONDAY);
        cal.set(Calendar.HOUR_OF_DAY, 9);
        cal.set(Calendar.MINUTE, 0);
        cal.set(Calendar.SECOND, 0);

        if (new Date().before(cal.getTime())) {
            cal.add(Calendar.WEEK_OF_YEAR, -1);
        }
        return cal.getTime();
    }

    private void addDetailedTableRow(DocumentSnapshot doc, ArrayList<Map<String, Object>> items) {
        TableRow row = new TableRow(this);
        row.setPadding(0, 10, 0, 10);

        TextView tvDate = new TextView(this);
        Timestamp ts = doc.getTimestamp("timestamp");
        tvDate.setText(ts != null ? ts.toDate().toString() : "Recent");
        tvDate.setTextSize(11);

        TextView tvDetails = new TextView(this);
        StringBuilder details = new StringBuilder();
        if (items != null) {
            for (Map<String, Object> item : items) {
                details.append(item.get("itemName")).append(" x").append(item.get("qty")).append("\n");
            }
        }
        tvDetails.setText(details.toString().trim());
        tvDetails.setTextSize(11);
        tvDetails.setPadding(20, 0, 20, 0);

        TextView tvAmt = new TextView(this);
        Double amount = doc.getDouble("totalAmount");
        tvAmt.setText(String.format(Locale.US, "%.2f", amount != null ? amount : 0.0));
        tvAmt.setGravity(android.view.Gravity.END);
        tvAmt.setTextSize(13);

        row.addView(tvDate);
        row.addView(tvDetails);
        row.addView(tvAmt);
        salesTable.addView(row);
    }
}