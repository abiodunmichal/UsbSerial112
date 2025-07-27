package com.felhr.myusbapp;

import android.app.Activity;
import android.hardware.usb.UsbDevice;
import android.hardware.usb.UsbDeviceConnection;
import android.hardware.usb.UsbManager;
import android.hardware.usb.UsbEndpoint;
import android.hardware.usb.UsbInterface;
import android.hardware.usb.UsbConstants;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import com.felhr.usbserial.UsbSerialDevice;
import com.felhr.usbserial.UsbSerialInterface;

import java.util.HashMap;

public class MainActivity extends Activity {
    UsbManager usbManager;
    UsbDevice device;
    UsbSerialDevice serialPort;
    UsbDeviceConnection connection;

    Button btnFront, btnBack, btnLeft, btnRight;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        usbManager = (UsbManager) getSystemService(Context.USB_SERVICE);

        btnFront = findViewById(R.id.btnFront);
        btnBack = findViewById(R.id.btnBack);
        btnLeft = findViewById(R.id.btnLeft);
        btnRight = findViewById(R.id.btnRight);

        findSerialDevice();  // Try to auto-connect on launch

        View.OnClickListener commandSender = v -> {
            String command = "";
            if (v == btnFront) command = "front";
            else if (v == btnBack) command = "back";
            else if (v == btnLeft) command = "left";
            else if (v == btnRight) command = "right";

            if (serialPort != null) {
                serialPort.write(command.getBytes());
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
                connection = usbManager.openDevice(device);
                serialPort = UsbSerialDevice.createUsbSerialDevice(device, connection);
                if (serialPort != null && serialPort.open()) {
                    serialPort.setBaudRate(9600);
                    serialPort.setDataBits(UsbSerialInterface.DATA_BITS_8);
                    serialPort.setStopBits(UsbSerialInterface.STOP_BITS_1);
                    serialPort.setParity(UsbSerialInterface.PARITY_NONE);
                    serialPort.setFlowControl(UsbSerialInterface.FLOW_CONTROL_OFF);
                    break;
                }
            }
        }
    }
}
