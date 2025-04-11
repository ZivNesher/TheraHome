# TheraHome: User Management and Scan Tracking Application

## Overview

This project is a comprehensive user management and scan tracking application. It allows users to register, log in using email or Google Sign-In, and complete their profiles. Additionally, users can perform scans, save scan data, and let their therapist view scan history. The application integrates with Firebase for authentication and real-time database functionalities.

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
   - Users can perform scans, The scan will be sent to his therapist so he can check the values.
   - Scan data is saved to Firebase Realtime Database.
  
<img width="341" alt="image" src="https://github.com/user-attachments/assets/b39b5399-1e42-41fb-a1cf-1d56d2e14c79" />
![image](https://github.com/user-attachments/assets/8fe03aab-0d4b-45e4-b65b-d49c2a1cb0ea)


4. **Therapist portal:**
   - User that his role is therapist, have access to check all patients scans values.
   - The data is streaming in realtime from firebase.
  
<img width="1332" alt="image" src="https://github.com/user-attachments/assets/4a1dc6a7-3f76-41ab-9a99-f639d24e4fb5" /><img width="1332" alt="image" src="https://github.com/user-attachments/assets/a815e679-3bdf-47d9-a5b9-a8f6ee30ad9b" />
<img width="1329" alt="image" src="https://github.com/user-attachments/assets/598a66d9-45be-4a0b-aa26-824b5a220cfa" />







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
