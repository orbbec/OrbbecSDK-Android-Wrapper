package com.orbbec.obsensor;

/**
 * \if English
 * Filter callback
 * \else
 * filter回调
 * \endif
 */
public interface FilterCallback {
    /**
	 * \if English
	 * dataframe callback
     *
     * @param frame dataframe
	 * \else
     * 数据帧回调
     *
     * @param frame 数据帧
	 * \endif
     */
    void onFrame(Frame frame);
}
