package com.example.finalproject;

import android.content.Context;
import android.widget.Toast;
import androidx.annotation.NonNull;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.*;
import java.text.SimpleDateFormat;
import java.util.*;

public class ScanManager {
    private DatabaseReference userScansRef;
    private Context context;
    private int lastScanValue = 0;
    private List<Scan> allScans = new ArrayList<>();

    public List<Scan> getAllScans() {
        return allScans;
    }

    public ScanManager(Context context) {
        this.context = context;
    }

    /**
     * This is your new method that accepts an external reading
     * (like from BLE). We store & handle the data in Firebase.
     */
    public void performScanAndSaveData(int newValue) {
        FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
        if (currentUser != null) {
            String userId = currentUser.getUid();
            userScansRef = FirebaseDatabase.getInstance().getReference("users")
                    .child(userId).child("scans");

            String currentDate = getCurrentDate();
            String comparisonResult = getComparisonResult(lastScanValue, newValue);

            Scan scan = new Scan(currentDate, newValue, comparisonResult);
            lastScanValue = newValue; // update for next comparison

            userScansRef.push().setValue(scan).addOnCompleteListener(task -> {
                if (task.isSuccessful()) {
                    allScans.add(scan);
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

    /**
     * Load any previously stored scans in Firebase so we can
     * display them when the user logs in.
     */
    public void loadScanHistory(String userId) {
        userScansRef = FirebaseDatabase.getInstance().getReference("users")
                .child(userId).child("scans");

        userScansRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                allScans.clear();
                for (DataSnapshot scanSnapshot : dataSnapshot.getChildren()) {
                    try {
                        Scan scan = scanSnapshot.getValue(Scan.class);
                        if (scan != null) {
                            allScans.add(scan);
                        }
                    } catch (Exception e) {
                        // fallback approach if data is stored differently
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                Toast.makeText(context, "Failed to load scan history", Toast.LENGTH_SHORT).show();
            }
        });
    }
}
