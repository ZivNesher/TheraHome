package com.ziv.therahome;

import android.os.Bundle;
import android.webkit.WebView;
import android.widget.ProgressBar;
import androidx.appcompat.app.AppCompatActivity;

import com.ziv.therahome.R;
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
        bleManager.setupScanButton(); // Prepare the scan button (but disabled initially)
        bleManager.loadUserDetailsButton();
        bleManager.loadPreviousScans();
        if (!AppManager.getInstance().isUserLoggedIn()) {
            bleManager.startInitialBleScan();
        }
        WebView webView = findViewById(R.id.instruction_webview);
        webView.getSettings().setJavaScriptEnabled(true);
        String html = "<iframe width=\"100%\" height=\"100%\" src=\"https://www.youtube.com/embed/bensREM3tnw\" frameborder=\"0\" allowfullscreen></iframe>";
        webView.loadData(html, "text/html", "utf-8");


    }

    public BleManager getBleManager() {
        return bleManager;
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
