package com.orbbec.obsensor;

import com.orbbec.internal.DataUtilities;

public class HdrConfig extends DataType {
    private byte enable;
    private byte sequenceName;
    private int exposure1;
    private int gain1;
    private int exposure2;
    private int gain2;

    public byte getEnable() {
        return enable;
    }

    public void setEnable(byte enable) {
        this.enable = enable;
    }

    public byte getSequenceName() {
        return sequenceName;
    }

    public void setSequenceName(byte sequenceName) {
        this.sequenceName = sequenceName;
    }

    public int getExposure1() {
        return exposure1;
    }

    public void setExposure1(int exposure1) {
        this.exposure1 = exposure1;
    }

    public int getGain1() {
        return gain1;
    }

    public void setGain1(int gain1) {
        this.gain1 = gain1;
    }

    public int getExposure2() {
        return exposure2;
    }

    public void setExposure2(int exposure2) {
        this.exposure2 = exposure2;
    }

    public int getGain2() {
        return gain2;
    }

    public void setGain2(int gain2) {
        this.gain2 = gain2;
    }

    @Override
    public int BYTES() {
        // Byte.BYTES * 2 + Integer.BYTES * 4
        return 18;
    }

    @Override
    protected boolean parseBytesImpl(byte[] bytes) {
        return true;
    }

    @Override
    protected boolean wrapBytesImpl(byte[] bytes) {
        int offset = 0, length = Byte.BYTES;
        DataUtilities.appendBytes(new byte[]{enable}, bytes, offset, length);
        offset += length;
        DataUtilities.appendBytes(new byte[]{sequenceName}, bytes, offset, length);
        offset += length;
        length = Integer.BYTES;
        DataUtilities.appendBytes(DataUtilities.intToBytes(exposure1), bytes, offset, length);
        offset += length;
        DataUtilities.appendBytes(DataUtilities.intToBytes(gain1), bytes, offset, length);
        offset += length;
        DataUtilities.appendBytes(DataUtilities.intToBytes(exposure2), bytes, offset, length);
        offset += length;
        DataUtilities.appendBytes(DataUtilities.intToBytes(gain2), bytes, offset, length);
        return true;
    }
}
