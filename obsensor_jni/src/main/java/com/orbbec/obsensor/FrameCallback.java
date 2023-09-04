package com.orbbec.obsensor;

/**
 * \if English
 * data frame callback interface
 * \else
 * 数据帧回调接口
 * \endif
 */
public interface FrameCallback {
    /**
     * \if English
     * Frame callback
     *
     * @param frame Data frame
     * \else
     * 数据帧回调
     *
     * @param frame 数据帧
     * \endif
     */
    void onFrame(Frame frame);
}
