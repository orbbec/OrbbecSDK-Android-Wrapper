package com.orbbec.obsensor;

import com.orbbec.internal.DataUtilities;
import com.orbbec.obsensor.datatype.CameraDistortion;
import com.orbbec.obsensor.datatype.CameraIntrinsic;
import com.orbbec.obsensor.datatype.D2CTransform;
import com.orbbec.obsensor.datatype.Point2f;
import com.orbbec.obsensor.datatype.Point3f;
import com.orbbec.obsensor.datatype.XYTables;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;

public class CoordinateUtil {
//    private static byte[] CameraParamBytes(CalibrationParam param) {
//        ByteBuffer buffer = ByteBuffer.allocate(CalibrationParam.BYTES());
//        buffer.order(ByteOrder.nativeOrder());
//
//        for (int i = 0; i < param.getIntrinsics().length; i++) {
//            buffer.put(param.getIntrinsics()[i].getBytes());
//        }
//        for (int i = 0; i < param.getDistortions().length; i++) {
//            buffer.put(param.getDistortions()[i].getBytes());
//        }
////        for (int i = 0; i < param.getExtrinsics().length; i++) {
////            buffer.put(param.getExtrinsics()[i / 9][i % 9].getBytes());
////        }
//        for (int i = 0; i < param.getExtrinsics().length; i++) {
//            for (int j = 0; j < param.getExtrinsics()[i].length; j++) {
//                buffer.put(param.getExtrinsics()[i][j].getBytes());
//            }
//        }
//
//        return buffer.array();
//    }

    private static byte[] calibrationParamToBytes(CalibrationParam cp) {
        CameraIntrinsic[] intrs = cp.intrinsics;
        int intrLen = intrs[0].BYTES();
        CameraDistortion[] distors = cp.distortions;
        int distLen = distors[0].BYTES();
        D2CTransform[][] d2cTrans = cp.extrinsics;
        int d2cTranLen = d2cTrans[0][0].BYTES();
        int bytesLen = intrs.length * intrLen
                + distors.length * distLen
                + d2cTrans.length * d2cTrans[0].length * d2cTranLen;
        byte[] bytes = new byte[bytesLen];
        int offset = 0;
        for (int i = 0; i < intrs.length; i++) {
            DataUtilities.appendBytes(intrs[i].getBytes(), bytes, offset, intrLen);
            offset += intrLen;
        }
        for (int i = 0; i < distors.length; i++) {
            DataUtilities.appendBytes(distors[i].getBytes(), bytes, offset, distLen);
            offset += distLen;
        }
        for (int i = 0; i < d2cTrans.length; i++) {
            for (int j = 0; j < d2cTrans[i].length; j++) {
                DataUtilities.appendBytes(d2cTrans[i][j].getBytes(), bytes, offset, d2cTranLen);
                offset += d2cTranLen;
            }
        }
        return bytes;
    }

    /**
     * \if English
     * Transform a 3d point of a source coordinate system into a 3d point of the target coordinate system.
     *
     * @param calibrationParam Device calibration param,see pipeline::getCalibrationParam
     * @param sourcePoint3f    Source 3d point value
     * @param sourceType       Source sensor type
     * @param targetType       Target sensor type
     * @param targetPoint3f    Target 3d point value
     * @param calibrationParam 设备校准参数，参考pipeline::getCalibrationParam
     * @param sourcePoint3f    源三维点值
     * @param sourceType       源传感器类型
     * @param targetType       目标传感器类型
     * @param targetPoint3f    目标三维点值
     * @return bool Transform result
     * \else
     * 将一个源坐标系下的三维点坐标转换到目标坐标系下的三维点坐标。
     * @return bool 转换结果
     */
    public static boolean is3dTo3d(CalibrationParam calibrationParam,
                                   Point3f sourcePoint3f,
                                   SensorType sourceType, SensorType targetType,
                                   Point3f targetPoint3f) {
        byte[] calibrationParamBytes = calibrationParamToBytes(calibrationParam);
        return nIs3dTo3d(calibrationParamBytes, sourcePoint3f.getBytes(),
                sourceType.value(), targetType.value(),
                targetPoint3f.getBytes());
    }

    /**
     * \if English
     * Transform a 2d pixel coordinate with an associated depth value of the source camera into a 3d point of the target coordinate system.
     *
     * @param calibrationParam Device calibration param,see pipeline::getCalibrationParam
     * @param sourcePoint2f    Source 2d point value
     * @param sourceDepthPixel The depth of sourcePoint2f in millimeters
     * @param sourceType       Source sensor type
     * @param targetType       Target sensor type
     * @param targetPoint3f    Target 3d point value
     * @param calibrationParam 设备校准参数，参考pipeline::getCalibrationParam
     * @param sourcePoint2f    源二维点值
     * @param sourceDepthPixel 源点坐标对应的深度值，单位为毫米
     * @param sourceType       源传感器类型
     * @param targetType       目标传感器类型
     * @param targetPoint3f    目标三维点值
     * @return bool Transform result
     * \else
     * 将一个源相机的2d像素坐标和关联的深度值转换到目标坐标系下的三维点坐标。
     * @return bool 转换结果
     */
    public static boolean is2dTo3d(CalibrationParam calibrationParam,
                                   Point2f sourcePoint2f,
                                   float sourceDepthPixel,
                                   SensorType sourceType, SensorType targetType,
                                   Point3f targetPoint3f) {
        byte[] calibrationParamBytes = calibrationParamToBytes(calibrationParam);
        return nIs2dTo3d(calibrationParamBytes, sourcePoint2f.getBytes(),
                sourceDepthPixel,
                sourceType.value(), targetType.value(),
                targetPoint3f.getBytes());
    }

    /**
     * \if English
     * Transform a 2d pixel coordinate with an associated depth value of the source camera into a 3d point of the target coordinate system.
     *
     * @param calibrationParam Device calibration param,see pipeline::getCalibrationParam
     * @param sourcePoint2f    Source 2d point value
     * @param sourceDepthPixel The depth of sourcePoint2f in millimeters
     * @param sourceType       Source sensor type
     * @param targetType       Target sensor type
     * @param targetPoint3f    Target 3d point value
     * @param calibrationParam 设备校准参数，参考pipeline::getCalibrationParam
     * @param sourcePoint2f    源二维点值
     * @param sourceDepthPixel 源点坐标对应的深度值，单位为毫米
     * @param sourceType       源传感器类型
     * @param targetType       目标传感器类型
     * @param targetPoint3f    目标三维点值
     * @return bool Transform result
     * \else
     * 将一个源相机的2d像素坐标和关联的深度值转换到目标坐标系下的三维点坐标。
     * @return bool 转换结果
     */
    public static boolean is2dTo3dUndistortion(CalibrationParam calibrationParam,
                                               Point2f sourcePoint2f,
                                               float sourceDepthPixel,
                                               SensorType sourceType, SensorType targetType,
                                               Point3f targetPoint3f) {
        byte[] calibrationParamBytes = calibrationParamToBytes(calibrationParam);
        return nIs2dTo3dUndistortion(calibrationParamBytes, sourcePoint2f.getBytes(),
                sourceDepthPixel,
                sourceType.value(), targetType.value(),
                targetPoint3f.getBytes());
    }

    /**
     * \if English
     * Transform a 3d point of a source coordinate system into a 2d pixel coordinate of the target camera.
     *
     * @param calibrationParam Device calibration param,see pipeline::getCalibrationParam
     * @param sourcePoint3f    Source 3d point value
     * @param sourceType       Source sensor type
     * @param targetType       Target sensor type
     * @param targetPoint2f    Target 2d point value
     * @param calibrationParam 设备校准参数，参考pipeline::getCalibrationParam
     * @param sourcePoint3f    源三维点值
     * @param sourceType       源传感器类型
     * @param targetType       目标传感器类型
     * @param targetPoint2f    目标二维点值
     * @return bool Transform result
     * \else
     * 将一个源坐标系下的三维点坐标转换到目标相机的2d像素坐标。
     * @return bool 转换结果
     */
    public static boolean is3dTo2d(CalibrationParam calibrationParam,
                                   Point3f sourcePoint3f,
                                   SensorType sourceType, SensorType targetType,
                                   Point2f targetPoint2f) {
        byte[] calibrationParamBytes = calibrationParamToBytes(calibrationParam);
        return nIs3dTo2d(calibrationParamBytes,
                sourcePoint3f.getBytes(),
                sourceType.value(), targetType.value(),
                targetPoint2f.getBytes());
    }

    /**
     * \if English
     * Transform a 2d pixel coordinate with an associated depth value of the source camera into a 2d pixel coordinate of the target camera
     *
     * @param calibrationParam Device calibration param,see pipeline::getCalibrationParam
     * @param sourcePoint2f    Source 2d point value
     * @param sourceDepthPixel The depth of sourcePoint2f in millimeters
     * @param sourceType       Source sensor type
     * @param targetType       Target sensor type
     * @param targetPoint2f    Target 2d point value
     * @param calibrationParam 设备校准参数，参考pipeline::getCalibrationParam
     * @param sourcePoint2f    源二维点值
     * @param sourceDepthPixel 源点坐标对应的深度值，单位为毫米
     * @param sourceType       源传感器类型
     * @param targetType       目标传感器类型
     * @param targetPoint2f    目标二维点值
     * @return bool Transform result
     * \else
     * 将一个源相机的2d像素坐标和关联的深度值转换到目标相机的2d像素坐标。
     * @return bool 转换结果
     */
    public static boolean is2dTo2d(CalibrationParam calibrationParam,
                                   Point2f sourcePoint2f,
                                   float sourceDepthPixel,
                                   SensorType sourceType, SensorType targetType,
                                   Point2f targetPoint2f) {
        byte[] calibrationParamBytes = calibrationParamToBytes(calibrationParam);
        return nIs2dTo2d(calibrationParamBytes,
                sourcePoint2f.getBytes(),
                sourceDepthPixel,
                sourceType.value(), targetType.value(),
                targetPoint2f.getBytes());
    }

    /**
     * \if English
     * Transforms the depth frame into the geometry of the color camera.
     *
     * @param device     Device handle
     * @param depthFrame Input depth frame
     * @param width      Target color camera width
     * @param height     Target color camera height
     * @param device     设备句柄
     * @param depthFrame 输入深度帧
     * @param width      目标颜色相机宽度
     * @param height     目标颜色相机高度
     * @return Transformed depth frame
     * \else
     * 将深度帧转换到颜色相机的几何空间。
     * @return 转换后的深度帧
     */
    public static Frame depthFrameToColorCamera(Device device, DepthFrame depthFrame,
                                                int width, int height) {
        if (device == null || depthFrame == null) {
            return null;
        }
        long deviceHandle = device.getHandle();
        long depthFrameHandle = depthFrame.getHandle();
        long handle = nDepthFrameToColorCamera(deviceHandle, depthFrameHandle, width, height);
        return handle != 0 ? new Frame(handle) : null;
    }

    /**
     * \if English
     * Init transformation tables
     *
     * @param calibrationParam Device calibration param,see pipeline::getCalibrationParam
     * @param sensorType       sensor type
     * @param data             input data,needs to be allocated externally.During initialization, the external allocation size is 'data_size', for example, data_size = 1920
     *                         * 1080 * 2*sizeof(float) (1920 * 1080 represents the image resolution, and 2 represents two LUTs, one for x-coordinate and one for y-coordinate).
     * @param size             input data size
     * @param xyTables         output xy tables
     * @param calibrationParam 设备校准参数，参考pipeline::getCalibrationParam
     * @param sensorType       传感器类型
     * @param data             输入数据，需要外部分配，初始化时，外部分配的大小为'data_size'，例如，data_size = 1920 * 1080 * 2 * sizeof(float) (1920 * 1080表示图像分辨率，2表示两个LUT，一个用于x坐标，一个用于y坐标)
     * @param size             输入数据大小
     * @param xyTables         输出xy表
     * @return Transform result
     * \else
     * 初始化转换表
     * @return 转换结果
     */
    public static boolean initXYTables(CalibrationParam calibrationParam,
                                       SensorType sensorType,
                                       float[] data, long size,
                                       XYTables xyTables) {
        byte[] calibrationParamBytes = calibrationParamToBytes(calibrationParam);
        return nInitXYTables(calibrationParamBytes, sensorType.value(),
                data, size, xyTables.getHandle());
    }

    public static void depthToPointcloud(XYTables xyTables, byte[] depthImageData, byte[] pointCloudData) {
        nDepthToPointcloud(xyTables.getHandle(), depthImageData, pointCloudData);
    }

    public static void depthToRgbdPointcloud(XYTables xyTables, byte[] depthImageData, byte[] colorImageData, byte[] pointCloudData) {
        nDepthToRgbdPointcloud(xyTables.getHandle(), depthImageData, colorImageData, pointCloudData);
    }

    private static native boolean nIs3dTo3d(byte[] calibrationParamBytes, byte[] sourcePoint3fBytes, int sourceType, int targetType, byte[] targetPoint3fBytes);

    private static native boolean nIs2dTo3d(byte[] calibrationParamBytes, byte[] sourcePoint2fBytes, float sourceDepthPixel, int sourceType, int targetType, byte[] targetPoint3fBytes);

    private static native boolean nIs2dTo3dUndistortion(byte[] calibrationParamBytes, byte[] sourcePoint2fBytes, float sourceDepthPixel, int sourceType, int targetType, byte[] targetPoint3fBytes);

    private static native boolean nIs3dTo2d(byte[] calibrationParamBytes, byte[] sourcePoint3fBytes, int sourceType, int targetType, byte[] targetPoint2fBytes);

    private static native boolean nIs2dTo2d(byte[] calibrationParamBytes, byte[] sourcePoint2fBytes, float sourceDepthPixel, int sourceType, int targetType, byte[] targetPoint2fBytes);

    private static native long nDepthFrameToColorCamera(long deviceHandle, long depthFrameHandle, int width, int height);

    private static native boolean nInitXYTables(byte[] calibrationParamBytes, int sensorType, float[] data, long size, long handle);

    private static native void nDepthToPointcloud(long handle, byte[] depthImageData, byte[] pointCloudData);

    private static native void nDepthToRgbdPointcloud(long handle, byte[] depthImageData, byte[] colorImageData, byte[] pointCloudData);
}
