package com.orbbec.obsensor;

/**
 * \if English
 * Depth Streaming DataFrame
 * \else
 * 深度流数据帧
 * \endif
 */
public class DepthFrame extends VideoFrame {
    DepthFrame(long handle) {
        super(handle);
    }

    /**
	 * \if English
	 * Get the value scale of the depth frame, the unit is mm/step,
     * such as valueScale=0.1, the pixel value of a certain coordinate is pixelValue=10000,
     * it means the depth value, value = pixelValue*valueScale = 10000*0.1=1000mm。
     *
     * @return A scale value of depth frame
	 * \else
     * 获取深度帧的值刻度，单位为 mm/step，
     * 如valueScale=0.1, 某坐标像素值为pixelValue=10000，
     * 则表示深度值value = pixelValue*valueScale = 10000*0.1=1000mm。
     *
     * @return 深度帧的值刻度
	 * \endif
     */
    public float getValueScale() {
        throwInitializeException();
        return nGetValueScale(mHandle);
    }

    private static native float nGetValueScale(long handle);
}
