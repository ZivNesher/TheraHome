package com.example.finalproject;

import android.app.DatePickerDialog;
import android.widget.*;
import androidx.appcompat.app.AppCompatActivity;
import com.google.android.gms.auth.api.signin.GoogleSignIn;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Locale;

public class AuthUIController {
    private final AppCompatActivity activity;
    private final AuthManager authManager;
    private Calendar calendar = Calendar.getInstance();

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
            EditText emailInput = activity.findViewById(R.id.email_input);
            EditText passInput = activity.findViewById(R.id.password_input);

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

        EditText ageInput = activity.findViewById(R.id.age_input);
        ageInput.setOnClickListener(v -> DatePickerHelper.showDatePicker(activity, calendar, ageInput));

        Button backBtn = activity.findViewById(R.id.back_button);
        backBtn.setOnClickListener(v -> loadLoginScreen());

        Button registerBtn = activity.findViewById(R.id.register_button);
        registerBtn.setOnClickListener(v -> {
            EditText email = activity.findViewById(R.id.email_input);
            EditText password = activity.findViewById(R.id.password_input);
            EditText rePassword = activity.findViewById(R.id.repassword_input);
            EditText id = activity.findViewById(R.id.id_fill_input);
            EditText fname = activity.findViewById(R.id.firstname_input);
            EditText sname = activity.findViewById(R.id.surname_input);
            EditText height = activity.findViewById(R.id.height_input);
            EditText weight = activity.findViewById(R.id.weight_input);

            if (!password.getText().toString().equals(rePassword.getText().toString())) {
                Toast.makeText(activity, "Passwords do not match", Toast.LENGTH_SHORT).show();
                return;
            }

            authManager.registerUser(
                    email.getText().toString().trim(),
                    password.getText().toString().trim(),
                    id.getText().toString().trim(),
                    fname.getText().toString().trim(),
                    sname.getText().toString().trim(),
                    ageInput.getText().toString().trim(),
                    weight.getText().toString().trim(),
                    height.getText().toString().trim()
            );
        });
    }

    private void showDatePicker(EditText ageInput) {
        DatePickerDialog picker = new DatePickerDialog(activity, (view, year, month, day) -> {
            calendar.set(year, month, day);
            ageInput.setText(new SimpleDateFormat("yyyy-MM-dd", Locale.US).format(calendar.getTime()));
        }, calendar.get(Calendar.YEAR), calendar.get(Calendar.MONTH), calendar.get(Calendar.DAY_OF_MONTH));

        picker.getDatePicker().setMaxDate(System.currentTimeMillis());
        picker.show();
    }
}
