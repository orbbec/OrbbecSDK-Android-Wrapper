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
	 * YUYV to RGB
	 * \else
     * YUYV转RGB
     * \endif
     */
    FORMAT_YUYV_TO_RGB(0),
    /**
	 * \if English
	 * I420 to RGB
	 * \else
     * I420转RGB
     * \endif
     */
    FORMAT_I420_TO_RGB(1),
    /**
	 * \if English
	 * NV21 to RGB
	 * \else
     * NV21转RGB
     * \endif
     */
    FORMAT_NV21_TO_RGB(2),
    /**
	 * \if English
	  * NV12 to RGB
	 * \else
     * NV12转RGB
     * \endif
     */
    FORMAT_NV12_TO_RGB(3),
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
     * RGB to BGR
	 * \else
     * RGB转BGR
     * \endif
     */
    FORMAT_RGB_TO_BGR(5),
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
	 * MJPG to RGB
	 * \else
     * MJPG转RGB
     * \endif
     */
    FORMAT_MJPEG_TO_RGB(7),
    /**
	 * \if English
	 * MJPG to BGR
	 * \else
     * MJPG转BGR
     * \endif
     */
    FORMAT_MJPG_TO_BGR(8),
    /**
     * \if English
     * MJPG to BGRA
     * \else
     * MJPG转BGRA
     * \endif
     */
    FORMAT_MJPG_TO_BGRA(9),
    /**
     * \if English
     * UYVY to RGB
     * \else
     * UYVY转RGB
     * \endif
     */
    FORMAT_UYVY_TO_RGB(10),
    /**
     * \if English
     * BGR to RGB
     * \else
     * BGR转RGB
     * \endif
     */
    FORMAT_BGR_TO_RGB(11),
    /**
     * \if English
     * MJPG to NV12
     * \else
     * MJPG转NV12
     * \endif
     */
    FORMAT_MJPG_TO_NV12(12),
    /**
     * \if English
     * MJPG to BGR
     * \else
     * MJPG转BGR
     * \endif
     */
    FORMAT_YUYV_TO_BGR(13),
    /**
     * \if English
     * YUYV to RGBA
     * \else
     * YUYV转RGBA
     * \endif
     */
    FORMAT_YUYV_TO_RGBA(14),
    /**
     * \if English
     * YUYV to BGRA
     * \else
     * YUYV转BGRA
     * \endif
     */
    FORMAT_YUYV_TO_BGRA(15),
    /**
     * \if English
     * YUYV to Y16
     * \else
     * YUYV转Y16
     * \endif
     */
    FORMAT_YUYV_TO_Y16(16),
    /**
     * \if English
     * YUYV to Y8
     * \else
     * YUYV转Y8
     * \endif
     */
    FORMAT_YUYV_TO_Y8(17),
    ;

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
