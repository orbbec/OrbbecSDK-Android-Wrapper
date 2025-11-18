package com.orbbec.obsensor.datatype;

import com.orbbec.internal.DataUtilities;
import com.orbbec.obsensor.DataType;

public class Point3f extends DataType {
    private float mX;
    private float mY;
    private float mZ;

    public float getX() {
        return mX;
    }

    public void setX(float mX) {
        this.mX = mX;
    }

    public float getY() {
        return mY;
    }

    public void setY(float mY) {
        this.mY = mY;
    }

    public float getZ() {
        return mZ;
    }

    public void setZ(float mZ) {
        this.mZ = mZ;
    }

    @Override
    public int BYTES() {
        // Float.BYTES * 3
        return 12;
    }

    @Override
    protected boolean parseBytesImpl(byte[] bytes) {
        return true;
    }

    @Override
    protected boolean wrapBytesImpl(byte[] bytes) {
        int offset = 0, length = Float.BYTES;
        DataUtilities.appendBytes(DataUtilities.floatToBytes(mX), bytes, offset, length);
        offset += length;
        DataUtilities.appendBytes(DataUtilities.floatToBytes(mY), bytes, offset, length);
        offset += length;
        DataUtilities.appendBytes(DataUtilities.floatToBytes(mZ), bytes, offset, length);
        return true;
    }

    @Override
    public String toString() {
        return "Point3f{" +
                "mX=" + mX +
                ", mY=" + mY +
                ", mZ=" + mZ +
                '}';
    }
}
