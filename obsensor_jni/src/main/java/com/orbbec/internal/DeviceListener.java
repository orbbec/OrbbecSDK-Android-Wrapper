package com.orbbec.internal;

import android.hardware.usb.UsbDevice;

/**
 * SDK device listening interface
 */
public interface DeviceListener {
    /**
     * Device add notification
     */
    void onDeviceAttach(UsbDevice usbDevice);

    /**
     * Device removal notification
     */
    void onDeviceDetach(UsbDevice usbDevice);
}
