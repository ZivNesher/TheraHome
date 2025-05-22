package com.ziv.therahome;

import android.app.DatePickerDialog;
import android.os.Bundle;
import android.text.TextUtils;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.ziv.therahome.R;
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
    private EditText dobEditText; // Replacing age field with Date of Birth
    private EditText heightEditText;
    private EditText weightEditText;
    private Button saveButton;
    private DatabaseReference usersRef;
    private String userId;
    private String email;
    private Calendar calendar; // Used for date selection
    private EditText IdEditText;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile_completion);

        IdEditText = findViewById(R.id.id_fill);
        surnameEditText = findViewById(R.id.surname_input);
        firstnameEditText = findViewById(R.id.firstname_input);
        dobEditText = findViewById(R.id.age_input); // Updated field
        heightEditText = findViewById(R.id.height_input);
        weightEditText = findViewById(R.id.weight_input);
        saveButton = findViewById(R.id.save_button);

        // Initialize Firebase
        usersRef = FirebaseDatabase.getInstance().getReference("users");

        // Get userId and email from Intent
        userId = getIntent().getStringExtra("userId");
        email = getIntent().getStringExtra("email");

        // Populate fields with current user data
        IdEditText.setText(getIntent().getStringExtra("ID"));
        surnameEditText.setText(getIntent().getStringExtra("surname"));
        firstnameEditText.setText(getIntent().getStringExtra("firstname"));
        dobEditText.setText(getIntent().getStringExtra("dateOfBirth"));
        heightEditText.setText(getIntent().getStringExtra("height"));
        weightEditText.setText(getIntent().getStringExtra("weight"));

        calendar = Calendar.getInstance();

        // Open DatePicker when DOB field is clicked
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
                    updateLabel();
                },
                year, month, day
        );

        // Restrict future dates
        datePickerDialog.getDatePicker().setMaxDate(System.currentTimeMillis());

        datePickerDialog.show();
    }

    private void updateLabel() {
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd", Locale.US);
        dobEditText.setText(sdf.format(calendar.getTime()));
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
        } else {
            // Create a map of fields to update
            Map<String, Object> updates = new HashMap<>();
            updates.put("Id", Id);
            updates.put("firstName", firstname);
            updates.put("surName", surname);
            updates.put("dateOfBirth", dateOfBirth); // Save dateOfBirth instead of age
            updates.put("height", height);
            updates.put("weight", weight);

            usersRef.child(userId).updateChildren(updates)
                    .addOnCompleteListener(task -> {
                        if (task.isSuccessful()) {
                            Toast.makeText(ProfileCompletionActivity.this, "Profile updated successfully", Toast.LENGTH_SHORT).show();
                            finish(); // Close the activity
                        } else {
                            Toast.makeText(ProfileCompletionActivity.this, "Failed to update profile", Toast.LENGTH_SHORT).show();
                        }
                    });
        }
    }
}
