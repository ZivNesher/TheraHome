package com.example.finalproject;

public class User {
    public String username;
    public String password;

    // Default constructor required for calls to DataSnapshot.getValue(User.class)
    public User() {
    }

    public User(String username, String password) {
        this.username = username;
        this.password = password;
    }
}

