package com.orbbec.internal;

/**
 * Usb device information
 */
public class UsbDeviceInfo {
    public UsbDeviceInfo(String name, int uid, int vid, int pid, int miId, String serialNum, int cls) {
        mName = name;
        mUid = uid;
        mVid = vid;
        mPid = pid;
        mMiId = miId;
        mSerialNum = serialNum;
        mCls = cls;
    }

    String mName;
    int mUid;
    int mVid;
    int mPid;
    int mMiId;
    String mSerialNum;
    int mCls;
}
