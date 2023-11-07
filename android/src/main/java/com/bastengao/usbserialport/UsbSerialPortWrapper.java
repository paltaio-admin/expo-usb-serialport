package com.bastengao.usbserialport;

import android.util.Log;

import com.facebook.react.bridge.Arguments;
import com.facebook.react.bridge.Promise;
import com.facebook.react.bridge.WritableMap;
import com.hoho.android.usbserial.driver.UsbSerialPort;
import com.hoho.android.usbserial.util.SerialInputOutputManager;

import java.io.IOException;
import java.util.Arrays;

public class UsbSerialPortWrapper implements SerialInputOutputManager.Listener {

    private static final int WRITE_WAIT_MILLIS = 2000;
    private static final int READ_WAIT_MILLIS = 2000;

    private static final String DataReceivedEvent = "usbSerialPortDataReceived";

    private int deviceId;
    private UsbSerialPort port;
    private EventSender sender;
    private boolean closed = false;
    private SerialInputOutputManager ioManager;

    UsbSerialPortWrapper(int deviceId, UsbSerialPort port, EventSender sender) {
        this.deviceId = deviceId;
        this.port = port;
        this.sender = sender;
        this.ioManager = new SerialInputOutputManager(port, this);
        ioManager.start();
    }

    public interface EventCallback {
        void onComplete(Exception e);
    }

    public void send(byte[] data, EventCallback callback) {
        try {
            this.port.write(data, WRITE_WAIT_MILLIS);
            callback.onComplete(null);
        } catch (IOException e) {
            callback.onComplete(e.getMessage());
        }
    }

    public void read(int bytes, Promise promise) throws IOException {
        if (bytes <= 0) {
            promise.reject(CODE_READ_FAILED, "read failed", "expected bytes must be greater than 0");
            return;
        }
        byte[] buffer = new byte[bytes];
        int read = this.port.read(buffer, READ_WAIT_MILLIS);
        if (read > 0) {
            byte[] data = Arrays.copyOf(buffer, read);
            String hex = UsbSerialportForAndroidModule.bytesToHex(data);
            promise.resolve(hex);
        } else {
            promise.reject(CODE_READ_FAILED, "read failed", "no response from device");
        }
    }

    public void onNewData(byte[] data) {
        WritableMap event = Arguments.createMap();
        String hex = UsbSerialportForAndroidModule.bytesToHex(data);
        event.putInt("deviceId", this.deviceId);
        event.putString("data", hex);
        Log.d("usbserialport", hex);
        sender.sendEvent(DataReceivedEvent, event);
    }

    public void onRunError(Exception e) {
        // TODO: implement
    }

    public void close() {
        if (closed) {
            return;
        }

        if(ioManager != null) {
            ioManager.setListener(null);
            ioManager.stop();
        }

        this.closed = true;
        try {
            port.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
