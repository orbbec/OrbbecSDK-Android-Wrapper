package com.orbbec.obsensor;

import com.orbbec.obsensor.SensorType;
import com.orbbec.obsensor.datatype.CameraDistortion;
import com.orbbec.obsensor.datatype.CameraIntrinsic;
import com.orbbec.obsensor.datatype.D2CTransform;

import java.util.Arrays;

/**
 * \if English
 * calibration parameters
 * \else
 * 校准参数
 * \endif
 */
public class CalibrationParam {
    CameraIntrinsic[] intrinsics;
    CameraDistortion[] distortions;
    D2CTransform[][] extrinsics;

    /**
     * \if English
     * Get sensor internal parameters
     *
     * @return Returns the sensor internal parameters {@link CameraIntrinsic}
     * \else
     * 获取传感器的内参
     * @return 返回传感器的内参 {@link CameraIntrinsic}
     */
    public CameraIntrinsic[] getIntrinsics() {
        return intrinsics;
    }

    /**
     * \if English
     * Get Sensor distortion
     *
     * @return Returns the Sensor distortion {@link CameraDistortion}
     * \else
     * 获取传感器的畸变参数
     * @return 返回传感器的畸变参数 {@link CameraDistortion}
     * \endif
     */
    public CameraDistortion[] getDistortions() {
        return distortions;
    }

    /**
     * \if English
     * To transform/rotation from a source to a target 3D coordinate system
     *
     * @return Returns the transformation/Rotation matrix {@link D2CTransform}
     * \else
     * 从源坐标系到目标坐标系的变换/旋转矩阵
     * @return 变换/旋转后的矩阵 {@link D2CTransform}
     */
    public D2CTransform[][] getExtrinsics() {
        return extrinsics;
    }

    @Override
    public String toString() {
        return "CalibrationParam{" +
                "intrinsics=" + intrinsics +
                ", distortions=" + distortions +
                ", extrinsics=" + extrinsics +
                '}';
    }
}
