package com.orbbec.obsensor.types;

import com.orbbec.obsensor.ByteConversion;

public class NoiseRemovalFilterParams implements ByteConversion {
    @StructField(offset = 0, size = 2)
    private int mMaxSize;
    @StructField(offset = 2, size = 2)
    private int mDispDiff;
    @StructField(offset = 4, size = 4)
    private int mType;

    public int getMaxSize() {
        return mMaxSize;
    }

    public void setMaxSize(int mMaxSize) {
        this.mMaxSize = mMaxSize;
    }

    public int getDispDiff() {
        return mDispDiff;
    }

    public void setDispDiff(int mDispDiff) {
        this.mDispDiff = mDispDiff;
    }

    public DDONoiseRemovalType getType() {
        return DDONoiseRemovalType.get(mType);
    }

    public void setType(DDONoiseRemovalType mType) {
        this.mType = mType.value();
    }

    private byte[] mBytes;

    public byte[] BYTES() {
        if (mBytes == null) {
            mBytes = new byte[8];
        }
        return mBytes;
    }

    @Override
    public boolean parseBytes() {
        return StructParser.parseBytes(this, mBytes);
    }
}
