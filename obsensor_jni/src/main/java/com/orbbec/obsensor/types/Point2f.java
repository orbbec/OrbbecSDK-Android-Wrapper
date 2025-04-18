package com.orbbec.obsensor.types;

import com.orbbec.obsensor.ByteConversion;

/**
 * \if English
 * 2D point structure in the SDK
 * \else
 * SDK中的2D点结构
 * \endif
 */
public class Point2f implements ByteConversion {
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

    private byte[] mBytes;

    public Point2f() {
    }

    public Point2f(float x, float y) {
        this.mX = x;
        this.mY = y;
    }

    @Override
    public byte[] getBytes() {
        if (mBytes == null) {
            mBytes = new byte[8];
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
            mBytes = new byte[8];
        }
        return StructParser.wrapBytes(this, mBytes);
    }

    @Override
    public String toString() {
        return "Point2f{" +
                "mX=" + mX +
                ", mY=" + mY +
                '}';
    }
}
