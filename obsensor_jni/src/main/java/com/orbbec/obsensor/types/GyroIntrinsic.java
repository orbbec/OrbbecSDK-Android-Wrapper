package com.orbbec.obsensor.types;

import com.orbbec.obsensor.ByteConversion;

/**
 * \if English
 * Structure for gyroscope intrinsic parameters
 * \else
 * 陀螺仪内参结构
 * \endif
 */
public class GyroIntrinsic implements ByteConversion {
    /**
     * \if English
     * In-run bias instability
     * \else
     * 噪声不稳定性
     * \endif
     */
    @StructField(offset = 0, size = 8)
    private double mNoiseDensity;
    /**
     * \if English
     * random walk
     * \else
     * 随机步长
     * \endif
     */
    @StructField(offset = 8, size = 8)
    private double mRandomWalk;
    /**
     * \if English
     * reference temperature
     * \else
     * 参考温度
     * \endif
     */
    @StructField(offset = 16, size = 8)
    private double mReferenceTemp;
    /**
     * \if English
     * bias for x, y, z axis
     * \else
     * x, y, z轴的偏置
     * \endif
     */
    @StructField(offset = 24, size = 24, arraySize = 3)
    private double[] mBias;
    /**
     * \if English
     * scale factor and three-axis non-orthogonal error
     * \else
     * 缩放因子和三个轴不对称误差
     * \endif
     */
    @StructField(offset = 48, size = 72, arraySize = 9)
    private double[] mScaleMisalignment;
    /**
     * \if English
     * linear temperature drift coefficient
     * \else
     * 线性温度漂移系数
     * \endif
     */
    @StructField(offset = 120, size = 72, arraySize = 9)
    private double[] mTempSlope;

    private byte[] mBytes;

    @Override
    public byte[] getBytes() {
        if (mBytes == null) {
            mBytes = new byte[192];
        }
        return mBytes;
    }

    @Override
    public boolean parseBytes() {
        return StructParser.parseBytes(this, mBytes);
    }
}
