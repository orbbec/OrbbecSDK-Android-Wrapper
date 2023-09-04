package com.orbbec.obsensor;

/**
 * \if English
 * Data stream configuration for Color, Depth, IR stream
 * \else
 * 数据流配置,用于Color, Depth, IR 流
 * \endif
 */
public class VideoStreamProfile extends StreamProfile {

    VideoStreamProfile(long handle) {
        super(handle);
        mHandle = handle;
    }

    /**
	 * \if English
	 * Get data stream width
     *
     * @return data stream width
	 * \else
     * 获取数据流宽
     *
     * @return 数据流宽
	 * \endif
     */
    public int getWidth() {
        throwInitializeException();
        return nGetWidth(mHandle);
    }

    /**
     * \if English
	 * Get data flow height
     *
     * @return data flow height
	 * \else
     * 获取数据流高
     *
     * @return 数据流高
	 * \endif
     */
    public int getHeight() {
        throwInitializeException();
        return nGetHeight(mHandle);
    }

    /**
	 * \if English
	 * Get data stream frame rate per second
     *
     * @return streaming frame rate
	 * \else
     * 获取数据流每秒帧率
     *
     * @return 数据流帧率
	 * \endif
     */
    public int getFps() {
        throwInitializeException();
        return nGetFps(mHandle);
    }

    /**
	 * \if English
	 * resource release
	 * \else
     * 资源释放
	 * \endif
     */
    @Override
    public void close() {
        super.close();
    }

    private static native int nGetFps(long handle);

    private static native int nGetWidth(long handle);

    private static native int nGetHeight(long handle);
}
