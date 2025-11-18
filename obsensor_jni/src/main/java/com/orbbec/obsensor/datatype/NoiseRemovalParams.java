package com.orbbec.obsensor.datatype;

import com.orbbec.internal.DataUtilities;
import com.orbbec.obsensor.DataType;
import com.orbbec.obsensor.type.DDONoiseRemovalType;

public class NoiseRemovalParams extends DataType {

    private int mMaxSize;
    private int mDispDiff;

    private DDONoiseRemovalType mType;

    public int getMaxSize() {
        throwInitializeException();
        return mMaxSize;
    }

    public void setMaxSize(int mMaxSize) {
        this.mMaxSize = mMaxSize;
    }

    public int getDispDiff() {
        throwInitializeException();
        return mDispDiff;
    }

    public void setDispDiff(int mDispDiff) {
        this.mDispDiff = mDispDiff;
    }

    public DDONoiseRemovalType getType() {
        throwInitializeException();
        return mType;
    }

    public void setType(DDONoiseRemovalType mType) {
        this.mType = mType;
    }

    @Override
    public int BYTES() {
        // Short.BYTES * 2 + Integer.BYTES
        return 8;
    }

    @Override
    protected boolean parseBytesImpl(byte[] bytes) {
        int offset = 0, length = Short.BYTES;
        mMaxSize = DataUtilities.bytesToUnit16(DataUtilities.subBytes(bytes, offset, length));
        offset += length;
        mDispDiff = DataUtilities.bytesToUnit16(DataUtilities.subBytes(bytes, offset, length));
        offset += length;
        length = Integer.BYTES;
        mType = DDONoiseRemovalType.get(DataUtilities.bytesToInt(DataUtilities.subBytes(bytes, offset, length)));
        return true;
    }

    @Override
    protected boolean wrapBytesImpl(byte[] bytes) {
        int offset = 0, length = Short.BYTES;
        DataUtilities.appendBytes(DataUtilities.uint16ToBytes(mMaxSize), bytes, offset, length);
        offset += length;
        DataUtilities.appendBytes(DataUtilities.uint16ToBytes(mDispDiff), bytes, offset, length);
        offset += length;
        length = Integer.BYTES;
        DataUtilities.appendBytes(DataUtilities.intToBytes(mType.value()), bytes, offset, length);
        return true;
    }

    @Override
    public String toString() {
        return "NoiseRemovalFilterParams{" +
                "mMaxSize=" + mMaxSize +
                ", mDispDiff=" + mDispDiff +
                ", mType=" + mType +
                '}';
    }
}
