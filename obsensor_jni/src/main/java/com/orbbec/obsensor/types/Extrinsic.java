package com.orbbec.obsensor.types;

import com.orbbec.obsensor.ByteConversion;

import java.util.Arrays;

/**
 * \if English
 * Structure for rotation/transformation
 * \else
 * 旋转/变换结构
 * \endif
 */
public class Extrinsic implements ByteConversion {
    /**
     * \if English
     * Rotation matrix
     * \else
     * 旋转矩阵
     * \endif
     */
    @StructField(offset = 0, size = 36, arraySize = 9)
    private float[] mRot;
    /**
     * \if English
     * Transformation matrix in millimeters
     * \else
     * 变换矩阵，单位：毫米
     * \endif
     */
    @StructField(offset = 36, size = 12, arraySize = 3)
    private float[] mTrans;

    private byte[] mBytes;

    @Override
    public byte[] getBytes() {
        if (mBytes == null) {
            mBytes = new byte[48];
        }
        return mBytes;
    }

    @Override
    public boolean parseBytes() {
        return StructParser.parseBytes(this, mBytes);
    }

    @Override
    public String toString() {
        return "Extrinsic{" +
                "mRot=" + Arrays.toString(mRot) +
                ", mTrans=" + Arrays.toString(mTrans) +
                '}';
    }
}
