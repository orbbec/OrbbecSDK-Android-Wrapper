package com.orbbec.obsensor;

/**
 * \if English
 * Enumeration value describing pixel format
 * \else
 * 描述像素格式的枚举值
 * \endif
 */
public enum Format {
    /**
	 * \if English
	 * YUYV format
	 * \else
     * YUYV格式
     * \endif
     */
    YUYV(0),
    /**
	 * \if English
	 * YUY2 format (the actual format is the same as YUYV)
	 * \else
     * YUY2格式（实际格式与YUYV相同）
     * \endif
     */
    YUY2(1),
    /**
	 * \if English
	 * UYVY format
	 * \else
     * UYVY格式
     * \endif
     */
    UYVY(2),
    /**
	 * \if English
	 * NV12 format
	 * \else
     * NV12格式
     * \endif
     */
    NV12(3),
    /**
	 * \if English
	 * NV21 format
	 * \else
     * NV21格式
     * \endif
     */
    NV21(4),
    /**
	 * \if English
	 * MJPG encoding format
	 * \else
     * MJPG编码格式
     * \endif
     */
    MJPG(5),
    /**
	 * \if English
	 * H.264 encoding format
	 * \else
     * H.264编码格式
     * \endif
     */
    H264(6),
    /**
	 * \if English
	 * H.265 encoding format
	 * \else
     * H.265编码格式
     * \endif
     */
    H265(7),
    /**
	 * \if English
	 * Y16 format, single channel 16bit depth
	 * \else
     * Y16格式，单通道16bit深度
     * \endif
     */
    Y16(8),
    /**
	 * \if English
	 * Y8 format, single channel 8bit depth
	 * \else
     * Y8格式，单通道8bit深度
     * \endif
     */
    Y8(9),
    /**
	 * \if English
	 * Y10 format, single channel 10bit depth (SDK will unpack into Y16 by default)
	 * \else
     * Y10格式，单通道10bit深度（SDK默认会解包成Y16）
     * \endif
     */
    Y10(10),
    /**
	 * \if English
	 * Y11 format, single channel 11bit depth (SDK will unpack into Y16 by default)
	 * \else
     * Y11格式，单通道11bit深度（SDK默认会解包成Y16）
     * \endif
     */
    Y11(11),
    /**
	 * \if English
	 * Y12 format, single channel 12bit depth (SDK will unpack into Y16 by default)
	 * \else
     * Y12格式，单通道12bit深度（SDK默认会解包成Y16）
     * \endif
     */
    Y12(12),
    /**
	 * \if English
	 * GRAY (the actual format is the same as YUYV)
	 * \else
     * GRAY灰度（实际格式与YUYV相同）
     * \endif
     */
    GRAY(13),
    /**
	 * \if English
	 * HEVC encoding format (the actual format is the same as H265)
	 * \else
     * HEVC编码格式（实际格式与H265相同）
     * \endif
     */
    HEVC(14),
    /**
	 * \if English
	 * I420 format
	 * \else
     * I420格式
     * \endif
     */
    I420(15),
    /**
	 * \if English
	 * Accelerometer data format
	 * \else
     * 加速度数据格式
     * \endif
     */
    ACCEL(16),
    /**
	 * \if English
	 * Gyroscope data format
	 * \else
     * 陀螺仪数据格式
     * \endif
     */
    GYRO(17),
    /**
	 * \if English
	 * xyz 3D coordinate point format
	 * \else
     * 纯x-y-z三维坐标点格式
     * \endif
     */
    POINT(19),
    /**
	 * \if English
	 * xyz 3D coordinate point format with RGB information
	 * \else
     * 带RGB信息的x-y-z三维坐标点格式
     * \endif
     */
    RGB_POINT(20),
    /**
	 * \if English
	 * RLE pressure test format (SDK will be unpacked into Y16 by default)
	 * \else
     * RLE压测格式（SDK默认会解包成Y16）
     * \endif
     */
    RLE(21),
    /**
	 * \if English
	 * RGB888 format
	 * \else
     * RGB888格式
     * \endif
     */
    RGB888(22),
    /**
	 * \if English
	 * BGR format (actual BRG888)
	 * \else
     * BGR格式（实际BRG888）
     * \endif
     */
    BGR(23),
	/**
	 * \if English
	 * Y14 format, single channel 14bit depth (SDK will unpack into Y16 by default)
	 * \else
	 * Y14格式，单通道14bit深度(SDK默认会解包成Y16)
	 * \endif
	 * */
	Y14(24),
	/**
	 * \if English
	 * BGRA format
	 * \else
	 * BGRA格式
	 * \endif
	 * */
	BGRA(25),
    /**
	 * \if English
	 * unknown format
	 * \else
     * 未知格式
     * \endif
     */
    UNKNOWN(0xff);

    private final int mValue;

    Format(int value) {
        mValue = value;
    }

    /**
	 * \if English
	 * Get the index corresponding to the data stream format
     *
     * @return index value
	 * \else
     * 获取数据流格式对应的索引
     *
     * @return 索引值
	 * \endif
     */
    public int value() {
        return mValue;
    }

    /**
	 * \if English
	 * Get the data stream format corresponding to the specified index
     *
     * @param value index value
     * @return data stream format
	 * \else
     * 获取指定索引对应的数据流格式
     *
     * @param value 索引值
     * @return 数据流格式
	 * \endif
     */
    public static Format get(int value) {
        for (int i = 0; i < values().length; i++) {
            if (values()[i].value() == value) {
                return values()[i];
            }
        }
        return UNKNOWN;
    }
}
