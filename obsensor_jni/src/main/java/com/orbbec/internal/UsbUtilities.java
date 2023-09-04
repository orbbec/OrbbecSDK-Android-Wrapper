package com.orbbec.internal;

import android.content.Context;
import android.hardware.usb.UsbDevice;
import android.hardware.usb.UsbManager;
import android.util.Log;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Utility class of USB
 */
public class UsbUtilities {
    private static final String TAG = "libob UsbUtilities";
    private static final int VID_ORBBEC = 0x2BC5;

    public static boolean isOrbbecDevice(UsbDevice usbDevice) {
        if (usbDevice.getVendorId() == VID_ORBBEC)
            return true;
        return false;
    }

    private static List<UsbDevice> getUsbDevices(Context context, Integer vid) {
        return getUsbDevices(context, vid, 0);
    }

    private static List<UsbDevice> getUsbDevices(Context context, Integer vid, Integer pid) {
        ArrayList<UsbDevice> res = new ArrayList<>();
        UsbManager usbManager = (UsbManager) context.getSystemService(Context.USB_SERVICE);

        HashMap<String, UsbDevice> devicesMap = usbManager.getDeviceList();
        //Log.d(TAG,"deviceMap="+devicesMap.toString());
        for (Map.Entry<String, UsbDevice> entry : devicesMap.entrySet()) {
            UsbDevice usbDevice = entry.getValue();
            Log.d(TAG, String.format("usbDevice.vid=0x%04x. pid=0x%04x", usbDevice.getVendorId(), usbDevice.getProductId()));
            if (usbDevice.getVendorId() == vid && (usbDevice.getProductId() == pid || pid == 0)) {
                res.add(usbDevice);
            }
        }
        if (res.isEmpty()) {
            Log.e(TAG, String.format("getUsbDevice: failed to locate USB device, VID: 0x%04x, PID: 0x%04x", vid, pid));
        }
        return res;
    }

    private static boolean hasUsbPermission(Context context, UsbDevice usbDevice) {
        if (usbDevice == null) {
            Log.w(TAG, "hasUsbPermission: null USB device");
            return false;
        }

        UsbManager usbManager = (UsbManager) context.getSystemService(Context.USB_SERVICE);
        return usbManager.hasPermission(usbDevice);
    }

    private static List<UsbDevice> getDevices(Context context) {
        return getUsbDevices(context, VID_ORBBEC);
    }

    public static String getUsbDeviceBriefText(UsbDevice usbDevice) {
        if (null == usbDevice) {
            return "null";
        }
        return String.format("{name: %s, deviceId: 0x%08x, vid: 0x%08x, pid: 0x%08x, serializeNumber: %s, isOrbbecDevice: %s}",
                String.valueOf(usbDevice.getDeviceName()),
                usbDevice.getDeviceId(),
                usbDevice.getVendorId(),
                usbDevice.getProductId(),
                String.valueOf(usbDevice.getSerialNumber()),
                String.valueOf(isOrbbecDevice(usbDevice)));
    }
}
