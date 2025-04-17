package com.orbbec.obsensor;

import com.orbbec.obsensor.types.AccelFullScaleRange;
import com.orbbec.obsensor.types.AlignMode;
import com.orbbec.obsensor.types.Format;
import com.orbbec.obsensor.types.FrameAggregateOutputMode;
import com.orbbec.obsensor.types.GyroFullScaleRange;
import com.orbbec.obsensor.types.IMUSampleRate;
import com.orbbec.obsensor.types.SensorType;
import com.orbbec.obsensor.types.StreamType;

/**
 * \if English
 * Used to configure Pipeline data flow status
 * \else
 * 用于配置Pipeline数据流状态
 * \endif
 */
public class Config extends LobClass {

    /**
	 * \if English
	 * 	Create a new pipeline configuration to configure the resolution and frame rate of the data stream and data stream that needs to be opened through the Pipeline
	 * \else
     * 创建新的pipeline配置,用于配置需要通过Pipeline开启的数据流及数据流的分辨率及帧率
     * \endif
     */
    public Config() {
        mHandle = nCreate();
    }

    /**
	 * \if English
     * It is only used to obtain the pipeline configuration of the specified handle that has been created, and will not create a new pipeline configuration
     *
     * @param handle The handle to the pipeline configuration that has been created
	 * \else
     * 仅用于获取已创建的指定句柄的pipeline配置，不会创建创建新的pipeline配置
     *
     * @param handle 已创建过的pipeline配置的句柄
	 * \endif
     */
    Config(long handle) {
        mHandle = handle;
    }

    /**
     * \if English
     * Enable a stream with default profile
     * @param streamType The type of the stream to be enabled
     * \else
     * 开启一个默认配置的流
     * @param streamType 需要开启的数据流类型{@link StreamType}
     * \endif
     */
    public void enableStream(StreamType streamType) {
        throwInitializeException();
        nEnableStream(mHandle, streamType.value());
    }

    /**
	 * \if English
     * Enable a stream with a specific sensor type
     * Will convert sensor type to stream type automatically.
     *
	 * @param sensorType The type of the stream to be enabled
	 * \else
     * 开启一个指定传感器类型的流
     * 自动将传感器类型转换为流类型。
     *
     * @param sensorType 需要开启的数据流传感器类型
	 * \endif
     */
    public void enableStream(SensorType sensorType) {
        StreamType type = TypeHelper.convertSensorTypeToStreamType(sensorType);
        enableStream(type);
    }

    /**
	 * \if English
	 * Start the data stream of the specified configuration

     * @param streamProfile Data flow configuration{@link StreamProfile}
     * @see Pipeline#getStreamProfileList(SensorType)
	 * \else
     * 开启指定配置的数据流
     *
     * @param streamProfile 数据流配置{@link StreamProfile}
     * @see Pipeline#getStreamProfileList(SensorType)
	 * \endif
     */
    public void enableStream(StreamProfile streamProfile) {
        throwInitializeException();
        nEnableStreamWithProfile(mHandle, streamProfile.getHandle());
    }

    /**
     * \if English
     * Enable a video stream to be used in the pipeline.
     *
     * @param streamType The video stream type.
     * @param width      The video stream width.
     * @param height     The video stream height.
     * @param fps        The video stream frame rate.
     * @param format     The video stream format.
     * \else
     * 启用一个视频流以在pipeline中使用。
     *
     * @param streamType 视频流类型。
     * @param width      视频流宽度.
     * @param height     视频流高度.
     * @param fps        视频流帧率.
     * @param format     视频流格式.
     * \endif
     */
    public void enableVideoStream(StreamType streamType, int width, int height,
                                  int fps, Format format) {
        throwInitializeException();
        nEnableVideoStream(mHandle, streamType.value(), width, height, fps, format.value());
    }

    /**
     * \if English
     * Enable a video stream to be used in the pipeline.
     * Will convert sensor type to stream type automatically.
     *
     * @param sensorType The sensor type to be enabled.
     * @param width      The video stream width.
     * @param height     The video stream height.
     * @param fps        The video stream frame rate.
     * @param format     The video stream format.
     * \else
     * 启用一个视频流以在pipeline中使用。
     * 自动将传感器类型转换为流类型。
     *
     * @param sensorType 要启用的传感器类型
     * @param width      视频流宽度.
     * @param height     视频流高度.
     * @param fps        视频流帧率.
     * @param format     视频流格式.
     * \endif
     */
    public void enableVideoStream(SensorType sensorType, int width, int height,
                                  int fps, Format format) {
        StreamType type = TypeHelper.convertSensorTypeToStreamType(sensorType);
        enableVideoStream(type, width, height, fps, format);
    }

    /**
     * \if English
     * Enable an accelerometer stream to be used in the pipeline.
     *
     * @param fullScaleRange The full-scale range of the accelerometer.
     * @param sampleRate     The sample rate of the accelerometer.
     * \else
     * 启用一个加速度计流以在pipeline中使用。
     *
     * @param fullScaleRange 加速度计的全标程范围。
     * @param sampleRate     加速度计的采样率。
     * \endif
     */
    public void enableAccelStream(AccelFullScaleRange fullScaleRange, IMUSampleRate sampleRate) {
        throwInitializeException();
        nEnableAccelStream(mHandle, fullScaleRange.value(), sampleRate.value());
    }

    public void enableAccelStream() {
        enableAccelStream(AccelFullScaleRange.FS_UNKNOWN, IMUSampleRate.UNKNOWN);
    }

    /**
     * \if English
     * Enable a gyroscope stream to be used in the pipeline.
     *
     * @param fullScaleRange The full-scale range of the gyroscope.
     * @param sampleRate     The sample rate of the gyroscope.
     * \else
     * 启用一个陀螺仪流以在pipeline中使用。
     *
     * @param fullScaleRange 陀螺仪的全标程范围。
     * @param sampleRate     陀螺仪的采样率。
     * \endif
     */
    public void enableGyroStream(GyroFullScaleRange fullScaleRange, IMUSampleRate sampleRate) {
        throwInitializeException();
        nEnableGyroStream(mHandle, fullScaleRange.value(), sampleRate.value());
    }

    public void enableGyroStream() {
        enableGyroStream(GyroFullScaleRange.FS_UNKNOWN, IMUSampleRate.UNKNOWN);
    }

    /**
     * \if English
     * Enable all streams in the pipeline configuration
     * \else
     * 开启所有流
     * \endif
     */
    public void enableAllStream() {
        throwInitializeException();
        nEnableAllStream(mHandle);
    }

    /**
	 * \if English
	 * Close the data stream of the specified configuration, through this interface, you can configure which data stream needs to be closed
     *
     * @param streamType Data stream configuration{@link StreamType}
	 * \else
     * 关闭指定配置的数据流,通过该接口可配置需要关闭哪一路数据流
     *
     * @param streamType 数据流配置{@link StreamType}
	 * \endif
     */
    public void disableStream(StreamType streamType) {
        throwInitializeException();
        nDisableStream(mHandle, streamType.value());
    }

    /**
     * \if English
     * Disable a sensor stream to be used in the pipeline.
     * Will convert sensor type to stream type automatically.
     * \else
     * 禁用pipeline中使用的传感器流。
     * 自动将传感器类型转换为流类型。
     * \endif
     */
    public void disableStream(SensorType sensorType) {
        StreamType type = TypeHelper.convertSensorTypeToStreamType(sensorType);
        disableStream(type);
    }

    /**
     * \if English
     * Disable all streams to be used in the pipeline
     * \else
     * 禁用pipeline中使用的所有流
     * \endif
     */
    public void disableAllStream() {
        throwInitializeException();
        nDisableAllStream(mHandle);
    }

    /**
     * \if English
     * Get the Enabled Stream Profile List
     * \else
     * 获取已启用的流配置列表
     * \endif
     */
    public StreamProfileList getEnabledStreamProfileList() {
        throwInitializeException();
        long handle = nGetEnabledStreamProfileList(mHandle);
        return handle != 0 ? new StreamProfileList(handle) : null;
    }

    /**
	 * \if English
     * Set the alignment mode
     *
     * @param mode AlignMode{@link AlignMode}
	 * \else
     * 设置对齐模式
     *
     * @param mode 对齐模式{@link AlignMode}
	 * \endif
     */
    public void setAlignMode(AlignMode mode) {
        throwInitializeException();
        nSetAlignMode(mHandle, mode.value());
    }

    /**
     * \if English
     * Whether the depth needs to be scaled after setting D2C
     *
     * @param enable Whether scaling is required
     * \else
     * 设置D2C后是否需要缩放深度
     *
     * @param enable 是否需要缩放
     * \endif
     */
    public void setDepthScaleRequire(boolean enable) {
        throwInitializeException();
        nSetDepthScaleRequire(mHandle, enable);
    }

    /**
     * \if English
     * Set the frame aggregation output mode for the pipeline configuration
     * The processing strategy when the FrameSet generated by the frame aggregation function does not contain the frames of all opened streams (which
     * can be caused by different frame rates of each stream, or by the loss of frames of one stream): drop directly or output to the user.
     *
     * @param mode The frame aggregation output mode to be set (default mode is @ref OB_FRAME_AGGREGATE_OUTPUT_ANY_SITUATION)
     * \else
     * 为pipeline配置设置帧聚合输出模式
     * 帧聚合函数生成的FrameSet不包含所有已打开流的帧（可能由每个流的帧率不同引起，或者由一个流的帧丢失引起）：直接丢弃还是输出给用户。
     *
     * @param mode 要设置的帧聚合输出模式（默认模式为@ref OB_FRAME_AGGREGATE_OUTPUT_ANY_SITUATION）
     * */
    public void setFrameAggregateOutputMode(FrameAggregateOutputMode mode) {
        throwInitializeException();
        nSetFrameAggregateOutputMode(mHandle, mode.value());
    }

    /**
	 * \if English
	 * Release Config object resources
	 * \else
     * 释放Config对象资源
     * \endif
     */
    @Override
    public void close() {
        throwInitializeException();
        nDelete(mHandle);
        mHandle = 0;
    }

    private static native long nCreate();

    private static native void nEnableStream(long handle, int streamType);

    private static native void nEnableStreamWithProfile(long handle, long streamProfileHandle);

    private static native void nEnableVideoStream(long handle, int streamType, int width, int height, int fps, int format);

    private static native void nEnableAccelStream(long handle, int fullScaleRange, int sampleRate);

    private static native void nEnableGyroStream(long handle, int fullScaleRange, int sampleRate);

    private static native void nEnableAllStream(long handle);

    private static native void nDisableStream(long handle, int streamType);

    private static native void nDisableAllStream(long handle);

    private static native long nGetEnabledStreamProfileList(long handle);

    private static native void nSetAlignMode(long handle, int mode);

    private static native void nSetDepthScaleRequire(long handle, boolean enable);

    private static native void nSetFrameAggregateOutputMode(long handle, int mode);

    private static native void nDelete(long handle);
}
