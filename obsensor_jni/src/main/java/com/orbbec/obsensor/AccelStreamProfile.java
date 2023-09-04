package com.orbbec.obsensor;

/**
 * \if English
 * Accelerometer Profile
 * \else
 * 加速度计Profile
 * \endif
 */
public class AccelStreamProfile extends StreamProfile {
    AccelStreamProfile(long handle) {
        super(handle);
    }

    /**
	 * \if English
	 * Get the range of the accelerometer configuration
     *
     * @return Accelerometer range {@link AccelFullScaleRange}
	 * \else
     * 获取加速度计配置的量程范围
     *
     * @return 加速度计量程范围 {@link AccelFullScaleRange}
	 * \endif
     */
    public AccelFullScaleRange getAccelFullScaleRange() {
        throwInitializeException();
        int type = nGetAccelFullScaleRange(mHandle);
        return AccelFullScaleRange.get(type);
    }

    /**
	 * \if English
	 * Get the sampling frequency of the accelerometer stream configuration
     *
     * @return Accelerometer sampling frequency {@link  SampleRate}
	 * \else
     * 获取加速度计流配置的采样频率
     *
     * @return 加速度计采样频率 {@link  SampleRate}
	 * \endif
     */
    public SampleRate getAccelSampleRate() {
        throwInitializeException();
        int rateIndex = nGetAccelSampleRate(mHandle);
        return SampleRate.get(rateIndex);
    }

    private static native int nGetAccelFullScaleRange(long mHandle);

    private static native int nGetAccelSampleRate(long mHandle);

}
