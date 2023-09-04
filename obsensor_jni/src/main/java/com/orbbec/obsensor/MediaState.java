package com.orbbec.obsensor;

/**
 * \if English
 * Record playback status 
 * \else
 * 录制回放状态
 * \endif
 */
public enum MediaState {
    /**
	 * \if English
	 * start
	 * \else
     * 开始
     * \endif
     */
    OB_MEDIA_BEGIN(0),

    /**
	 * \if English
	 * pause
	 * \else
     * 暂停
     * \endif
     */
    OB_MEDIA_PAUSE(1),

    /**
	 * \if English
	 * recover
	 * \else
     * 恢复
     * \endif
     */
    OB_MEDIA_RESUME(2),

    /**
	 * \if English
	 * end
	 * \else
     * 结束
     * \endif
     */
    OB_MEDIA_END(3);

    private final int mValue;

    MediaState(int value) {
        mValue = value;
    }

    /**
	 * \if English
	 * Get the index corresponding to the recording and playback status
     *
     * @return index value
	 * \else
     * 获取录制回放状态对应的索引
     *
     * @return 索引值
	 * \endif
     */
    public int value() {
        return mValue;
    }

    /**
	 * \if English
	 * Get the recording and playback status corresponding to the specified index
     *
     * @param value index value
     * @return Record playback status
	 * \else
     * 获取指定索引对应的录制回放状态
     *
     * @param value 索引值
     * @return 录制回放状态
	 * \endif
     */
    public static MediaState get(int value) {
        for (int i = 0; i < values().length; i++) {
            if (values()[i].value() == value) {
                return values()[i];
            }
        }
        return null;
    }
}
