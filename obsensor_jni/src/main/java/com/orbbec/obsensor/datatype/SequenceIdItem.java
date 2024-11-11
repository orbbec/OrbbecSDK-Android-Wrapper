package com.orbbec.obsensor.datatype;

import com.orbbec.internal.DataUtilities;
import com.orbbec.obsensor.DataType;

import java.util.Arrays;

/**
 * \if English
 * SequenceId fliter list item
 * \else
 * 序列化ID过滤器列表项
 * \endif
 */
public class SequenceIdItem extends DataType {
    private int mSequenceSelectId;
    private String mName;

    public int getmSequenceSelectId() {
        throwInitializeException();
        return mSequenceSelectId;
    }

    public String getmName() {
        throwInitializeException();
        return mName;
    }

    @Override
    public int BYTES() {
        return 20;
    }

    @Override
    protected boolean parseBytesImpl(byte[] bytes) {
        int offset = 0, length = Integer.BYTES;
        mSequenceSelectId = DataUtilities.bytesToInt(DataUtilities.subBytes(bytes, offset, length));
        offset += length;
        length = 16;
        mName = DataUtilities.bytesToString(DataUtilities.subBytes(bytes, offset, length));
        return true;
    }

    @Override
    protected boolean wrapBytesImpl(byte[] bytes) {
        return true;
    }

    @Override
    public String toString() {
        return "SequenceIdItem{" +
                "mSequenceSelectId=" + mSequenceSelectId +
                ", mName=" + mName +
                '}';
    }
}
