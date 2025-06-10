package com.ziv.therahome;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.FirebaseDatabase;
import android.app.DatePickerDialog;
import java.util.Calendar;


public class RegisterActivity extends AppCompatActivity {

    private FirebaseAuth auth;
    private LoadingDialogHelper loadingDialog;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.register_screen);

        auth = FirebaseAuth.getInstance();
        loadingDialog = new LoadingDialogHelper();

        // Input fields
        TextInputEditText emailInput = findViewById(R.id.email_input);
        TextInputEditText passInput = findViewById(R.id.password_input);
        TextInputEditText repassInput = findViewById(R.id.repassword_input);
        TextInputEditText idInput = findViewById(R.id.id_fill_input);
        TextInputEditText fnameInput = findViewById(R.id.firstname_input);
        TextInputEditText snameInput = findViewById(R.id.surname_input);
        TextInputEditText dobInput = findViewById(R.id.age_input);
        dobInput.setFocusable(false);
        dobInput.setOnClickListener(v -> {
            Calendar calendar = Calendar.getInstance();
            int year = calendar.get(Calendar.YEAR);
            int month = calendar.get(Calendar.MONTH);
            int day = calendar.get(Calendar.DAY_OF_MONTH);

            DatePickerDialog datePickerDialog = new DatePickerDialog(this, (view, selectedYear, selectedMonth, selectedDay) -> {
                // Format: YYYY-MM-DD
                String dateStr = String.format("%04d-%02d-%02d", selectedYear, selectedMonth + 1, selectedDay);
                dobInput.setText(dateStr);
            }, year, month, day);

            datePickerDialog.show();
        });
        TextInputEditText heightInput = findViewById(R.id.height_input);
        TextInputEditText weightInput = findViewById(R.id.weight_input);
        Button registerBtn = findViewById(R.id.register_button);
        ImageButton backBtn = findViewById(R.id.back_button);

        // Go back to LoginActivity
        backBtn.setOnClickListener(v -> {
            Intent intent = new Intent(this, LoginActivity.class);
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
            startActivity(intent);
            finish();
        });

        registerBtn.setOnClickListener(v -> {
            // Get values
            String email = emailInput.getText().toString().trim();
            String pass = passInput.getText().toString().trim();
            String repass = repassInput.getText().toString().trim();
            String id = idInput.getText().toString().trim();
            String fname = fnameInput.getText().toString().trim();
            String sname = snameInput.getText().toString().trim();
            String dob = dobInput.getText().toString().trim(); // e.g., 1995-08-25
            String height = heightInput.getText().toString().trim();
            String weight = weightInput.getText().toString().trim();

            // Validate
            if (email.isEmpty() || pass.isEmpty() || repass.isEmpty() || id.isEmpty()
                    || fname.isEmpty() || sname.isEmpty() || dob.isEmpty()
                    || height.isEmpty() || weight.isEmpty()) {
                Toast.makeText(this, "Please fill all fields", Toast.LENGTH_SHORT).show();
                return;
            }

            if (!pass.equals(repass)) {
                Toast.makeText(this, "Passwords do not match", Toast.LENGTH_SHORT).show();
                return;
            }

            loadingDialog.show(this);

            auth.createUserWithEmailAndPassword(email, pass).addOnCompleteListener(task -> {
                loadingDialog.hide();
                if (task.isSuccessful()) {
                    FirebaseUser user = auth.getCurrentUser();
                    if (user != null) {
                        user.sendEmailVerification();

                        // Create and save full User object
                        User newUser = new User(
                                id, pass, fname, sname, dob,
                                weight, height, user.getUid(), email
                        );

                        FirebaseDatabase.getInstance().getReference("users")
                                .child(user.getUid())
                                .setValue(newUser)
                                .addOnCompleteListener(saveTask -> {
                                    if (saveTask.isSuccessful()) {
                                        Toast.makeText(this, "Registration successful! Please verify your email.", Toast.LENGTH_LONG).show();
                                        auth.signOut();
                                        startActivity(new Intent(this, LoginActivity.class));
                                        finish();
                                    } else {
                                        Toast.makeText(this, "Failed to save user data", Toast.LENGTH_SHORT).show();
                                    }
                                });
                    }
                } else {
                    Toast.makeText(this, "Registration failed: " + task.getException().getMessage(), Toast.LENGTH_SHORT).show();
                }
            });
        });
    }
}
