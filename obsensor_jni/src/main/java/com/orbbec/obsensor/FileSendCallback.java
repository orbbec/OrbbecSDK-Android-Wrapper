package com.orbbec.obsensor;

/**
 * \if English
 * File transfer callback class
 * \else
 * 文件传输回调类
 * \endif
 */
public interface FileSendCallback {

    /**
	 * \if English
	 * file transfer
	 * \else
     * 文件传输中
     * \endif
     */
    short FILE_TRAN_STAT_TRANSFER = 2;
    /**
	 * \if English
	 * File transfer succeeded
	 * \else
     * 文件传输成功
     * \endif
     */
    short FILE_TRAN_STAT_DONE = 1;
    /**
	 * \if English
	 * preparing
	 * \else
     * 准备中
     * \endif
     */
    short FILE_TRAN_STAT_PREPARE = 0;
    /**
	 * \if English
	 * DDR access failed
	 * \else
     * DDR访问失败
     * \endif
     */
    short FILE_TRAN_ERR_DDR = -1;
    /**
	 * \if English
	 * Insufficient target space error
	 * \else
     * 目标空间不足错误
     * \endif
     */
    short FILE_TRAN_ERR_NOT_ENOUGH_SPACE = -2;
    /**
	 * \if English
	 * Destination path is not writable
	 * \else
     * 目标路径不可写
     * \endif
     */
    short FILE_TRAN_ERR_PATH_NOT_WRITABLE = -3;
    /**
	 * \if English
	 * MD5 checksum error
	 * \else
     * MD5校验错误
     * \endif
     */
    short FILE_TRAN_ERR_MD5_ERROR = -4;
    /**
	 * \if English
	 * write flash error
	 * \else
     * 写Flash错误
     * \endif
     */
    short FILE_TRAN_ERR_WRITE_FLASH_ERROR = -5;
    /**
	 * \if English
	 * timeout error
	 * \else
     * 超时错误
     * \endif
     */
    short FILE_TRAN_ERR_TIMEOUT = -6;

    /**
	 * \if English
	 * file transfer callback
     *
     * @param state   current state
     * @param msg     status information
     * @param percent current status progress, percentage 0~100
	 * \else
     * 文件传输回调
     *
     * @param state   当前状态
     * @param msg     状态信息
     * @param percent 当前状态进度，百分比0~100
	 * \endif
     */
    void onCallback(short state, short percent, String msg);
}
