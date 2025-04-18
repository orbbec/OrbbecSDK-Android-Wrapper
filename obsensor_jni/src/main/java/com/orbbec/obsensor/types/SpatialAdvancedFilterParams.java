package com.orbbec.obsensor.types;

import com.orbbec.obsensor.ByteConversion;

public class SpatialAdvancedFilterParams implements ByteConversion {
    @StructField(offset = 0, size = 1)
    private short mMagnitude;
    @StructField(offset = 1, size = 4)
    private float mAlpha;
    @StructField(offset = 5, size = 2)
    private int mDispDiff;
    @StructField(offset = 7, size = 2)
    private int mRadius;

    public short getMagnitude() {
        return mMagnitude;
    }

    public void setMagnitude(short mMagnitude) {
        this.mMagnitude = mMagnitude;
    }

    public float getAlpha() {
        return mAlpha;
    }

    public void setAlpha(float mAlpha) {
        this.mAlpha = mAlpha;
    }

    public int getDispDiff() {
        return mDispDiff;
    }

    public void setDispDiff(int mDispDiff) {
        this.mDispDiff = mDispDiff;
    }

    public int getRadius() {
        return mRadius;
    }

    public void setRadius(int mRadius) {
        this.mRadius = mRadius;
    }

    private byte[] mBytes;

    @Override
    public byte[] getBytes() {
        if (mBytes == null) {
            mBytes = new byte[9];
        }
        return mBytes;
    }

    @Override
    public boolean parseBytes() {
        return StructParser.parseBytes(this, mBytes);
    }
}
