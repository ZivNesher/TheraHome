package com.ziv.therahome;

import android.content.Intent;
import android.widget.*;
import androidx.appcompat.app.AppCompatActivity;

import com.ziv.therahome.R;
import com.google.android.material.textfield.TextInputEditText;

import java.util.Calendar;

public class AuthUIController {
    private final AppCompatActivity activity;
    private final AuthManager authManager;
    private final Calendar calendar = Calendar.getInstance();

    public AuthUIController(AppCompatActivity activity, AuthManager authManager) {
        this.activity = activity;
        this.authManager = authManager;
    }

    public void loadLoginScreen() {
        activity.setContentView(R.layout.login_screen);
        ImageButton emailBtn = activity.findViewById(R.id.email_login_button);
        ImageButton gmailBtn = activity.findViewById(R.id.gmail_login_button);
        Button loginBtn = activity.findViewById(R.id.login_button);

        emailBtn.setOnClickListener(v -> loadRegisterScreen());
        gmailBtn.setOnClickListener(v -> {
            android.content.Intent signInIntent = authManager.getGoogleSignInClient().getSignInIntent();
            activity.startActivityForResult(signInIntent, 9001);
        });

        loginBtn.setOnClickListener(v -> {
            TextInputEditText emailInput = activity.findViewById(R.id.email_input);
            TextInputEditText passInput = activity.findViewById(R.id.password_input);

            String email = emailInput.getText().toString().trim();
            String pass = passInput.getText().toString().trim();

            if (email.isEmpty() || pass.isEmpty()) {
                Toast.makeText(activity, "Email and password are required", Toast.LENGTH_SHORT).show();
            } else {
                authManager.loginUser(email, pass);
            }
        });
    }

    private void loadRegisterScreen() {
        activity.setContentView(R.layout.register_screen);

        TextInputEditText dobInput = activity.findViewById(R.id.age_input); // נשאר עם אותו ID
        dobInput.setFocusable(false);
        dobInput.setClickable(true);
        dobInput.setOnClickListener(v -> DatePickerHelper.showDatePicker(activity, calendar, dobInput));

        ImageButton backBtn = activity.findViewById(R.id.back_button);
        backBtn.setOnClickListener(v -> loadLoginScreen());

        Button registerBtn = activity.findViewById(R.id.register_button);
        registerBtn.setOnClickListener(v -> {
            TextInputEditText email = activity.findViewById(R.id.email_input);
            TextInputEditText password = activity.findViewById(R.id.password_input);
            TextInputEditText rePassword = activity.findViewById(R.id.repassword_input);
            TextInputEditText id = activity.findViewById(R.id.id_fill_input);
            TextInputEditText fname = activity.findViewById(R.id.firstname_input);
            TextInputEditText sname = activity.findViewById(R.id.surname_input);
            TextInputEditText weight = activity.findViewById(R.id.weight_input);
            TextInputEditText height = activity.findViewById(R.id.height_input);

            String emailStr = email.getText().toString().trim();
            String passwordStr = password.getText().toString().trim();
            String rePasswordStr = rePassword.getText().toString().trim();
            String idStr = id.getText().toString().trim();
            String fnameStr = fname.getText().toString().trim();
            String snameStr = sname.getText().toString().trim();
            String dobStr = dobInput.getText().toString().trim();
            String weightStr = weight.getText().toString().trim();
            String heightStr = height.getText().toString().trim();

            if (emailStr.isEmpty() || passwordStr.isEmpty() || rePasswordStr.isEmpty() ||
                    idStr.isEmpty() || fnameStr.isEmpty() || snameStr.isEmpty() ||
                    dobStr.isEmpty() || weightStr.isEmpty() || heightStr.isEmpty()) {
                Toast.makeText(activity, "All fields must be filled", Toast.LENGTH_SHORT).show();
                return;
            }

            if (!passwordStr.equals(rePasswordStr)) {
                Toast.makeText(activity, "Passwords do not match", Toast.LENGTH_SHORT).show();
                return;
            }

            authManager.registerUser(
                    emailStr,
                    passwordStr,
                    idStr,
                    fnameStr,
                    snameStr,
                    dobStr,
                    weightStr,
                    heightStr,
                    user -> {
                        Intent intent = new Intent(activity, ProfileCompletionActivity.class);
                        intent.putExtra("userId", user.getUid());
                        intent.putExtra("email", emailStr);
                        intent.putExtra("ID", idStr);
                        intent.putExtra("firstname", fnameStr);
                        intent.putExtra("surname", snameStr);
                        intent.putExtra("dateOfBirth", dobStr);
                        intent.putExtra("height", heightStr);
                        intent.putExtra("weight", weightStr);
                        activity.startActivity(intent);
                    }
            );
        });
    }
}
