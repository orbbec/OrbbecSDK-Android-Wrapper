package com.orbbec.obsensor.datatype;

import com.orbbec.internal.DataUtilities;
import com.orbbec.obsensor.DataType;
import com.orbbec.obsensor.type.EdgeNoiseRemovalType;

public class EdgeNoiseRemovalParams extends DataType {
    private EdgeNoiseRemovalType type;
    private int mMarginLeftTh;
    private int mMarginRightTh;
    private int mMarginTopTh;
    private int mMarginBottomTh;

    public EdgeNoiseRemovalType getType() {
        throwInitializeException();
        return type;
    }

    public void setMarginLeftTh(int mMarginLeftTh) {
        throwInitializeException();
        this.mMarginLeftTh = mMarginLeftTh;
    }

    public int getMarginLeftTh() {
        throwInitializeException();
        return mMarginLeftTh;
    }

    public void setMarginRightTh(int mMarginRightTh) {
        throwInitializeException();
        this.mMarginRightTh = mMarginRightTh;
    }

    public int getMarginRightTh() {
        throwInitializeException();
        return mMarginRightTh;
    }

    public void setMarginTopTh(int mMarginTopTh) {
        throwInitializeException();
        this.mMarginTopTh = mMarginTopTh;
    }

    public int getMarginTopTh() {
        throwInitializeException();
        return mMarginTopTh;
    }

    public void setMarginBottomTh(int mMarginBottomTh) {
        throwInitializeException();
        this.mMarginBottomTh = mMarginBottomTh;
    }

    public int getMarginBottomTh() {
        throwInitializeException();
        return mMarginBottomTh;
    }

    @Override
    public int BYTES() {
        return 12;
    }

    @Override
    protected boolean parseBytesImpl(byte[] bytes) {
        int offset = 0, length = Integer.BYTES;
        type = EdgeNoiseRemovalType.get(DataUtilities.bytesToInt(DataUtilities.subBytes(bytes, offset, length)));
        offset += length;
        length = Short.BYTES;
        mMarginLeftTh = DataUtilities.bytesToUnit16(DataUtilities.subBytes(bytes, offset, length));
        offset += length;
        mMarginRightTh = DataUtilities.bytesToUnit16(DataUtilities.subBytes(bytes, offset, length));
        offset += length;
        mMarginTopTh = DataUtilities.bytesToUnit16(DataUtilities.subBytes(bytes, offset, length));
        offset += length;
        mMarginBottomTh = DataUtilities.bytesToUnit16(DataUtilities.subBytes(bytes, offset, length));
        return true;
    }

    @Override
    protected boolean wrapBytesImpl(byte[] bytes) {
        int offset = 0, length = Integer.BYTES;
        DataUtilities.appendBytes(DataUtilities.intToBytes(type.value()), bytes, offset, length);
        offset += length;
        length = Short.BYTES;
        DataUtilities.appendBytes(DataUtilities.uint16ToBytes(mMarginLeftTh), bytes, offset, length);
        offset += length;
        DataUtilities.appendBytes(DataUtilities.uint16ToBytes(mMarginRightTh), bytes, offset, length);
        offset += length;
        DataUtilities.appendBytes(DataUtilities.uint16ToBytes(mMarginTopTh), bytes, offset, length);
        offset += length;
        DataUtilities.appendBytes(DataUtilities.uint16ToBytes(mMarginBottomTh), bytes, offset, length);
        return true;
    }

    @Override
    public String toString() {
        return "EdgeNoiseRemovalFilterParams{" +
                "type=" + type +
                ", mMarginLeftTh=" + mMarginLeftTh +
                ", mMarginRightTh=" + mMarginRightTh +
                ", mMarginTopTh=" + mMarginTopTh +
                ", mMarginBottomTh=" + mMarginBottomTh +
                '}';
    }
}
