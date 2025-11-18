package com.orbbec.obsensor;

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
        nEnableStream(mHandle, streamProfile.getHandle());
    }

    /**
     * \if English
     * @brief Enable all streams to be used in the pipeline.
     * \else
     * @brief 开启所有流以用于pipeline。
     * \endif
     */
    public void enableAllStream() {
        throwInitializeException();
        nEnableAllStream(mHandle);
    }

    /**
     * \if English
     * @brief Enable a video stream to be used in the configuration.
     *
     * @param streamType The video stream type.
     * @param width      The video stream width.
     * @param height     The video stream height.
     * @param fps        The video stream frame rate.
     * @param format     The video stream format.
     * \else
     * @brief 启用一个视频流以用于配置。
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
     * @brief Enable an accelerometer stream to be used in the configuration.
     *
     * @param fullScaleRange The full-scale range of the accelerometer.
     * @param sampleRate     The sample rate of the accelerometer.
     * \else
     * @brief 启用一个加速度计流以用于配置。
     *
     * @param fullScaleRange 加速度计的全标程范围。
     * @param sampleRate     加速度计的采样率。
     * \endif
     */
    public void enableAccelStream(AccelFullScaleRange fullScaleRange, SampleRate sampleRate) {
        throwInitializeException();
        nEnableAccelStream(mHandle, fullScaleRange.value(), sampleRate.value());
    }

    /**
     * \if English
     * @brief Enable a gyroscope stream to be used in the configuration.
     *
     * @param fullScaleRange The full-scale range of the gyroscope.
     * @param sampleRate     The sample rate of the gyroscope.
     * \else
     * @brief 启用一个陀螺仪流以用于配置。
     *
     * @param fullScaleRange 陀螺仪的全标程范围。
     * @param sampleRate     陀螺仪的采样率。
     * \endif
     */
    public void enableGyroStream(GyroFullScaleRange fullScaleRange, SampleRate sampleRate) {
        throwInitializeException();
        nEnableGyroStream(mHandle, fullScaleRange.value(), sampleRate.value());
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
     * @brief Get the enabled stream profile list in the pipeline configuration.
     * \else
     * @brief 获取pipeline配置中启用的流配置列表。
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
     * Set the D2C target resolution, which is applicable to cases where the Color stream is not enabled using the OrbbecSDK and the depth needs to be D2C
     * Note: When you use OrbbecSDK to enable the Color stream, you also use this interface to set the D2C target resolution. The configuration of the enabled Color stream is preferred for D2C。
     *
     * @param width The D2C target has a wide resolution
     * @param height The D2C targets has a high resolution
     * \else
     * 设置D2C目标分辨率，适用于未使用OrbbecSDK开启Color流，且需要对深度进行D2C的情况
     * 注意:当使用OrbbecSDK开启Color流时，同时使用了此接口设置了D2C目标分辨率时。优先使用开启的Color流的配置进行D2C。
     *
     * @param width D2C目标分辨率宽
     * @param height D2C目标分辨率高
     * \endif
     */
    public void setD2CTargetResolution(int width, int height) {
        throwInitializeException();
        nSetD2CTargetResolution(mHandle, width, height);
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

    private static native void nEnableStream(long handle, long streamProfileHandle);

    private static native void nEnableAllStream(long handle);

    private static native void nEnableVideoStream(long handle, int streamType, int width, int height,
                                                   int fps, int format);

    private static native void nEnableAccelStream(long handle, int fullScaleRange, int sampleRate);

    private static native void nEnableGyroStream(long handle, int fullScaleRange, int sampleRate);

    private static native void nDisableStream(long handle, int streamType);

    private static native long nGetEnabledStreamProfileList(long handle);

    private static native void nSetAlignMode(long handle, int mode);

    private static native void nSetDepthScaleRequire(long handle, boolean enable);

    private static native void nSetD2CTargetResolution(long handle, int width, int height);

    private static native void nDelete(long handle);
}
