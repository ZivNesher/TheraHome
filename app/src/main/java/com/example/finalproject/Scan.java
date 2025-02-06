package com.example.finalproject;

public class Scan {
    private String date;
    private int value;
    private String comparison;

    public Scan() {
        // Default constructor required for calls to DataSnapshot.getValue(Scan.class)
    }

    public Scan(String date, int value, String comparison) {
        this.date = date;
        this.value = value;
        this.comparison = comparison;
    }

    public String getDate() {
        return date;
    }

    public int getValue() {
        return value;
    }

    public String getComparison() {
        return comparison;
    }
}
