package com.ziv.therahome;

import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.WindowManager;
import android.webkit.WebView;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.viewpager2.widget.ViewPager2;

import com.google.android.material.textview.MaterialTextView;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.ziv.therahome.R;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.messaging.FirebaseMessaging;


public class MainActivity extends AppCompatActivity implements UserManagerCallback {

    private ProgressBar progressBar;
    private AuthManager authManager;
    private ScanManager scanManager;
    private UserManager userManager;
    private AuthUIController authUIController;
    private BleManager bleManager;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.loading_screen);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        progressBar = findViewById(R.id.progressBar);
        authManager = new AuthManager(this, this, this);
        scanManager = new ScanManager(this);
        userManager = new UserManager(this);
        authUIController = new AuthUIController(this, authManager);
        bleManager = new BleManager(this, scanManager);

        progressBar.setVisibility(ProgressBar.VISIBLE);
        FirebaseMessaging.getInstance().getToken()
                .addOnCompleteListener(task -> {
                    if (!task.isSuccessful()) {
                        Log.w("FCM_TOKEN", "Fetching FCM registration token failed", task.getException());
                        return;
                    }

                    String token = task.getResult();
                    Log.d("FCM_TOKEN", "Token: " + token);

                    // Optional: Save it to Firebase under the user's UID
                    FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
                    if (user != null) {
                        DatabaseReference tokenRef = FirebaseDatabase.getInstance()
                                .getReference("users")
                                .child(user.getUid())
                                .child("fcmToken");
                        tokenRef.setValue(token);
                    }
                });


        FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
        if (currentUser != null && currentUser.isEmailVerified()) {
            userManager.checkUserData(currentUser.getUid(), this); // Go to main activity flow
        } else {
            new android.os.Handler().postDelayed(() -> authUIController.loadLoginScreen(), 7000);
        }

    }

    @Override
    public void onStart() {
        super.onStart();
        FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
        if (currentUser != null) currentUser.reload();
    }

    @Override
    public void loadMainActivity() {

        setContentView(R.layout.activity_main);
        Button reconnectButton = findViewById(R.id.reconnect_button);
        reconnectButton.setOnClickListener(v -> bleManager.startInitialBleScan());
        bleManager.setupScanButton();
        bleManager.loadUserDetailsButton();
        bleManager.loadPreviousScans();
        if (!AppManager.getInstance().isUserLoggedIn()) {
            bleManager.startInitialBleScan();
        }
        ViewPager2 instructionPager = findViewById(R.id.instruction_pager);
        int[] instructionImages = {
                R.drawable.instruction_1,
                R.drawable.instruction_2,
                R.drawable.instruction_3
        };
        ImageAdapter adapter = new ImageAdapter(this, instructionImages);
        instructionPager.setAdapter(adapter);

    }
    public void updateBleStatus(boolean isConnected) {
        MaterialTextView bleStatusTextView = findViewById(R.id.ble_status);
        Button reconnectButton = findViewById(R.id.reconnect_button);

        if (bleStatusTextView == null || reconnectButton == null) return;

        runOnUiThread(() -> {
            if (isConnected) {
                bleStatusTextView.setText("TheraHome BLE: Connected");
                bleStatusTextView.setTextColor(Color.parseColor("#4CAF50")); // Green
                reconnectButton.setVisibility(View.GONE);
            } else {
                bleStatusTextView.setText("TheraHome BLE: Not Connected");
                bleStatusTextView.setTextColor(Color.parseColor("#F44336")); // Red
                reconnectButton.setVisibility(View.VISIBLE);
            }
        });
    }



    public BleManager getBleManager() {
        return bleManager;
    }

    @Override
    public void goToLoginScreen() {
        Intent intent = new Intent(this, LoginActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(intent);
        finish();
    }


}
