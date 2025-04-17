package com.orbbec.obsensor.types;

/**
 * \if English
 * Enumeration value describing the file transfer status
 * \else
 * 文件传输状态枚举值
 * \endif
 */
public enum FileTranState {
    /**
     * \if English
     * File transfer
     * \else
     * 文件传输
     * \endif
     */
    FILE_TRAN_STAT_TRANSFER(2),
    /**
     * \if English
     * File transfer succeeded
     * \else
     * 文件传输成功
     * \endif
     */
    FILE_TRAN_STAT_DONE(1),
    /**
     * \if English
     * Preparing
     * \else
     * 准备中
     * \endif
     */
    FILE_TRAN_STAT_PREPAR(0),
    /**
     * \if English
     * DDR access failed
     * \else
     * DDR访问失败
     * \endif
     */
    FILE_TRAN_ERR_DDR(-1),
    /**
     * \if English
     * Insufficient target space error
     * \else
     * 目标空间不足
     * \endif
     */
    FILE_TRAN_ERR_NOT_ENOUGH_SPACE(-2),
    /**
     * \if English
     * Destination path is not writable
     * \else
     * 目标路径不可写
     * \endif
     */
    FILE_TRAN_ERR_PATH_NOT_WRITABLE(-3),
    /**
     * \if English
     * MD5 checksum error
     * \else
     * MD5校验失败
     * \endif
     */
    FILE_TRAN_ERR_MD5_ERROR(-4),
    /**
     * \if English
     * Write flash error
     * \else
     * 写入Flash失败
     * \endif
     */
    FILE_TRAN_ERR_WRITE_FLASH_ERROR(-5),
    /**
     * \if English
     * Timeout error
     * \else
     * 超时错误
     * \endif
     */
    FILE_TRAN_ERR_TIMEOUT(-6);

    private final int mValue;

    FileTranState(int value) {
        mValue = value;
    }

    public int value() {
        return mValue;
    }

    public static FileTranState get(int value) {
        for (int i = 0; i < values().length; i++) {
            if (values()[i].value() == value) {
                return values()[i];
            }
        }
        return null;
    }
}
