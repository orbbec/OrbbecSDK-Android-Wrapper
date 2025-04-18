package com.orbbec.obsensor.types;

import com.orbbec.obsensor.ByteConversion;

import java.util.Arrays;

/**
 * \if English
 * calibration parameters
 * \else
 * 校准参数
 * \endif
 */
public class CalibrationParam implements ByteConversion {
    /**
     * \if English
     * Sensor internal parameters
     * \else
     * 传感器内参
     * \endif
     */
    @StructField(offset = 0, size = 180, arraySize = 9)
    private CameraIntrinsic[] mIntrinsics;
    /**
     * \if English
     * Sensor distortion
     * \else
     * 传感器变形
     * \endif
     */
    @StructField(offset = 180, size = 324, arraySize = 9)
    private CameraDistortion[] mDistortions;
    /**
     * \if English
     * The extrinsic parameters allow 3D coordinate conversions between sensor.To transform from a
     * source to a target 3D coordinate system,under extrinsics[source][target].
     * \else
     * 外参参数，允许将源坐标系下的三维点坐标转换到目标坐标系下的三维点坐标。
     * 在 extrinsics[source][target]中，可以转换从源坐标系到目标坐标系的三维点坐标。
     * \endif
     */
    @StructField(offset = 504, size = 3888, arraySize = 81, rows = 9)
    private Extrinsic[][] mExtrinsics;

    private byte[] mBytes;

    @Override
    public byte[] getBytes() {
        if (mBytes == null) {
            mBytes = new byte[4392];
        }
        return mBytes;
    }

    @Override
    public boolean parseBytes() {
        return StructParser.parseBytes(this, mBytes);
    }

    @Override
    public String toString() {
        return "CalibrationParam{" +
                "mIntrinsics=" + Arrays.toString(mIntrinsics) +
                ", mDistortions=" + Arrays.toString(mDistortions) +
                ", mExtrinsics=" + Arrays.toString(mExtrinsics) +
                '}';
    }
}
