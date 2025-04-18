package com.orbbec.obsensor.types;

import com.orbbec.obsensor.ByteConversion;

/**
 * \if English
 * Distortion Parameters
 * \else
 * 畸变参数
 * \endif
 */
public class CameraDistortion implements ByteConversion {
    /**
     * \if English
     * Radial distortion factor 1
     * \else
     * 径向畸变系数1
     * \endif
     */
    @StructField(offset = 0, size = 4)
    private float mK1;
    /**
     * \if English
     * Radial distortion factor 2
     * \else
     * 径向畸变系数2
     * \endif
     */
    @StructField(offset = 4, size = 4)
    private float mK2;
    /**
     * \if English
     * Radial distortion factor 3
     * \else
     * 径向畸变系数3
     * \endif
     */
    @StructField(offset = 8, size = 4)
    private float mK3;
    /**
     * \if English
     * Radial distortion factor 4
     * \else
     * 径向畸变系数4
     * \endif
     */
    @StructField(offset = 12, size = 4)
    private float mK4;
    /**
     * \if English
     * Radial distortion factor 5
     * \else
     * 径向畸变系数5
     * \endif
     */
    @StructField(offset = 16, size = 4)
    private float mK5;
    /**
     * \if English
     * Radial distortion factor 6
     * \else
     * 径向畸变系数6
     * \endif
     */
    @StructField(offset = 20, size = 4)
    private float mK6;
    /**
     * \if English
     * Tangential distortion factor 1
     * \else
     * 切向畸变系数1
     * \endif
     */
    @StructField(offset = 24, size = 4)
    private float mP1;
    /**
     * \if English
     * Tangential distortion factor 2
     * \else
     * 切向畸变系数2
     * \endif
     */
    @StructField(offset = 28, size = 4)
    private float mP2;

    @StructField(offset = 32, size = 4)
    private int model;

    private byte[] mBytes;

    @Override
    public byte[] getBytes() {
        if (mBytes == null) {
            mBytes = new byte[36];
        }
        return mBytes;
    }

    @Override
    public boolean parseBytes() {
        return StructParser.parseBytes(this, mBytes);
    }

    @Override
    public String toString() {
        return "CameraDistortion{" +
                "mK1=" + mK1 +
                ", mK2=" + mK2 +
                ", mK3=" + mK3 +
                ", mK4=" + mK4 +
                ", mK5=" + mK5 +
                ", mK6=" + mK6 +
                ", mP1=" + mP1 +
                ", mP2=" + mP2 +
                ", model=" + CameraDistortionModel.get(model) +
                '}';
    }
}
