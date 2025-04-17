package com.orbbec.obsensor.types;

/**
 * \if English
 * Enumeration value describing the data transfer status
 * \else
 * 数据传输状态枚举值
 * \endif
 */
public enum DataTranState {
    /**
     * \if English
     * data verify done
     * \else
     * 数据验证完成
     * \endif
     */
    DATA_TRAN_STAT_VERIFY_DONE(4),
    /**
     * \if English
     * data transfer stopped
     * \else
     * 数据传输停止
     * \endif
     */
    DATA_TRAN_STAT_STOPPED(3),
    /**
     * \if English
     * data transfer completed
     * \else
     * 数据传输完成
     * \endif
     */
    DATA_TRAN_STAT_DONE(2),
    /**
     * \if English
     * data verifying
     * \else
     * 数据验证中
     * \endif
     */
    DATA_TRAN_STAT_VERIFYING(1),
    /**
     * \if English
     * data transferring
     * \else
     * 数据传输中
     * \endif
     */
    DATA_TRAN_STAT_TRANSFERRING(0),
    /**
     * \if English
     * Transmission is busy
     * \else
     * 传输繁忙
     * \endif
     */
    DATA_TRAN_ERR_BUSY(-1),
    /**
     * \if English
     * Not supported
     * \else
     * 不支持
     * \endif
     */
    DATA_TRAN_ERR_UNSUPPORTED(-2),
    /**
     * \if English
     * Transfer failed
     * \else
     * 传输失败
     * \endif
     */
    DATA_TRAN_ERR_TRAN_FAILED(-3),
    /**
     * \if English
     * Test failed
     * \else
     * 测试失败
     * \endif
     */
    DATA_TRAN_ERR_VERIFY_FAILED(-4),
    /**
     * \if English
     * Other errors
     * \else
     * 其他错误
     * \endif
     */
    DATA_TRAN_ERR_OTHER(-5);

    private final int mValue;

    DataTranState(int value) {
        mValue = value;
    }

    public int value() {
        return mValue;
    }

    public static DataTranState get(int value) {
        for (int i = 0; i < values().length; i++) {
            if (values()[i].value() == value) {
                return values()[i];
            }
        }
        return DATA_TRAN_ERR_OTHER;
    }
}
