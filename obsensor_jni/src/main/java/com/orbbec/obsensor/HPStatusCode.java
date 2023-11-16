package com.orbbec.obsensor;

/**
 * \if English
 * @brief send data or receive data return status type
 * \else
 * @brief 返回状态类型
 * \endif
 */
public enum HPStatusCode {
    /**
     * \if English
     * success
     * \else
     * 成功
     * \endif
     */
    HP_STATUS_OK(0),
    /**
     * \if English
     * No device found
     * \else
     * 没有发现设备
     * \endif
     */
    HP_STATUS_NO_DEVICE_FOUND(1),
    /**
     * \if English
     * Transfer failed
     * \else
     * 传输失败
     * \endif
     */
    HP_STATUS_CONTROL_TRANSFER_FAILED(2),
    /**
     * \if English
     * Unknown error
     * \else
     * 未知错误
     * \endif
     */
    HP_STATUS_UNKNOWN_ERROR(0xffff);

    private final int mValue;

    private HPStatusCode(int value) {
        this.mValue = value;
    }

    /**
     * \if English
     * Get the sensor type corresponding to the specified index
     *
     * @param value index value
     * @return sensor type
     * \else
     * 获取指定索引对应的传感器类型
     *
     * @param value 索引值
     * @return 传感器类型
     * \endif
     */
    public static HPStatusCode get(int value) {
        for (int i = 0; i < values().length; i++) {
            if (values()[i].value() == value) {
                return values()[i];
            }
        }
        return null;
    }

    /**
     * \if English
     * Get the int value of HPStatusCode
     * \else
     * 获取枚举对应的int值
     * \endif
     */
    public int value() {
        return mValue;
    }

    @Override
    public String toString() {
        switch (mValue) {
            case 0:
                return "HP_STATUS_OK";
            case 1:
                return "HP_STATUS_NO_DEVICE_FOUND";
            case 2:
                return "HP_STATUS_CONTROL_TRANSFER_FAILED";
            case 3:
                return "HP_STATUS_UNKNOWN_ERROR";
            default:
                return "HP_STATUS_" + mValue;
        }
    }
}
