package io.palta.expousbserialport;

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

    private final int deviceId;
    private final UsbSerialPort port;
    private final EventSender sender;
    private boolean closed = false;
    private final SerialInputOutputManager ioManager;

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
            callback.onComplete(e);
        }
    }

    public void read(int bytes, Promise promise) {
        if (bytes <= 0) {
            promise.reject("read_failed", "expected bytes must be greater than 0");
            return;
        }
        try {
            byte[] buffer = new byte[bytes];
            int read = this.port.read(buffer, READ_WAIT_MILLIS);
            if (read > 0) {
                byte[] data = Arrays.copyOf(buffer, read);
                String hex = UsbSerialPortForAndroidModule.bytesToHex(data);
                promise.resolve(hex);
            } else {
                promise.reject("read_failed", "no response from device");
            }
        } catch (IOException e) {
            promise.reject("read_failed", "read failed", e);
        }
    }

    public void onNewData(byte[] data) {
        WritableMap event = Arguments.createMap();
        String hex = UsbSerialPortForAndroidModule.bytesToHex(data);
        event.putInt("deviceId", this.deviceId);
        event.putString("data", hex);
        Log.d("expousbserialport", hex);
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
