package com.orbbec.obsensor;

/**
 * \if English
  * Gyro stream profile
 * \else
 * 陀螺仪Profile
 * \endif
 */
public class GyroStreamProfile extends StreamProfile {
    GyroStreamProfile(long handle) {
        super(handle);
    }

    /**
	 * \if English
	 * Get the range of the gyroscope configuration
     *
     * @return Gyroscope range {@link GyroFullScaleRange}
	 * \else
     * 获取陀螺仪配置的量程范围
     *
     * @return 陀螺仪量程范围 {@link GyroFullScaleRange}
	 * \endif
     */
    public GyroFullScaleRange getGyroFullScaleRange() {
        throwInitializeException();
        int type = nGetGyroFullScaleRange(mHandle);
        return GyroFullScaleRange.get(type);
    }

    /**
	 * \if English
	 * Get the data sampling rate of the gyroscope configuration
     *
     * @return Gyroscope data sample rate {@link SampleRate}
	 * \else
     * 获取陀螺仪配置的数据采样率
     *
     * @return 陀螺仪数据采样率 {@link SampleRate}
	 * \endif
     */
    public SampleRate getGyroSampleRate() {
        throwInitializeException();
        int rateIndex = nGetGyroSampleRate(mHandle);
        return SampleRate.get(rateIndex);
    }

    private static native int nGetGyroFullScaleRange(long mHandle);

    private static native int nGetGyroSampleRate(long mHandle);

}
