package com.example.finalproject;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException;
import com.google.firebase.auth.FirebaseAuthInvalidUserException;
import com.google.firebase.auth.FirebaseUser;

public class MainActivity extends AppCompatActivity implements UserManagerCallback {
    private static final int RC_SIGN_IN = 9001;
    private ProgressBar progressBar;
    private EditText emailEditText;
    private EditText passwordEditText;
    private EditText usernameEditText;
    private EditText repasswordEditText;
    private EditText surnameEditText;
    private EditText firstnameEditText;
    private EditText ageEditText;
    private EditText heightEditText;
    private EditText weightEditText;
    private Button registerButton;
    private Button loginButton;
    private ImageButton scanButton;
    private ImageButton emailButton;
    private ImageButton gmailButton;
    private TableLayout scanTable;

    private AuthManager authManager;
    private ScanManager scanManager;
    private UserManager userManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.loading_screen);
        progressBar = findViewById(R.id.progressBar);

        authManager = new AuthManager(this,this);
        scanManager = new ScanManager(this);
        userManager = new UserManager(this); // Pass the activity as UserManagerCallback

        // Check if user is logged in
        showProgressBar();
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                loadLoginScreen();
            }
        }, 3000);
    }

    @Override
    public void onStart() {
        super.onStart();
        // Check if user is signed in (non-null) and update UI accordingly.
        FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
        if (currentUser != null) {
            currentUser.reload();
        }
    }

    private void showProgressBar() {
        progressBar.setVisibility(View.VISIBLE);
    }

    private void hideProgressBar() {
        progressBar.setVisibility(View.GONE);
    }

    private void loadLoginScreen() {
        setContentView(R.layout.login_screen);
        emailButton = findViewById(R.id.email_login_button);
        gmailButton = findViewById(R.id.gmail_login_button);
        loginButton = findViewById(R.id.login_button);

        emailButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                loadRegisterScreen();
            }

            private void loadRegisterScreen() {
                setContentView(R.layout.register_screen);
                usernameEditText = findViewById(R.id.username_input);
                passwordEditText = findViewById(R.id.password_input);
                repasswordEditText = findViewById(R.id.repassword_input);
                registerButton = findViewById(R.id.register_button);
                surnameEditText = findViewById(R.id.surname_input);
                firstnameEditText = findViewById(R.id.firstname_input);
                ageEditText = findViewById(R.id.age_input);
                heightEditText = findViewById(R.id.height_input);
                weightEditText = findViewById(R.id.weight_input);
                emailEditText = findViewById(R.id.email_input);

                registerButton.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        String email = emailEditText.getText().toString().trim();
                        String password = passwordEditText.getText().toString().trim();
                        String username = usernameEditText.getText().toString().trim();
                        String firstName = firstnameEditText.getText().toString().trim();
                        String surName = surnameEditText.getText().toString().trim();
                        String age = ageEditText.getText().toString().trim();
                        String height = heightEditText.getText().toString().trim();
                        String weight = weightEditText.getText().toString().trim();

                        authManager.registerUser(email, password, username, firstName, surName, age, weight, height);
                    }
                });
            }
        });

        gmailButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                signInWithGoogle();
            }
        });

        loginButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                emailEditText = findViewById(R.id.email_input); // Assuming you have added an EditText with this ID for email
                passwordEditText = findViewById(R.id.password_input); // Assuming you have added an EditText with this ID for password
                String email = emailEditText.getText().toString().trim();
                String password = passwordEditText.getText().toString().trim();

                authManager.loginUser(email, password);
            }
        });
    }

    private void signInWithGoogle() {
        Intent signInIntent = authManager.getGoogleSignInClient().getSignInIntent();
        startActivityForResult(signInIntent, RC_SIGN_IN);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        // Result returned from launching the Intent from GoogleSignInApi.getSignInIntent(...);
        if (requestCode == RC_SIGN_IN) {
            Task<GoogleSignInAccount> task = GoogleSignIn.getSignedInAccountFromIntent(data);
            handleSignInResult(task);
        }
    }

    private void handleSignInResult(Task<GoogleSignInAccount> completedTask) {
        try {
            GoogleSignInAccount account = completedTask.getResult(ApiException.class);
            // Google Sign In was successful, authenticate with Firebase
            authManager.firebaseAuthWithGoogle(account);
        } catch (ApiException e) {
            // Google Sign In failed, update UI appropriately
            Toast.makeText(MainActivity.this, "Google sign in failed", Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    public void loadMainActivity() {
        setContentView(R.layout.activity_main);

        // Initialize the scan button and set the click listener
        scanButton = findViewById(R.id.scan_button);
        scanButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                scanManager.performScanAndSaveData();
            }
        });

        // Initialize the scan table
        scanTable = findViewById(R.id.scan_table);

        // Load scan history
        FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
        if (currentUser != null) {
            scanManager.loadScanHistory(currentUser.getUid());
        }
    }

    public void addRowToTable(String date, int value, String comparison) {
        TableRow newRow = new TableRow(this);

        LinearLayout dateLayout = new LinearLayout(this);
        dateLayout.setLayoutParams(new TableRow.LayoutParams(0, TableRow.LayoutParams.WRAP_CONTENT, 2));
        TextView dateTextView = new TextView(this);
        dateTextView.setLayoutParams(new LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1));
        dateTextView.setText(date);
        dateLayout.addView(dateTextView);

        LinearLayout valueLayout = new LinearLayout(this);
        valueLayout.setLayoutParams(new TableRow.LayoutParams(0, TableRow.LayoutParams.WRAP_CONTENT, 2));
        TextView valueTextView = new TextView(this);
        valueTextView.setLayoutParams(new LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1));
        valueTextView.setText(String.valueOf(value));
        valueLayout.addView(valueTextView);

        LinearLayout comparisonLayout = new LinearLayout(this);
        comparisonLayout.setLayoutParams(new TableRow.LayoutParams(0, TableRow.LayoutParams.WRAP_CONTENT, 1));
        TextView comparisonTextView = new TextView(this);
        comparisonTextView.setLayoutParams(new LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1));
        comparisonTextView.setText(comparison);
        comparisonLayout.addView(comparisonTextView);

        newRow.addView(dateLayout);
        newRow.addView(valueLayout);
        newRow.addView(comparisonLayout);

        scanTable.addView(newRow);
    }
}
