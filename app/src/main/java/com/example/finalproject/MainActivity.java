package com.example.finalproject;

import android.animation.Animator;
import android.animation.ValueAnimator;
import android.annotation.SuppressLint;
import android.app.Dialog;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Handler;
import android.util.TypedValue;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.github.mikephil.charting.charts.LineChart;
import com.github.mikephil.charting.components.LimitLine;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.LineData;
import com.github.mikephil.charting.data.LineDataSet;
import com.github.mikephil.charting.formatter.ValueFormatter;
import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity implements UserManagerCallback {
    private static final int RC_SIGN_IN = 9001;
    private ProgressBar progressBar;
    private EditText emailEditText;
    private EditText passwordEditText;
    private EditText usernameEditText;
    private EditText repasswordEditText;
    private EditText surnameEditText;
    private EditText firstnameEditText;
    private EditText ageEditText;
    private EditText heightEditText;
    private EditText weightEditText;
    private Button registerButton;
    private Button loginButton;
    private ImageButton scanButton;
    private ImageButton emailButton;
    private ImageButton gmailButton;
    private ImageButton backButton;
    private LineChart lineChart;
    private AuthManager authManager;
    private ScanManager scanManager;
    private UserManager userManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.loading_screen);
        progressBar = findViewById(R.id.progressBar);

        authManager = new AuthManager(this, this, this);  // Pass MainActivity instance
        scanManager = new ScanManager(this);
        userManager = new UserManager(this); // Pass the activity as UserManagerCallback

        // Check if user is logged in
        showProgressBar();
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                loadLoginScreen();
            }
        }, 7000);
    }

    @Override
    public void onStart() {
        super.onStart();
        // Check if user is signed in (non-null) and update UI accordingly.
        FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
        if (currentUser != null) {
            currentUser.reload();
        }
    }

    private void showProgressBar() {
        progressBar.setVisibility(View.VISIBLE);
    }

    private void hideProgressBar() {
        progressBar.setVisibility(View.GONE);
    }

    @SuppressLint("WrongViewCast")
    public void loadLoginScreen() {
        setContentView(R.layout.login_screen);
        emailButton = findViewById(R.id.email_login_button);
        gmailButton = findViewById(R.id.gmail_login_button);
        loginButton = findViewById(R.id.login_button);

        emailButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                loadRegisterScreen();
            }

            private void loadRegisterScreen() {
                setContentView(R.layout.register_screen);
                usernameEditText = findViewById(R.id.username_input);
                passwordEditText = findViewById(R.id.password_input);
                repasswordEditText = findViewById(R.id.repassword_input);
                registerButton = findViewById(R.id.register_button);
                backButton = findViewById(R.id.back_button);
                surnameEditText = findViewById(R.id.surname_input);
                firstnameEditText = findViewById(R.id.firstname_input);
                ageEditText = findViewById(R.id.age_input);
                heightEditText = findViewById(R.id.height_input);
                weightEditText = findViewById(R.id.weight_input);
                emailEditText = findViewById(R.id.email_input);

                backButton.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        loadLoginScreen();
                    }
                });

                registerButton.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        String email = emailEditText.getText().toString().trim();
                        String password = passwordEditText.getText().toString().trim();
                        String username = usernameEditText.getText().toString().trim();
                        String firstName = firstnameEditText.getText().toString().trim();
                        String surName = surnameEditText.getText().toString().trim();
                        String age = ageEditText.getText().toString().trim();
                        String height = heightEditText.getText().toString().trim();
                        String weight = weightEditText.getText().toString().trim();
                        if(!password.equals(repasswordEditText.getText().toString().trim())) {
                            Toast.makeText(MainActivity.this, "Passwords do not match", Toast.LENGTH_SHORT).show();
                            return;
                        }
                        authManager.registerUser(email, password, username, firstName, surName, age, weight, height);
                    }
                });
            }
        });

        gmailButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                signInWithGoogle();
            }
        });

        loginButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                emailEditText = findViewById(R.id.email_input);
                passwordEditText = findViewById(R.id.password_input);

                String email = emailEditText.getText().toString().trim();
                String password = passwordEditText.getText().toString().trim();

                // Check if email or password fields are empty
                if (email.isEmpty()) {
                    Toast.makeText(MainActivity.this, "Please enter your email", Toast.LENGTH_SHORT).show();
                    return;  // Exit the method to prevent further execution
                }

                if (password.isEmpty()) {
                    Toast.makeText(MainActivity.this, "Please enter your password", Toast.LENGTH_SHORT).show();
                    return;  // Exit the method to prevent further execution
                }

                // Proceed with login if both fields are filled
                authManager.loginUser(email, password);
            }
        });

    }

    private void signInWithGoogle() {
        Intent signInIntent = authManager.getGoogleSignInClient().getSignInIntent();
        startActivityForResult(signInIntent, RC_SIGN_IN);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        // Result returned from launching the Intent from GoogleSignInApi.getSignInIntent(...);
        if (requestCode == RC_SIGN_IN) {
            Task<GoogleSignInAccount> task = GoogleSignIn.getSignedInAccountFromIntent(data);
            handleSignInResult(task);
        }
    }

    private void handleSignInResult(Task<GoogleSignInAccount> completedTask) {
        try {
            GoogleSignInAccount account = completedTask.getResult(ApiException.class);
            // Google Sign In was successful, authenticate with Firebase
            authManager.firebaseAuthWithGoogle(account);
        } catch (ApiException e) {
            // Google Sign In failed, update UI appropriately
            Toast.makeText(MainActivity.this, "Google sign in failed", Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    public void loadMainActivity() {
        setContentView(R.layout.activity_main);

        // ✅ Initialize the LineChart before loading scan history
        lineChart = findViewById(R.id.line_chart);
        if (lineChart == null) {
            Toast.makeText(this, "Chart not initialized. Check layout ID!", Toast.LENGTH_SHORT).show();
            return; // Prevents further execution if the chart is not found
        }

        // Initialize the scan button and set the click listener
        scanButton = findViewById(R.id.scan_button);
        Button btnLast10 = findViewById(R.id.btn_last_10);
        Button btnLast30 = findViewById(R.id.btn_last_30);
        Button btnLast100 = findViewById(R.id.btn_last_100);

        btnLast10.setOnClickListener(v -> displayScanHistoryOnGraph(scanManager.getAllScans(), 10));
        btnLast30.setOnClickListener(v -> displayScanHistoryOnGraph(scanManager.getAllScans(), 30));
        btnLast100.setOnClickListener(v -> displayScanHistoryOnGraph(scanManager.getAllScans(), 100));



        scanButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showPopUp();
                scanManager.performScanAndSaveData();
            }
        });

        // Initialize the burger menu button and set the click listener
        ImageButton burgerMenuButton = findViewById(R.id.menu);
        burgerMenuButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
                if (currentUser != null) {
                    UserManager userManager = new UserManager(new UserManagerCallback() {
                        @Override
                        public void loadMainActivity() {
                            // Not needed here
                        }
                    });

                    userManager.usersRef.child(currentUser.getUid()).addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot snapshot) {
                            User user = snapshot.getValue(User.class);
                            if (user != null) {
                                UserDetailsBottomSheet bottomSheet = UserDetailsBottomSheet.newInstance(user);
                                bottomSheet.show(getSupportFragmentManager(), "UserDetailsBottomSheet");
                            }
                        }

                        @Override
                        public void onCancelled(@NonNull DatabaseError error) {
                            Toast.makeText(MainActivity.this, "Failed to load user data", Toast.LENGTH_SHORT).show();
                        }
                    });
                }
            }
        });

        // ✅ Load scan history after initializing the LineChart
        FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
        if (currentUser != null) {
            scanManager.loadScanHistory(currentUser.getUid());
        }
    }


    public void addScanToGraph(Scan scan) {
        LineData data = lineChart.getData();
        if (data == null) {
            data = new LineData();
            lineChart.setData(data);
        }

        LineDataSet dataSet;
        if (data.getDataSetCount() == 0) {
            dataSet = new LineDataSet(new ArrayList<>(), "Scan History");
            dataSet.setColor(getResources().getColor(R.color.main2));
            dataSet.setValueTextColor(getResources().getColor(android.R.color.white));
            data.addDataSet(dataSet);
        } else {
            dataSet = (LineDataSet) data.getDataSetByIndex(0);
        }

        int index = dataSet.getEntryCount();
        data.addEntry(new Entry(index, scan.getValue()), 0);
        data.notifyDataChanged();
        lineChart.notifyDataSetChanged();
        lineChart.invalidate();
    }
    public void displayScanHistoryOnGraph(List<Scan> scans, int limit) {
        List<Entry> entries = new ArrayList<>();
        List<String> dateLabels = new ArrayList<>();

        // ✅ Limit to last 10, 30, or 100 scans
        int startIndex = Math.max(0, scans.size() - limit);

        for (int i = startIndex; i < scans.size(); i++) {
            entries.add(new Entry(i - startIndex, scans.get(i).getValue()));
            dateLabels.add(scans.get(i).getDate());
        }

        LineDataSet dataSet = new LineDataSet(entries, "Scan History");
        dataSet.setColor(getResources().getColor(R.color.main2));
        dataSet.setValueTextColor(getResources().getColor(android.R.color.white));
        dataSet.setLineWidth(2f);
        dataSet.setCircleRadius(4f);
        dataSet.setDrawCircles(true);
        dataSet.setDrawValues(true);

        LineData lineData = new LineData(dataSet);
        lineChart.setData(lineData);

        // ✅ Custom X-Axis Formatting
        XAxis xAxis = lineChart.getXAxis();
        xAxis.setPosition(XAxis.XAxisPosition.BOTTOM);
        xAxis.setDrawGridLines(false);
        xAxis.setGranularity(1f);
        xAxis.setLabelRotationAngle(-45); // ✅ Diagonal direction

        xAxis.setValueFormatter(new ValueFormatter() {
            @Override
            public String getFormattedValue(float value) {
                int index = (int) value;
                return (index >= 0 && index < dateLabels.size()) ? dateLabels.get(index) : "";
            }
        });

        // ✅ Enable Horizontal Scrolling
        lineChart.setDragEnabled(true);
        lineChart.setScaleXEnabled(true); // Enable horizontal zoom if needed
        lineChart.setVisibleXRangeMaximum(10); // Default to showing 10 entries

        lineChart.getDescription().setText("Scan Timeline (Date & Time)");
        lineChart.getDescription().setTextColor(getResources().getColor(android.R.color.white));
        lineChart.getAxisRight().setEnabled(false);

        lineChart.invalidate();
    }


    public void showPopUp() {
        // Create the dialog
        Dialog dialog = new Dialog(this);
        dialog.setContentView(R.layout.logo_pop_up);

        // Get references to the ImageView and the mask view
        ImageView logoImageView = dialog.findViewById(R.id.logoImageView);
        View waterMask = dialog.findViewById(R.id.waterMask);


        // Set up the mask animation to create the water-filling effect
        ValueAnimator animator = ValueAnimator.ofInt(0, 100);
        animator.setDuration(3000); // Duration of the animation
        animator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator animation) {
                int value = (Integer) animation.getAnimatedValue();
                ViewGroup.LayoutParams layoutParams = waterMask.getLayoutParams();
                layoutParams.height = (int) TypedValue.applyDimension(
                        TypedValue.COMPLEX_UNIT_DIP, value, getResources().getDisplayMetrics());
                waterMask.setLayoutParams(layoutParams);
            }
        });

        // Add a listener to close the dialog after the animation ends
        animator.addListener(new Animator.AnimatorListener() {
            @Override
            public void onAnimationStart(Animator animation) {
                // No action needed at the start of the animation
            }

            @Override
            public void onAnimationEnd(Animator animation) {
                // Close the dialog when the animation ends
                dialog.dismiss();
            }

            @Override
            public void onAnimationCancel(Animator animation) {
                // Optional: Close the dialog if the animation is canceled
                dialog.dismiss();
            }

            @Override
            public void onAnimationRepeat(Animator animation) {
                // No action needed on animation repeat
            }
        });

        // Start the animation
        animator.start();

        // Show the dialog
        dialog.show();
    }




}
