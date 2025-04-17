package com.orbbec.obsensor;

/**
 * \if English
 * Acceleration streaming data frame
 * \else
 * 加速度流数据帧
 * \endif
 */
public class AccelFrame extends Frame {
    AccelFrame(long handle) {
        super(handle);
    }

    /**
	 * \if English
	 * Get acceleration frame data
     *
     * @return float[3]{x,y,z} returns acceleration frame data
	 * \else
     * 获取加速度帧数据
     *
     * @return float[3]{x,y,z} 返回加速度帧数据
	 * \endif
     */
    public float[] getAccelData() {
        throwInitializeException();
        float[] data = new float[3];
        return nGetAccelData(mHandle, data);
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
        return nGetAccelTemperature(mHandle);
    }

    private static native float[] nGetAccelData(long mHandle, float[] data);

    private static native float nGetAccelTemperature(long mHandle);

}
