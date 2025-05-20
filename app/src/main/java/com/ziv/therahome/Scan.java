package com.ziv.therahome;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

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

    private String getCurrentDate() {
        SimpleDateFormat sdf = new SimpleDateFormat("dd.MM.yyyy HH:mm", Locale.getDefault());
        return sdf.format(new Date());
    }
}
