package io.palta.expousbserialport;

import android.annotation.SuppressLint;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.hardware.usb.UsbDevice;
import android.hardware.usb.UsbDeviceConnection;
import android.hardware.usb.UsbManager;
import android.os.Build;

import androidx.annotation.NonNull;

import com.facebook.react.BuildConfig;
import com.facebook.react.bridge.Arguments;
import com.facebook.react.bridge.Promise;
import com.facebook.react.bridge.ReactApplicationContext;
import com.facebook.react.bridge.ReactContextBaseJavaModule;
import com.facebook.react.bridge.ReactMethod;
import com.facebook.react.bridge.WritableArray;
import com.facebook.react.bridge.WritableMap;
import com.facebook.react.module.annotations.ReactModule;
import com.facebook.react.modules.core.DeviceEventManagerModule;
import com.hoho.android.usbserial.driver.UsbSerialDriver;
import com.hoho.android.usbserial.driver.UsbSerialPort;
import com.hoho.android.usbserial.driver.UsbSerialProber;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

@ReactModule(name = UsbSerialPortForAndroidModule.NAME)
public class UsbSerialPortForAndroidModule extends ReactContextBaseJavaModule implements EventSender {
    public static final String NAME = "UsbSerialPortForAndroid";
    private static final String INTENT_ACTION_GRANT_USB = BuildConfig.LIBRARY_PACKAGE_NAME + ".GRANT_USB";

    public static final String CODE_DEVICE_NOT_FOUND = "device_not_found";
    public static final String CODE_DRIVER_NOT_FOUND = "driver_not_found";
    public static final String CODE_NOT_ENOUGH_PORTS = "not_enough_ports";
    public static final String CODE_PERMISSION_DENIED = "permission_denied";
    public static final String CODE_OPEN_FAILED = "open_failed";
    public static final String CODE_DEVICE_NOT_OPEN = "device_not_open";
    public static final String CODE_SEND_FAILED = "send_failed";
    public static final String CODE_READ_FAILED = "read_failed";
    public static final String CODE_DEVICE_NOT_OPEN_OR_CLOSED = "device_not_open_or_closed";

    private final ReactApplicationContext reactContext;
    private final Map<Integer, UsbSerialPortWrapper> usbSerialPorts = new HashMap<>();

    public final CustomProber customProber = new CustomProber();

    private final UsbEventReceiver usbEventReceiver;

    public UsbSerialPortForAndroidModule(ReactApplicationContext reactContext) {
        super(reactContext);
        this.reactContext = reactContext;
        this.usbEventReceiver = new UsbEventReceiver();
        usbEventReceiver.initialize(reactContext, this);
        usbEventReceiver.addListener();
    }

    @ReactMethod
    public void addListener(String eventName) {
    }

    @ReactMethod
    public void removeListeners(Integer count) {
    }

    @Override
    public void onCatalystInstanceDestroy() {
        super.onCatalystInstanceDestroy();
        usbEventReceiver.removeListener();
    }

    @Override
    @NonNull
    public String getName() {
        return NAME;
    }

    @Override
    public Map<String, Object> getConstants() {
        final Map<String, Object> constants = new HashMap<>();
        constants.put("CODE_DEVICE_NOT_FOUND", CODE_DEVICE_NOT_FOUND);
        constants.put("CODE_DRIVER_NOT_FOUND", CODE_DRIVER_NOT_FOUND);
        constants.put("CODE_NOT_ENOUGH_PORTS", CODE_NOT_ENOUGH_PORTS);
        constants.put("CODE_PERMISSION_DENIED", CODE_PERMISSION_DENIED);
        constants.put("CODE_OPEN_FAILED", CODE_OPEN_FAILED);
        constants.put("CODE_DEVICE_NOT_OPEN", CODE_DEVICE_NOT_OPEN);
        constants.put("CODE_SEND_FAILED", CODE_SEND_FAILED);
        constants.put("CODE_READ_FAILED", CODE_READ_FAILED);
        constants.put("CODE_DEVICE_NOT_OPEN_OR_CLOSED", CODE_DEVICE_NOT_OPEN_OR_CLOSED);
        return constants;
    }

    @ReactMethod
    public void list(Promise promise) {
        WritableArray devices = Arguments.createArray();
        UsbManager usbManager = (UsbManager) Objects.requireNonNull(getCurrentActivity()).getSystemService(Context.USB_SERVICE);

        for (UsbDevice device : usbManager.getDeviceList().values()) {
            WritableMap d = Arguments.createMap();
            d.putInt("deviceId", device.getDeviceId());
            d.putInt("vendorId", device.getVendorId());
            d.putInt("productId", device.getProductId());
            devices.pushMap(d);
        }
        promise.resolve(devices);
    }

    @SuppressLint("UnspecifiedRegisterReceiverFlag")
    @ReactMethod
    public void tryRequestPermission(int deviceId, Promise promise) {
        UsbManager usbManager = (UsbManager) Objects.requireNonNull(getCurrentActivity()).getSystemService(Context.USB_SERVICE);

        UsbDevice device = findDevice(deviceId);
        if (device == null) {
            promise.reject(CODE_DEVICE_NOT_FOUND, "device not found");
            return;
        }

        if (usbManager.hasPermission(device)) {
            promise.resolve(1);
            return;
        }

        BroadcastReceiver usbPermissionReceiver = new BroadcastReceiver() {
            public void onReceive(Context context, Intent intent) {
                Objects.requireNonNull(getCurrentActivity()).unregisterReceiver(this);

                boolean permissionGranted = intent.getBooleanExtra(UsbManager.EXTRA_PERMISSION_GRANTED, false);
                if (permissionGranted) {
                    promise.resolve(1);
                } else {
                    promise.reject(CODE_PERMISSION_DENIED, "permission denied");
                }
            }
        };

        IntentFilter filter = new IntentFilter(INTENT_ACTION_GRANT_USB);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            getCurrentActivity().registerReceiver(usbPermissionReceiver, filter, Context.RECEIVER_EXPORTED);
        } else {
            getCurrentActivity().registerReceiver(usbPermissionReceiver, filter);
        }


        PendingIntent usbPermissionIntent;
        Intent usbIntent = new Intent(INTENT_ACTION_GRANT_USB);

        if (android.os.Build.VERSION.SDK_INT >= Build.VERSION_CODES.UPSIDE_DOWN_CAKE) {
            usbPermissionIntent = PendingIntent.getBroadcast(getCurrentActivity(), 0, usbIntent, PendingIntent.FLAG_MUTABLE | PendingIntent.FLAG_ALLOW_UNSAFE_IMPLICIT_INTENT);
        } else {
            usbPermissionIntent = PendingIntent.getBroadcast(getCurrentActivity(), 0, usbIntent, PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE);
        }

        usbManager.requestPermission(device, usbPermissionIntent);
    }

    @ReactMethod
    public void hasPermission(int deviceId, Promise promise) {
        UsbManager usbManager = (UsbManager) Objects.requireNonNull(getCurrentActivity()).getSystemService(Context.USB_SERVICE);
        UsbDevice device = findDevice(deviceId);
        if (device == null) {
            promise.reject(CODE_DEVICE_NOT_FOUND, "device not found");
            return;
        }

        promise.resolve(usbManager.hasPermission(device));
    }

    @ReactMethod
    public void addDevice(int vendorId, int productId, String driverName) {
        customProber.addProduct(vendorId, productId, driverName);
    }

    @ReactMethod
    public void open(int deviceId, int baudRate, int dataBits, int stopBits, int parity, Promise promise) {
        UsbSerialPortWrapper wrapper = usbSerialPorts.get(deviceId);
        if (wrapper != null) {
            promise.resolve(deviceId);
            return;
        }

        UsbManager usbManager = (UsbManager) Objects.requireNonNull(getCurrentActivity()).getSystemService(Context.USB_SERVICE);
        UsbDevice device = findDevice(deviceId);
        if (device == null) {
            promise.reject(CODE_DEVICE_NOT_FOUND, "device not found");
            return;
        }

        UsbSerialDriver driver = UsbSerialProber.getDefaultProber().probeDevice(device);
        if (driver == null) {
            driver = customProber.getCustomProber().probeDevice(device);
        }
        if (driver == null) {
            promise.reject(CODE_DRIVER_NOT_FOUND, "no driver for device");
            return;
        }
        if (driver.getPorts().isEmpty()) {
            promise.reject(CODE_NOT_ENOUGH_PORTS, "not enough ports at device");
            return;
        }

        UsbDeviceConnection connection = usbManager.openDevice(driver.getDevice());
        if(connection == null) {
            if (!usbManager.hasPermission(driver.getDevice())) {
                promise.reject(CODE_PERMISSION_DENIED, "connection failed: permission denied");
            } else {
                promise.reject(CODE_OPEN_FAILED, "connection failed: open failed");
            }
            return;
        }

        UsbSerialPort port = driver.getPorts().get(0);
        try {
            port.open(connection);
            port.setParameters(baudRate, dataBits, stopBits, parity);
        } catch (IOException e) {
            try {
                 port.close();
            } catch (IOException ignored) {}
            promise.reject(CODE_OPEN_FAILED, "connection failed", e);
            return;
        }

        wrapper = new UsbSerialPortWrapper(deviceId, port, this);
        usbSerialPorts.put(deviceId, wrapper);
        promise.resolve(deviceId);
    }

    @ReactMethod
    public void send(int deviceId, String hexStr, Promise promise) {
        UsbSerialPortWrapper wrapper = usbSerialPorts.get(deviceId);
        if (wrapper == null) {
            promise.reject(CODE_DEVICE_NOT_OPEN, "device not open");
            return;
        }

        byte[] data = hexStringToByteArray(hexStr);
        wrapper.send(data, exception -> {
            if (exception == null) {
                promise.resolve(null);
            } else {
                promise.reject(CODE_SEND_FAILED, "send failed", exception);
            }
        });
    }

    @ReactMethod
    public void sendWithResponse(int deviceId, String hexStr, int bytes, Promise promise) {
        UsbSerialPortWrapper wrapper = usbSerialPorts.get(deviceId);
        if (wrapper == null) {
            promise.reject(CODE_DEVICE_NOT_OPEN, "device not open");
            return;
        }

        byte[] data = hexStringToByteArray(hexStr);
        wrapper.send(data, exception -> {
            if (exception == null) {
                wrapper.read(bytes, promise);
            } else {
                promise.reject(CODE_SEND_FAILED, "send failed", exception);
            }
        });
    }

    @ReactMethod
    public void close(int deviceId, Promise promise) {
        UsbSerialPortWrapper wrapper = usbSerialPorts.get(deviceId);
        if (wrapper == null) {
            promise.reject(CODE_DEVICE_NOT_OPEN_OR_CLOSED, "serial port not open or closed");
            return;
        }

        wrapper.close();
        usbSerialPorts.remove(deviceId);
        promise.resolve(null);
    }

    public void sendEvent(String eventName, WritableMap params) {
        reactContext.getJSModule(DeviceEventManagerModule.RCTDeviceEventEmitter.class)
            .emit(eventName, params);
    }

    private UsbDevice findDevice(int deviceId) {
        UsbManager usbManager = (UsbManager) Objects.requireNonNull(getCurrentActivity()).getSystemService(Context.USB_SERVICE);
        for (UsbDevice device : usbManager.getDeviceList().values()) {
            if (device.getDeviceId() == deviceId) {
                return device;
            }
        }

        return null;
    }

    public static byte[] hexStringToByteArray(String s) {
        if (s.length() % 2 == 1) {
            throw new IllegalArgumentException("Invalid hexadecimal string");
        }
        int len = s.length();
        byte[] data = new byte[len / 2];
        for (int i = 0; i < len; i += 2) {
            data[i / 2] = (byte) ((Character.digit(s.charAt(i), 16) << 4)
                + Character.digit(s.charAt(i + 1), 16));
        }
        return data;
    }

    private static final char[] HEX_ARRAY = "0123456789ABCDEF".toCharArray();

    public static String bytesToHex(byte[] bytes) {
        char[] hexChars = new char[bytes.length * 2];
        for (int j = 0; j < bytes.length; j++) {
            int v = bytes[j] & 0xFF;
            hexChars[j * 2] = HEX_ARRAY[v >>> 4];
            hexChars[j * 2 + 1] = HEX_ARRAY[v & 0x0F];
        }
        return new String(hexChars);
    }
}
