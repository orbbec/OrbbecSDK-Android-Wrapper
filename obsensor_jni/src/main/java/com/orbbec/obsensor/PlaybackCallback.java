package com.orbbec.obsensor;

/**
 * \if English
 * Data frame playback callback interface
 * \else
 * 数据帧回放回调接口
 * \endif
 */
public interface PlaybackCallback {
    /**
	 * \if English
	 * Data frame playback callback
     *
     * @param frame Data Frame
	 * \else
     * 数据帧回放回调
     *
     * @param frame 数据帧
	 * \endif
     */
    void onPlayback(Frame frame);
}
