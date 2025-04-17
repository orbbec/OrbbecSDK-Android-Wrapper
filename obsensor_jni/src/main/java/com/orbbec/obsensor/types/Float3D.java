package com.orbbec.obsensor.types;

import com.orbbec.obsensor.ByteConversion;

/**
 * \if English
 * Data structures for accelerometers and gyroscopes
 * \else
 * 加速度计和陀螺仪的数据结构
 * \endif
 */
public class Float3D implements ByteConversion {
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

    public byte[] BYTES() {
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
    public boolean wrapBytes(byte[] bytes) {
        return StructParser.wrapBytes(this, bytes);
    }

    @Override
    public String toString() {
        return "Float3D{" +
                "mX=" + mX +
                ", mY=" + mY +
                ", mZ=" + mZ +
                '}';
    }
}
