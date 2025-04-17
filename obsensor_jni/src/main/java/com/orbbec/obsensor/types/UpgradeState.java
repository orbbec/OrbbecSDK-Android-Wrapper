package com.orbbec.obsensor.types;

/**
 * \if English
 * Enumeration value describing the firmware upgrade status
 * \else
 * 升级状态枚举值
 * \endif
 */
public enum UpgradeState {
    /**
     * \if English
     * update completed, but some files were duplicated and ignored
     * \else
     * 更新完成，但有些文件重复被忽略
     * \endif
     */
    STAT_DONE_WITH_DUPLICATES(6),
    /**
     * \if English
     * Image file verifify success
     * \else
     * 升级完成
     * \endif
     */
    STAT_VERIFY_SUCCESS(5),
    /**
     * \if English
     * file transfer
     * \else
     * 文件传输
     * \endif
     */
    STAT_FILE_TRANSFER(4),
    /**
     * \if English
     * update completed
     * \else
     * 更新完成
     * \endif
     */
    STAT_DONE(3),
    /**
     * \if English
     * upgrade in process
     * \else
     * 升级中
     * \endif
     */
    STAT_IN_PROGRESS(2),
    /**
     * \if English
     * start the upgrade
     * \else
     * 开始升级
     * \endif
     */
    STAT_START(1),
    /**
     * \if English
     * Image file verification
     * \else
     * 升级失败
     * \endif
     */
    STAT_VERIFY_IMAGE(0),
    /**
     * \if English
     * Verification failed
     * \else
     * 升级失败
     * \endif
     */
    ERR_VERIFY(-1),
    /**
     * \if English
     * Program execution failed
     * \else
     * 程序执行失败
     * \endif
     */
    ERR_PROGRAM(-2),
    /**
     * \if English
     * Flash parameter failed
     * \else
     * Flash参数失败
     * \endif
     */
    ERR_ERASE(-3),
    /**
     * \if English
     * Flash type error
     * \else
     * Flash类型错误
     * \endif
     */
    ERR_FLASH_TYPE(-4),
    /**
     * \if English
     * Image file size error
     * \else
     * 图片文件大小错误
     * \endif
     */
    ERR_IMAGE_SIZE(-5),
    /**
     * \if English
     * Other errors
     * \else
     * 其他错误
     * \endif
     */
    ERR_OTHER(-6),
    /**
     * \if English
     * DDR access error
     * \else
     * DDR访问错误
     * \endif
     */
    ERR_DDR(-7),
    /**
     * \if English
     * Timeout error
     * \else
     * 超时错误
     * \endif
     */
    ERR_TIMEOUT(-8),
    /**
     * \if English
     * Mismatch firmware error
     * \else
     * 匹配固件错误
     * \endif
     */
    ERR_MISMATCH(-9),
    /**
     * \if English
     * Unsupported device error
     * \else
     * 不支持的设备错误
     * \endif
     */
    ERR_UNSUPPORT_DEV(-10),
    /**
     * \if English
     * invalid firmware/preset count
     * \else
     * 无效固件/预设数量
     * \endif
     */
    ERR_INVALID_COUNT(-11);

    private final int mValue;

    UpgradeState(int value) {
        mValue = value;
    }

    public int value() {
        return mValue;
    }

    public static UpgradeState get(int value) {
        for (int i = 0; i < values().length; i++) {
            if (values()[i].value() == value) {
                return values()[i];
            }
        }
        return ERR_OTHER;
    }
}
