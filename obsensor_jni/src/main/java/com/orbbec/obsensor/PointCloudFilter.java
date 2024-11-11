package com.orbbec.obsensor;

/**
 * \if English
 * PointCloudFilter used to generate normal point cloud data, RGB point cloud data
 * \else
 * PointCloudFilter 用于生成普通点云数据，RGB点云数据  
 * \endif
 */
public class PointCloudFilter extends Filter {
    private static final String TAG = "PointCloudFilter";

    /**
	 * \if English
     * Create point cloud filter
	 * \else
     * 创建点云filter
     * \endif
     */
    public PointCloudFilter() {
        super(nCreate());
    }

    /**
	 * \if English
	 * Set the generated point cloud format
     *
     * @param format point cloud format {@link Format}
	 * \else
     * 设置生成点云的格式
     *
     * @param format 点云格式 {@link Format}
	 * \endif
     */
    public void setPointFormat(Format format) {
        throwInitializeException();
        nSetPointFormat(format.value(), mHandle);
    }

    /**
	 * \if English
	 * PointCloud Filter device camera parameters
     *
     * @param param Camera parameter {@link CameraParam}
	 * \else
     * PointCloud Filter设备相机参数
     *
     * @param param 相机参数 {@link CameraParam}
	 * \endif
     */
    public void setCameraParam(CameraParam param) {
        throwInitializeException();
        if (param.mDepthIntrinsic.wrapBytes() && param.mColorIntrinsic.wrapBytes()
                && param.mDepthDistortion.wrapBytes() && param.mColorDistortion.wrapBytes()
                && param.mTransform.wrapBytes()) {
            nSetCameraParam(mHandle, param.mDepthIntrinsic.getBytes(), param.mColorIntrinsic.getBytes(),
                    param.mDepthDistortion.getBytes(), param.mColorDistortion.getBytes(), param.mTransform.getBytes(), param.mIsMirrored);
        } else {
            throw new OBException("setCameraParam wrap bytes error!");
        }
    }

    /**
	 * \if English
	 * Set D2C alignment state
     *
     * @param isAlign D2C alignment status, true: aligned, false: not aligned
	 * \else
     * 设置D2C对齐状态
     *
     * @param isAlign D2C对齐状态，true: 对齐， false: 不对齐
	 * \endif
     */
    public void setD2CAlignStatus(boolean isAlign) {
        throwInitializeException();
        nSetD2CAlignStatus(isAlign, mHandle);
    }

    /**
     * \if English
     * Set the point cloud data scaling factor
     *
     * @param scale Set the point cloud coordinate data zoom factor
     * \else
     * 设置点云数据缩放比例
     *
     * @param scale 设置点云坐标数据缩放比例
     * \endif
     */
    public void setPositionDataScale(float scale) {
        throwInitializeException();
        nSetPositionDataScale(mHandle, scale);
    }

    /**
     * \if English
     * Set point cloud color data normalization
     *
     * @param state Sets whether the point cloud color data is normalized
     * \else
     * 设置点云颜色数据归一化
     *
     * @param state 设置点云颜色数据是否归一化
     * \endif
     */
    public void setColorDataNormalization(boolean state) {
        throwInitializeException();
        nSetColorDataNormalization(mHandle, state);
    }

    private static native long nCreate();

    private static native void nSetPointFormat(int format, long filterPtr);

    private static native void nSetCameraParam(long handle, byte[] depthIntr, byte[] colorIntr,
                                               byte[] depthDistor, byte[] colorDistor, byte[] tran, boolean isMirrored);

    private static native void nSetD2CAlignStatus(boolean d2cStatus, long filterPtr);

    private static native void nSetPositionDataScale(long handle, float scale);

    private static native void nSetColorDataNormalization(long handle, boolean state);
}
