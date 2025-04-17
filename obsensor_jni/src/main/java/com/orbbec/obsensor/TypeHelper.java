package com.orbbec.obsensor;

import com.orbbec.obsensor.types.Format;
import com.orbbec.obsensor.types.FrameType;
import com.orbbec.obsensor.types.StreamType;
import com.orbbec.obsensor.types.SensorType;
import com.orbbec.obsensor.types.IMUSampleRate;
import com.orbbec.obsensor.types.GyroFullScaleRange;
import com.orbbec.obsensor.types.AccelFullScaleRange;
import com.orbbec.obsensor.types.FrameMetadataType;

public class TypeHelper {
    /**
     * \if English
     * Convert OBFormat to " char* " type and then return.
     * \else
     * 将OBFormat转换为" char* "类型，然后返回。
     * \endif
     */
    public static String convertOBFormatTypeToString(Format type) {
        return nConvertOBFormatTypeToString(type.value());
    }

    /**
     * \if English
     * Convert OBFrameType to " string " type and then return.
     * \else
     * 将OBFrameType转换为" string "类型，然后返回。
     * \endif
     */
    public static String convertOBFrameTypeToString(FrameType type) {
        return nConvertOBFrameTypeToString(type.value());
    }

    /**
     * \if English
     * Convert OBStreamType to " string " type and then return.
     * \else
     * 将OBStreamType转换为" string "类型，然后返回。
     * \endif
     */
    public static String convertOBStreamTypeToString(StreamType type) {
        return nConvertOBStreamTypeToString(type.value());
    }

    /**
     * \if English
     * Convert OBStreamType to " string " type and then return.
     * \else
     * 将OBStreamType转换为" string "类型，然后返回。
     * \endif
     */
    public static String convertOBSensorTypeToString(SensorType type) {
        return nConvertOBSensorTypeToString(type.value());
    }

    /**
     * \if English
     * Convert OBIMUSampleRate to " string " type and then return.
     * \else
     * 将OBIMUSampleRate转换为" string "类型，然后返回。
     * \endif
     */
    public static String convertOBIMUSampleRateTypeToString(IMUSampleRate type) {
        return nConvertOBIMUSampleRateTypeToString(type.value());
    }

    /**
     * \if English
     * Convert OBGyroFullScaleRange to " string " type and then return.
     * \else
     * 将OBGyroFullScaleRange转换为" string "类型，然后返回。
     * \endif
     */
    public static String convertOBGyroFullScaleRangeTypeToString(GyroFullScaleRange type) {
        return nConvertOBGyroFullScaleRangeTypeToString(type.value());
    }

    /**
     * \if English
     * Convert OBAccelFullScaleRange to " string " type and then return.
     * \else
     * 将OBAccelFullScaleRange转换为" string "类型，然后返回。
     * \endif
     */
    public static String convertOBAccelFullScaleRangeTypeToString(AccelFullScaleRange type) {
        return nConvertOBAccelFullScaleRangeTypeToString(type.value());
    }

    /**
     * \if English
     * Convert OBFrameMetadataType to " string " type and then return.
     * \else
     * 将OBFrameMetadataType转换为" string "类型，然后返回。
     * \endif
     */
    public static String convertOBFrameMetadataTypeToString(FrameMetadataType type) {
        return nConvertOBFrameMetadataTypeToString(type.value());
    }

    /**
     * \if English
     * Convert OBSensorType to OBStreamType type and then return.
     * \else
     * 将OBSensorType转换为OBStreamType类型，然后返回。
     * \endif
     */
    public static StreamType convertSensorTypeToStreamType(SensorType type) {
        return StreamType.get(nConvertSensorTypeToStreamType(type.value()));
    }

    /**
     * \if English
     * Check if the given sensor type is a video sensor.
     * Video sensors are sensors that produce video frames.
     * The following sensor types are considered video sensors:
     *      COLOR,
     *      DEPTH,
     *      IR,
     *      IR_LEFT,
     *      IR_RIGHT,
     * \else
     * 判断给定的传感器类型是否为视频传感器。
     * 视频传感器是产生视频帧的传感器。
     * 下列传感器类型被 consider 为视频传感器：
     *      COLOR,
     *      DEPTH,
     *      IR,
     *      IR_LEFT,
     *      IR_RIGHT,
     * \endif
     */
    public static boolean isVideoSensorType(SensorType type) {
        return nIsVideoSensorType(type.value());
    }

    /**
     * \if English
     * Check if the given stream type is a video stream.
     * Video streams are streams that contain video frames.
     * The following stream types are considered video streams:
     *      VIDEO,
     *      DEPTH,
     *      COLOR,
     *      IR，
     *      IR_LEFT,
     *      IR_RIGHT,
     * \else
     * 判断给定的流类型是否为视频流。
     * 视频流是包含视频帧的流。
     * 下列流类型被 consider 为视频流：
     *      VIDEO,
     *      DEPTH,
     *      COLOR,
     *      IR，
     *      IR_LEFT,
     *      IR_RIGHT,
     * \endif
     */
    private static boolean isVideoStreamType(StreamType type) {
        return nIsVideoStreamType(type.value());
    }

    private static native String nConvertOBFormatTypeToString(int type);

    private static native String nConvertOBFrameTypeToString(int type);

    private static native String nConvertOBStreamTypeToString(int type);

    private static native String nConvertOBSensorTypeToString(int type);

    private static native String nConvertOBIMUSampleRateTypeToString(int type);

    private static native String nConvertOBGyroFullScaleRangeTypeToString(int type);

    private static native String nConvertOBAccelFullScaleRangeTypeToString(int type);

    private static native String nConvertOBFrameMetadataTypeToString(int type);

    private static native int nConvertSensorTypeToStreamType(int type);

    private static native boolean nIsVideoSensorType(int type);

    private static native boolean nIsVideoStreamType(int type);
}
