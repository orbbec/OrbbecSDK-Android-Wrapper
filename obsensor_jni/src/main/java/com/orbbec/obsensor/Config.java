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

    private static native void nDisableStream(long handle, int streamType);

    private static native void nSetAlignMode(long handle, int mode);

    private static native void nSetDepthScaleRequire(long handle, boolean enable);

    private static native void nSetD2CTargetResolution(long handle, int width, int height);

    private static native void nDelete(long handle);
}
