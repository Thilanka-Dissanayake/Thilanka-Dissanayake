package com.example.myrestaurant;

import java.io.Serializable;

/**
 * BillItem class represents a single row in the customer's bill.
 * It implements Serializable to allow passing lists between Activities.
 */
public class BillItem implements Serializable {
    private String itemName;
    private int qty;
    private double unitPrice;
    private double linePrice;

    // Constructor to initialize all fields
    public BillItem(String itemName, int qty, double unitPrice, double linePrice) {
        this.itemName = itemName;
        this.qty = qty;
        this.unitPrice = unitPrice;
        this.linePrice = linePrice;
    }

    // Required getters for the PrintActivity logic
    public String getItemName() {
        return itemName != null ? itemName : "Unknown Item";
    }

    public int getQty() {
        return qty;
    }

    public double getLinePrice() {
        return linePrice;
    }

    // Added getter for unit price in case you want to show it on the bill
    public double getUnitPrice() {
        return unitPrice;
    }

    // Added setters in case you need to update an item after it's added to the list
    public void setItemName(String itemName) { this.itemName = itemName; }
    public void setQty(int qty) { this.qty = qty; }
    public void setUnitPrice(double unitPrice) { this.unitPrice = unitPrice; }
    public void setLinePrice(double linePrice) { this.linePrice = linePrice; }
}