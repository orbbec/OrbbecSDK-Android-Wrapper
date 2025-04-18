package com.orbbec.obsensor.types;

import com.orbbec.obsensor.ByteConversion;

public class EdgeNoiseRemovalFilterParams implements ByteConversion {
    @StructField(offset = 0, size = 4)
    private int mType;
    @StructField(offset = 4, size = 2)
    private int mMarginLeftTh;
    @StructField(offset = 6, size = 2)
    private int mMarginRightTh;
    @StructField(offset = 8, size = 2)
    private int mMarginTopTh;
    @StructField(offset = 10, size = 2)
    private int mMarginBottomTh;

    public void setType(EdgeNoiseRemovalType mType) {
        this.mType = mType.value();
    }

    public EdgeNoiseRemovalType getType() {
        return EdgeNoiseRemovalType.get(mType);
    }

    public void setMarginLeftTh(int mMarginLeftTh) {
        this.mMarginLeftTh = mMarginLeftTh;
    }

    public int getMarginLeftTh() {
        return mMarginLeftTh;
    }

    public void setMarginRightTh(int mMarginRightTh) {
        this.mMarginRightTh = mMarginRightTh;
    }

    public int getMarginRightTh() {
        return mMarginRightTh;
    }

    public void setMarginTopTh(int mMarginTopTh) {
        this.mMarginTopTh = mMarginTopTh;
    }

    public int getMarginTopTh() {
        return mMarginTopTh;
    }

    public void setMarginBottomTh(int mMarginBottomTh) {
        this.mMarginBottomTh = mMarginBottomTh;
    }

    public int getMarginBottomTh() {
        return mMarginBottomTh;
    }

    private byte[] mBytes;

    @Override
    public byte[] getBytes() {
        if (mBytes == null) {
            mBytes = new byte[12];
        }
        return mBytes;
    }

    @Override
    public boolean parseBytes() {
        return StructParser.parseBytes(this, mBytes);
    }
}
