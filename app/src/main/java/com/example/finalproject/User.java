package com.example.finalproject;

import java.io.Serializable;
import java.time.LocalDate;
import java.time.Period;
import java.time.format.DateTimeFormatter;

public class User implements Serializable {
    public String username;
    public String password;
    public String firstName;
    public String surName;
    public String dateOfBirth; // Store birthdate instead of age
    public String weight;
    public String height;
    public String userId;
    public String Email;

    // Default constructor required for calls to DataSnapshot.getValue(User.class)
    public User() {
    }

    public User(String username, String password, String firstName, String surName, String dateOfBirth, String weight, String height, String userId, String Email) {
        this.username = username;
        this.password = password;
        this.firstName = firstName;
        this.surName = surName;
        this.dateOfBirth = dateOfBirth;
        this.weight = weight;
        this.height = height;
        this.userId = userId;
        this.Email = Email;
    }

    // Method to calculate age dynamically
    public int getAge() {
        if (dateOfBirth == null || dateOfBirth.isEmpty()) {
            return -1; // Handle case where DOB is not set
        }
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd"); // Adjust format as needed
        LocalDate birthDate = LocalDate.parse(dateOfBirth, formatter);
        return Period.between(birthDate, LocalDate.now()).getYears();
    }
}
