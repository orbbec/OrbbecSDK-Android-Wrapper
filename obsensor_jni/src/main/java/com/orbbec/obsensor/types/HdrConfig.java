package com.orbbec.obsensor.types;

import com.orbbec.obsensor.ByteConversion;

/**
 * \if English
 * HDR Configuration
 * \else
 * HDR配置
 * \endif
 */
public class HdrConfig implements ByteConversion {
    @StructField(offset = 0, size = 1)
    private short enable;
    /**
     * \if English
     * Sequence name
     * \else
     * 序列名称
     * \endif
     */
    @StructField(offset = 1, size = 1)
    private short sequenceName;
    /**
     * \if English
     * Exposure time 1
     * \else
     * 曝光时间1
     * \endif
     */
    @StructField(offset = 2, size = 4)
    private long exposure1;
    /**
     * \if English
     * Gain 1
     * \else
     * 增益1
     * \endif
     */
    @StructField(offset = 6, size = 4)
    private long gain1;
    /**
     * \if English
     * Exposure time 2
     * \else
     * 曝光时间2
     * \endif
     */
    @StructField(offset = 10, size = 4)
    private long exposure2;
    /**
     * \if English
     * Gain 2
     * \else
     * 增益2
     * \endif
     */
    @StructField(offset = 14, size = 4)
    private long gain2;

    public short getEnable() {
        return enable;
    }

    public void setEnable(byte enable) {
        this.enable = enable;
    }

    public short getSequenceName() {
        return sequenceName;
    }

    public void setSequenceName(byte sequenceName) {
        this.sequenceName = sequenceName;
    }

    public long getExposure1() {
        return exposure1;
    }

    public void setExposure1(int exposure1) {
        this.exposure1 = exposure1;
    }

    public long getGain1() {
        return gain1;
    }

    public void setGain1(int gain1) {
        this.gain1 = gain1;
    }

    public long getExposure2() {
        return exposure2;
    }

    public void setExposure2(int exposure2) {
        this.exposure2 = exposure2;
    }

    public long getGain2() {
        return gain2;
    }

    public void setGain2(int gain2) {
        this.gain2 = gain2;
    }

    private byte[] mBytes;

    @Override
    public byte[] getBytes() {
        if (mBytes == null) {
            mBytes = new byte[18];
        }
        return mBytes;
    }

    @Override
    public boolean parseBytes() {
        return StructParser.parseBytes(this, mBytes);
    }

    @Override
    public boolean wrapBytes() {
        if (mBytes == null) {
            mBytes = new byte[18];
        }
        return StructParser.wrapBytes(this, mBytes);
    }

    @Override
    public String toString() {
        return "HdrConfig{" +
                "enable=" + enable +
                ", sequenceName=" + sequenceName +
                ", exposure1=" + exposure1 +
                ", gain1=" + gain1 +
                ", exposure2=" + exposure2 +
                ", gain2=" + gain2 +
                '}';
    }
}
