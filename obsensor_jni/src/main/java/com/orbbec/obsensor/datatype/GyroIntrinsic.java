package com.orbbec.obsensor.datatype;

import com.orbbec.internal.DataUtilities;
import com.orbbec.obsensor.DataType;

import java.util.Arrays;

public class GyroIntrinsic extends DataType {
    /**
     * 噪声密度
     */
    private double mNoiseDensity;

    /**
     *
     */
    private double mRandomWalk;

    /**
     * 参考温度
     */
    private double mReferenceTemp;

    /**
     * 加速度计偏置
     */
    private double[] mBias;

    /**
     *
     */
    private double[] mScaleMisalignment;

    /**
     *
     */
    private double[] mTempSlope;

    public double getNoiseDensity() {
        throwInitializeException();
        return mNoiseDensity;
    }

    public double getRandomWalk() {
        throwInitializeException();
        return mRandomWalk;
    }

    public double getReferenceTemp() {
        throwInitializeException();
        return mReferenceTemp;
    }

    public double[] getBias() {
        throwInitializeException();
        return mBias;
    }

    public double[] getScaleMisalignment() {
        throwInitializeException();
        return mScaleMisalignment;
    }

    public double[] getTempSlope() {
        throwInitializeException();
        return mTempSlope;
    }

    @Override
    public int BYTES() {
        // Double.BYTES * 24
        return 192;
    }

    @Override
    protected boolean parseBytesImpl(byte[] bytes) {
        int offset = 0, length = Double.BYTES;
        mNoiseDensity = DataUtilities.bytesToDouble(DataUtilities.subBytes(bytes, offset, length));
        offset += length;
        mRandomWalk = DataUtilities.bytesToDouble(DataUtilities.subBytes(bytes, offset, length));
        offset += length;
        mReferenceTemp = DataUtilities.bytesToDouble(DataUtilities.subBytes(bytes, offset, length));
        offset += length;
        length = Double.BYTES * 3;
        mBias = DataUtilities.bytesToDoubles(DataUtilities.subBytes(bytes, offset, length), 3);
        offset += length;
        length = Double.BYTES * 9;
        mScaleMisalignment = DataUtilities.bytesToDoubles(DataUtilities.subBytes(bytes, offset, length), 9);
        offset += length;
        mTempSlope = DataUtilities.bytesToDoubles(DataUtilities.subBytes(bytes, offset, length), 9);
        return true;
    }

    @Override
    protected boolean wrapBytesImpl(byte[] bytes) {
        return true;
    }

    @Override
    public String toString() {
        return "GyroIntrinsic{" +
                "mNoiseDensity=" + mNoiseDensity +
                ", mRandomWalk=" + mRandomWalk +
                ", mReferenceTemp=" + mReferenceTemp +
                ", mBias=" + Arrays.toString(mBias) +
                ", mScaleMisalignment=" + Arrays.toString(mScaleMisalignment) +
                ", mTempSlope=" + Arrays.toString(mTempSlope) +
                '}';
    }
}
