package com.example.finalproject;

import android.Manifest;
import android.animation.Animator;
import android.animation.ValueAnimator;
import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.app.DatePickerDialog;
import android.app.Dialog;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothGattCallback;
import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothGattDescriptor;
import android.bluetooth.BluetoothGattService;
import android.bluetooth.BluetoothManager;
import android.bluetooth.BluetoothProfile;
import android.bluetooth.le.BluetoothLeScanner;
import android.bluetooth.le.ScanCallback;
import android.bluetooth.le.ScanResult;
import android.bluetooth.le.ScanSettings;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.util.TypedValue;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.ProgressBar;
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
import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.ValueEventListener;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Locale;

public class MainActivity extends AppCompatActivity implements UserManagerCallback {
    private static final int RC_SIGN_IN = 9001;
    private ProgressBar progressBar;
    private EditText emailEditText;
    private EditText passwordEditText;
    private EditText IdEditText;
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
    private Calendar calendar;

    // BLE-related fields
    private static final String TAG = "MainActivityBLE";
    // (Optional) Default device name, though we'll show all devices in the picker
    private static final String DEVICE_NAME = "TheraHome";
    private static final int BLE_PERMISSION_REQUEST_CODE = 1001;

    private BluetoothAdapter bluetoothAdapter;
    private BluetoothLeScanner bluetoothLeScanner;
    private BluetoothGatt bluetoothGatt;

    // UUIDs (must match your Arduino BLE configuration)
    private static final String SERVICE_UUID_180A = "0000180a-0000-1000-8000-00805f9b34fb";
    private static final String SWITCH_CHAR_UUID = "00002a57-0000-1000-8000-00805f9b34fb";
    private static final String EMG_CHAR_UUID    = "00002a58-0000-1000-8000-00805f9b34fb";
    private static final String NOTIFY_DESCRIPTOR_UUID = "00002902-0000-1000-8000-00805f9b34fb";

    // Discovered BLE characteristics
    private BluetoothGattCharacteristic switchCharacteristic;
    private BluetoothGattCharacteristic emgCharacteristic;

    // Fields for the device picker
    private final List<BluetoothDevice> discoveredDevices = new ArrayList<>();
    private final List<String> discoveredDeviceNames = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.loading_screen);
        progressBar = findViewById(R.id.progressBar);

        authManager = new AuthManager(this, this, this);
        scanManager = new ScanManager(this);
        userManager = new UserManager(this);

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
                IdEditText = findViewById(R.id.id_fill_input);
                passwordEditText = findViewById(R.id.password_input);
                repasswordEditText = findViewById(R.id.repassword_input);
                registerButton = findViewById(R.id.register_button);
                backButton = findViewById(R.id.back_button);
                surnameEditText = findViewById(R.id.surname_input);
                firstnameEditText = findViewById(R.id.firstname_input);
                heightEditText = findViewById(R.id.height_input);
                weightEditText = findViewById(R.id.weight_input);
                emailEditText = findViewById(R.id.email_input);
                ageEditText = findViewById(R.id.age_input);
                calendar = Calendar.getInstance();

                ageEditText.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        showDatePicker();
                    }
                });

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
                        String Id = IdEditText.getText().toString().trim();
                        String firstName = firstnameEditText.getText().toString().trim();
                        String surName = surnameEditText.getText().toString().trim();
                        String age = ageEditText.getText().toString().trim();
                        String height = heightEditText.getText().toString().trim();
                        String weight = weightEditText.getText().toString().trim();
                        if(!password.equals(repasswordEditText.getText().toString().trim())) {
                            Toast.makeText(MainActivity.this, "Passwords do not match", Toast.LENGTH_SHORT).show();
                            return;
                        }
                        authManager.registerUser(email, password, Id, firstName, surName, age, weight, height);
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

                if (email.isEmpty()) {
                    Toast.makeText(MainActivity.this, "Please enter your email", Toast.LENGTH_SHORT).show();
                    return;
                }
                if (password.isEmpty()) {
                    Toast.makeText(MainActivity.this, "Please enter your password", Toast.LENGTH_SHORT).show();
                    return;
                }

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

        // Google sign-in
        if (requestCode == RC_SIGN_IN) {
            Task<GoogleSignInAccount> task = GoogleSignIn.getSignedInAccountFromIntent(data);
            handleSignInResult(task);
        }
    }

    private void handleSignInResult(Task<GoogleSignInAccount> completedTask) {
        try {
            GoogleSignInAccount account = completedTask.getResult(ApiException.class);
            authManager.firebaseAuthWithGoogle(account);
        } catch (ApiException e) {
            Toast.makeText(MainActivity.this, "Google sign in failed", Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    public void loadMainActivity() {
        setContentView(R.layout.activity_main);

        // Initialize the LineChart
        lineChart = findViewById(R.id.line_chart);
        if (lineChart == null) {
            Toast.makeText(this, "Chart not initialized. Check layout ID!", Toast.LENGTH_SHORT).show();
            return;
        }

        // Buttons to filter scans
        scanButton = findViewById(R.id.scan_button);
        scanButton.setEnabled(false);

        Button btnLast10 = findViewById(R.id.btn_last_10);
        Button btnLast30 = findViewById(R.id.btn_last_30);
        Button btnLast100 = findViewById(R.id.btn_last_100);

        // Update graph with last X scans
        btnLast10.setOnClickListener(v ->
                displayScanHistoryOnGraph(scanManager.getAllScans(), 10));
        btnLast30.setOnClickListener(v ->
                displayScanHistoryOnGraph(scanManager.getAllScans(), 30));
        btnLast100.setOnClickListener(v ->
                displayScanHistoryOnGraph(scanManager.getAllScans(), 100));

        // Request BLE permissions if needed
        if (checkSelfPermission(Manifest.permission.BLUETOOTH_SCAN) != PackageManager.PERMISSION_GRANTED ||
                checkSelfPermission(Manifest.permission.BLUETOOTH_CONNECT) != PackageManager.PERMISSION_GRANTED ||
                checkSelfPermission(Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            requestPermissions(new String[]{
                    Manifest.permission.BLUETOOTH_SCAN,
                    Manifest.permission.BLUETOOTH_CONNECT,
                    Manifest.permission.ACCESS_FINE_LOCATION
            }, BLE_PERMISSION_REQUEST_CODE);
        } else {
            startBleScan();
        }

        // SCAN button: when pressed, write 0x01 to Arduino (if already connected)
        scanButton.setOnClickListener(v -> {
            showPopUp();
            writeSwitchCharacteristic((byte) 0x01);
        });

        // Burger menu button
        ImageButton burgerMenuButton = findViewById(R.id.menu);
        burgerMenuButton.setOnClickListener(v -> {
            FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
            if (currentUser != null) {
                UserManager userManager = new UserManager(() -> { /* Not needed here */ });
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
        });

        // Finally, load any existing scan history
        FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
        if (currentUser != null) {
            scanManager.loadScanHistory(currentUser.getUid());
        }
    }

    // === BLE METHODS BELOW ===

    /**
     * Start scanning for BLE devices.
     * This scan will run for 10 seconds and then show a device picker dialog.
     */
    private void startBleScan() {
        // Check for required permissions
        if (checkSelfPermission(Manifest.permission.BLUETOOTH_SCAN) != PackageManager.PERMISSION_GRANTED ||
                checkSelfPermission(Manifest.permission.BLUETOOTH_CONNECT) != PackageManager.PERMISSION_GRANTED ||
                checkSelfPermission(Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {

            requestPermissions(new String[]{
                    Manifest.permission.BLUETOOTH_SCAN,
                    Manifest.permission.BLUETOOTH_CONNECT,
                    Manifest.permission.ACCESS_FINE_LOCATION
            }, BLE_PERMISSION_REQUEST_CODE);
            return;
        }

        BluetoothManager manager = (BluetoothManager) getSystemService(BLUETOOTH_SERVICE);
        if (manager != null) {
            bluetoothAdapter = manager.getAdapter();
        }

        if (bluetoothAdapter == null) {
            Toast.makeText(this, "Bluetooth not supported on this device", Toast.LENGTH_SHORT).show();
            return;
        }

        if (!bluetoothAdapter.isEnabled()) {
            Intent enableBtIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
            startActivityForResult(enableBtIntent, 1002);
            return;
        }

        bluetoothLeScanner = bluetoothAdapter.getBluetoothLeScanner();
        if (bluetoothLeScanner == null) {
            Toast.makeText(this, "BLE not supported on this device", Toast.LENGTH_SHORT).show();
            return;
        }

        // Clear previously discovered devices
        discoveredDevices.clear();
        discoveredDeviceNames.clear();

        ScanSettings settings = new ScanSettings.Builder()
                .setScanMode(ScanSettings.SCAN_MODE_LOW_LATENCY)
                .build();

        Toast.makeText(this, "Scanning for BLE devices...", Toast.LENGTH_SHORT).show();
        // Scan without filters to show all devices
        bluetoothLeScanner.startScan(null, settings, scanCallback);

        // Stop scanning after 10 seconds and show the picker
        new Handler(Looper.getMainLooper()).postDelayed(() -> {
            bluetoothLeScanner.stopScan(scanCallback);
            Toast.makeText(this, "Scan complete", Toast.LENGTH_SHORT).show();
            showDevicePickerDialog();
        }, 10000);
    }

    /**
     * Callback for BLE scan results.
     * Discovered devices are added to a list for user selection.
     */
    private final ScanCallback scanCallback = new ScanCallback() {
        @SuppressLint("MissingPermission")
        @Override
        public void onScanResult(int callbackType, ScanResult result) {
            BluetoothDevice device = result.getDevice();
            String name = device.getName() != null ? device.getName() : "Unnamed Device";
            String displayName = name + " [" + device.getAddress() + "]";

            if (!discoveredDevices.contains(device)) {
                discoveredDevices.add(device);
                discoveredDeviceNames.add(displayName);
                Log.d(TAG, "Found device: " + displayName);
            }
        }

        @Override
        public void onScanFailed(int errorCode) {
            Log.e(TAG, "BLE scan failed with error: " + errorCode);
        }
    };

    /**
     * Displays a dialog listing all discovered BLE devices.
     * The user can select a device to connect to.
     */
    private void showDevicePickerDialog() {
        if (discoveredDevices.isEmpty()) {
            Toast.makeText(this, "No BLE devices found", Toast.LENGTH_SHORT).show();
            return;
        }

        new AlertDialog.Builder(this)
                .setTitle("Select a Bluetooth Device")
                .setItems(discoveredDeviceNames.toArray(new String[0]), (dialog, which) -> {
                    BluetoothDevice selectedDevice = discoveredDevices.get(which);
                    connectToDevice(selectedDevice);
                })
                .setNegativeButton("Cancel", null)
                .show();
    }

    /**
     * Connect to the BLE device and discover GATT services.
     */
    @SuppressLint("MissingPermission")
    private void connectToDevice(BluetoothDevice device) {
        Toast.makeText(this, "Connecting to " + device.getName(), Toast.LENGTH_SHORT).show();
        bluetoothGatt = device.connectGatt(this, false, gattCallback, BluetoothDevice.TRANSPORT_LE);
    }

    private final BluetoothGattCallback gattCallback = new BluetoothGattCallback() {
        @SuppressLint("MissingPermission")
        @Override
        public void onConnectionStateChange(BluetoothGatt gatt, int status, int newState) {
            if (newState == BluetoothProfile.STATE_CONNECTED) {
                Log.i(TAG, "Connected to GATT. Discovering services...");
                bluetoothGatt.discoverServices();
            } else if (newState == BluetoothProfile.STATE_DISCONNECTED) {
                Log.i(TAG, "Disconnected from GATT.");
                if (bluetoothGatt != null) {
                    bluetoothGatt.close();
                    bluetoothGatt = null;
                }
            }
        }

        @Override
        public void onServicesDiscovered(BluetoothGatt gatt, int status) {
            if (status == BluetoothGatt.GATT_SUCCESS) {
                BluetoothGattService service = gatt.getService(java.util.UUID.fromString(SERVICE_UUID_180A));
                if (service != null) {
                    switchCharacteristic = service.getCharacteristic(
                            java.util.UUID.fromString(SWITCH_CHAR_UUID));
                    emgCharacteristic = service.getCharacteristic(
                            java.util.UUID.fromString(EMG_CHAR_UUID));

                    if (switchCharacteristic != null) {
                        Log.i(TAG, "Switch characteristic ready.");
                        runOnUiThread(() -> scanButton.setEnabled(true));
                    } else {
                        Log.e(TAG, "Switch characteristic not found!");
                    }

                    if (emgCharacteristic != null) {
                        setNotifications(emgCharacteristic, true);
                    }
                }
            }
        }

        @Override
        public void onCharacteristicChanged(BluetoothGatt gatt, BluetoothGattCharacteristic characteristic) {
            super.onCharacteristicChanged(gatt, characteristic);

            // If the Arduino is sending EMG values on 2A58, handle them here.
            if (characteristic.getUuid().toString().equalsIgnoreCase(EMG_CHAR_UUID)) {
                int emgValue = characteristic.getIntValue(BluetoothGattCharacteristic.FORMAT_SINT32, 0);
                Log.d(TAG, "Received EMG: " + emgValue);
                runOnUiThread(() -> scanManager.performScanAndSaveData(emgValue));
            }
        }
    };

    /**
     * Helper to enable notifications for the given characteristic.
     */
    @SuppressLint("MissingPermission")
    private void setNotifications(BluetoothGattCharacteristic characteristic, boolean enable) {
        if (bluetoothGatt == null) return;

        bluetoothGatt.setCharacteristicNotification(characteristic, enable);

        BluetoothGattDescriptor descriptor = characteristic.getDescriptor(
                java.util.UUID.fromString(NOTIFY_DESCRIPTOR_UUID));
        if (descriptor != null) {
            descriptor.setValue(enable
                    ? BluetoothGattDescriptor.ENABLE_NOTIFICATION_VALUE
                    : BluetoothGattDescriptor.DISABLE_NOTIFICATION_VALUE);
            bluetoothGatt.writeDescriptor(descriptor);
        }
    }

    /**
     * Write a single byte command to the switchCharacteristic (2A57).
     * For "case 1", we write 0x01.
     */
    private void writeSwitchCharacteristic(byte value) {
        if (switchCharacteristic == null || bluetoothGatt == null) {
            Toast.makeText(this, "Switch characteristic not ready. Did you scan/connect?", Toast.LENGTH_SHORT).show();
            return;
        }
        if (checkSelfPermission(Manifest.permission.BLUETOOTH_CONNECT) != PackageManager.PERMISSION_GRANTED) {
            Toast.makeText(this, "No BLUETOOTH_CONNECT permission", Toast.LENGTH_SHORT).show();
            return;
        }

        switchCharacteristic.setValue(new byte[]{ value });
        boolean success = bluetoothGatt.writeCharacteristic(switchCharacteristic);
        if (!success) {
            Toast.makeText(this, "Failed to write to switchCharacteristic", Toast.LENGTH_SHORT).show();
        } else {
            Toast.makeText(this, "Wrote 0x" + String.format("%02X", value) + " to switchCharacteristic", Toast.LENGTH_SHORT).show();
        }
    }

    // === END BLE METHODS ===

    // === Existing chart & UI logic for your EMG scans ===

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
            dataSet.setValueTextColor(getResources().getColor(android.R.color.holo_green_dark));
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

        int startIndex = Math.max(0, scans.size() - limit);

        if (limit == 10) {
            for (int i = startIndex; i < scans.size(); i++) {
                entries.add(new Entry(i - startIndex, scans.get(i).getValue()));
                dateLabels.add(scans.get(i).getDate());
            }
        } else {
            int groupSize = (limit == 30) ? 3 : 10;
            int totalGroups = 10;
            int scansPerGroup = groupSize;
            int scansToConsider = totalGroups * scansPerGroup;
            int adjustedStartIndex = Math.max(0, scans.size() - scansToConsider);

            for (int i = adjustedStartIndex; i < scans.size(); i += scansPerGroup) {
                int sum = 0, count = 0;
                for (int j = i; j < i + scansPerGroup && j < scans.size(); j++) {
                    sum += scans.get(j).getValue();
                    count++;
                }
                if (count > 0) {
                    float mean = (float) sum / count;
                    entries.add(new Entry(entries.size(), mean));
                    dateLabels.add(scans.get(i).getDate());
                }
            }
        }

        LineDataSet dataSet = new LineDataSet(entries, (limit == 10) ? "Last 10 Scans" : "Grouped Averages");
        dataSet.setColor(getResources().getColor(R.color.main2));
        dataSet.setValueTextColor(getResources().getColor(android.R.color.white));
        dataSet.setLineWidth(2f);
        dataSet.setCircleRadius(4f);
        dataSet.setDrawCircles(true);
        dataSet.setDrawValues(true);

        LineData lineData = new LineData(dataSet);
        lineChart.setData(lineData);

        XAxis xAxis = lineChart.getXAxis();
        xAxis.setPosition(XAxis.XAxisPosition.BOTTOM);
        xAxis.setDrawGridLines(false);
        xAxis.setGranularity(1f);
        xAxis.setLabelRotationAngle(-45);
        xAxis.setValueFormatter(new ValueFormatter() {
            @Override
            public String getFormattedValue(float value) {
                int index = (int) value;
                return (index >= 0 && index < dateLabels.size()) ? dateLabels.get(index) : "";
            }
        });

        lineChart.getDescription().setText((limit == 10) ? "Last 10 Scans" : "Grouped Averages");
        lineChart.getDescription().setTextColor(getResources().getColor(android.R.color.white));
        lineChart.getAxisRight().setEnabled(false);
        lineChart.invalidate();

        LimitLine thresholdLine = new LimitLine(300f, "Threshold 300");
        thresholdLine.setLineColor(getResources().getColor(android.R.color.holo_red_light));
        thresholdLine.setLineWidth(2f);
        thresholdLine.enableDashedLine(10f, 10f, 0f);
        lineChart.getAxisLeft().addLimitLine(thresholdLine);
    }

    public void showPopUp() {
        Dialog dialog = new Dialog(this);
        dialog.setContentView(R.layout.logo_pop_up);
        ImageView logoImageView = dialog.findViewById(R.id.logoImageView);
        View waterMask = dialog.findViewById(R.id.waterMask);

        ValueAnimator animator = ValueAnimator.ofInt(0, 100);
        animator.setDuration(3000);
        animator.addUpdateListener(animation -> {
            int value = (Integer) animation.getAnimatedValue();
            ViewGroup.LayoutParams layoutParams = waterMask.getLayoutParams();
            layoutParams.height = (int) TypedValue.applyDimension(
                    TypedValue.COMPLEX_UNIT_DIP, value, getResources().getDisplayMetrics());
            waterMask.setLayoutParams(layoutParams);
        });

        animator.addListener(new Animator.AnimatorListener() {
            @Override
            public void onAnimationStart(Animator animation) { }
            @Override
            public void onAnimationEnd(Animator animation) { dialog.dismiss(); }
            @Override
            public void onAnimationCancel(Animator animation) { dialog.dismiss(); }
            @Override
            public void onAnimationRepeat(Animator animation) { }
        });

        animator.start();
        dialog.show();
    }

    private void showDatePicker() {
        int year = calendar.get(Calendar.YEAR);
        int month = calendar.get(Calendar.MONTH);
        int day = calendar.get(Calendar.DAY_OF_MONTH);
        DatePickerDialog datePickerDialog = new DatePickerDialog(
                this,
                (view, selectedYear, selectedMonth, selectedDay) -> {
                    calendar.set(selectedYear, selectedMonth, selectedDay);
                    updateLabel();
                },
                year, month, day
        );
        datePickerDialog.getDatePicker().setMaxDate(System.currentTimeMillis());
        datePickerDialog.show();
    }

    private void updateLabel() {
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd", Locale.US);
        ageEditText.setText(sdf.format(calendar.getTime()));
    }
}
