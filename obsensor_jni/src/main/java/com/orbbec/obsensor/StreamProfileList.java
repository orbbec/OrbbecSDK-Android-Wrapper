package com.orbbec.obsensor;

/**
 * \if English
 * stream profile list
 * \else
 * 流配置列表
 * \endif
 */
public class StreamProfileList extends LobClass {

    StreamProfileList(long handle) {
        mHandle = handle;
    }

    /**
	 * \if English
	 * get stream profile count
     *
     * @return stream profile count
	 * \else
     * 获取流配置数量
     *
     * @return 流配置数量
	 * \endif
     */
    public int getStreamProfileCount() {
        throwInitializeException();
        return nGetStreamProfileCount(mHandle);
    }

    /**
	 * \if English
	 * Get the configuration according to the specified index in the stream configuration list
     *
     * @param index index value
     * @return stream configuration {@link StreamProfile}
	 * \else
     * 根据流配置列表中指定索引，获取配置
     *
     * @param index 索引值
     * @return 流配置 {@link StreamProfile}
	 * \endif
     */
    public StreamProfile getStreamProfile(int index) {
        throwInitializeException();
        long handle = nGetStreamProfile(mHandle, index);
        return handle != 0 ? new StreamProfile(handle) : null;
    }

    /**
	 * \if English
	 * Match the corresponding StreamProfile through the passed parameters, if there are multiple matches, the first one in the list will be returned by default
     *
     * @param width  Width, if no matching width is required, pass 0
     * @param height Height, if no matching height is required, pass 0
     * @param format Type {@link Format} , if no matching type is required, pass {@link Format#UNKNOWN}
     * @param fps    Frame rate, pass 0 if there is no need to match the frame rate
     * @return Returns the matching stream configuration
	 * \else
     * 通过传入的参数进行匹配对应的StreamProfile，若有多个匹配项默认返回列表中的第一个，若未找到匹配的，则抛异常
     *
     * @param width  宽度，如无需匹配宽度，则传0
     * @param height 高度，如无需匹配高度，则传0
     * @param format 类型 {@link Format},如无需匹配类型，则传{@link Format#UNKNOWN}
     * @param fps    帧率,如无需匹配帧率，则传0
     * @return 返回匹配的流配置
	 * \endif
     */
    public VideoStreamProfile getVideoStreamProfile(int width, int height, Format format, int fps) {
        throwInitializeException();
        long handle = nGetVideoStreamProfile(mHandle, width, height, format.value(), fps);
        return handle != 0 ? new VideoStreamProfile(handle) : null;
    }

    /**
	 * \if English
	 * resources release
	 * \else
     * 资源释放
     * \endif
     */
    @Override
    public void close() {
        throwInitializeException();
        nDelete(mHandle);
        mHandle = 0;
    }

    private static native int nGetStreamProfileCount(long handle);

    private static native long nGetStreamProfile(long handle, int index);

    private static native long nGetVideoStreamProfile(long handle, int width, int height, int format, int fps);

    private static native void nDelete(long handle);
}
