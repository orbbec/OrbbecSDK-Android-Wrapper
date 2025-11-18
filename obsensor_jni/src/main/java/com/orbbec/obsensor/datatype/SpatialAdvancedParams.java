package com.orbbec.obsensor.datatype;

import com.orbbec.internal.DataUtilities;
import com.orbbec.obsensor.DataType;

public class SpatialAdvancedParams extends DataType {

    private short mMagnitude;
    private float mAlpha;
    private int mDispDiff;
    private int mRadius;

    public void setMagnitude(short mMagnitude) {
        this.mMagnitude = mMagnitude;
    }

    public short getMagnitude() {
        throwInitializeException();
        return mMagnitude;
    }

    public void setAlpha(float mAlpha) {
        this.mAlpha = mAlpha;
    }

    public float getAlpha() {
        throwInitializeException();
        return mAlpha;
    }

    public void setDispDiff(int mDispDiff) {
        this.mDispDiff = mDispDiff;
    }

    public int getDispDiff() {
        throwInitializeException();
        return mDispDiff;
    }

    public void setRadius(int mRadius) {
        this.mRadius = mRadius;
    }

    public int getRadius() {
        throwInitializeException();
        return mRadius;
    }

    @Override
    public int BYTES() {
        // Byte.BYTES + Float.BYTES + Short.BYTES * 2
        return 9;
    }

    @Override
    protected boolean parseBytesImpl(byte[] bytes) {
        int offset = 0, length = Byte.BYTES;
        mMagnitude = DataUtilities.bytesToUint8(DataUtilities.subBytes(bytes, offset, length));
        offset += length;
        length = Float.BYTES;
        mAlpha = DataUtilities.bytesToFloat(DataUtilities.subBytes(bytes, offset, length));
        offset += length;
        length = Short.BYTES;
        mDispDiff = DataUtilities.bytesToUnit16(DataUtilities.subBytes(bytes, offset, length));
        offset += length;
        mRadius = DataUtilities.bytesToUnit16(DataUtilities.subBytes(bytes, offset, length));
        return true;
    }

    @Override
    protected boolean wrapBytesImpl(byte[] bytes) {
        int offset = 0, length = Byte.BYTES;
        DataUtilities.appendBytes(DataUtilities.uint8ToBytes(mMagnitude), bytes, offset, length);
        offset += length;
        length = Float.BYTES;
        DataUtilities.appendBytes(DataUtilities.floatToBytes(mAlpha), bytes, offset, length);
        offset += length;
        length = Short.BYTES;
        DataUtilities.appendBytes(DataUtilities.uint16ToBytes(mDispDiff), bytes, offset, length);
        offset += length;
        DataUtilities.appendBytes(DataUtilities.uint16ToBytes(mRadius), bytes, offset, length);
        return true;
    }

    @Override
    public String toString() {
        return "SpatialAdvancedFilterParams{" +
                "mMagnitude=" + mMagnitude +
                ", mAlpha=" + mAlpha +
                ", mDispDiff=" + mDispDiff +
                ", mRadius=" + mRadius +
                '}';
    }
}
