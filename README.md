# Final Project: User Management and Scan Tracking Application

## Overview

This project is a comprehensive user management and scan tracking application. It allows users to register, log in using email or Google Sign-In, and complete their profiles. Additionally, users can perform scans, save scan data, and view scan history. The application integrates with Firebase for authentication and real-time database functionalities.

## Features

1. **User Registration and Login:**
   - Email and Password Registration
   - Google Sign-In
   - Email Verification for New Users

![image](https://github.com/user-attachments/assets/14193de8-999d-4e73-a711-a97f3b59ba74)



2. **Profile Completion:**
   - Users needs complete their profiles with personal details.
   - Profile information is stored in Firebase Realtime Database.
  
![image](https://github.com/user-attachments/assets/de27abfc-73e6-4e3e-b3d7-d037f7ab2d73)




3. **Scan Tracking:**
   - Users can perform scans and compare them with previous scan values. (At this time, the values are generated randomly).
   - Scan data is saved to Firebase Realtime Database.
   - Users can view their scan history.
  
![image](https://github.com/user-attachments/assets/163c49cc-da77-4eb5-aef2-e02d47cb038a)




## Architecture

The project follows a modular design with separate classes for managing different aspects of the application. This enhances code readability, maintainability, and scalability.

### Main Components

1. **MainActivity:**
   - The primary activity that handles user navigation and initial setup.
   - Manages user login, registration, and Google Sign-In.

2. **ProfileCompletionActivity:**
   - Allows users to complete their profile with additional information.
   - Saves user profile data to Firebase.

3. **User:**
   - A model class representing the user.
   - Contains user attributes like username, password, first name, surname, age, weight, height, userId, and email.

4. **AuthManager:**
   - Manages user authentication.
   - Handles email/password registration, Google Sign-In, and email verification.

5. **UserManager:**
   - Manages user data interactions with Firebase.
   - Checks if user profile data is complete and handles user data saving.

6. **ScanManager:**
   - Manages scan-related functionalities.
   - Handles scan data generation, saving, and retrieval from Firebase.

7. **UserManagerCallback:**
   - An interface to decouple `UserManager` from the `MainActivity`.
   - Provides a callback method for loading the main activity.

## Firebase Integration

The application uses Firebase for:

1. **Authentication:**
   - Firebase Authentication for email/password login and Google Sign-In.
   - Email verification for new users.

2. **Database:**
   - Firebase Realtime Database for storing user profiles and scan data.
