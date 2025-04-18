package com.orbbec.obsensor;

import android.util.Log;

import com.orbbec.obsensor.types.CalibrationParam;
import com.orbbec.obsensor.types.Point2f;
import com.orbbec.obsensor.types.Point3f;
import com.orbbec.obsensor.types.SensorType;
import com.orbbec.obsensor.types.Extrinsic;
import com.orbbec.obsensor.types.CameraIntrinsic;
import com.orbbec.obsensor.types.CameraDistortion;

public class CoordinateTransformHelper {
    private static final String TAG = "CoordinateTransformHelper";
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
        return nIs3dTo3d(calibrationParam.getBytes(), sourcePoint3f.getBytes(),
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
        return nIs2dTo3d(calibrationParam.getBytes(), sourcePoint2f.getBytes(),
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
        return nIs2dTo3dUndistortion(calibrationParam.getBytes(), sourcePoint2f.getBytes(),
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
        return nIs3dTo2d(calibrationParam.getBytes(),
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
        return nIs2dTo2d(calibrationParam.getBytes(),
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
        return nInitXYTables(calibrationParam.getBytes(), sensorType.value(),
                data, size, xyTables.getHandle());
    }

    public static void depthToPointcloud(XYTables xyTables, byte[] depthImageData, byte[] pointCloudData) {
        nDepthToPointcloud(xyTables.getHandle(), depthImageData, pointCloudData);
    }

    public static void depthToRgbdPointcloud(XYTables xyTables, byte[] depthImageData, byte[] colorImageData, byte[] pointCloudData) {
        nDepthToRgbdPointcloud(xyTables.getHandle(), depthImageData, colorImageData, pointCloudData);
    }

    /**
     * \if English
     * Transform a 3d point of a source coordinate system into a 3d point of the target coordinate system.
     *
     * @param sourcePoint3f Source 3d point value
     * @param extrinsic Transformation matrix from source to target
     * @param targetPoint3f Target 3d point value
     *
     * @return boolean Transform result
     * \else
     * 将一个源坐标系下的3d点转换到目标坐标系下的3d点。
     *
     * @param sourcePoint3f 源3d点值
     * @param extrinsic 源坐标系到目标坐标系的转换矩阵
     * @param targetPoint3f 目标3d点值
     *
     * @return boolean 转换结果
     * \endif
     */
    public static boolean transformation3dto3d(Point3f sourcePoint3f, Extrinsic extrinsic, Point3f targetPoint3f) {
        if (!sourcePoint3f.wrapBytes()) {
            Log.w(TAG, "transformation3dto3d: sourcePoint3f wrap bytes error!");
            return false;
        }

        if (!nTransformation3dto3d(sourcePoint3f.getBytes(), extrinsic.getBytes(), targetPoint3f.getBytes())) {
            Log.w(TAG, "transformation3dto3d: Transformation failed!");
            return false;
        }

        if (targetPoint3f.parseBytes()) {
            Log.w(TAG, "transformation3dto3d: targetPoint3f parse bytes error!");
            return false;
        }

        return true;
    }

    /**
     * \if English
     * Transform a 2d pixel coordinate with an associated depth value of the source camera into a 3d point of the target coordinate system.
     *
     * @param sourcePoint2f Source 2d point value
     * @param sourceDepthPixel The depth of sourcePoint2f in millimeters
     * @param sourceIntrinsic Source intrinsic parameters
     * @param extrinsic Transformation matrix from source to target
     * @param targetPoint3f Target 3d point value
     *
     * @return boolean Transform result
     * \else
     * 将源相机的2d像素坐标与关联的深度值转换到目标坐标系下的3d点。
     *
     * @param sourcePoint2f 源2d点值
     * @param sourceDepthPixel 源Point2f的深度值，单位为毫米
     * @param sourceIntrinsic 源内参
     * @param extrinsic 源坐标系到目标坐标系的转换矩阵
     * @param targetPoint3f 目标3d点值
     *
     * @return boolean 转换结果
     * \endif
     */
    public static boolean transformation2dto3d(Point2f sourcePoint2f, float sourceDepthPixel, CameraIntrinsic sourceIntrinsic,
                                               Extrinsic extrinsic, Point3f targetPoint3f) {
        if (!sourcePoint2f.wrapBytes()) {
            Log.w(TAG, "transformation2dto3d: sourcePoint2f wrap bytes error!");
            return false;
        }

        if (!nTransformation2dto3d(sourcePoint2f.getBytes(), sourceDepthPixel, sourceIntrinsic.getBytes(), extrinsic.getBytes(), targetPoint3f.getBytes())) {
            Log.w(TAG, "transformation2dto3d: Transformation failed!");
            return false;
        }

        if (targetPoint3f.parseBytes()) {
            Log.w(TAG, "transformation2dto3d: targetPoint3f parse bytes error!");
            return false;
        }

        return true;
    }

    /**
     * \if English
     * Transform a 3d point of a source coordinate system into a 2d pixel coordinate of the target camera.
     *
     * @param sourcePoint3f Source 3d point value
     * @param targetIntrinsic Target intrinsic parameters
     * @param targetDistortion Target distortion parameters
     * @param extrinsic Transformation matrix from source to target
     * @param targetPoint2f Target 2d point value
     *
     * @return boolean Transform result
     * \else
     * 将一个源坐标系下的3d点转换到目标相机的2d像素坐标。
     *
     * @param sourcePoint3f 源3d点值
     * @param targetIntrinsic 目标内参
     * @param targetDistortion 目标畸变参数
     * @param extrinsic 源坐标系到目标坐标系的转换矩阵
     * @param targetPoint2f 目标2d点值
     *
     * @return boolean 转换结果
     * \endif
     */
    public static boolean transformation3dto2d(Point3f sourcePoint3f, CameraIntrinsic targetIntrinsic, CameraDistortion targetDistortion,
                                               Extrinsic extrinsic, Point2f targetPoint2f) {
        if (!sourcePoint3f.wrapBytes()) {
            Log.w(TAG, "transformation3dto2d: sourcePoint3f wrap bytes error!");
            return false;
        }

        if (!nTransformation3dto2d(sourcePoint3f.getBytes(), targetIntrinsic.getBytes(), targetDistortion.getBytes(), extrinsic.getBytes(), targetPoint2f.getBytes())) {
            Log.w(TAG, "transformation3dto2d: Transformation failed!");
            return false;
        }

        if (targetPoint2f.parseBytes()) {
            Log.w(TAG, "transformation3dto2d: targetPoint2f parse bytes error!");
            return false;
        }

        return true;
    }

    /**
     * \if English
     * Transform a 2d pixel coordinate with an associated depth value of the source camera into a 2d pixel coordinate of the target camera
     *
     * @param sourcePoint2f Source 2d point value
     * @param sourceDepthPixel The depth of sourcePoint2f in millimeters
     * @param sourceIntrinsic Source intrinsic parameters
     * @param sourceDistortion Source distortion parameters
     * @param targetIntrinsic Target intrinsic parameters
     * @param targetDistortion Target distortion parameters
     * @param extrinsic Transformation matrix from source to target
     * @param targetPoint2f Target 2d point value
     *
     * @return boolean Transform result
     * \else
     * 将源相机的2d像素坐标与关联的深度值转换到目标相机的2d像素坐标。
     *
     * @param sourcePoint2f 源2d点值
     * @param sourceDepthPixel 源Point2f的深度值，单位为毫米
     * @param sourceIntrinsic 源内参
     * @param sourceDistortion 源畸变参数
     * @param targetIntrinsic 目标内参
     * @param targetDistortion 目标畸变参数
     * @param extrinsic 源坐标系到目标坐标系的转换矩阵
     * @param targetPoint2f 目标2d点值
     *
     * @return boolean 转换结果
     * \endif
     */
    public static boolean transformation2dto2d(Point2f sourcePoint2f, float sourceDepthPixel,
                                               CameraIntrinsic sourceIntrinsic, CameraDistortion sourceDistortion,
                                               CameraIntrinsic targetIntrinsic, CameraDistortion targetDistortion,
                                               Extrinsic extrinsic, Point2f targetPoint2f) {
        if (!sourcePoint2f.wrapBytes()) {
            Log.w(TAG, "transformation2dto2d: sourcePoint2f wrap bytes error!");
            return false;
        }

        if (!nTransformation2dto2d(sourcePoint2f.getBytes(), sourceDepthPixel, sourceIntrinsic.getBytes(), sourceDistortion.getBytes(),
                                   targetIntrinsic.getBytes(), targetDistortion.getBytes(), extrinsic.getBytes(), targetPoint2f.getBytes())) {
            Log.w(TAG, "transformation2dto2d: Transformation failed!");
            return false;
        }

        if (targetPoint2f.parseBytes()) {
            Log.w(TAG, "transformation2dto2d: targetPoint2f parse bytes error!");
            return false;
        }

        return true;
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

    private static native boolean nTransformation3dto3d(byte[] sourcePoint3fBytes, byte[] extrinsicBytes, byte[] targetPoint3fBytes);

    private static native boolean nTransformation2dto3d(byte[] sourcePoint2fBytes, float sourceDepthPixel, byte[] sourceIntrinsicBytes, byte[] extrinsicBytes, byte[] targetPoint3fBytes);

    private static native boolean nTransformation3dto2d(byte[] sourcePoint3fBytes, byte[] targetIntrinsicBytes, byte[] targetDistortionBytes, byte[] extrinsicBytes, byte[] targetPoint2fBytes);

    private static native boolean nTransformation2dto2d(byte[] sourcePoint2fBytes, float sourceDepthPixel, byte[] sourceIntrinsicBytes, byte[] sourceDistortionBytes, byte[] targetIntrinsicBytes, byte[] targetDistortionBytes, byte[] extrinsicBytes, byte[] targetPoint2fBytes);
}
