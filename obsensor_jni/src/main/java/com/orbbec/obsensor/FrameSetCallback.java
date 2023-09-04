package com.orbbec.obsensor;

/**
 * \if English
 * frameset callback interface
 * \else
 * 数据帧集回调接口
 * \endif
 */
public interface FrameSetCallback {
    /**
     * \if English
     * Callback with a FrameSet
     *
     * @param frameSet Data FrameSet
     * \else
     * 数据帧集回调
     *
     * @param frameSet 数据帧集
     * \endif
     */
    void onFrameSet(FrameSet frameSet);
}
