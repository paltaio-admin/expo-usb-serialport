package io.palta.expousbserialport;

import android.annotation.SuppressLint;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.hardware.usb.UsbDevice;
import android.hardware.usb.UsbManager;
import android.os.Build;
import android.util.Log;

import androidx.annotation.NonNull;

import com.facebook.react.bridge.Arguments;
import com.facebook.react.bridge.ReactApplicationContext;
import com.facebook.react.bridge.WritableMap;
import com.facebook.react.modules.core.DeviceEventManagerModule;

public class UsbEventReceiver extends BroadcastReceiver {
    private static final String TAG = "UsbEventReceiver";
    private ReactApplicationContext reactContext;
    private EventSender sender;

    public UsbEventReceiver() {
        this.sender = (eventName, event) -> reactContext.getJSModule(DeviceEventManagerModule.RCTDeviceEventEmitter.class).emit(eventName, event);
    }

    public void initialize(ReactApplicationContext reactContext, EventSender sender) {
        this.reactContext = reactContext;
        this.sender = sender;
    }

    @Override
    public void onReceive(Context context, Intent intent) {
        if (reactContext == null) {
            Log.e(TAG, "React context is not initialized.");
            return;
        }

        String action = intent.getAction();
        UsbDevice device = intent.getParcelableExtra(UsbManager.EXTRA_DEVICE);

        if (device != null && action != null) {
            WritableMap params = Arguments.createMap();
            params.putInt("deviceId", device.getDeviceId());
            params.putString("deviceName", device.getDeviceName());
            params.putInt("vendorId", device.getVendorId());
            params.putInt("productId", device.getProductId());
            // Ensure to safely get non-null strings for nullable values
            params.putString("vendorName", device.getManufacturerName() != null ? device.getManufacturerName() : "Unknown");
            params.putString("productName", device.getProductName() != null ? device.getProductName() : "Unknown");

            if (UsbManager.ACTION_USB_DEVICE_ATTACHED.equals(action)) {
                Log.d(TAG, "Usb device attached");
                sender.sendEvent("usbDeviceAttached", params);
            } else if (UsbManager.ACTION_USB_DEVICE_DETACHED.equals(action)) {
                Log.d(TAG, "Usb device detached");
                sender.sendEvent("usbDeviceDetached", params);
            }
        }
    }
    @NonNull
    private static IntentFilter getIntentFilter() {
        IntentFilter filter = new IntentFilter();
        filter.addAction(UsbManager.ACTION_USB_DEVICE_ATTACHED);
        filter.addAction(UsbManager.ACTION_USB_DEVICE_DETACHED);
        return filter;
    }

    @SuppressLint("UnspecifiedRegisterReceiverFlag")
    public void addListener() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            reactContext.registerReceiver(this, getIntentFilter(), Context.RECEIVER_EXPORTED);
        } else {
            reactContext.registerReceiver(this, getIntentFilter());
        }
    }

    public void removeListener() {
        reactContext.unregisterReceiver(this);
    }
}
