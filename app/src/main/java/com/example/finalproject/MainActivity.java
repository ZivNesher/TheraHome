package com.example.finalproject;

import android.os.Bundle;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;


public class MainActivity extends AppCompatActivity {
    private ProgressBar progressBar;
    private FirebaseDatabase database;
    private DatabaseReference usersRef;
    private EditText usernameEditText;
    private EditText passwordEditText;
    private Button registerButton;
    //todo - connect to the login screen

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.loading_screen);
        progressBar = findViewById(R.id.progressBar);
        // Initialize Firebase
        database = FirebaseDatabase.getInstance();
        usersRef = database.getReference("users");

        // Check if user is logged in
        showProgressBar();
        loadLoginScreen();
        loadMainActivity();


    }
    private void saveUserData(String username, String password) {
        String userId = usersRef.push().getKey();
        String hashedPassword = hashPassword(password);

        User user = new User(username, hashedPassword);

        if (userId != null) {
            usersRef.child(userId).setValue(user);
        }
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

        if (!TextUtils.isEmpty(username) && !TextUtils.isEmpty(password)) {
            saveUserData(username, password);
        } else {
            Toast.makeText(this, "Please enter both username and password", Toast.LENGTH_SHORT).show();
        }
    }

    private void loadLoginScreen() {
        setContentView(R.layout.login_screen);
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
