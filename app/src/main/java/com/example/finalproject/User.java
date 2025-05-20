package com.example.finalproject;

import com.google.firebase.database.Exclude;

import java.io.Serializable;
import java.time.LocalDate;
import java.time.Period;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;

public class User implements Serializable {
    public String password;
    public String firstName;
    public String surName;
    public String dateOfBirth; // Store birthdate instead of age
    public String weight;
    public String height;
    public String userId;
    public String Email;
    public String Id;

    // Default constructor required for calls to DataSnapshot.getValue(User.class)
    public User() {
    }

    public User(String Id, String password, String firstName, String surName, String dateOfBirth, String weight, String height, String userId, String Email) {
        this.password = password;
        this.firstName = firstName;
        this.surName = surName;
        this.dateOfBirth = dateOfBirth;
        this.weight = weight;
        this.height = height;
        this.userId = userId;
        this.Email = Email;
        this.Id = Id;
    }


    @Exclude
    public int getAge() {
        if (dateOfBirth == null || dateOfBirth.isEmpty()) {
            return -1; // No DOB
        }
        try {
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");
            LocalDate birthDate = LocalDate.parse(dateOfBirth, formatter);
            return Period.between(birthDate, LocalDate.now()).getYears();
        } catch (DateTimeParseException e) {
            return -1; // Invalid format
        }
    }
}
