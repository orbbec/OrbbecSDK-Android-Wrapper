package com.orbbec.obsensor;

/**
 * \if English
 * Playback Status Callback
 * \else
 * 回放状态回调
 * \endif
 */
public interface MediaStateCallback {
    /**
	 * \if English
	 * playback status callback
     *
     * @param state playback status
	 * \else
     * 回放状态回调
     *
     * @param state 回放状态
	 * \endif
     */
    void onState(MediaState state);
}
