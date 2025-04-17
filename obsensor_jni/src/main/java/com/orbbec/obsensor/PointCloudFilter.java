package com.orbbec.obsensor;

import com.orbbec.obsensor.types.Format;
import com.orbbec.obsensor.types.CoordinateSystemType;

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
        setConfigValue("pointFormat", format.value());
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
    @Deprecated
    public void setD2CAlignStatus(boolean isAlign) {
        throwInitializeException();
        nSetD2CAlignStatus(isAlign, mHandle);
    }

    /**
     * \if English
     * Set the point cloud coordinate data zoom factor.
     * Calling this function to set the scale will change the point coordinate scaling factor of the output point cloud frame, The point coordinate
     * scaling factor for the output point cloud frame can be obtained via @ref PointsFrame::getCoordinateValueScale function.
     *
     * @param factor The scale factor.
     * \else
     * 设置点云坐标数据缩放因子。
     * 调用此函数设置缩放比例，会改变输出点云帧的点坐标缩放因子，输出点云帧的点坐标缩放因子可通过@ref PointsFrame::getCoordinateValueScale函数获取。
     *
     * @param factor 缩放因子
     * \endif
     */
    public void setCoordinateDataScaled(float factor) {
        throwInitializeException();
        setConfigValue("coordinateDataScale", factor);
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
        setConfigValue("coordinateSystemType", state ? 1 : 0);
    }

    /**
     * \if English
     * Set the point cloud coordinate system
     *
     * @param type The coordinate system type
     * \else
     * 设置点云坐标系
     *
     * @param type 坐标系类型
     * \endif
     */
    public void setCoordinateSystem(CoordinateSystemType type) {
        throwInitializeException();
        setConfigValue("coordinateSystemType", type.value());
    }

    private static native long nCreate();

    private static native void nSetD2CAlignStatus(boolean d2cStatus, long filterPtr);
}
