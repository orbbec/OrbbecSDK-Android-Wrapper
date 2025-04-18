package com.orbbec.obsensor.types;

import com.orbbec.obsensor.ByteConversion;

/**
 * \if English
 * 3D point structure in the SDK
 * \else
 * SDK中的3D点结构
 * \endif
 */
public class Point3f implements ByteConversion {
    /**
     * \if English
     * X coordinate
     * \else
     * X 坐标
     * \endif
     */
    @StructField(offset = 0, size = 4)
    private float mX;
    /**
     * \if English
     * Y coordinate
     * \else
     * Y 坐标
     * \endif
     */
    @StructField(offset = 4, size = 4)
    private float mY;
    /**
     * \if English
     * Z coordinate
     * \else
     * Z 坐标
     * \endif
     */
    @StructField(offset = 8, size = 4)
    private float mZ;

    private byte[] mBytes;

    public Point3f() {
    }

    public Point3f(float x, float y, float z) {
        this.mX = x;
        this.mY = y;
        this.mZ = z;
    }

    @Override
    public byte[] getBytes() {
        if (mBytes == null) {
            mBytes = new byte[12];
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
            mBytes = new byte[12];
        }
        return StructParser.wrapBytes(this, mBytes);
    }

    @Override
    public String toString() {
        return "Point3f{" +
                "mX=" + mX +
                ", mY=" + mY +
                ", mZ=" + mZ +
                '}';
    }
}
