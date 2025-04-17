package com.orbbec.internal;

import android.content.Context;
import android.hardware.usb.UsbDevice;
import android.hardware.usb.UsbDeviceConnection;
import android.hardware.usb.UsbInterface;
import android.hardware.usb.UsbManager;
import android.util.Log;

import com.orbbec.obsensor.LobClass;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class DeviceWatcher extends LobClass {
    private static final String TAG = "DeviceWatcher";

    private long mNativeHandle = 0;

    private final Context mContext;
    private final Enumerator mEnumerator;

    private HashMap<String, List<UsbDeviceInfo>> mUsbDeviceInfos = new LinkedHashMap<>();
    private HashMap<String, UsbDeviceConnection> mUsbDeviceConnections = new LinkedHashMap<>();

    private DeviceListener mListener = new DeviceListener() {
        @Override
        public void onDeviceAttach(UsbDevice usbDevice) {
            Log.d(TAG, " onDeviceAttach Adding device. deviceName: " + usbDevice.getDeviceName()
                    + ", sn: " + UsbUtilities.safeGetSerialNumber(usbDevice) + ", deviceId:" + usbDevice.getDeviceId());
            addDevice(usbDevice);
        }

        @Override
        public void onDeviceDetach(UsbDevice usbDevice) {
            Log.d(TAG, " onDeviceDetach remove device.  deviceName: " + usbDevice.getDeviceName()
                    + ", sn: " + UsbUtilities.safeGetSerialNumber(usbDevice) + ", deviceId:" + usbDevice.getDeviceId());
            removeDevice(usbDevice);
        }
    };

    public DeviceWatcher(Context context) {
        mContext = context;
        nRegisterClassObj(this);
        mEnumerator = new Enumerator(mContext, mListener);
    }

    private void removeDevice(UsbDeviceInfo usbDevInfo) {
        nRemoveUsbDevice(usbDevInfo);
        Log.d(TAG, "Device: " + usbDevInfo.mName + " removed successfully");
    }

    private void addDevice(UsbDevice device) {
        Log.d(TAG, "Device:  addDevice");
        if (device == null) {
            return;
        }

        UsbManager usbManager = (UsbManager) mContext.getSystemService(Context.USB_SERVICE);
        if (!usbManager.hasPermission(device)) {
            Log.e(TAG, "Device: " + "has not permission ");
            return;
        }
        int miId = 0;
        int cls = 0;
        List<UsbDeviceInfo> usbDevInfoList = new ArrayList<>();
        for (int i = 0; i < device.getInterfaceCount(); i++) {
            UsbInterface usbInterface = device.getInterface(i);
            // avoid publish streaming interfaces TODO:MK
            if (usbInterface.getInterfaceSubclass() == 2) {
                continue;
            }

            // when device is in DFU state, two USB devices are detected, one of OB_USB_CLASS_VENDOR_SPECIFIC (255) class
            // and the other of OB_USB_CLASS_APPLICATION_SPECIFIC (254) class.
            // in order to avoid listing two usb devices for a single physical device we ignore the application specific class
            // https://www.usb.org/defined-class-codes#anchor_BaseClassFEh
            if (usbInterface.getInterfaceClass() == 0xFE || usbInterface.getInterfaceClass() == 0x09) {
                continue;
            }

            miId = usbInterface.getId();
            cls = usbInterface.getInterfaceClass();

            String[] split = device.getDeviceName().split("/");
            String url = Integer.valueOf(split[split.length - 2]) + "-" +
                    device.getDeviceProtocol() + "." + device.getDeviceSubclass() + "-" +
                    Integer.valueOf(split[split.length - 1]);
            UsbDeviceInfo usbDevInfo = new UsbDeviceInfo(device.getDeviceName(), device.getDeviceId(), url,
                    device.getVendorId(), device.getProductId(), miId,
                    UsbUtilities.safeGetSerialNumber(device), cls);
            Log.d(TAG, "Adding device: " + usbDevInfo.mName
                    + String.format(" uid: 0x%08x url: %s vid: 0x%04x  pid: 0x%04x", usbDevInfo.mUid, usbDevInfo.mUrl, usbDevInfo.mVid, usbDevInfo.mPid)
                    + " miId: " + usbDevInfo.mMiId + " serialNum: " + usbDevInfo.mSerialNum
                    + " cls: " + usbDevInfo.mCls);
            usbDevInfoList.add(usbDevInfo);
            nAddUsbDevice(usbDevInfo);
            Log.d(TAG, "Device: " + usbDevInfo.mName + " added successfully");
        }
        mUsbDeviceInfos.put(device.getDeviceName(), usbDevInfoList);
    }

    private void removeDevice(UsbDevice usbDevice) {
        List<UsbDeviceInfo> deviceInfoList = mUsbDeviceInfos.get(usbDevice.getDeviceName());
        if (null != deviceInfoList) {
            int removedCount = 0;
            for (UsbDeviceInfo info : deviceInfoList) {
                if (info.mUid == usbDevice.getDeviceId() && info.mVid == usbDevice.getVendorId()
                        && info.mPid == usbDevice.getProductId()) {
                    removeDevice(info);
                    removedCount++;
                }
            }

            if (deviceInfoList.size() - removedCount <= 0) {
                mUsbDeviceInfos.remove(usbDevice.getDeviceName());
            }
        }
    }

    /**
     * resource release
     */
    @Override
    public void close() {
        if (mEnumerator != null) {
            mEnumerator.close();
        }
    }

    /**
     * It is provided to the c++ layer to open usb device and determine which devices to open by uid.
     *
     * @param url Usb device identify，
     * @return File description of usb device
     */
    protected int openUsbDevice(String url) {
        UsbManager usbManager = (UsbManager) mContext.getSystemService(Context.USB_SERVICE);
        HashMap<String, UsbDevice> devicesMap = usbManager.getDeviceList();
        for (Map.Entry<String, UsbDevice> entry : devicesMap.entrySet()) {
            UsbDevice usbDevice = entry.getValue();
            String[] split = usbDevice.getDeviceName().split("/");
            String usbDeviceUrl = Integer.valueOf(split[split.length - 2]) + "-" +
                    usbDevice.getDeviceProtocol() + "." + usbDevice.getDeviceSubclass() + "-" +
                    Integer.valueOf(split[split.length - 1]);
            if (url.equals(usbDeviceUrl)) {
                synchronized (mUsbDeviceConnections) {
                    UsbDeviceConnection connection = mUsbDeviceConnections.get(url);
                    if (null == connection) {
                        if (!usbManager.hasPermission(usbDevice)) {
                            Log.e(TAG, "openUsbDevice: the matched usb device has no permission!");
                            return 0;
                        }
                        connection = usbManager.openDevice(usbDevice);
                        if (null == connection) {
                            Log.e(TAG, "openUsbDevice failed:connection is null!");
                            return 0;
                        }
                        Log.i(TAG, "openUsbDevice: usbDevice: " + UsbUtilities.getUsbDeviceBriefText(usbDevice));
                        mUsbDeviceConnections.put(url, connection);
                    }
                    return connection.getFileDescriptor();
                }
            }
        }
        return 0;
    }

    /**
     * It is provided to the c++ layer to close usb device and determine which devices to close by uid
     *
     * @param url Usb device identify，
     */
    protected void closeUsbDevice(String url) {
        synchronized (mUsbDeviceConnections) {
            UsbDeviceConnection deviceConnection = mUsbDeviceConnections.get(url);
            if (null != deviceConnection) {
                deviceConnection.close();
                mUsbDeviceConnections.remove(url);
            }
        }
    }

    private static native void nRegisterClassObj(DeviceWatcher deviceWatcher);

    private native void nAddUsbDevice(UsbDeviceInfo usbDeviceInfo);

    private native void nRemoveUsbDevice(UsbDeviceInfo usbDeviceInfo);
}
