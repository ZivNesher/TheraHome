package com.example.finalproject;

public class User {
    public String username;
    public String password;
    public String firstName;
    public String surName;
    public String age;
    public String weight;
    public String height;
    public String userId;
    public String Email;




    // Default constructor required for calls to DataSnapshot.getValue(User.class)
    public User() {
    }

    public User(String username, String password, String firstName, String surName, String age, String weight, String height, String userId, String Email) {
        this.username = username;
        this.password = password;
        this.firstName = firstName;
        this.surName = surName;
        this.age = age;
        this.weight = weight;
        this.height = height;
        this.userId = userId;
        this.Email = Email;
    }
    // todo login


    //todo register

}

