package com.example.finalproject;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.bluetooth.*;
import android.bluetooth.le.*;
import android.content.pm.PackageManager;
import android.os.Environment;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.widget.ImageButton;
import android.widget.Toast;

import androidx.core.app.ActivityCompat;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.*;

public class BleManager {
    private final MainActivity activity;
    private final ScanManager scanManager;
    private BluetoothAdapter adapter;
    private BluetoothLeScanner scanner;
    private BluetoothGatt gatt;
    private BluetoothGattCharacteristic switchCharacteristic;

    private final List<BluetoothDevice> devices = new ArrayList<>();
    private final List<String> names = new ArrayList<>();

    private ImageButton scanBtn;
    private final StringBuilder fileDataBuffer = new StringBuilder();

    public BleManager(MainActivity activity, ScanManager scanManager) {
        this.activity = activity;
        this.scanManager = scanManager;
    }

    public void setupScanButton() {
        scanBtn = activity.findViewById(R.id.scan_button);
        scanBtn.setEnabled(false); // Enable only after BLE connection

        scanBtn.setOnClickListener(v -> {
            LogoPopUpManager.show(activity);
            fileDataBuffer.setLength(0); // Clear previous data

            if (switchCharacteristic != null) {
                switchCharacteristic.setValue(new byte[]{4}); // Case 4 = request file
                @SuppressLint("MissingPermission") boolean success = gatt.writeCharacteristic(switchCharacteristic);
                Log.d("BLE", "Requested Case 4: " + success);

                Toast.makeText(activity, "Requesting file from Arduino...", Toast.LENGTH_SHORT).show();

                // Optional timeout to save file after 6 seconds
                new Handler(Looper.getMainLooper()).postDelayed(() -> {
                    saveReceivedDataToFile("emg_result.txt");
                }, 6000);
            } else {
                Toast.makeText(activity, "Device not ready", Toast.LENGTH_SHORT).show();
            }
        });
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
                    @Override
                    public void loadMainActivity() {}

                    @Override
                    public void goToLoginScreen() {}
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

        scanner.startScan(null, new ScanSettings.Builder().setScanMode(ScanSettings.SCAN_MODE_LOW_LATENCY).build(), scanCallback);

        new Handler(Looper.getMainLooper()).postDelayed(() -> {
            scanner.stopScan(scanCallback);
            showDeviceDialog();
        }, 8000);
    }

    private final ScanCallback scanCallback = new ScanCallback() {
        public void onScanResult(int callbackType, ScanResult result) {
            BluetoothDevice device = result.getDevice();
            @SuppressLint("MissingPermission") String name = device.getName() != null ? device.getName() : "Unnamed Device";
            String displayName = name + " [" + device.getAddress() + "]";
            if (!devices.contains(device)) {
                devices.add(device);
                names.add(displayName);
            }
        }
    };

    private void showDeviceDialog() {
        if (devices.isEmpty()) {
            Toast.makeText(activity, "No BLE devices found", Toast.LENGTH_SHORT).show();
            return;
        }

        new AlertDialog.Builder(activity)
                .setTitle("Select Device")
                .setItems(names.toArray(new String[0]), (d, i) -> connect(devices.get(i)))
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
                                scanBtn.setEnabled(true);
                                Toast.makeText(activity, "Device connected. You can now start scan.", Toast.LENGTH_SHORT).show();
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
                    fileDataBuffer.append(chunk);
                    Log.d("BLE", "Received chunk: " + chunk);
                }
            }
        });
    }

    private void saveReceivedDataToFile(String filename) {
        try {
            File file = new File(activity.getExternalFilesDir(null), filename);
            FileOutputStream fos = new FileOutputStream(file);
            fos.write(fileDataBuffer.toString().getBytes());
            fos.close();
            Log.d("BLE", "File saved: " + file.getAbsolutePath());

            activity.runOnUiThread(() ->
                    Toast.makeText(activity, "File saved to " + file.getAbsolutePath(), Toast.LENGTH_LONG).show()
            );
            System.out.println("File saved: " + file.getAbsolutePath());
        } catch (IOException e) {
            Log.e("BLE", "Error saving file", e);
        }
    }
}
