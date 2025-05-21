# TheraHome: Rehab Monitoring System

**TheraHome** is a full-stack rehabilitation tracking system designed to help patients recover from physical injuries and allow therapists to monitor their progress remotely. The solution includes an Android mobile application for patients and a React-based web portal for therapists.

---

## Features

### Android Patient App
- Firebase Authentication (Email & Google Sign-In)
- Guided profile completion: ID, date of birth, height, weight
- BLE (Bluetooth Low Energy) scan button to simulate EMG data input
- Real-time Firebase Realtime Database integration
- Scan history saved and displayed chronologically
- Pop-up user profile view and animated app logo

### Therapist Web Portal
- Firebase Authentication with Admin/Staff role support
- Patient search by ID with profile and scan history display
- Interactive graph of all scans sorted by date
- Exercise-based visualization and session averaging
- Admin panel to add authorized therapist accounts
- For testing feel free to contact me for a test user - https://bodysync-f5388.web.app/

---

## Tech Stack

| Platform        | Technologies                                |
|----------------|---------------------------------------------|
| Android (Java) | Firebase Auth, Realtime DB, BLE, XML Layout |
| Web (React)    | React.js, Firebase, Material UI, Chart.js   |

---

## User Roles

- **Patients** (Android):
  - Register/login and complete their profile
  - Perform BLE scans simulating EMG readings
  - View their scan history

- **Therapists** (Web):
  - Login to the web portal (authorized by admin)
  - Search patients and view scan charts
  - Visualize scan progress over time

- **Admin**:
  - Use hardcoded credentials to access admin dashboard
  - Add new therapist users to the Firebase database

---

### Android App
1. Clone the repo and open in Android Studio.
2. Set up Firebase project and update `google-services.json`.
3. Run on emulator or physical device with Bluetooth support.

