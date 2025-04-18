package com.orbbec.obsensor.types;

import com.orbbec.obsensor.ByteConversion;

/**
 * \if English
 * Camera parameters
 * \else
 * 相机参数
 * \endif
 */
public class CameraParam implements ByteConversion {
    /**
     * \if English
     * Depth camera internal parameters
     * \else
     * 深度相机的内参
     * \endif
     */
    @StructField(offset = 0, size = 20)
    private CameraIntrinsic mDepthIntrinsic;
    /**
     * \if English
     * Color camera internal parameters
     * \else
     * 彩色相机的内参
     * \endif
     */
    @StructField(offset = 20, size = 20)
    private CameraIntrinsic mColorIntrinsic;
    /**
     * \if English
     * Depth camera distortion parameters
     * \else
     * 深度相机的畸变参数
     * \endif
     */
    @StructField(offset = 40, size = 36)
    private CameraDistortion mDepthDistortion;
    /**
     * \if English
     * Color camera distortion parameters
     * \else
     * 彩色相机的畸变参数
     * \endif
     */
    @StructField(offset = 76, size = 36)
    private CameraDistortion mColorDistortion;
    /**
     * \if English
     * Rotation/transformation matrix
     * \else
     * 旋转/变换矩阵
     * \endif
     */
    @StructField(offset = 112, size = 48)
    private Extrinsic mTransform;
    /**
     * \if English
     * Whether the image frame corresponding to this group of parameters is mirrored
     * \else
     * 图像帧是否镜像
     * \endif
     */
    @StructField(offset = 160, size = 1)
    private byte mIsMirrored;

    public CameraIntrinsic getDepthIntrinsic() {
        return mDepthIntrinsic;
    }

    public CameraIntrinsic getColorIntrinsic() {
        return mColorIntrinsic;
    }

    public CameraDistortion getDepthDistortion() {
        return mDepthDistortion;
    }

    public CameraDistortion getColorDistortion() {
        return mColorDistortion;
    }

    public Extrinsic getTransform() {
        return mTransform;
    }

    public byte getIsMirrored() {
        return mIsMirrored;
    }

    private byte[] mBytes;

    @Override
    public byte[] getBytes() {
        if (mBytes == null) {
            mBytes = new byte[161];
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
            mBytes = new byte[161];
        }
        return StructParser.wrapBytes(this, mBytes);
    }

    @Override
    public String toString() {
        return "CameraParam{" +
                "mDepthIntrinsic=" + mDepthIntrinsic +
                ", mColorIntrinsic=" + mColorIntrinsic +
                ", mDepthDistortion=" + mDepthDistortion +
                ", mColorDistortion=" + mColorDistortion +
                ", mTransform=" + mTransform +
                ", mIsMirrored=" + mIsMirrored +
                '}';
    }
}
