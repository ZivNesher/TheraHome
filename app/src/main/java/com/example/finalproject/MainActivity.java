package com.example.finalproject;

import android.os.Bundle;
import android.widget.ProgressBar;
import androidx.appcompat.app.AppCompatActivity;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

public class MainActivity extends AppCompatActivity implements UserManagerCallback {

    private ProgressBar progressBar;
    private AuthManager authManager;
    private ScanManager scanManager;
    private UserManager userManager;
    private AuthUIController authUIController;
    private BleManager bleManager;

    private static final int RC_SIGN_IN = 9001;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.loading_screen);

        progressBar = findViewById(R.id.progressBar);
        authManager = new AuthManager(this, this, this);
        scanManager = new ScanManager(this);
        userManager = new UserManager(this);
        authUIController = new AuthUIController(this, authManager);
        bleManager = new BleManager(this, scanManager);

        progressBar.setVisibility(ProgressBar.VISIBLE);
        new android.os.Handler().postDelayed(() -> authUIController.loadLoginScreen(), 7000);
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
        bleManager.setupScanButton();
        bleManager.loadUserDetailsButton();
        bleManager.loadPreviousScans();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, android.content.Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == RC_SIGN_IN) {
            authManager.handleGoogleSignIn(data);
        }
    }
    @Override
    public void goToLoginScreen() {
        authUIController.loadLoginScreen();
    }

}
