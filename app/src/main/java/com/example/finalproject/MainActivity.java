package com.example.finalproject;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ProgressBar;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException;
import com.google.firebase.auth.FirebaseAuthInvalidUserException;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.GoogleAuthProvider;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

public class MainActivity extends AppCompatActivity {
    private static final int RC_SIGN_IN = 9001;
    private ProgressBar progressBar;
    private FirebaseDatabase database;
    private DatabaseReference usersRef;
    private EditText usernameEditText;
    private EditText passwordEditText;
    private EditText repasswordEditText;
    private EditText surnameEditText;
    private EditText firstnameEditText;
    private EditText ageEditText;
    private EditText heightEditText;
    private EditText weightEditText;
    private EditText mailEditText;
    private Button registerButton;
    private Button loginButton;
    private FirebaseAuth mAuth;
    private GoogleSignInClient mGoogleSignInClient;
    private ImageButton emailButton;
    private ImageButton gmailButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.loading_screen);
        progressBar = findViewById(R.id.progressBar);

        // Initialize Firebase
        database = FirebaseDatabase.getInstance();
        usersRef = database.getReference("users");
        mAuth = FirebaseAuth.getInstance();

        // Configure Google Sign-In
        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken(getString(R.string.default_web_client_id))
                .requestEmail()
                .build();
        mGoogleSignInClient = GoogleSignIn.getClient(this, gso);

        // Check if user is logged in
        showProgressBar();
        loadLoginScreen();
    }

    @Override
    public void onStart() {
        super.onStart();
        // Check if user is signed in (non-null) and update UI accordingly.
        FirebaseUser currentUser = mAuth.getCurrentUser();
        if (currentUser != null) {
            currentUser.reload();
        }
    }

    private void saveUserData(String userId, String email, String username, String password, String firstName, String surName, String age, String weight, String height) {
        String hashedPassword = hashPassword(password);
        User user = new User(username, hashedPassword, firstName, surName, age, weight, height, userId, email);

        usersRef.child(userId).setValue(user);
    }

    private String hashPassword(String password) {
        try {
            MessageDigest md = MessageDigest.getInstance("SHA-256");
            byte[] hash = md.digest(password.getBytes());
            StringBuilder hexString = new StringBuilder();
            for (byte b : hash) {
                hexString.append(String.format("%02x", b));
            }
            return hexString.toString();
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        }
        return null;
    }

    private void registerUser() {
        String username = usernameEditText.getText().toString().trim();
        String password = passwordEditText.getText().toString().trim();
        String repassword = repasswordEditText.getText().toString().trim();
        String surname = surnameEditText.getText().toString().trim();
        String firstname = firstnameEditText.getText().toString().trim();
        String age = ageEditText.getText().toString().trim();
        String height = heightEditText.getText().toString().trim();
        String weight = weightEditText.getText().toString().trim();
        String email = mailEditText.getText().toString().trim();

        if (!TextUtils.isEmpty(username) && !TextUtils.isEmpty(password) && password.equals(repassword)) {
            // Save user data to Firebase Realtime Database
            String userId = usersRef.push().getKey();
            saveUserData(userId, email, username, password, firstname, surname, age, weight, height);

            // Register the user with Firebase Authentication
            mAuth.createUserWithEmailAndPassword(email, password)
                    .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                        @Override
                        public void onComplete(@NonNull Task<AuthResult> task) {
                            if (task.isSuccessful()) {
                                // Registration successful
                                Toast.makeText(MainActivity.this, "Registration Successful", Toast.LENGTH_SHORT).show();
                            } else {
                                // Registration failed
                                Toast.makeText(MainActivity.this, "Registration Failed: " + task.getException().getMessage(), Toast.LENGTH_SHORT).show();
                            }
                        }
                    });
        } else {
            Toast.makeText(this, "Please fill all fields correctly", Toast.LENGTH_SHORT).show();
        }
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
                mailEditText = findViewById(R.id.email_input);

                registerButton.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        registerUser();
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
                loginUser();
            }
        });
    }

    private void loginUser() {
        EditText emailEditText = findViewById(R.id.email_input); // Assuming you have added an EditText with this ID for email
        EditText passwordEditText = findViewById(R.id.password_input); // Assuming you have added an EditText with this ID for password

        String email = emailEditText.getText().toString().trim();
        String password = passwordEditText.getText().toString().trim();

        if (TextUtils.isEmpty(email)) {
            Toast.makeText(MainActivity.this, "Enter email address!", Toast.LENGTH_SHORT).show();
            return;
        }

        if (TextUtils.isEmpty(password)) {
            Toast.makeText(MainActivity.this, "Enter password!", Toast.LENGTH_SHORT).show();
            return;
        }

        mAuth.signInWithEmailAndPassword(email, password)
                .addOnCompleteListener(MainActivity.this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {
                            // Sign in success
                            FirebaseUser user = mAuth.getCurrentUser();
                            checkUserData(user.getUid());
                        } else {
                            // If sign in fails, display a message to the user
                            try {
                                throw task.getException();
                            } catch (FirebaseAuthInvalidCredentialsException e) {
                                // Invalid password
                                Toast.makeText(MainActivity.this, "Invalid password. Please try again.", Toast.LENGTH_LONG).show();
                            } catch (FirebaseAuthInvalidUserException e) {
                                // Invalid email
                                Toast.makeText(MainActivity.this, "Invalid email. Please try again.", Toast.LENGTH_LONG).show();
                            } catch (Exception e) {
                                // Other errors
                                Toast.makeText(MainActivity.this, getString(R.string.auth_failed), Toast.LENGTH_LONG).show();
                            }
                        }
                    }
                });
    }

    private void signInWithGoogle() {
        Intent signInIntent = mGoogleSignInClient.getSignInIntent();
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
            firebaseAuthWithGoogle(account);
        } catch (ApiException e) {
            // Google Sign In failed, update UI appropriately
            Toast.makeText(MainActivity.this, "Google sign in failed", Toast.LENGTH_SHORT).show();
        }
    }

    private void firebaseAuthWithGoogle(GoogleSignInAccount acct) {
        AuthCredential credential = GoogleAuthProvider.getCredential(acct.getIdToken(), null);
        mAuth.signInWithCredential(credential)
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {
                            // Sign in success, update UI with the signed-in user's information
                            FirebaseUser user = mAuth.getCurrentUser();
                            checkUserData(user.getUid());
                        } else {
                            // If sign in fails, display a message to the user.
                            Toast.makeText(MainActivity.this, "Authentication Failed.", Toast.LENGTH_SHORT).show();
                        }
                    }
                });
    }

    private void checkUserData(String userId) {
        usersRef.child(userId).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                User user = snapshot.getValue(User.class);
                if (user == null || TextUtils.isEmpty(user.username) || TextUtils.isEmpty(user.firstName) ||
                        TextUtils.isEmpty(user.surName) || TextUtils.isEmpty(user.age) || TextUtils.isEmpty(user.height) ||
                        TextUtils.isEmpty(user.weight)) {
                    Intent intent = new Intent(MainActivity.this, ProfileCompletionActivity.class);
                    intent.putExtra("userId", userId);
                    intent.putExtra("email", user != null ? user.Email : "");
                    startActivity(intent);
                } else {
                    loadMainActivity();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(MainActivity.this, "Failed to load user data", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void loadMainActivity() {
        setContentView(R.layout.activity_main);
    }

    private void showProgressBar() {
        progressBar.setVisibility(View.VISIBLE);
    }

    private void hideProgressBar() {
        progressBar.setVisibility(View.GONE);
    }
}
