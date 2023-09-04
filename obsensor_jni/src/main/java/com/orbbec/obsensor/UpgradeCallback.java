package com.orbbec.obsensor;

/**
 * \if English
 * Firmware upgrade callback class
 * \else
 * 固件升级回调类
 * \endif
 */
public interface UpgradeCallback {

    /**
	 * \if English
	 * Firmware file transfer status
	 * \else
     * 固件文件传输状态
     * \endif
     */
    short STAT_FILE_TRANSFER = 4;
    /**
     * \if English
	 * Firmware upgrade complete
	 * \else
     * 固件升级完成
     * \endif
     */
    short STAT_DONE = 3;
    /**
     * \if English
	 * Firmware upgrade in progress
	 * \else
     * 固件升级中
     * \endif
     */
    short STAT_IN_PROGRESS = 2;
    /**
	 * \if English
	 * Firmware upgrade starts
	 * \else
     * 固件升级开始
     * \endif
     */
    short STAT_START = 1;
    /**
	 * \if English
	 * Firmware file verification
	 * \else
     * 固件文件校验
     * \endif
     */
    short STAT_VERIFY_IMAGE = 0;
    /**
	 * \if English
	 * Firmware file verification failed
	 * \else
     * 固件文件校验失败
     * \endif
     */
    short ERR_VERIFY = -1;
    /**
	 * \if English
	 * program error
	 * \else
     * 程序错误
     * \endif
     */
    short ERR_PROGRAM = -2;
    /**
	 * \if English
	 * Erase failed
	 * \else
     * 擦除失败
     * \endif
     */
    short ERR_ERASE = -3;
    /**
	 * \if English
	 * flash type failed
	 * \else
     * flash类型失败
     * \endif
     */
    short ERR_FLASH_TYPE = -4;
    /**
	 * \if English
	 * Firmware file size failed
	 * \else
     * 固件文件大小失败
     * \endif
     */
    short ERR_IMG_SIZE = -5;
    /**
	 * \if English
	 * other errors
	 * \else
     * 其他错误
	 * \endif
     */
    short ERR_OTHER = -6;
    /**
	 * \if English
	 * DDR error
	 * \else
     * DDR错误
     * \endif
     */
    short ERR_DDR = -7;
    /**
	 * \if English
	 * Upgrade timed out
	 * \else
     * 升级超时
     * \endif
     */
    short ERR_TIMEOUT = -8;

    /**
	 * \if English
	 * Firmware upgrade callback
     *
     * @param state   current state
     * @param msg     status information
     * @param percent Current status progress, percentage 0~100
	 * \else
     * 固件升级回调
     *
     * @param state   当前状态
     * @param msg     状态信息
     * @param percent 当前状态进度，百分比0~100
	 * \endif
     */
    void onCallback(short state, short percent, String msg);
}
