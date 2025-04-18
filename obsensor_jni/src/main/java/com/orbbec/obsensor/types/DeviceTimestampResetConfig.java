package com.orbbec.obsensor.types;

import com.orbbec.obsensor.ByteConversion;

/**
 * \if English
 * The timestamp reset configuration of the device
 * \else
 * 设备的时间戳重置配置
 * \endif
 */
public class DeviceTimestampResetConfig implements ByteConversion {
    /**
     * \if English
     * Whether to enable the timestamp reset function.
     * If the timestamp reset function is enabled, the timer for calculating the timestamp for output frames will be reset to 0 when the timestamp reset
     * command or timestamp reset signal is received, and one timestamp reset signal will be output via TIMER_SYNC_OUT pin on synchronization port by default.
     * The timestamp reset signal is input via TIMER_SYNC_IN pin on the synchronization port.
     *
     * @attention For some models, the timestamp reset function is always enabled and cannot be disabled.
     * \else
     * 是否启用时间戳重置功能。
     * 当时间戳重置功能被启用时，当接收到时间戳重置命令或时间戳重置信号时，输出帧的时间戳计算计时器将会被重置为0，默认会通过同步端口上的TIMER_SYNC_OUT引脚输出一个时间戳重置信号。
     * 时间戳重置信号是通过同步端口上的TIMER_SYNC_IN引脚输入的。
     * @attention 此参数仅对某些型号有效，详情请参考产品手册。
     */
    @StructField(offset = 0, size = 1)
    private byte enable;
    /**
     * \if English
     * The delay time of executing the timestamp reset function after receiving the command or signal in microseconds.
     * \else
     * 时间戳重置功能执行的延时时间，单位为微秒。
     * \endif
     */
    @StructField(offset = 1, size = 4)
    private int timestampResetDelayUs;
    /**
     * \if English
     * the timestamp reset signal output enable flag.
     *
     * @attention For some models, the timestamp reset signal output is always enabled and cannot be disabled.
     * \else
     * 时间戳重置信号输出使能标志。
     * @attention 此参数仅对某些型号有效，详情请参考产品手册。
     */
    @StructField(offset = 5, size = 1)
    private byte timestampResetSignalOutputEnable;

    private byte[] mBytes;

    @Override
    public byte[] getBytes() {
        if (mBytes == null) {
            mBytes = new byte[6];
        }
        return mBytes;
    }

    @Override
    public boolean parseBytes() {
        return StructParser.parseBytes(this, mBytes);
    }

    @Override
    public boolean wrapBytes() {
        if (mBytes == null) {
            mBytes = new byte[6];
        }
        return StructParser.wrapBytes(this, mBytes);
    }

    @Override
    public String toString() {
        return "DeviceTimestampResetConfig{" +
                "enable=" + enable +
                ", timestampResetDelayUs=" + timestampResetDelayUs +
                ", timestampResetSignalOutputEnable=" + timestampResetSignalOutputEnable +
                '}';
    }
}
