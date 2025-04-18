package com.orbbec.obsensor.types;

import com.orbbec.obsensor.ByteConversion;

/**
 * \if English
 * Camera internal parameters
 * \else
 * 相机内参
 * \endif
 */
public class CameraIntrinsic implements ByteConversion {
    /**
     * \if English
     * Focal length in x direction, unit: pixel
     * \else
     * x方向焦距，单位：像素
     * \endif
     */
    @StructField(offset = 0, size = 4)
    private float mFx;
    /**
     * \if English
     * Focal length in y direction, unit: pixel
     * \else
     * y方向焦距，单位：像素
     * \endif
     */
    @StructField(offset = 4, size = 4)
    private float mFy;
    /**
     * \if English
     * Optical center abscissa
     * \else
     * 光心横坐标
     * \endif
     */
    @StructField(offset = 8, size = 4)
    private float mCx;
    /**
     * \if English
     * Optical center ordinate
     * \else
     * 光心纵坐标
     * \endif
     */
    @StructField(offset = 12, size = 4)
    private float mCy;
    /**
     * \if English
     * image width
     * \else
     * 图像宽度
     * \endif
     */
    @StructField(offset = 16, size = 2)
    private short mWidth;
    /**
     * \if English
     * image height
     * \else
     * 图像高度
     * \endif
     */
    @StructField(offset = 18, size = 2)
    private short mHeight;

    private byte[] mBytes;

    @Override
    public byte[] getBytes() {
        if (mBytes == null) {
            mBytes = new byte[20];
        }
        return mBytes;
    }

    @Override
    public boolean parseBytes() {
        return StructParser.parseBytes(this, mBytes);
    }

    @Override
    public String toString() {
        return "CameraIntrinsic{" +
                "mFx=" + mFx +
                ", mFy=" + mFy +
                ", mCx=" + mCx +
                ", mCy=" + mCy +
                ", mWidth=" + mWidth +
                ", mHeight=" + mHeight +
                '}';
    }
}
