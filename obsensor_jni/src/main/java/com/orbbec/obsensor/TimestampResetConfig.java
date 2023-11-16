package com.orbbec.obsensor;

/**
 * \if English
 * The timestamp reset configuration of the device.
 * \else
 * 设备的时间戳重置配置
 * \endif
 */
public class TimestampResetConfig {
    private boolean enable;
    private int timestampResetDelayUs;
    private boolean timestampResetSignalOutputEnable;

    /**
     * \if English
     * @brief Whether to enable the timestamp reset function.
     * @brief If the timestamp reset function is enabled, the timer for calculating the timestamp for output frames will be reset to 0 when the timestamp reset
     * command or timestamp reset signal is received, and one timestamp reset signal will be output via TIMER_SYNC_OUT pin on synchronization port by default.
     * The timestamp reset signal is input via TIMER_SYNC_IN pin on the synchronization port.
     *
     * @attention For some models, the timestamp reset function is always enabled and cannot be disabled.
     * \else
     * @brief 是否启用时间戳重置功能
     * @brief
     * 如果启用了时间戳重置功能，当收到时间戳重置命令或时间戳重置信号时，用于计算输出帧的时间戳的计时器将重置为0，并且默认情况下，通过同步端口上的TIMER_SYNC_OUT引脚输出一个时间戳重置信号
     * 时间戳重置信号通过同步端口上的TIMER_SYNC_IN引脚输入
     *
     * @attention 对于某些型号，时间戳重置功能始终处于启用状态，不能禁用
     * \endif
     */
    public boolean isEnable() {
        return enable;
    }

    /**
     * \if English
     * @brief Whether to enable the timestamp reset function.
     * @brief If the timestamp reset function is enabled, the timer for calculating the timestamp for output frames will be reset to 0 when the timestamp reset
     * command or timestamp reset signal is received, and one timestamp reset signal will be output via TIMER_SYNC_OUT pin on synchronization port by default.
     * The timestamp reset signal is input via TIMER_SYNC_IN pin on the synchronization port.
     *
     * @attention For some models, the timestamp reset function is always enabled and cannot be disabled.
     * \else
     * @brief 是否启用时间戳重置功能
     * @brief
     * 如果启用了时间戳重置功能，当收到时间戳重置命令或时间戳重置信号时，用于计算输出帧的时间戳的计时器将重置为0，并且默认情况下，通过同步端口上的TIMER_SYNC_OUT引脚输出一个时间戳重置信号
     * 时间戳重置信号通过同步端口上的TIMER_SYNC_IN引脚输入
     *
     * @attention 对于某些型号，时间戳重置功能始终处于启用状态，不能禁用
     * \endif
     */
    public void setEnable(boolean enable) {
        this.enable = enable;
    }

    /**
     * \if English
     * @brief The delay time of executing the timestamp reset function after receiving the command or signal in microseconds.
     * \else
     * @brief 在接收到命令或信号之后执行时间戳重置功能的延迟时间（以微秒为单位）
     * \endif
     */
    public int getTimestampResetDelayUs() {
        return timestampResetDelayUs;
    }

    /**
     * \if English
     * @brief The delay time of executing the timestamp reset function after receiving the command or signal in microseconds.
     * \else
     * @brief 在接收到命令或信号之后执行时间戳重置功能的延迟时间（以微秒为单位）
     * \endif
     */
    public void setTimestampResetDelayUs(int delayUs) {
        this.timestampResetDelayUs = delayUs;
    }

    /**
     * \if English
     * @brief the timestamp reset signal output enable flag.
     *
     * @attention For some models, the timestamp reset signal output is always enabled and cannot be disabled.
     * \else
     * @brief 时间戳重置信号输出使能标志
     *
     * @attention 对于某些型号，时间戳重置信号输出始终处于启用状态，不能禁用
     * \endif
     */
    public boolean isTimestampResetSignalOutputEnable() {
        return timestampResetSignalOutputEnable;
    }

    /**
     * \if English
     * @brief the timestamp reset signal output enable flag.
     *
     * @attention For some models, the timestamp reset signal output is always enabled and cannot be disabled.
     * \else
     * @brief 时间戳重置信号输出使能标志
     *
     * @attention 对于某些型号，时间戳重置信号输出始终处于启用状态，不能禁用
     * \endif
     */
    public void setTimestampResetSignalOutputEnable(boolean enable) {
        this.timestampResetSignalOutputEnable = enable;
    }
}
