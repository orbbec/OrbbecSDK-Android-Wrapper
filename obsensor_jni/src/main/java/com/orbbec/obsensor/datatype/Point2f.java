package com.orbbec.obsensor.datatype;

import com.orbbec.internal.DataUtilities;
import com.orbbec.obsensor.DataType;

public class Point2f extends DataType {
    private float mX;
    private float mY;

    public Point2f(float x, float y) {
        this.mX = x;
        this.mY = y;
    }

    public void setX(float x) {
        this.mX = x;
    }

    public void setY(float y) {
        this.mY = y;
    }

    public float getX() {
        return mX;
    }

    public float getY() {
        return mY;
    }

    @Override
    public int BYTES() {
        // Float.BYTES * 2
        return 8;
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
        return true;
    }

    @Override
    public String toString() {
        return "Point2f{" +
                "mX=" + mX +
                ", mY=" + mY +
                '}';
    }
}
