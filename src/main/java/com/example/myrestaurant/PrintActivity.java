package com.example.myrestaurant;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.pdf.PdfDocument;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.FileProvider;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Locale;

public class PrintActivity extends AppCompatActivity {

    private TableLayout billTable;
    private TextView tvBillDateTime, tvBillTotal;
    private LinearLayout billContainer;

    @Override



    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_print);

        // Initialize Views
        billTable = findViewById(R.id.billTable);
        tvBillDateTime = findViewById(R.id.tvBillDateTime);
        tvBillTotal = findViewById(R.id.tvBillTotal);
        billContainer = findViewById(R.id.billContainer);

        // Set Real-time Date
        String currentDateTime = new SimpleDateFormat("yyyy-MM-dd         HH:mm:ss", Locale.getDefault()).format(new Date());
        tvBillDateTime.setText("DATE: " + currentDateTime);

        // Get Data from Intent
        ArrayList<BillItem> billList = (ArrayList<BillItem>) getIntent().getSerializableExtra("BILL_LIST");
        double grandTotal = getIntent().getDoubleExtra("GRAND_TOTAL", 0.0);

        if (billList != null) {
            for (BillItem item : billList) {
                addBillRow(item.getItemName(), item.getQty(), item.getLinePrice());
            }
        }
        tvBillTotal.setText(String.format(Locale.getDefault(), "%.2f", grandTotal));

        // PDF Generation Button
        Button btnPrint = findViewById(R.id.btnGeneratePdf);
        btnPrint.setOnClickListener(v -> generateDynamicPdf());
    }

    private void generateWiderPdf() {
        // Force measurement of the new 600dp wide container
        billContainer.measure(
                android.view.View.MeasureSpec.makeMeasureSpec(600, android.view.View.MeasureSpec.EXACTLY),
                android.view.View.MeasureSpec.makeMeasureSpec(0, android.view.View.MeasureSpec.UNSPECIFIED)
        );

        int dynamicHeight = billContainer.getMeasuredHeight();
        int dynamicWidth = billContainer.getMeasuredWidth();

        // Create Bitmap and PDF with the new wider dimensions
        Bitmap bitmap = Bitmap.createBitmap(dynamicWidth, dynamicHeight, Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(bitmap);
        billContainer.draw(canvas);

        PdfDocument document = new PdfDocument();
        PdfDocument.PageInfo pageInfo = new PdfDocument.PageInfo.Builder(dynamicWidth, dynamicHeight, 1).create();
        PdfDocument.Page page = document.startPage(pageInfo);
        page.getCanvas().drawBitmap(bitmap, 0, 0, null);
        document.finishPage(page);

        File filePath = new File(getExternalFilesDir(Environment.DIRECTORY_DOCUMENTS), "VibezzBill_Wide.pdf");
        try {
            document.writeTo(new FileOutputStream(filePath));
            document.close();

            Uri uri = FileProvider.getUriForFile(this, getPackageName() + ".provider", filePath);
            Intent shareIntent = new Intent(Intent.ACTION_SEND);
            shareIntent.setType("application/pdf");
            shareIntent.putExtra(Intent.EXTRA_STREAM, uri);
            shareIntent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
            startActivity(Intent.createChooser(shareIntent, "Share Wider PDF to Fun Print"));
        } catch (IOException e) {
            Toast.makeText(this, "Print Error: " + e.getMessage(), Toast.LENGTH_SHORT).show();
        }
    }
    private void addBillRow(String name, int qty, double total) {
        TableRow row = new TableRow(this);
        row.setPadding(0, 12, 0, 12); // Extra spacing for larger paper

        TextView tvName = new TextView(this);
        tvName.setText(name);
        tvName.setTextSize(28); // Increased from 24sp
        tvName.setTextColor(android.graphics.Color.BLACK);

        TextView tvQty = new TextView(this);
        tvQty.setText(String.valueOf(qty));
        tvQty.setTextSize(28);
        tvQty.setGravity(android.view.Gravity.CENTER);
        tvQty.setTextColor(android.graphics.Color.BLACK);

        TextView tvTotal = new TextView(this);
        tvTotal.setText(String.format(Locale.getDefault(), "%.2f", total));
        tvTotal.setTextSize(28);
        tvTotal.setGravity(android.view.Gravity.END);
        tvTotal.setTextColor(android.graphics.Color.BLACK);

        row.addView(tvName);
        row.addView(tvQty);
        row.addView(tvTotal);
        billTable.addView(row);
    }


    private void generateDynamicPdf() {
        // Measure the container to get the dynamic height based on bill length
        billContainer.measure(
                android.view.View.MeasureSpec.makeMeasureSpec(billContainer.getWidth(), android.view.View.MeasureSpec.EXACTLY),
                android.view.View.MeasureSpec.makeMeasureSpec(0, android.view.View.MeasureSpec.UNSPECIFIED)
        );

        int totalHeight = billContainer.getMeasuredHeight();
        int totalWidth = billContainer.getMeasuredWidth();

        if (totalHeight <= 0 || totalWidth <= 0) {
            Toast.makeText(this, "Error: Layout not ready", Toast.LENGTH_SHORT).show();
            return;
        }

        // 1. Create Bitmap with dynamic height
        Bitmap bitmap = Bitmap.createBitmap(totalWidth, totalHeight, Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(bitmap);
        billContainer.draw(canvas);

        // 2. Wrap into PDF with adjusted page length
        PdfDocument document = new PdfDocument();
        PdfDocument.PageInfo pageInfo = new PdfDocument.PageInfo.Builder(totalWidth, totalHeight, 1).create();
        PdfDocument.Page page = document.startPage(pageInfo);
        page.getCanvas().drawBitmap(bitmap, 0, 0, null);
        document.finishPage(page);

        // 3. Save to storage
        File filePath = new File(getExternalFilesDir(Environment.DIRECTORY_DOCUMENTS), "VibezzBill.pdf");
        try {
            document.writeTo(new FileOutputStream(filePath));
            document.close();

            // 4. Share to Fun Print using FileProvider
            Uri uri = FileProvider.getUriForFile(this, getPackageName() + ".provider", filePath);
            Intent shareIntent = new Intent(Intent.ACTION_SEND);
            shareIntent.setType("application/pdf");
            shareIntent.putExtra(Intent.EXTRA_STREAM, uri);
            shareIntent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
            startActivity(Intent.createChooser(shareIntent, "Share to Fun Print"));
        } catch (IOException e) {
            Toast.makeText(this, "PDF Error: " + e.getMessage(), Toast.LENGTH_SHORT).show();
        }
    }
}