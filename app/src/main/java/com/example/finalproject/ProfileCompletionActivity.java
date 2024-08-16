package com.example.finalproject;

import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

public class ProfileCompletionActivity extends AppCompatActivity {

    private EditText usernameEditText;
    private EditText surnameEditText;
    private EditText firstnameEditText;
    private EditText ageEditText;
    private EditText heightEditText;
    private EditText weightEditText;
    private Button saveButton;
    private DatabaseReference usersRef;
    private String userId;
    private String email;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile_completion);

        usernameEditText = findViewById(R.id.username_input);
        surnameEditText = findViewById(R.id.surname_input);
        firstnameEditText = findViewById(R.id.firstname_input);
        ageEditText = findViewById(R.id.age_input);
        heightEditText = findViewById(R.id.height_input);
        weightEditText = findViewById(R.id.weight_input);
        saveButton = findViewById(R.id.save_button);

        // Initialize Firebase
        usersRef = FirebaseDatabase.getInstance().getReference("users");

        // Get userId and email from Intent
        userId = getIntent().getStringExtra("userId");
        email = getIntent().getStringExtra("email");

        // Populate fields with current user data
        usernameEditText.setText(getIntent().getStringExtra("username"));
        surnameEditText.setText(getIntent().getStringExtra("surname"));
        firstnameEditText.setText(getIntent().getStringExtra("firstname"));
        ageEditText.setText(getIntent().getStringExtra("age"));
        heightEditText.setText(getIntent().getStringExtra("height"));
        weightEditText.setText(getIntent().getStringExtra("weight"));

        saveButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                saveUserData();
            }
        });
    }


    private void saveUserData() {
        String username = usernameEditText.getText().toString().trim();
        String surname = surnameEditText.getText().toString().trim();
        String firstname = firstnameEditText.getText().toString().trim();
        String age = ageEditText.getText().toString().trim();
        String height = heightEditText.getText().toString().trim();
        String weight = weightEditText.getText().toString().trim();

        if (TextUtils.isEmpty(username) || TextUtils.isEmpty(surname) || TextUtils.isEmpty(firstname) ||
                TextUtils.isEmpty(age) || TextUtils.isEmpty(height) || TextUtils.isEmpty(weight)) {
            Toast.makeText(this, "Please fill all fields", Toast.LENGTH_SHORT).show();
        } else {
            User user = new User(username, "", firstname, surname, age, weight, height, userId, email);
            usersRef.child(userId).setValue(user)
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
