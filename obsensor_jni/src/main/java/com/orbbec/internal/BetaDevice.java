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
}
