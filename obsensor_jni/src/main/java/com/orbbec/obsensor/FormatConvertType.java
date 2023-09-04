package com.orbbec.obsensor;

/**
 * \if English
 * for data stream format conversion
 * \else
 * 格式转换类型枚举
 * \endif
 **/
public enum FormatConvertType {
    /**
	 * \if English
	 * YUYV to RGB888
	 * \else
     * YUYV转RGB888
     * \endif
     */
    FORMAT_YUYV_TO_RGB888(0),
    /**
	 * \if English
	 * I420 to RGB888
	 * \else
     * I420转RGB888
     * \endif
     */
    FORMAT_I420_TO_RGB888(1),
    /**
	 * \if English
	 * NV21 to RGB888
	 * \else
     * NV21转RGB888
     * \endif
     */
    FORMAT_NV21_TO_RGB888(2),
    /**
	 * \if English
	  * NV12 to RGB888
	 * \else
     * NV12转RGB888
     * \endif
     */
    FORMAT_NV12_TO_RGB888(3),
    /**
	 * \if English
	 * MJPG to I420
	 * \else
     * MJPG转I420
     * \endif
     */
    FORMAT_MJPEG_TO_I420(4),
    /**
	 * \if English
     * RGB888 to BGR
	 * \else
     * RGB888转BGR
     * \endif
     */
    FORMAT_RGB888_TO_BGR(5),
    /**
	 * \if English
	 * MJPG to NV21
	 * \else
     * MJPG转NV21
	 * \endif
     */
    FORMAT_MJPEG_TO_NV21(6),
    /**
	 * \if English
	 * MJPG to RG888
	 * \else
     * MJPG转RGB888
     * \endif
     */
    FORMAT_MJPEG_TO_RGB888(7);

    private final int mValue;

    FormatConvertType(int value) {
        mValue = value;
    }

    /**
	 * \if English
	 * Get the data stream conversion format corresponding to a specific index value
     *
     * @return index value
	 * \else
     * 获取特定索引值对应的数据流转换格式
     *
     * @return 索引值
	 * \endif
     */
    public int value() {
        return mValue;
    }

    /**
	 * \if English
	 * Get the data stream conversion format corresponding to the specified index
     *
     * @param value index value
     * @return data stream format
	 * \else
     * 获取指定索引对应的数据流转换格式
     *
     * @param value 索引值
     * @return 数据流格式
	 * \endif
     */
    public static FormatConvertType get(int value) {
        for (int i = 0; i < values().length; i++) {
            if (values()[i].value() == value) {
                return values()[i];
            }
        }
        return null;
    }
}
