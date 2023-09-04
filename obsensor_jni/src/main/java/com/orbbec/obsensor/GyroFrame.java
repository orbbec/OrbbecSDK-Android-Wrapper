package com.orbbec.obsensor;

/**
 * \if English
 * Gyroscope data frame
 * \else
 * 陀螺仪数据帧
 * \endif
 */
public class GyroFrame extends Frame {
    GyroFrame(long handle) {
        super(handle);
    }

    /**
	 * \if English
	 * Get gyroscope data
     *
     * @return float[3]{x,y,z} returns acceleration data
	 * \else
     * 获取陀螺仪数据
     *
     * @return float[3]{x,y,z} 返回加速度数据
	 * \endif
     */
    public float[] getGyroData() {
        throwInitializeException();
        float[] data = new float[3];
        return nGetGyroData(mHandle, data);
    }

    /**
	 * \if English
	 * Get the temperature when the frame is sampled
     *
     * @return temperature at frame sampling
	 * \else
     * 获取帧采样时的温度
     *
     * @return 帧采样时的温度
	 * \endif
     */
    public float getTemperature() {
        throwInitializeException();
        return nGetGyroTemperature(mHandle);
    }

    private static native float[] nGetGyroData(long mHandle, float[] data);

    private static native float nGetGyroTemperature(long mHandle);

}
