package com.orbbec.obsensor.types;

/**
 * \if English
 * SequenceId fliter list item
 * \else
 * 序列化ID过滤器列表项
 * \endif
 */
public class SequenceIdItem {
    @StructField(offset = 0, size = 4)
    private int mSequenceSelectId;
    @StructField(offset = 4, size = 8)
    private String mName;

    public int getSequenceSelectId() {
        return mSequenceSelectId;
    }

    public void setSequenceSelectId(int mSequenceSelectId) {
        this.mSequenceSelectId = mSequenceSelectId;
    }

    public String getName() {
        return mName;
    }

    public void setName(String mName) {
        this.mName = mName;
    }

    private byte[] mBytes;

    public byte[] BYTES() {
        if (mBytes == null) {
            mBytes = new byte[12];
        }
        return mBytes;
    }
}
