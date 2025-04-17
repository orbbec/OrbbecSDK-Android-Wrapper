package com.orbbec.internal;

import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.hardware.usb.UsbDevice;
import android.hardware.usb.UsbManager;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.Looper;
import android.os.Message;
import android.text.TextUtils;
import android.util.Log;
import android.util.SparseArray;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

/**
 * <h1>Enumerator</h1>
 * Notify regarding attach/detach events of a orbbec device
 */
public class Enumerator {
    private static final String TAG = "java: Enumerator";

    private static final String ACTION_REQUIRE_USB_PERMISSION = "orbbec.action.REQUIRE_USB_PERMISSION";

    private Context mContext;
    private DeviceListener mListener;
    private HandlerThread mMessagesThread;
    private Handler mHandler;
    private SparseArray<UsbDevice> mUsbDeviceMap = new SparseArray<>();

    private BroadcastReceiver mBroadcastReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            UsbDevice usbDevice = intent.getParcelableExtra(UsbManager.EXTRA_DEVICE);
            String deviceText = UsbUtilities.getUsbDeviceBriefText(usbDevice);
            Log.i(TAG, "onReceive: " + action + ", usbDevice: " + deviceText);
            if (null == usbDevice || !UsbUtilities.isOrbbecDevice(usbDevice)) {
                return;
            }

            switch (action) {
                case UsbManager.ACTION_USB_DEVICE_ATTACHED: {
                    UsbManager usbManager = (UsbManager) context.getSystemService(Context.USB_SERVICE);
                    Log.i(TAG, "USB Device attach. device: " + deviceText + ", hasPermission: " + usbManager.hasPermission(usbDevice));
                    if (null != usbDevice && UsbUtilities.isOrbbecDevice(usbDevice)) {
                        if (usbManager.hasPermission(usbDevice)) {
                            if (null != mHandler) {
                                Message msg = mHandler.obtainMessage(MessagesHandler.MSG_ON_DEVICE_ATTACHED);
                                msg.obj = usbDevice;
                                mHandler.sendMessage(msg);
                            } // if mHandler
                        } else {
                            Log.i(TAG, "USB Device attach. device: " + deviceText + ", require permission");
                            requireUsbPermission(context, usbDevice);
                        }
                    }
                    break;
                }
                case UsbManager.ACTION_USB_DEVICE_DETACHED: {
                    Log.i(TAG, "USB Device detach. device: " + deviceText);
                    if (null != usbDevice && UsbUtilities.isOrbbecDevice(usbDevice)) {
                        if (null != mHandler) {
                            Message msg = mHandler.obtainMessage(MessagesHandler.MSG_ON_DEVICE_DETACHED);
                            msg.obj = usbDevice;
                            mHandler.sendMessage(msg);
                        } // if mHandler
                    }
                    break;
                }
                case Intent.ACTION_SCREEN_ON: {
                    Log.i(TAG, "Screen on check usb device connections!");
                    if (null != mHandler) {
                        Message msg = mHandler.obtainMessage(MessagesHandler.MSG_NOTIFY_QUERY_DEVICES);
                        msg.obj = context;
                        mHandler.sendMessage(msg);
                    } // if mHandler
                    break;
                }
            }
        }
    };

    /**
     * In case a device is already available, onUsbDeviceAttach callback will be called.
     * close must be called at the of the usage.
     *
     * @param context  application's context.
     * @param listener The listener object which handles the state change.
     */
    public Enumerator(Context context, DeviceListener listener) {
        if (listener == null) {
            Log.e(TAG, "Enumerator: provided listener is null");
            throw new NullPointerException("provided listener is null");
        }
        if (context == null) {
            Log.e(TAG, "Enumerator: provided context is null");
            throw new NullPointerException("provided context is null");
        }

        mListener = listener;
        mContext = context;

        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(UsbManager.ACTION_USB_DEVICE_ATTACHED);
        intentFilter.addAction(UsbManager.ACTION_USB_DEVICE_DETACHED);
        intentFilter.addAction(Intent.ACTION_SCREEN_ON);
        context.registerReceiver(mBroadcastReceiver, intentFilter);

        mMessagesThread = new HandlerThread("DeviceManager device availability message thread");
        mMessagesThread.start();

        mHandler = new MessagesHandler(mMessagesThread.getLooper());
        Message msg = mHandler.obtainMessage(MessagesHandler.MSG_NOTIFY_QUERY_DEVICES);
        msg.obj = context;
        mHandler.sendMessage(msg);
    }

    /**
     * Stop listening to the USB events and clean resources.
     */
    public synchronized void close() {
        for (int i = 0; i < mUsbDeviceMap.size(); i++) {
            UsbDevice usbDevice = mUsbDeviceMap.valueAt(i);
            if (null != usbDevice && UsbUtilities.isOrbbecDevice(usbDevice)) {
                mListener.onDeviceDetach(usbDevice);
            }
        }

        mListener = null;
        if (mContext != null) {
            try {
                mContext.unregisterReceiver(mBroadcastReceiver);
            } catch (Exception ignore) {
            }
        }

        mHandler.removeCallbacksAndMessages(null);
        if (mMessagesThread != null) {
            mMessagesThread.quitSafely();
            Log.v(TAG, "joining message handler");
            try {
                mMessagesThread.join();
            } catch (InterruptedException e) {
                Log.e(TAG, "close: " + e.getMessage());
            }
            Log.v(TAG, "joined message handler");
            mMessagesThread = null;
        }
        mHandler = null;

        synchronized (mUsbDeviceMap) {
            mUsbDeviceMap.clear();
        }
    }

    @Override
    protected void finalize() throws Throwable {
        if (mMessagesThread != null) {
            close();
        }
        super.finalize();
    }

    private void requireUsbPermission(Context context, UsbDevice usbDevice) {
        final String action = ACTION_REQUIRE_USB_PERMISSION + "#" + usbDevice.getDeviceName() + "#"
                + String.format("0x%08x", usbDevice.getDeviceId());
        if (null == mHandler) {
            Log.w(TAG, "requireUsbPermission mHandler=null, no require usb permission. action: " + action);
            return;
        }

        IntentFilter intentFilter = new IntentFilter(action);
        final USBPermissionReceiver receiver = new USBPermissionReceiver(context, usbDevice, action);
        context.registerReceiver(receiver, intentFilter);

        if (null != mHandler) {
            Message msg = Message.obtain(mHandler, () -> {
                receiver.unregisterReceiver();
            });
            msg.obj = receiver;
            mHandler.sendMessageDelayed(msg, 60000);
        }

        Log.d(TAG, "requireUsbPermission launch action: " + action);
        UsbManager usbManager = (UsbManager) context.getSystemService(Context.USB_SERVICE);
        PendingIntent pi = PendingIntent.getBroadcast(context, 0, new Intent(action), PendingIntent.FLAG_IMMUTABLE);
        usbManager.requestPermission(usbDevice, pi);
    }

    private class USBPermissionReceiver extends BroadcastReceiver {
        private final Context   requireContext;
        private final UsbDevice requireDevice;
        private volatile boolean isUnregisterReceiver = false;
        private final String action;

        public USBPermissionReceiver(Context requireContext, UsbDevice requireDevice, String action) {
            this.requireContext = requireContext;
            this.requireDevice = requireDevice;
            this.action = action;
        }

        public synchronized void unregisterReceiver() {
            if (!isUnregisterReceiver) {
                requireContext.unregisterReceiver(this);
                isUnregisterReceiver = true;
            }
        }

        @Override
        public void onReceive(Context context, Intent intent) {
            Log.i(TAG, "USBPermissionReceiver#onReceive action: " + intent.getAction());
            if (!TextUtils.equals(intent.getAction(), action)) {
                Log.e(TAG, "USBPermissionReceiver#onReceive invalid action: " + intent.getAction());
                return;
            }
            unregisterReceiver();
            if (null != mHandler) {
                mHandler.removeCallbacksAndMessages(this);
            }

            final UsbDevice broadUsbDevice = intent.getParcelableExtra(UsbManager.EXTRA_DEVICE);
            UsbManager usbManager = (UsbManager) context.getSystemService(Context.USB_SERVICE);
            UsbDevice usbDevice = null;
            Map<String, UsbDevice> deviceMap = usbManager.getDeviceList();
            Iterator<Map.Entry<String, UsbDevice>> iters =  deviceMap.entrySet().iterator();
            while (iters.hasNext()) {
                Map.Entry<String, UsbDevice> e = iters.next();
                if (TextUtils.equals(requireDevice.getDeviceName(), e.getValue().getDeviceName())
                    && requireDevice.getDeviceId() == e.getValue().getDeviceId()) {
                    usbDevice = e.getValue();
                    break;
                }
            }
            if (null == usbDevice) {
                Log.d(TAG, "USBPermissionReceiver#onReceive find device from UsbManager failed."
                        + " action: " + action);
                return;
            }

            // Just for debug
            if (broadUsbDevice != usbDevice) {
                if (null != broadUsbDevice) {
                    Log.i(TAG, "USBPermissionReceiver#onReceive broadUsbDevice != usbDevice. broadUsbDevice: {name: "
                            + broadUsbDevice.getDeviceName() + ", SN: " + UsbUtilities.safeGetSerialNumber(broadUsbDevice)
                            + ", id: " + broadUsbDevice.getDeviceId() + "}"
                            + ", usbDevice: {name: " + usbDevice.getDeviceName() + ", SN: " + UsbUtilities.safeGetSerialNumber(usbDevice)
                            + ", id: " + usbDevice.getDeviceId() + "}");
                } else {
                    Log.i(TAG, "USBPermissionReceiver#onReceive broadUsbDevice = null"
                            + ", usbDevice: {name: " + usbDevice.getDeviceName() + ", SN: " + UsbUtilities.safeGetSerialNumber(usbDevice)
                            + ", id: " + usbDevice.getDeviceId() + "}");
                }
            }

            boolean isGranted = intent.getBooleanExtra(UsbManager.EXTRA_PERMISSION_GRANTED, false);
            boolean hasPermission = (null != usbDevice ? usbManager.hasPermission(usbDevice) : false);
            String deviceText = UsbUtilities.getUsbDeviceBriefText(usbDevice);
            Log.i(TAG, "USBPermissionReceiver#onReceive. device: " + deviceText + ", isGranted: " + isGranted
                    + ", hasPermission: " + hasPermission);
            if (null != usbDevice && hasPermission && null != mHandler) {
                // Notify device attached
                Message devAttachedMsg = mHandler.obtainMessage(MessagesHandler.MSG_ON_DEVICE_ATTACHED);
                devAttachedMsg.obj = usbDevice;
                mHandler.sendMessage(devAttachedMsg);

                // Notify query all devices
                Message queryDevicesMsg = mHandler.obtainMessage(MessagesHandler.MSG_NOTIFY_QUERY_DEVICES);
                queryDevicesMsg.obj = context;
                mHandler.sendMessage(queryDevicesMsg);
            }
        }
    }

    private class MessagesHandler extends Handler {
        private static final String TAG = "java MessagesHandler";

        public static final int MSG_ON_DEVICE_ATTACHED = 101;
        public static final int MSG_ON_DEVICE_DETACHED = 102;
        public static final int MSG_NOTIFY_QUERY_DEVICES = 103;

        public MessagesHandler(Looper looper) {
            super(looper);
        }

        @Override
        public void handleMessage(Message msg) {
            try {
                switch (msg.what) {
                    case MSG_ON_DEVICE_ATTACHED: {
                        Log.i(TAG, "handleMessage: device attached");
                        handleDeviceAttached((UsbDevice) msg.obj);
                        break;
                    }
                    case MSG_ON_DEVICE_DETACHED: {
                        Log.i(TAG, "handleMessage: device detached");
                        handleDeviceDetached((UsbDevice) msg.obj);
                        break;
                    }
                    case MSG_NOTIFY_QUERY_DEVICES: {
                        handleQueryDevices((Context) msg.obj);
                        break;
                    }
                }
            } catch (Exception e) {
                Log.e(TAG, "handleMessage: failed to open device, error: " + e.getMessage());
            }
        }

        private void handleDeviceAttached(UsbDevice usbDevice) {
            boolean isAdded = false;
            synchronized (mUsbDeviceMap) {
                if (mUsbDeviceMap.indexOfKey(usbDevice.getDeviceId()) < 0) {
                    mUsbDeviceMap.put(usbDevice.getDeviceId(), usbDevice);
                    isAdded = true;
                }
            }
            if (isAdded && null != mListener) {
                mListener.onDeviceAttach(usbDevice);
            }
        }

        private void handleDeviceDetached(UsbDevice usbDevice) {
            boolean isRemoved = false;
            synchronized (mUsbDeviceMap) {
                if (mUsbDeviceMap.indexOfKey(usbDevice.getDeviceId()) >= 0) {
                    mUsbDeviceMap.remove(usbDevice.getDeviceId());
                    isRemoved = true;
                }
            }
            if (isRemoved && null != mListener) {
                mListener.onDeviceDetach(usbDevice);
            }
        }

        private void handleQueryDevices(Context context) {
            UsbManager usbManager = (UsbManager) context.getSystemService(Context.USB_SERVICE);
            Map<String, UsbDevice> deviceMap = usbManager.getDeviceList();
            Iterator<Map.Entry<String, UsbDevice>> iters = deviceMap.entrySet().iterator();
            // Check need to add attached usb device.
            while (iters.hasNext()) {
                Map.Entry<String, UsbDevice> e = iters.next();
                UsbDevice usbDevice = e.getValue();
                if (UsbUtilities.isOrbbecDevice(usbDevice)) {
                    String deviceText = UsbUtilities.getUsbDeviceBriefText(usbDevice);
                    if (usbManager.hasPermission(usbDevice)) {
                        boolean isAdded = false;
                        synchronized (mUsbDeviceMap) {
                            if (mUsbDeviceMap.indexOfKey(usbDevice.getDeviceId()) < 0) {
                                mUsbDeviceMap.put(usbDevice.getDeviceId(), usbDevice);
                                isAdded = true;
                            }
                        }
                        if (isAdded && null != mListener) {
                            mListener.onDeviceAttach(usbDevice);
                        }
                    } else {
                        Log.i(TAG, "Query USB devices. device: " + deviceText + ", require permission");
                        requireUsbPermission(context, usbDevice);
                    }
                } // if isOrbbecDevice
            } // while

            // Check need to remove detached usb device.
            synchronized (mUsbDeviceMap) {
                List<Integer> removeKeyList = new ArrayList<>();
                for (int i = 0, c = mUsbDeviceMap.size(); i < c; i++) {
                    int key = mUsbDeviceMap.keyAt(i);
                    UsbDevice usbDevice = mUsbDeviceMap.valueAt(i);
                    if (!deviceMap.containsValue(usbDevice)) {
                        removeKeyList.add(key);
                        if (null != mListener) {
                            mListener.onDeviceDetach(usbDevice);
                        }
                    }
                }

                for (int i = 0; i < removeKeyList.size(); i++) {
                    mUsbDeviceMap.remove(removeKeyList.get(i));
                }
            } // synchronized (mUsbDeviceMap)
        } // function handleQueryDevices()
    }
}
