package com.ziv.therahome;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.bluetooth.*;
import android.bluetooth.le.*;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.core.app.ActivityCompat;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.*;

public class BleManager {
    private final MainActivity activity;
    private final ScanManager scanManager;
    private BluetoothAdapter adapter;
    private BluetoothLeScanner scanner;
    private BluetoothGatt gatt;
    private BluetoothGattCharacteristic switchCharacteristic;
    private AlertDialog searchingDialog;

    private final List<BluetoothDevice> devices = new ArrayList<>();
    private final List<String> names = new ArrayList<>();

    private Button exercise_1_Btn;
    private Button exercise_2_Btn;
    private Button exercise_3_Btn;
    private Button exercise_4_Btn;
    private Button exercise_5_Btn;
    private final Map<Button, Boolean> buttonStates = new HashMap<>();
    private final StringBuilder fileDataBuffer = new StringBuilder();
    private int currentExerciseCase = 0;

    private boolean isReceiving = false;
    private final Handler toastHandler = new Handler(Looper.getMainLooper());

    public BleManager(MainActivity activity, ScanManager scanManager) {
        this.activity = activity;
        this.scanManager = scanManager;
    }

    public void setupScanButton() {
        exercise_1_Btn = activity.findViewById(R.id.start_button_1);
        exercise_2_Btn = activity.findViewById(R.id.start_button_2);
        exercise_3_Btn = activity.findViewById(R.id.start_button_3);
        exercise_4_Btn = activity.findViewById(R.id.start_button_4);
        exercise_5_Btn = activity.findViewById(R.id.start_button_5);

        for (Button btn : Arrays.asList(exercise_1_Btn, exercise_2_Btn, exercise_3_Btn, exercise_4_Btn, exercise_5_Btn)) {
            btn.setEnabled(false);
            buttonStates.put(btn, false);
        }

        View.OnClickListener exerciseClickListener = btn -> {
            int caseNum = 1;
            if (btn == exercise_1_Btn) caseNum = 1;
            else if (btn == exercise_2_Btn) caseNum = 2;
            else if (btn == exercise_3_Btn) caseNum = 3;
            else if (btn == exercise_4_Btn) caseNum = 4;
            else if (btn == exercise_5_Btn) caseNum = 5;

            final int finalCaseNum = caseNum;

            if (switchCharacteristic != null) {
                Button clickedBtn = (Button) btn;
                boolean isActive = buttonStates.get(clickedBtn);
                if (!isActive) {
                    clickedBtn.setBackgroundColor(Color.YELLOW);
                    clickedBtn.setTextColor(Color.BLACK);
                    clickedBtn.setText("3");

                    Handler countdownHandler = new Handler(Looper.getMainLooper());
                    Runnable countdownRunnable = new Runnable() {
                        int count = 3;

                        @Override
                        public void run() {
                            count--;
                            if (count > 0) {
                                clickedBtn.setText(String.valueOf(count));
                                countdownHandler.postDelayed(this, 1000);
                            } else {
                                currentExerciseCase = finalCaseNum;
                                switchCharacteristic.setValue(new byte[]{(byte) finalCaseNum});
                                @SuppressLint("MissingPermission") boolean success = gatt.writeCharacteristic(switchCharacteristic);
                                Log.d("BLE", "Started Exercise Case " + finalCaseNum + ": " + success);
                                Toast.makeText(activity, "Started Exercise " + finalCaseNum, Toast.LENGTH_SHORT).show();

                                clickedBtn.setBackgroundColor(0xFFFF4444); // red
                                clickedBtn.setText("STOP");
                                buttonStates.put(clickedBtn, true);
                            }
                        }
                    };

                    countdownHandler.postDelayed(countdownRunnable, 1000);

                } else {
                    fileDataBuffer.setLength(0);
                    isReceiving = true;
                    showReceivingToast();

                    switchCharacteristic.setValue(new byte[]{9});
                    @SuppressLint("MissingPermission") boolean success = gatt.writeCharacteristic(switchCharacteristic);

                    Log.d("BLE", "Sent STOP (9): " + success);
                    Toast.makeText(activity, "Stopped Exercise " + caseNum, Toast.LENGTH_SHORT).show();

                    clickedBtn.setBackgroundColor(0xD3D3D3);
                    clickedBtn.setText("RESTART");
                    clickedBtn.setTextColor(Color.BLACK);
                    buttonStates.put(clickedBtn, false);
                }
            } else {
                Toast.makeText(activity, "BLE not ready", Toast.LENGTH_SHORT).show();
            }
        };

        exercise_1_Btn.setOnClickListener(exerciseClickListener);
        exercise_2_Btn.setOnClickListener(exerciseClickListener);
        exercise_3_Btn.setOnClickListener(exerciseClickListener);
        exercise_4_Btn.setOnClickListener(exerciseClickListener);
        exercise_5_Btn.setOnClickListener(exerciseClickListener);
    }



    private void showReceivingToast() {
        toastHandler.postDelayed(new Runnable() {
            @Override
            public void run() {
                if (isReceiving) {
                    Toast.makeText(activity, "Receiving file...", Toast.LENGTH_SHORT).show();
                    toastHandler.postDelayed(this, 2000);
                }
            }
        }, 0);
    }

    public void startInitialBleScan() {
        if (ActivityCompat.checkSelfPermission(activity, Manifest.permission.BLUETOOTH_SCAN) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(activity,
                    new String[]{Manifest.permission.BLUETOOTH_SCAN, Manifest.permission.BLUETOOTH_CONNECT, Manifest.permission.ACCESS_FINE_LOCATION},
                    1001);
        } else {
            startBleScan();
        }
    }

    public void loadUserDetailsButton() {
        ImageButton burgerMenu = activity.findViewById(R.id.menu);
        burgerMenu.setOnClickListener(v -> {
            FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
            if (user != null) {
                UserManager um = new UserManager(new UserManagerCallback() {
                    @Override public void loadMainActivity() {}
                    @Override public void goToLoginScreen() {}
                });

                um.usersRef.child(user.getUid()).addListenerForSingleValueEvent(new com.google.firebase.database.ValueEventListener() {
                    @Override
                    public void onDataChange(com.google.firebase.database.DataSnapshot snapshot) {
                        User u = snapshot.getValue(User.class);
                        if (u != null) {
                            UserDetailsBottomSheet bs = UserDetailsBottomSheet.newInstance(u);
                            bs.show(activity.getSupportFragmentManager(), "UserDetailsBottomSheet");
                        }
                    }

                    @Override
                    public void onCancelled(com.google.firebase.database.DatabaseError error) {
                        Toast.makeText(activity, "Failed to load user data", Toast.LENGTH_SHORT).show();
                    }
                });
            }
        });
    }

    public void loadPreviousScans() {
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        if (user != null) {
            scanManager.loadScanHistory(user.getUid());
        }
    }

    @SuppressLint("MissingPermission")
    private void startBleScan() {
        BluetoothManager manager = (BluetoothManager) activity.getSystemService(android.content.Context.BLUETOOTH_SERVICE);
        if (manager != null) adapter = manager.getAdapter();
        if (adapter == null || !adapter.isEnabled()) return;

        scanner = adapter.getBluetoothLeScanner();
        if (scanner == null) return;

        devices.clear();
        names.clear();

        if (searchingDialog != null && searchingDialog.isShowing()) searchingDialog.dismiss();

        AlertDialog.Builder builder = new AlertDialog.Builder(activity);
        builder.setCancelable(false);

        LinearLayout layout = new LinearLayout(activity);
        layout.setOrientation(LinearLayout.VERTICAL);
        layout.setPadding(40, 40, 40, 40);
        layout.setBackgroundColor(Color.WHITE);
        layout.setGravity(Gravity.CENTER);

        ProgressBar spinner = new ProgressBar(activity);
        TextView message = new TextView(activity);
        message.setText("Searching for TheraHome devices...");
        message.setTextSize(18);
        message.setTextColor(Color.BLACK);
        message.setPadding(0, 20, 0, 0);

        layout.addView(spinner);
        layout.addView(message);

        builder.setView(layout);
        searchingDialog = builder.create();
        searchingDialog.show();

        scanner.startScan(null,
                new ScanSettings.Builder().setScanMode(ScanSettings.SCAN_MODE_LOW_LATENCY).build(),
                scanCallback);

        // Stop scan after 8 seconds only if no device was found
        new Handler(Looper.getMainLooper()).postDelayed(() -> {
            if (devices.isEmpty()) {
                scanner.stopScan(scanCallback);
                if (searchingDialog != null && searchingDialog.isShowing()) {
                    searchingDialog.dismiss();
                }
                showDeviceDialog(); // show "not found" dialog with retry
            }
        }, 8000);
    }



    private final ScanCallback scanCallback = new ScanCallback() {
        @SuppressLint("MissingPermission")
        @Override
        public void onScanResult(int callbackType, ScanResult result) {
            BluetoothDevice device = result.getDevice();
            String name = device.getName() != null ? device.getName() : "Unnamed Device";
            String displayName = name + " [" + device.getAddress() + "]";

            if (name != null && name.startsWith("TheraHome") && !devices.contains(device)) {
                devices.add(device);
                names.add(displayName);

                if (scanner != null) {
                    scanner.stopScan(this);
                }

                if (searchingDialog != null && searchingDialog.isShowing()) {
                    searchingDialog.dismiss();
                }

                showDeviceDialog(); // Show the dialog only once
            }
        }
    };



    private void showDeviceDialog() {
        if (devices.isEmpty()) {
            new AlertDialog.Builder(activity)
                    .setTitle("No BLE devices found")
                    .setMessage("Would you like to search again?")
                    .setPositiveButton("Search Again", (d, i) -> startBleScan())
                    .setNegativeButton("Cancel", null)
                    .show();
            return;
        }

        new AlertDialog.Builder(activity)
                .setTitle("Select Device")
                .setItems(names.toArray(new String[0]), (d, i) -> connect(devices.get(i)))
                .setNeutralButton("Search Again", (d, i) -> startBleScan())
                .setNegativeButton("Cancel", null)
                .show();
    }

    @SuppressLint("MissingPermission")
    private void connect(BluetoothDevice device) {
        gatt = device.connectGatt(activity, false, new BluetoothGattCallback() {
            @Override
            public void onConnectionStateChange(BluetoothGatt gatt, int status, int newState) {
                if (newState == BluetoothProfile.STATE_CONNECTED) {
                    Log.d("BLE", "Connected to GATT server.");
                    gatt.discoverServices();
                }
            }

            @Override
            public void onServicesDiscovered(BluetoothGatt gatt, int status) {
                if (status == BluetoothGatt.GATT_SUCCESS) {
                    BluetoothGattService service = gatt.getService(UUID.fromString("0000180a-0000-1000-8000-00805f9b34fb"));
                    if (service != null) {
                        switchCharacteristic = service.getCharacteristic(UUID.fromString("00002a57-0000-1000-8000-00805f9b34fb"));
                        if (switchCharacteristic != null) {
                            gatt.setCharacteristicNotification(switchCharacteristic, true);
                            BluetoothGattDescriptor descriptor = switchCharacteristic.getDescriptor(UUID.fromString("00002902-0000-1000-8000-00805f9b34fb"));
                            if (descriptor != null) {
                                descriptor.setValue(BluetoothGattDescriptor.ENABLE_NOTIFICATION_VALUE);
                                gatt.writeDescriptor(descriptor);
                            }

                            activity.runOnUiThread(() -> {
                                exercise_1_Btn.setEnabled(true);
                                exercise_2_Btn.setEnabled(true);
                                exercise_3_Btn.setEnabled(true);
                                exercise_4_Btn.setEnabled(true);
                                exercise_5_Btn.setEnabled(true);
                                Toast.makeText(activity, "Device connected. You can now start an exercise.", Toast.LENGTH_SHORT).show();
                            });
                        }
                    }
                }
            }

            @Override
            public void onCharacteristicChanged(BluetoothGatt gatt, BluetoothGattCharacteristic characteristic) {
                if (characteristic.getUuid().equals(UUID.fromString("00002a57-0000-1000-8000-00805f9b34fb"))) {
                    byte[] data = characteristic.getValue();
                    String chunk = new String(data);

                    Log.d("BLE", "Received chunk: " + chunk);
                    fileDataBuffer.append(chunk);

                    if (fileDataBuffer.toString().endsWith("EOF")) {
                        isReceiving = false;
                        toastHandler.removeCallbacksAndMessages(null);
                        Log.d("BLE", "EOF fully received — saving file.");

                        String filename = "EXT" + currentExerciseCase + ".txt";
                        saveReceivedDataToFile(filename);

                        activity.runOnUiThread(() -> {
                            Button targetBtn = null;
                            switch (currentExerciseCase) {
                                case 1: targetBtn = exercise_1_Btn; break;
                                case 2: targetBtn = exercise_2_Btn; break;
                                case 3: targetBtn = exercise_3_Btn; break;
                                case 4: targetBtn = exercise_4_Btn; break;
                                case 5: targetBtn = exercise_5_Btn; break;
                            }

                            if (targetBtn != null) {
                                targetBtn.setBackgroundColor(Color.LTGRAY); // Ensure it's gray
                                targetBtn.setText("RESTART");
                                targetBtn.setTextColor(Color.BLACK);
                                buttonStates.put(targetBtn, false);
                            }
                        });
                    }
                }
            }


        });
    }

    private void saveReceivedDataToFile(String filename) {
        try {
            String cleanData = fileDataBuffer.toString().replace("EOF", "");

            File file = new File(activity.getExternalFilesDir(null), filename);
            FileOutputStream fos = new FileOutputStream(file);
            fos.write(cleanData.getBytes());
            fos.flush();
            fos.close();

            Log.d("BLE", "File saved: " + file.getAbsolutePath());

            activity.runOnUiThread(() ->
                    Toast.makeText(activity, "File saved to " + file.getAbsolutePath(), Toast.LENGTH_LONG).show()
            );
        } catch (IOException e) {
            Log.e("BLE", "Error saving file", e);
        }
        new Handler(Looper.getMainLooper()).postDelayed(() -> uploadFileToFirebase(filename), 100);
    }

    private void uploadFileToFirebase(String filename) {
        File file = new File(activity.getExternalFilesDir(null), filename);
        if (!file.exists()) {
            Log.e("BLE", "File not found: " + filename);
            return;
        }

        StringBuilder fileContent = new StringBuilder();
        try (Scanner scanner = new Scanner(file)) {
            while (scanner.hasNextLine()) {
                String line = scanner.nextLine();
                if (!line.contains("EOF")) {
                    fileContent.append(line).append("\n");
                }
            }
        } catch (IOException e) {
            Log.e("BLE", "Error reading file", e);
            return;
        }

        List<String> values = Arrays.asList(fileContent.toString().split("\n"));

        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        if (user == null) {
            Toast.makeText(activity, "No user logged in", Toast.LENGTH_SHORT).show();
            return;
        }

        String sessionId = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(new Date());

        DatabaseReference ref = FirebaseDatabase.getInstance()
                .getReference("users")
                .child(user.getUid())
                .child("sessions")
                .child(sessionId)
                .child("exercises")
                .child("exercise" + currentExerciseCase);

        ref.setValue(values).addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                Toast.makeText(activity, "Data uploaded to Firebase", Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(activity, "Upload failed", Toast.LENGTH_SHORT).show();
            }
        });
    }

    @SuppressLint("MissingPermission")
    public void disconnectBluetooth() {
        if (gatt != null) {
            gatt.disconnect();
            gatt.close();
            gatt = null;
            Log.d("BLE", "Disconnected from Bluetooth device");
        }
    }
}
