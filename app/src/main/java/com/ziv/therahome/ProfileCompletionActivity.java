package com.ziv.therahome;

import android.app.DatePickerDialog;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

public class ProfileCompletionActivity extends AppCompatActivity {

    private EditText surnameEditText;
    private EditText firstnameEditText;
    private EditText dobEditText;
    private EditText heightEditText;
    private EditText weightEditText;
    private EditText IdEditText;
    private Button saveButton;
    private boolean fromEdit = false;


    private DatabaseReference usersRef;
    private String userId;
    private String email;
    private Calendar calendar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile_completion);

        IdEditText = findViewById(R.id.id_fill);
        surnameEditText = findViewById(R.id.surname_input);
        firstnameEditText = findViewById(R.id.firstname_input);
        dobEditText = findViewById(R.id.age_input);
        heightEditText = findViewById(R.id.height_input);
        weightEditText = findViewById(R.id.weight_input);
        saveButton = findViewById(R.id.save_button);
        fromEdit = getIntent().getBooleanExtra("fromEdit", false);


        // Disable manual DOB input
        dobEditText.setFocusable(false);
        dobEditText.setClickable(true);
        dobEditText.setKeyListener(null);

        // Firebase
        usersRef = FirebaseDatabase.getInstance().getReference("users");
        userId = getIntent().getStringExtra("userId");
        email = getIntent().getStringExtra("email");

        // Prefill if available
        IdEditText.setText(getIntent().getStringExtra("ID"));
        surnameEditText.setText(getIntent().getStringExtra("surname"));
        firstnameEditText.setText(getIntent().getStringExtra("firstname"));
        dobEditText.setText(getIntent().getStringExtra("dateOfBirth"));
        heightEditText.setText(getIntent().getStringExtra("height"));
        weightEditText.setText(getIntent().getStringExtra("weight"));

        calendar = Calendar.getInstance();

        dobEditText.setOnClickListener(v -> showDatePicker());
        saveButton.setOnClickListener(v -> saveUserData());
    }

    private void showDatePicker() {
        int year = calendar.get(Calendar.YEAR);
        int month = calendar.get(Calendar.MONTH);
        int day = calendar.get(Calendar.DAY_OF_MONTH);

        DatePickerDialog datePickerDialog = new DatePickerDialog(
                this,
                (view, selectedYear, selectedMonth, selectedDay) -> {
                    calendar.set(selectedYear, selectedMonth, selectedDay);
                    SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd", Locale.US);
                    dobEditText.setText(sdf.format(calendar.getTime()));
                },
                year, month, day
        );

        // Disable future dates
        datePickerDialog.getDatePicker().setMaxDate(System.currentTimeMillis());

        // Optional: Enforce age limit (e.g., at least 10 years old)
        Calendar minCal = Calendar.getInstance();
        minCal.add(Calendar.YEAR, -100); // max 100 years old
        datePickerDialog.getDatePicker().setMinDate(minCal.getTimeInMillis());

        datePickerDialog.show();
    }

    private void saveUserData() {
        String Id = IdEditText.getText().toString().trim();
        String surname = surnameEditText.getText().toString().trim();
        String firstname = firstnameEditText.getText().toString().trim();
        String dateOfBirth = dobEditText.getText().toString().trim();
        String height = heightEditText.getText().toString().trim();
        String weight = weightEditText.getText().toString().trim();

        if (TextUtils.isEmpty(Id) || TextUtils.isEmpty(surname) || TextUtils.isEmpty(firstname) ||
                TextUtils.isEmpty(dateOfBirth) || TextUtils.isEmpty(height) || TextUtils.isEmpty(weight)) {
            Toast.makeText(this, "Please fill all fields", Toast.LENGTH_SHORT).show();
            return;
        }

        if (userId == null || userId.isEmpty()) {
            Toast.makeText(this, "Invalid userId — cannot save.", Toast.LENGTH_LONG).show();
            return;
        }

        Map<String, Object> updates = new HashMap<>();
        updates.put("Id", Id);
        updates.put("firstName", firstname);
        updates.put("surName", surname);
        updates.put("dateOfBirth", dateOfBirth);
        updates.put("height", height);
        updates.put("weight", weight);

        Log.d("ProfileSave", "Saving to /users/" + userId);

        usersRef.child(userId).updateChildren(updates)
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        Toast.makeText(this, "Profile updated successfully", Toast.LENGTH_SHORT).show();
                        if (fromEdit) {
                            finish();
                        } else {
                            startActivity(new Intent(this, MainActivity.class));
                            finish();
                        }
                    } else {
                        Toast.makeText(this, "Failed to update profile", Toast.LENGTH_SHORT).show();
                    }
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(this, "Error: " + e.getMessage(), Toast.LENGTH_LONG).show();
                    e.printStackTrace();
                });
    }

}
