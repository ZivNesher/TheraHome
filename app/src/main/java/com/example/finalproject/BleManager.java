package com.example.finalproject;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.bluetooth.*;
import android.bluetooth.le.*;
import android.content.pm.PackageManager;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.widget.ImageButton;
import android.widget.Toast;

import androidx.core.app.ActivityCompat;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

import java.util.*;

public class BleManager {
    private final MainActivity activity;
    private final ScanManager scanManager;
    private BluetoothAdapter adapter;
    private BluetoothLeScanner scanner;
    private BluetoothGatt gatt;

    private final List<BluetoothDevice> devices = new ArrayList<>();
    private final List<String> names = new ArrayList<>();

    public BleManager(MainActivity activity, ScanManager scanManager) {
        this.activity = activity;
        this.scanManager = scanManager;
    }

    public void setupScanButton() {
        ImageButton scanBtn = activity.findViewById(R.id.scan_button);
        scanBtn.setOnClickListener(v -> {
            LogoPopUpManager.show(activity);

            int randomVal = (int) (Math.random() * 100);
            Log.d("BLE", "Random EMG: " + randomVal);
            scanManager.performScanAndSaveData(randomVal);
        });

        if (ActivityCompat.checkSelfPermission(activity, Manifest.permission.BLUETOOTH_SCAN) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(activity,
                    new String[]{ Manifest.permission.BLUETOOTH_SCAN, Manifest.permission.BLUETOOTH_CONNECT, Manifest.permission.ACCESS_FINE_LOCATION },
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
                    public void loadMainActivity() {
                        // Not needed in this context
                    }

                    @Override
                    public void goToLoginScreen() {
                        // Not needed in this context
                    }
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
        }, 10000);
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
        gatt = device.connectGatt(activity, false, new BluetoothGattCallback() {});
    }
}
