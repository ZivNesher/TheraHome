# TheraHome: User Management and Scan Tracking Application

## Overview

**TheraHome** is a mobile application designed to support physical rehabilitation by tracking muscle activity and enabling therapist-patient interactions. The app allows users to register, log in (via email or Google), complete their profiles, perform EMG-based scans, and share scan data with their therapists in real-time using Firebase.

This project includes:
- A mobile app for patients  
- A web portal for therapists  
- Firebase backend integration

---

## Features

### 1. User Registration and Login
- Email and password registration
- Google Sign-In integration
- Email verification for new users

<img src="https://github.com/user-attachments/assets/14193de8-999d-4e73-a711-a97f3b59ba74" width="300"/>

---

### 2. Profile Completion
- Users complete their profile with personal details (age, weight, height, etc.)
- Data is saved in Firebase Realtime Database

<img src="https://github.com/user-attachments/assets/de27abfc-73e6-4e3e-b3d7-d037f7ab2d73" width="300"/>

---

### 3. Scan Tracking
- Users can perform EMG-based scans using the app
- Scan data is saved to Firebase and shared with the assigned therapist

<img width="341" alt="Scan Screen" src="https://github.com/user-attachments/assets/b39b5399-1e42-41fb-a1cf-1d56d2e14c79" />
<img src="https://github.com/user-attachments/assets/bf83e263-fb9e-499c-a761-886af7ca3dae" width="300"/>

---

### 4. Therapist Portal
- Therapists can log in via a web portal to monitor patient scan data
- Real-time data retrieval from Firebase

<img width="1332" alt="Portal 1" src="https://github.com/user-attachments/assets/4a1dc6a7-3f76-41ab-9a99-f639d24e4fb5" />
<img width="1332" alt="Portal 2" src="https://github.com/user-attachments/assets/a815e679-3bdf-47d9-a5b9-a8f6ee30ad9b" />
<img width="1329" alt="Portal 3" src="https://github.com/user-attachments/assets/598a66d9-45be-4a0b-aa26-824b5a220cfa" />

---

## Architecture

The project is structured in a modular way, with separate classes managing different functionalities. This promotes clean code organization, readability, and maintainability.

### Main Components

**MainActivity**  
- Handles login, registration, and user navigation  
- Integrates Google Sign-In and FirebaseAuth  

**ProfileCompletionActivity**  
- Prompts users to complete their profile  
- Saves user data to Firebase  

**User (Model)**  
- Represents user data structure  
- Fields include: `userId`, `email`, `password`, `firstName`, `surName`, `dateOfBirth`, `weight`, `height`  

**AuthManager**  
- Manages authentication logic  
- Handles email/password registration, Google Sign-In, and email verification  

**UserManager**  
- Manages profile data and interaction with Firebase  
- Checks profile completion status and saves user information  

**ScanManager**  
- Manages scan generation, saving, and retrieval  
- Stores scan data in Firebase under each user  

**UserManagerCallback**  
- Interface to decouple logic between `UserManager` and `MainActivity`  
- Triggers UI updates on successful data saving  

---

## Firebase Integration

**Authentication**  
- Email/password login and Google Sign-In  
- Email verification required for new users  

**Realtime Database**  
- Stores user profile data  
- Saves scan data under each user's node  
- Real-time updates for therapist dashboard  
