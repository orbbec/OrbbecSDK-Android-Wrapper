package com.orbbec.obsensor.types;

/**
 * \if English
 * log level, the higher the level, the stronger the log filter
 * \else
 * log等级, 等级越高Log过滤力度越大
 * \endif
 */
public enum LogSeverity {
    /**
     * \if English
     * debug
     * \else
     * 调试
     * \endif
     */
    DEBUG(0),
    /**
     * \if English
     * information
     * \else
     * 信息
     * \endif
     */
    INFO(1),
    /**
     * \if English
     * warning
     * \else
     * 警告
     * \endif
     */
    WARN(2),
    /**
     * \if English
     * error
     * \else
     * 错误
     * \endif
     */
    ERROR(3),
    /**
     * \if English
     * fatal error
     * \else
     * 致命错误
     * \endif
     */
    FATAL(4),
    /**
     * \if English
     * None (close LOG)
     * \else
     * 无(关闭LOG)
     * \endif
     */
    NONE(5);

    private final int mValue;

    LogSeverity(int value) {
        mValue = value;
    }

    /**
     * \if English
     * Get the index corresponding to the log output level
     *
     * @return index value
     * \else
     * 获取日志输出等级对应的索引
     * @return 索引值
     * \endif
     */
    public int value() {
        return mValue;
    }

    /**
     * \if English
     * Get the log output level corresponding to the specified index
     *
     * @param value index value
     * @param value 索引值
     * @return log output level
     * \else
     * 获取指定索引对应的日志输出等级
     * @return 日志输出等级
     * \endif
     */
    public static LogSeverity get(int value) {
        for (int i = 0; i < values().length; i++) {
            if (values()[i].value() == value) {
                return values()[i];
            }
        }
        return null;
    }
}
