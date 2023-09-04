package com.orbbec.internal;

import com.orbbec.obsensor.Device;

/**
 * @Author lumiaozi
 * @Date 2023/3/7 11:56
 * @CopyRight Orbbec
 * @Description Internal API, no publish yet
 */
public class BetaDevice extends Device {
    public BetaDevice(long handle) {
        super(handle);
    }

    public boolean isPropertySupported(int propertyId, int obPermission) {
        throwInitializeException();
        return nIsPropertySupported(mHandle, propertyId, obPermission);
     }

    public boolean getDefaultB(int propertyId) {
        return getRangeB(propertyId).def;
    }

    public int getMinRangeI(int propertyId) {
        return getRangeI(propertyId).min;
    }

    public int getMaxRangeI(int propertyId) {
        return getRangeI(propertyId).max;
    }

    public int getStepI(int propertyId) {
        return getRangeI(propertyId).step;
    }

    public int getDefaultI(int propertyId) {
        return getRangeI(propertyId).def;
    }

    public float getMinRangeF(int propertyId) {
        return getRangeF(propertyId).min;
    }

    public float getMaxRangeF(int propertyId) {
        return getRangeF(propertyId).max;
    }

    public float getStepF(int propertyId) {
        return getRangeF(propertyId).step;
    }

    public float getDefaultF(int propertyId) {
        return getRangeF(propertyId).def;
    }

    public boolean getPropertyValueB(int propertyId) {
        throwInitializeException();
        return Device.nGetPropertyValueB(mHandle, propertyId);
    }

    public void setPropertyValueB(int propertyId, boolean value) {
        throwInitializeException();
        Device.nSetPropertyValueB(mHandle, propertyId, value);
    }

    public int getPropertyValueI(int propertyId) {
        throwInitializeException();
        return Device.nGetPropertyValueI(mHandle, propertyId);
    }

    public void setPropertyValueI(int propertyId, int value) {
        throwInitializeException();
        Device.nSetPropertyValueI(mHandle, propertyId, value);
    }

    public float getPropertyValueF(int propertyId) {
        throwInitializeException();
        return Device.nGetPropertyValueF(mHandle, propertyId);
    }

    public void setPropertyValueI(int propertyId, float value) {
        throwInitializeException();
        Device.nSetPropertyValueF(mHandle, propertyId, value);
    }

    /**
     * \if English
     * The device restarts delay mode. After the device restarts, the original device resources need to be released, and can be re-acquired after the device is reconnected.
     * Support devices: Gemini2 L
     *
     * @param delayMs Time unit：ms。delayMs == 0：No delay；delayMs > 0, Delay millisecond connect to host device after reboot
     * \else
     * 设备重启, 延迟模式, 设备重启后需要将原来的设备资源进行释放,待设备重新连接后可重新获取
     * 支持产品：Gemini2 L
     * @param delayMs 延迟时间单位：ms。delayMs为0：不延迟；delayMs大于0，重启后延迟delayMs毫秒连接上位机
     * \endif
     */
    public void reboot(int delayMs) {
        throwInitializeException();
        nRebootDelayMode(mHandle, delayMs);
    }
}
