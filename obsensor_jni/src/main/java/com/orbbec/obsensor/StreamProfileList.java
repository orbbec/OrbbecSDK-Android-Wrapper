package com.orbbec.obsensor;

import com.orbbec.obsensor.types.Format;
import com.orbbec.obsensor.types.AccelFullScaleRange;
import com.orbbec.obsensor.types.IMUSampleRate;
import com.orbbec.obsensor.types.GyroFullScaleRange;

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
    public int getCount() {
        throwInitializeException();
        return nGetCount(mHandle);
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
    public StreamProfile getProfile(int index) {
        throwInitializeException();
        long handle = nGetProfile(mHandle, index);
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
     * Match the corresponding accelerometer stream profile based on the passed-in parameters. If multiple Match are found, the first one in the list
     * is returned by default. Throws an exception if no matching profile is found.
     *
     * @param fullScaleRange The full scale range. Pass 0 if no matching condition is required.
     * @param sampleRate The sampling frequency. Pass 0 if no matching condition is required.
     * \else
     * 根据传入的参数匹配相应的加速计流配置文件。如果找到多个匹配项，默认情况下将返回列表中的第一个匹配项。如果找不到匹配的配置文件，则抛出异常。
     *
     * @param fullScaleRange 全量标称范围。如无需匹配条件，则传0。
     * @param sampleRate 采样频率。如无需匹配条件，则传0。
     */
    public AccelStreamProfile getAccelStreamProfile(AccelFullScaleRange fullScaleRange, IMUSampleRate sampleRate) {
        throwInitializeException();
        long handle = nGetAccelStreamProfile(mHandle, fullScaleRange.value(), sampleRate.value());
        return handle != 0 ? new AccelStreamProfile(handle) : null;
    }

    /**
     * \if English
     * Match the corresponding gyroscope stream profile based on the passed-in parameters. If multiple Match are found, the first one in the list
     * is returned by default. Throws an exception if no matching profile is found.
     *
     * @param fullScaleRange The full scale range. Pass 0 if no matching condition is required.
     * @param sampleRate The sampling frequency. Pass 0 if no matching condition is required.
     * \else
     * 根据传入的参数匹配相应的陀螺仪流配置文件。如果找到多个匹配项，默认情况下将返回列表中的第一个匹配项。如果找不到匹配的配置文件，则抛出异常。
     *
     * @param fullScaleRange 全量标称范围。如无需匹配条件，则传0。
     * @param sampleRate 采样频率。如无需匹配条件，则传0。
     */
    public GyroStreamProfile getGyroStreamProfile(GyroFullScaleRange fullScaleRange, IMUSampleRate sampleRate) {
        throwInitializeException();
        long handle = nGetGyroStreamProfile(mHandle, fullScaleRange.value(), sampleRate.value());
        return handle != 0 ? new GyroStreamProfile(handle) : null;
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

    private static native int nGetCount(long handle);

    private static native long nGetProfile(long handle, int index);

    private static native long nGetVideoStreamProfile(long handle, int width, int height, int format, int fps);

    private static native long nGetAccelStreamProfile(long handle, int fullScaleRange, int sampleRate);

    private static native long nGetGyroStreamProfile(long handle, int fullScaleRange, int sampleRate);

    private static native void nDelete(long handle);
}
