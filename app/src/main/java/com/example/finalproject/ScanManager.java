package com.example.finalproject;

import android.content.Context;
import android.widget.Toast;

import androidx.annotation.NonNull;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.Random;

public class ScanManager {
    private DatabaseReference userScansRef;
    private Context context;
    private String lastScanValue = "0"; // To hold the last scan value

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

            String scanData = currentDate + ", " + newValue + ", " + comparisonResult;
            lastScanValue = String.valueOf(newValue); // Update the last scan value

            userScansRef.push().setValue(scanData).addOnCompleteListener(task -> {
                if (task.isSuccessful()) {
                    ((MainActivity) context).addRowToTable(currentDate, newValue, comparisonResult);
                    Toast.makeText(context, "Scan data saved successfully", Toast.LENGTH_SHORT).show();
                } else {
                    Toast.makeText(context, "Failed to save scan data", Toast.LENGTH_SHORT).show();
                }
            });
        }
    }

    private String getComparisonResult(String lastValueStr, int newValue) {
        int lastValue = Integer.parseInt(lastValueStr);
        if (newValue > lastValue) {
            return "+";
        } else if (newValue < lastValue) {
            return "-";
        } else {
            return "=";
        }
    }

    private String getCurrentDate() {
        SimpleDateFormat sdf = new SimpleDateFormat("dd.MM.yyyy", Locale.getDefault());
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
            public void onDataChange(@NonNull com.google.firebase.database.DataSnapshot dataSnapshot) {
                for (com.google.firebase.database.DataSnapshot scanSnapshot : dataSnapshot.getChildren()) {
                    String scanData = scanSnapshot.getValue(String.class);
                    if (scanData != null) {
                        String[] parts = scanData.split(", ");
                        if (parts.length == 3) {
                            ((MainActivity) context).addRowToTable(parts[0], Integer.parseInt(parts[1]), parts[2]);
                        }
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull com.google.firebase.database.DatabaseError databaseError) {
                Toast.makeText(context, "Failed to load scan history", Toast.LENGTH_SHORT).show();
            }
        });
    }
}
