package com.orbbec.obsensor;

/**
 * \if English
 * Sync mode
 * \else
 * 同步模式
 * \endif
 */
public enum SyncMode {
    /**
     * \if English
     * @brief Close synchronize mode
     * @brief Single device, neither process input trigger signal nor output trigger signal
     * @brief Each Sensor in a single device automatically triggers
     * \else
     * @brief 同步关闭
     * @brief 单机，不接收外部触发信号，不输出触发信号
     * @brief 单机内各 Sensor 自触发
     * \endif
     *
     */
    OB_SYNC_MODE_CLOSE(0x00),

    /**
     * \if English
     * @brief Standalone synchronize mode
     * @brief Single device, neither process input trigger signal nor output trigger signal
     * @brief Inside single device, RGB as Major sensor: RGB -> IR/Depth/TOF
     * \else
     * @brief 单机模式
     * @brief 单机，不接收外部触发信号，不输出触发信号
     * @brief 单机内 RGB 做主： RGB -> IR/Depth/TOF
     * \endif
     */
    OB_SYNC_MODE_STANDALONE(0x01),

    /**
     * \if English
     * @brief Primary synchronize mode
     * @brief Primary device. Ignore process input trigger signal, only output trigger signal to secondary devices.
     * @brief Inside single device, RGB as Major sensor: RGB -> IR/Depth/TOF
     * \else
     * @brief 主机模式
     * @brief 主机，不接收外部触发信号，向外输出触发信号
     * @brief 单机内 RGB 做主：RGB -> IR/Depth/TOF
     *
     * @attention 部分设备型号不支持该模式： Gemini 2 设备设置该模式会自动变更为 OB_SYNC_MODE_PRIMARY_MCU_TRIGGER 模式
     *
     */
    OB_SYNC_MODE_PRIMARY(0x02),

    /**
     * \if English
     * @brief Secondary synchronize mode
     * @brief Secondary device. Both process input trigger signal and output trigger signal to other devices.
     * @brief Different sensors in a single devices receive trigger signals respectively：ext trigger -> RGB && ext trigger -> IR/Depth/TOF
     *
     * @attention With the current Gemini 2 device set to this mode, each Sensor receives the first external trigger signal
     *     after the stream is turned on and starts timing self-triggering at the set frame rate until the stream is turned off
     * \else
     * @brief 从机模式
     * @brief 从机，接收外部触发信号，同时向外中继输出触发信号
     * @brief 单机内不同 Sensor 各自接收触发信号：ext trigger -> RGB && ext trigger -> IR/Depth/TOF
     *
     * @attention 当前 Gemini 2 设备设置为该模式后，各Sensor在开流后，接收到第一次外部触发信号即开始按照设置的帧率进行定时自触发，直到流关闭
     * \endif
     *
     */
    OB_SYNC_MODE_SECONDARY(0x03),

    /**
     * \if English
     * @brief MCU Primary synchronize mode
     * @brief Primary device. Ignore process input trigger signal, only output trigger signal to secondary devices.
     * @brief Inside device, MCU is the primary signal source:  MCU -> RGB && MCU -> IR/Depth/TOF
     * \else
     * @brief MCU 主模式
     * @brief 主机，不接收外部触发信号，向外输出触发信号
     * @brief 单机内 MCU 做主： MCU -> RGB && MCU -> IR/Depth/TOF
     * \endif
     */
    OB_SYNC_MODE_PRIMARY_MCU_TRIGGER(0x04),

    /**
     * \if English
     * @brief IR Primary synchronize mode
     * @brief Primary device. Ignore process input trigger signal, only output trigger signal to secondary devices.
     * @brief Inside device, IR is the primary signal source: IR/Depth/TOF -> RGB
     *
     * \else
     * @brief IR 主模式
     * @brief 主机，不接收外部触发信号，向外输出触发信号
     * @brief 单机内 IR 做主：IR/Depth/TOF -> RGB
     * \endif
     */
    OB_SYNC_MODE_PRIMARY_IR_TRIGGER(0x05),

    /**
     * \if English
     * @brief Software trigger synchronize mode
     * @brief Host, triggered by software control (receive the upper computer command trigger), at the same time to the trunk output trigger signal
     * @brief Different sensors in a single machine receive trigger signals respectively: soft trigger -> RGB && soft trigger -> IR/Depth/TOF
     *
     * @attention Support product: Gemini2
     * \else
     * @brief 软触发模式
     * @brief 主机，由软件控制触发（接收上位机命令触发），同时向外中继输出触发信号
     * @brief 单机内不同 Sensor 各自接收触发信号：soft trigger -> RGB && soft trigger -> IR/Depth/TOF
     *
     * @attention 当前仅 Gemini2 支持该模式
     * \endif
     */
    OB_SYNC_MODE_PRIMARY_SOFT_TRIGGER(0x06),

    /**
     * \if English
     * @brief Software trigger synchronize mode as secondary device
     * @brief The slave receives the external trigger signal (the external trigger signal comes from the soft trigger host) and outputs the trigger signal to
     * the external relay.
     * @brief Different sensors in a single machine receive trigger signals respectively：ext trigger -> RGB && ext  trigger -> IR/Depth/TOF
     * \else
     * @brief 软触发从机模式
     * @brief 从机，接收外部触发信号（外部触发信号来自软触发的主机），同时向外中继输出触发信号。
     * @brief 单机内不同 Sensor 各自接收触发信号：ext trigger -> RGB && ext  trigger -> IR/Depth/TOF
     *
     * @attention 当前仅 Gemini2 支持该模式
     * \endif
     */
    OB_SYNC_MODE_SECONDARY_SOFT_TRIGGER(0x07),

    /**
     * \if English
     * @brief Unknown type
     * \else
     * @brief 未知类型
     * \endif
     */
    OB_SYNC_MODE_UNKNOWN (0xff)
    ;

    private final int mValue;

    SyncMode(int value) {
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
    public static SyncMode get(int value) {
        for (int i = 0; i < values().length; i++) {
            if (values()[i].value() == value) {
                return values()[i];
            }
        }
        return null;
    }
}
