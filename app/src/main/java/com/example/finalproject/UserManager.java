package com.example.finalproject;

import android.content.Context;
import android.content.Intent;
import android.text.TextUtils;
import android.widget.Toast;

import androidx.annotation.NonNull;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

public class UserManager {
    DatabaseReference usersRef;
    private UserManagerCallback callback;

    public UserManager(UserManagerCallback callback) {
        this.usersRef = FirebaseDatabase.getInstance().getReference("users");
        this.callback = callback;
    }

    public void saveUserData(String userId, String email, String username, String password, String firstName, String surName, String age, String weight, String height) {
        User user = new User(username, password, firstName, surName, age, weight, height, userId, email);
        usersRef.child(userId).setValue(user);
    }

    public void checkUserData(String userId, Context context) {
        usersRef.child(userId).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                User user = snapshot.getValue(User.class);
                if (user == null || TextUtils.isEmpty(user.username) || TextUtils.isEmpty(user.firstName) ||
                        TextUtils.isEmpty(user.surName) || TextUtils.isEmpty(user.age) || TextUtils.isEmpty(user.height) ||
                        TextUtils.isEmpty(user.weight)) {
                    Intent intent = new Intent(context, ProfileCompletionActivity.class);
                    intent.putExtra("userId", userId);
                    intent.putExtra("email", user != null ? user.Email : "");
                    context.startActivity(intent);
                } else {
                    callback.loadMainActivity();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(context, "Failed to load user data", Toast.LENGTH_SHORT).show();
            }
        });
    }

    public void checkUserData(String userId, String email, Context context) {
        usersRef.child(userId).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                User user = snapshot.getValue(User.class);
                if (user == null || TextUtils.isEmpty(user.username) || TextUtils.isEmpty(user.firstName) ||
                        TextUtils.isEmpty(user.surName) || TextUtils.isEmpty(user.age) || TextUtils.isEmpty(user.height) ||
                        TextUtils.isEmpty(user.weight)) {
                    Intent intent = new Intent(context, ProfileCompletionActivity.class);
                    intent.putExtra("userId", userId);
                    intent.putExtra("email", email); // Pass the email extracted from GoogleSignInAccount
                    context.startActivity(intent);
                } else {
                    callback.loadMainActivity();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(context, "Failed to load user data", Toast.LENGTH_SHORT).show();
            }
        });
    }
}
