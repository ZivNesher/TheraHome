package com.example.finalproject;

import android.content.Context;
import android.widget.Toast;

import androidx.annotation.NonNull;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.Random;

public class ScanManager {
    private DatabaseReference userScansRef;
    private Context context;
    private int lastScanValue = 0; // To hold the last scan value
    private List<Scan> allScans = new ArrayList<>();

    public List<Scan> getAllScans() {
        return allScans;
    }

    public ScanManager(Context context) {
        this.context = context;
    }

    public void performScanAndSaveData() {
        FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
        if (currentUser != null) {
            String userId = currentUser.getUid();
            userScansRef = FirebaseDatabase.getInstance().getReference("users").child(userId).child("scans");

            int newValue = getRandomValue();
            String currentDate = getCurrentDate();
            String comparisonResult = getComparisonResult(lastScanValue, newValue);

            Scan scan = new Scan(currentDate, newValue, comparisonResult);
            lastScanValue = newValue; // Update the last scan value

            userScansRef.push().setValue(scan).addOnCompleteListener(task -> {
                if (task.isSuccessful()) {
                    allScans.add(scan); // ✅ Add new scan to the global list
                    ((MainActivity) context).displayScanHistoryOnGraph(allScans, 10); // ✅ Refresh graph
                    Toast.makeText(context, "Scan data saved successfully", Toast.LENGTH_SHORT).show();
                } else {
                    Toast.makeText(context, "Failed to save scan data", Toast.LENGTH_SHORT).show();
                }
            });
        }
    }

    private String getComparisonResult(int lastValue, int newValue) {
        if (newValue > lastValue) {
            return "+";
        } else if (newValue < lastValue) {
            return "-";
        } else {
            return "=";
        }
    }

    private String getCurrentDate() {
        SimpleDateFormat sdf = new SimpleDateFormat("dd.MM.yyyy HH:mm", Locale.getDefault());
        return sdf.format(new Date());
    }

    private int getRandomValue() {
        Random random = new Random();
        return 100 + random.nextInt(401); // Random value between 100 and 500
    }

    public void loadScanHistory(String userId) {
        userScansRef = FirebaseDatabase.getInstance().getReference("users").child(userId).child("scans");
        userScansRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                allScans.clear(); // ✅ Clear existing scans before loading new data
                for (DataSnapshot scanSnapshot : dataSnapshot.getChildren()) {
                    try {
                        Scan scan = scanSnapshot.getValue(Scan.class);
                        if (scan != null) {
                            allScans.add(scan);
                        }
                    } catch (Exception e) {
                        String scanData = scanSnapshot.getValue(String.class);
                        if (scanData != null) {
                            String[] parts = scanData.split(", ");
                            if (parts.length == 3) {
                                try {
                                    String date = parts[0];
                                    int value = Integer.parseInt(parts[1]);
                                    String comparison = parts[2];
                                    Scan fallbackScan = new Scan(date, value, comparison);
                                    allScans.add(fallbackScan);
                                } catch (NumberFormatException nfe) {
                                    Toast.makeText(context, "Invalid scan data format", Toast.LENGTH_SHORT).show();
                                }
                            }
                        }
                    }
                }
                ((MainActivity) context).displayScanHistoryOnGraph(allScans, 10); // ✅ Display the latest scans
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                Toast.makeText(context, "Failed to load scan history", Toast.LENGTH_SHORT).show();
            }
        });
    }
}
