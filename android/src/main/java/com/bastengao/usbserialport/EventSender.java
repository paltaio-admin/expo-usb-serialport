package com.paltaio.usbserialport;

import com.facebook.react.bridge.WritableMap;

public interface EventSender {
    void sendEvent(String eventName, WritableMap event);
}
