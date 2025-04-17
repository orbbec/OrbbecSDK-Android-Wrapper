package com.orbbec.obsensor.types;

/**
 * \if English
 * TOF Exposure Threshold
 * \else
 * TOF曝光阈值
 * \endif
 */
public class ExposureThresholdControl {
    /**
     * \if English
     * Upper threshold, unit: ms
     * \else
     * 上限阈值，单位：ms
     * \endif
     */
    @StructField(offset = 0, size = 4)
    private int mUpper;
    /**
     * \if English
     * Lower threshold, unit: ms
     * \else
     * 下限阈值，单位：ms
     * \endif
     */
    @StructField(offset = 4, size = 4)
    private int mLower;

    private byte[] mBytes;

    public byte[] BYTES() {
        if (mBytes == null) {
            mBytes = new byte[8];
        }
        return mBytes;
    }
}
