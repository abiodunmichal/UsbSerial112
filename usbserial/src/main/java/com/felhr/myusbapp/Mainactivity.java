package com.felhr.myusbapp;

import android.app.Activity;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.hardware.usb.UsbDevice;
import android.hardware.usb.UsbDeviceConnection;
import android.hardware.usb.UsbManager;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import com.felhr.usbserial.UsbSerialDevice;
import com.felhr.usbserial.UsbSerialInterface;

import java.util.HashMap;

public class MainActivity extends Activity {
    private static final String ACTION_USB_PERMISSION = "com.felhr.USB_PERMISSION";

    UsbManager usbManager;
    UsbDevice device;
    UsbSerialDevice serialPort;
    UsbDeviceConnection connection;

    Button btnFront, btnBack, btnLeft, btnRight;

    private final BroadcastReceiver usbReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            if (ACTION_USB_PERMISSION.equals(intent.getAction())) {
                synchronized (this) {
                    device = intent.getParcelableExtra(UsbManager.EXTRA_DEVICE);
                    if (intent.getBooleanExtra(UsbManager.EXTRA_PERMISSION_GRANTED, false)) {
                        if (device != null) {
                            connectSerial();
                        }
                    } else {
                        Toast.makeText(context, "Permission denied for USB", Toast.LENGTH_SHORT).show();
                    }
                }
            }
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        usbManager = (UsbManager) getSystemService(Context.USB_SERVICE);
        btnFront = findViewById(R.id.btnFront);
        btnBack = findViewById(R.id.btnBack);
        btnLeft = findViewById(R.id.btnLeft);
        btnRight = findViewById(R.id.btnRight);

        registerReceiver(usbReceiver, new IntentFilter(ACTION_USB_PERMISSION));

        findSerialDevice();

        View.OnClickListener commandSender = v -> {
            String command = "";
            if (v == btnFront) command = "front";
            else if (v == btnBack) command = "back";
            else if (v == btnLeft) command = "left";
            else if (v == btnRight) command = "right";

            if (serialPort != null) {
                serialPort.write((command + "\n").getBytes());
                Toast.makeText(this, "Sent: " + command, Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(this, "No USB serial connected", Toast.LENGTH_SHORT).show();
            }
        };

        btnFront.setOnClickListener(commandSender);
        btnBack.setOnClickListener(commandSender);
        btnLeft.setOnClickListener(commandSender);
        btnRight.setOnClickListener(commandSender);
    }

    private void findSerialDevice() {
        HashMap<String, UsbDevice> usbDevices = usbManager.getDeviceList();
        if (!usbDevices.isEmpty()) {
            for (UsbDevice d : usbDevices.values()) {
                device = d;
                PendingIntent pi = PendingIntent.getBroadcast(this, 0, new Intent(ACTION_USB_PERMISSION), PendingIntent.FLAG_IMMUTABLE);
                usbManager.requestPermission(device, pi);
                break; // Only connect to first available device
            }
        } else {
            Toast.makeText(this, "No USB devices found", Toast.LENGTH_SHORT).show();
        }
    }

    private void connectSerial() {
        connection = usbManager.openDevice(device);
        if (connection == null) {
            Toast.makeText(this, "Could not open USB connection", Toast.LENGTH_SHORT).show();
            return;
        }

        serialPort = UsbSerialDevice.createUsbSerialDevice(device, connection);
        if (serialPort != null && serialPort.open()) {
            serialPort.setBaudRate(9600);
            serialPort.setDataBits(UsbSerialInterface.DATA_BITS_8);
            serialPort.setStopBits(UsbSerialInterface.STOP_BITS_1);
            serialPort.setParity(UsbSerialInterface.PARITY_NONE);
            serialPort.setFlowControl(UsbSerialInterface.FLOW_CONTROL_OFF);
            Toast.makeText(this, "Serial connected", Toast.LENGTH_SHORT).show();
        } else {
            Toast.makeText(this, "Failed to open serial port", Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        unregisterReceiver(usbReceiver);
        if (serialPort != null) {
            serialPort.close();
        }
    }
    }
