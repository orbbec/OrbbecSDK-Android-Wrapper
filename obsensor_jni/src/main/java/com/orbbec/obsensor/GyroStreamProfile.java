package com.orbbec.obsensor;

import com.orbbec.obsensor.types.GyroIntrinsic;
import com.orbbec.obsensor.types.GyroFullScaleRange;
import com.orbbec.obsensor.types.IMUSampleRate;

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
    public GyroFullScaleRange getFullScaleRange() {
        throwInitializeException();
        int type = nGetFullScaleRange(mHandle);
        return GyroFullScaleRange.get(type);
    }

    /**
	 * \if English
	 * Get the data sampling rate of the gyroscope configuration
     *
     * @return Gyroscope data sample rate {@link IMUSampleRate}
	 * \else
     * 获取陀螺仪配置的数据采样率
     *
     * @return 陀螺仪数据采样率 {@link IMUSampleRate}
	 * \endif
     */
    public IMUSampleRate getSampleRate() {
        throwInitializeException();
        int rateIndex = nGetGyroSampleRate(mHandle);
        return IMUSampleRate.get(rateIndex);
    }

    /**
     * \if English
     * Get the intrinsic of the gyroscope stream.
     *
     * @return Return the intrinsic of the accelerometer stream. {@link  GyroIntrinsic}
     * \else
     * 获取陀螺仪内参
     *
     * @return 返回陀螺仪内参 {@link  GyroIntrinsic}
     * \endif
     */
    public GyroIntrinsic getIntrinsic() {
        throwInitializeException();
        GyroIntrinsic intrinsic = new GyroIntrinsic();
        nGetIntrinsic(mHandle, intrinsic.BYTES());
        boolean result = intrinsic.parseBytes();
        if (!result) {
            throw new OBException("getIntrinsic parse bytes error!");
        }
        return intrinsic;
    }

    private static native int nGetFullScaleRange(long mHandle);

    private static native int nGetGyroSampleRate(long mHandle);

    private static native void nGetIntrinsic(long handle, byte[] intrinsic);
}
