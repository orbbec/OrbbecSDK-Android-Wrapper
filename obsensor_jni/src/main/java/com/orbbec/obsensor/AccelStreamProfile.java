package com.orbbec.obsensor;

import com.orbbec.obsensor.types.AccelIntrinsic;
import com.orbbec.obsensor.types.AccelFullScaleRange;
import com.orbbec.obsensor.types.IMUSampleRate;

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
    public AccelFullScaleRange getFullScaleRange() {
        throwInitializeException();
        int type = nGetFullScaleRange(mHandle);
        return AccelFullScaleRange.get(type);
    }

    /**
	 * \if English
	 * Get the sampling frequency of the accelerometer stream configuration
     *
     * @return Accelerometer sampling frequency {@link  IMUSampleRate}
	 * \else
     * 获取加速度计流配置的采样频率
     *
     * @return 加速度计采样频率 {@link  IMUSampleRate}
	 * \endif
     */
    public IMUSampleRate getSampleRate() {
        throwInitializeException();
        int rateIndex = nGetSampleRate(mHandle);
        return IMUSampleRate.get(rateIndex);
    }

    /**
     * \if English
     * Get the intrinsic of the accelerometer stream.
     *
     * @return Return the intrinsic of the accelerometer stream. {@link  AccelIntrinsic}
     * \else
     * 获取加速度计内参
     *
     * @return 返回加速度计内参 {@link  AccelIntrinsic}
     * \endif
     */
    public AccelIntrinsic getIntrinsic() {
        throwInitializeException();
        AccelIntrinsic intrinsic = new AccelIntrinsic();
        nGetIntrinsic(mHandle, intrinsic.BYTES());
        boolean result = intrinsic.parseBytes();
        if (!result) {
            throw new OBException("getIntrinsic parse bytes error!");
        }
        return intrinsic;
    }

    private static native int nGetFullScaleRange(long mHandle);

    private static native int nGetSampleRate(long mHandle);

    private static native void nGetIntrinsic(long handle, byte[] intrinsic);
}
