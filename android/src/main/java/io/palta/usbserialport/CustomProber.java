package io.palta.expousbseriaport;

import com.hoho.android.usbserial.driver.CdcAcmSerialDriver;
import com.hoho.android.usbserial.driver.FtdiSerialDriver;
import com.hoho.android.usbserial.driver.ProbeTable;
import com.hoho.android.usbserial.driver.UsbSerialProber;

class CustomProber {

    static UsbSerialProber getCustomProber() {
        ProbeTable customTable = new ProbeTable();
        customTable.addProduct(0x248a, 0x8002, CdcAcmSerialDriver.class);
        customTable.addProduct(0x5131, 0x2007, FtdiSerialDriver.class);
        return new UsbSerialProber(customTable);
    }

}
