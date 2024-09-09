package io.palta.expousbserialport;

import com.hoho.android.usbserial.driver.CdcAcmSerialDriver;
import com.hoho.android.usbserial.driver.FtdiSerialDriver;
import com.hoho.android.usbserial.driver.Ch34xSerialDriver;
import com.hoho.android.usbserial.driver.Cp21xxSerialDriver;
import com.hoho.android.usbserial.driver.ChromeCcdSerialDriver;
import com.hoho.android.usbserial.driver.GsmModemSerialDriver;
import com.hoho.android.usbserial.driver.ProlificSerialDriver;
import com.hoho.android.usbserial.driver.ProbeTable;
import com.hoho.android.usbserial.driver.UsbSerialDriver;
import com.hoho.android.usbserial.driver.UsbSerialProber;

import java.util.HashMap;

public class CustomProber {
    private final ProbeTable customTable;
    private static final HashMap<String, Class<? extends UsbSerialDriver>> driverMap;

    static {
        driverMap = new HashMap<>();
        driverMap.put("cdc-acm", CdcAcmSerialDriver.class);
        driverMap.put("ftdi", FtdiSerialDriver.class);
        driverMap.put("ch34x", Ch34xSerialDriver.class);
        driverMap.put("cp21xx", Cp21xxSerialDriver.class);
        driverMap.put("chrome-ccd", ChromeCcdSerialDriver.class);
        driverMap.put("gsm-modem", GsmModemSerialDriver.class);
        driverMap.put("prolific", ProlificSerialDriver.class);
    }

    public CustomProber() {
        customTable = new ProbeTable();
    }

    public void addProduct(int vendorId, int productId, String driverName) {
        Class<? extends UsbSerialDriver> driverClass = driverMap.get(driverName);
        if (driverClass != null) {
            customTable.addProduct(vendorId, productId, driverClass);
        } else {
            throw new IllegalArgumentException("Unknown driver name: " + driverName);
        }
    }

    public UsbSerialProber getCustomProber() {
        return new UsbSerialProber(customTable);
    }

}
