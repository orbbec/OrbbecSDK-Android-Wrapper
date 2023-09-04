package com.orbbec.obsensor;

import com.orbbec.obsensor.datatype.CameraDistortion;
import com.orbbec.obsensor.datatype.CameraIntrinsic;
import com.orbbec.obsensor.datatype.D2CTransform;

/**
 * \if English
 * Camera parameters
 * \else
 * 相机参数
 * \endif
 */
public class CameraParam {
    CameraIntrinsic mDepthIntrinsic;
    CameraIntrinsic mColorIntrinsic;
    CameraDistortion mDepthDistortion;
    CameraDistortion mColorDistortion;
    D2CTransform mTransform;
    boolean mIsMirrored;

    /**
	 * \if English
	 * Get depth camera internal parameters
     *
     * @return Returns the depth camera intrinsic parameter {@link CameraIntrinsic}
	 * \else
     * 获取深度相机内参
     *
     * @return 返回深度相机内参 {@link CameraIntrinsic}
	 * \endif
     */
    public CameraIntrinsic getDepthIntrinsic() {
        return mDepthIntrinsic;
    }

    /**
	 * \if English
	 * Get color camera internal parameters
     *
     * @return Returns the color camera intrinsic parameter {@link CameraIntrinsic}
	 * \else
     * 获取彩色相机内参
     *
     * @return 返回彩色相机内参 {@link CameraIntrinsic}
	 * \endif
     */
    public CameraIntrinsic getColorIntrinsic() {
        return mColorIntrinsic;
    }

    /**
	 * \if English
	 * Get depth camera distortion parameters
     *
     * @return Returns the depth camera distortion parameter {@link CameraDistortion}
	 * \else
     * 获取深度相机去畸变参数
     *
     * @return 返回深度相机去畸变参数 {@link CameraDistortion}
	 * \endif
     */
    public CameraDistortion getDepthDistortion() {
        return mDepthDistortion;
    }

    /**
	 * \if English
	 * Get color camera dewarping parameters
     *
     * @return Returns the color camera dewarping parameter {@link Camera Distortion}
	 * \else
     * 获取彩色相机去畸变参数
     *
     * @return 返回彩色相机去畸变参数 {@link CameraDistortion}
	 * \endif
     */
    public CameraDistortion getColorDistortion() {
        return mColorDistortion;
    }

    /**
	 * \if English
	 * Get rotation/transformation matrix
     *
     * @return Rotation/Transformation matrix {@link D2CTransform}
	 * \else
     * 获取旋转/变换矩阵
     *
     * @return 旋转/变换矩阵 {@link D2CTransform}
	 * \endif
     */
    public D2CTransform getD2CTransform() {
        return mTransform;
    }

    /**
	 * \if English
	 * Whether the image frame corresponding to this group of parameters is mirrored
     *
     * @return Whether the image frame corresponding to this group of parameters is mirrored
	 * \else
     * 本组参数对应的图像帧是否被镜像
     *
     * @return 本组参数对应的图像帧是否被镜像
	 * \endif
     */
    public boolean isMirrored() {
        return mIsMirrored;
    }

    @Override
    public String toString() {
        return "CameraParams{\n" + "DepthIntrinsic:" + mDepthIntrinsic.toString() + "\n"
                + "ColorIntrinsic:" + mColorIntrinsic.toString() + "\n"
                + "DepthDistortion:" + mDepthDistortion.toString() + "\n"
                + "ColorDistortion:" + mColorDistortion.toString() + "\n"
                + "D2CTransform:" + mTransform.toString() + "\n"
                + "IsMirrored:" + mIsMirrored + "\n}";
    }
}
