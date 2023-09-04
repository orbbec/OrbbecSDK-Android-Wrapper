package com.orbbec.obsensor;

/**
 * \if English
 *
 * @brief Device synchronization configuration
 * \else
 * @brief 设备同步配置
 * @brief 单机内不同 Sensor 的同步 及 多机间同步 配置
 * \endif
 */
public class MultiDeviceSyncConfig {
    private int syncModeValue = SyncMode.OB_SYNC_MODE_UNKNOWN.value();
    private int irTriggerSignalInDelay;
    private int rgbTriggerSignalInDelay;
    private int deviceTriggerSignalOutDelay;
    private int deviceTriggerSignalOutPolarity;
    private int mcuTriggerFrequency;
    private int deviceId;

    /**
     * \if English
     *
     * @brief Getter
     * @brief Device synchronize mode
     * \else
     * @brief Getter
     * @brief 同步模式
     * \endif
     */
    public SyncMode getSyncMode() {
        return SyncMode.get(syncModeValue);
    }

    /**
     * \if English
     *
     * @brief Setter
     * @brief Device synchronize mode
     * \else
     * @brief Setter
     * @brief 同步模式
     * \endif
     */
    public void setSyncMode(SyncMode syncMode) {
        this.syncModeValue = syncMode.value();
    }

    /**
     * \if English
     *
     * @brief Getter
     * @brief IR Trigger signal input delay: Used to configure the delay between the IR/Depth/TOF Sensor receiving the trigger signal and starting exposure,
     * Unit: microsecond
     * @attention This parameter is invalid when the synchronization MODE is set to @ref OB_SYNC_MODE_PRIMARY_IR_TRIGGER
     * \else
     * @brief Getter
     * @brief IR 触发信号输入延时，用于 IR/Depth/TOF Sensor 接收到触发信号后到开始曝光的延时配置，单位为微秒
     * @attention 同步模式配置为  @ref OB_SYNC_MODE_PRIMARY_IR_TRIGGER 时无效
     * \endif
     */
    public int getIrTriggerSignalInDelay() {
        return irTriggerSignalInDelay;
    }

    /**
     * \if English
     *
     * @brief Setter
     * @brief IR Trigger signal input delay: Used to configure the delay between the IR/Depth/TOF Sensor receiving the trigger signal and starting exposure,
     * Unit: microsecond
     * @attention This parameter is invalid when the synchronization MODE is set to @ref OB_SYNC_MODE_PRIMARY_IR_TRIGGER
     * \else
     * @brief Setter
     * @brief IR 触发信号输入延时，用于 IR/Depth/TOF Sensor 接收到触发信号后到开始曝光的延时配置，单位为微秒
     * @attention 同步模式配置为  @ref OB_SYNC_MODE_PRIMARY_IR_TRIGGER 时无效
     * \endif
     */
    public void setIrTriggerSignalInDelay(int irTriggerSignalInDelay) {
        this.irTriggerSignalInDelay = irTriggerSignalInDelay;
    }

    /**
     * \if English
     *
     * @brief Getter
     * @brief RGB trigger signal input delay is used to configure the delay from the time when an RGB Sensor receives the trigger signal to the time when the
     * exposure starts. Unit: microsecond
     * @attention This parameter is invalid when the synchronization MODE is set to @ref OB_SYNC_MODE_PRIMARY
     * \else
     * @brief Getter
     * @brief RGB 触发信号输入延时，用于 RGB Sensor 接收到触发信号后到开始曝光的延时配置，单位为微秒
     * @attention 同步模式配置为  @ref OB_SYNC_MODE_PRIMARY 时无效
     * \endif
     */
    public int getRgbTriggerSignalInDelay() {
        return rgbTriggerSignalInDelay;
    }

    /**
     * \if English
     *
     * @brief Setter
     * @brief RGB trigger signal input delay is used to configure the delay from the time when an RGB Sensor receives the trigger signal to the time when the
     * exposure starts. Unit: microsecond
     * @attention This parameter is invalid when the synchronization MODE is set to @ref OB_SYNC_MODE_PRIMARY
     * \else
     * @brief Setter
     * @brief RGB 触发信号输入延时，用于 RGB Sensor 接收到触发信号后到开始曝光的延时配置，单位为微秒
     * @attention 同步模式配置为  @ref OB_SYNC_MODE_PRIMARY 时无效
     * \endif
     */
    public void setRgbTriggerSignalInDelay(int rgbTriggerSignalInDelay) {
        this.rgbTriggerSignalInDelay = rgbTriggerSignalInDelay;
    }

    /**
     * \if English
     *
     * @brief Getter
     * @brief Device trigger signal output delay, used to control the delay configuration of the host device to output trigger signals or the slave device to
     * output trigger signals. Unit: microsecond
     * @attention This parameter is invalid when the synchronization MODE is set to @ref OB_SYNC_MODE_CLOSE or @ref OB_SYNC_MODE_STANDALONE
     * \else
     * @brief Getter
     * @brief 设备触发信号输出延时，用于控制主机设备向外输 或 从机设备向外中继输出 触发信号的延时配置，单位：微秒
     * @attention 同步模式配置为 @ref OB_SYNC_MODE_CLOSE 和  @ref OB_SYNC_MODE_STANDALONE 时无效
     * \endif
     */
    public int getDeviceTriggerSignalOutDelay() {
        return deviceTriggerSignalOutDelay;
    }

    /**
     * \if English
     *
     * @brief Setter
     * @brief Device trigger signal output delay, used to control the delay configuration of the host device to output trigger signals or the slave device to
     * output trigger signals. Unit: microsecond
     * @attention This parameter is invalid when the synchronization MODE is set to @ref OB_SYNC_MODE_CLOSE or @ref OB_SYNC_MODE_STANDALONE
     * \else
     * @brief Setter
     * @brief 设备触发信号输出延时，用于控制主机设备向外输 或 从机设备向外中继输出 触发信号的延时配置，单位：微秒
     * @attention 同步模式配置为 @ref OB_SYNC_MODE_CLOSE 和  @ref OB_SYNC_MODE_STANDALONE 时无效
     * \endif
     */
    public void setDeviceTriggerSignalOutDelay(int deviceTriggerSignalOutDelay) {
        this.deviceTriggerSignalOutDelay = deviceTriggerSignalOutDelay;
    }

    /**
     * \if English
     *
     * @brief Getter
     * @brief The device trigger signal output polarity is used to control the polarity configuration of the trigger signal output from the host device or the
     * trigger signal output from the slave device
     * @brief 0: forward pulse; 1: negative pulse
     * @attention This parameter is invalid when the synchronization MODE is set to @ref OB_SYNC_MODE_CLOSE or @ref OB_SYNC_MODE_STANDALONE
     * \else
     * @brief Getter
     * @brief 设备触发信号输出极性，用于控制主机设备向外输 或 从机设备向外中继输出 触发信号的极性配置
     * @brief 0: 正向脉冲；1: 负向脉冲
     * @attention 同步模式配置为 @ref OB_SYNC_MODE_CLOSE 和  @ref OB_SYNC_MODE_STANDALONE 时无效
     * \endif
     */
    public int getDeviceTriggerSignalOutPolarity() {
        return deviceTriggerSignalOutPolarity;
    }

    /**
     * \if English
     *
     * @brief Setter
     * @brief The device trigger signal output polarity is used to control the polarity configuration of the trigger signal output from the host device or the
     * trigger signal output from the slave device
     * @brief 0: forward pulse; 1: negative pulse
     * @attention This parameter is invalid when the synchronization MODE is set to @ref OB_SYNC_MODE_CLOSE or @ref OB_SYNC_MODE_STANDALONE
     * \else
     * @brief Setter
     * @brief 设备触发信号输出极性，用于控制主机设备向外输 或 从机设备向外中继输出 触发信号的极性配置
     * @brief 0: 正向脉冲；1: 负向脉冲
     * @attention 同步模式配置为 @ref OB_SYNC_MODE_CLOSE 和  @ref OB_SYNC_MODE_STANDALONE 时无效
     * \endif
     */
    public void setDeviceTriggerSignalOutPolarity(int deviceTriggerSignalOutPolarity) {
        this.deviceTriggerSignalOutPolarity = deviceTriggerSignalOutPolarity;
    }

    /**
     * \if English
     *
     * @brief Getter
     * @brief MCU trigger frequency, used to configure the output frequency of MCU trigger signal in MCU master mode, unit: Hz
     * @brief This configuration will directly affect the image output frame rate of the Sensor. Unit: FPS （frame pre second）
     * @attention This parameter is invalid only when the synchronization MODE is set to @ref OB_SYNC_MODE_PRIMARY_MCU_TRIGGER
     * \else
     * @brief Getter
     * @brief MCU 触发频率，用于 MCU 主模式下，MCU触发信号输出频率配置，单位：Hz
     * @brief 该配置会直接影响 Sensor 的图像输出帧率，即也可以认为单位为：FPS （frame pre second）
     * @attention 仅当同步模式配置为 @ref OB_SYNC_MODE_PRIMARY_MCU_TRIGGER 时无效
     * \endif
     */
    public int getMcuTriggerFrequency() {
        return mcuTriggerFrequency;
    }

    /**
     * \if English
     *
     * @brief Setter
     * @brief MCU trigger frequency, used to configure the output frequency of MCU trigger signal in MCU master mode, unit: Hz
     * @brief This configuration will directly affect the image output frame rate of the Sensor. Unit: FPS （frame pre second）
     * @attention This parameter is invalid only when the synchronization MODE is set to @ref OB_SYNC_MODE_PRIMARY_MCU_TRIGGER
     * \else
     * @brief Setter
     * @brief MCU 触发频率，用于 MCU 主模式下，MCU触发信号输出频率配置，单位：Hz
     * @brief 该配置会直接影响 Sensor 的图像输出帧率，即也可以认为单位为：FPS （frame pre second）
     * @attention 仅当同步模式配置为 @ref OB_SYNC_MODE_PRIMARY_MCU_TRIGGER 时无效
     * \endif
     */
    public void setMcuTriggerFrequency(int mcuTriggerFrequency) {
        this.mcuTriggerFrequency = mcuTriggerFrequency;
    }

    /**
     * \if English
     *
     * @brief Getter
     * @brief Device number. Users can mark the device with this number
     * \else
     * @brief Getter
     * @brief 设备编号，用户可用该编号对设备进行标记
     * \endif
     */
    public int getDeviceId() {
        return deviceId;
    }

    /**
     * \if English
     *
     * @brief Setter
     * @brief Device number. Users can mark the device with this number
     * \else
     * @brief Setter
     * @brief 设备编号，用户可用该编号对设备进行标记
     * \endif
     */
    public void setDeviceId(int deviceId) {
        this.deviceId = deviceId;
    }

    @Override
    public String toString() {
        return "DeviceSyncConfig{" +
                "syncModeValue=" + syncModeValue +
                ", irTriggerSignalInDelay=" + irTriggerSignalInDelay +
                ", rgbTriggerSignalInDelay=" + rgbTriggerSignalInDelay +
                ", deviceTriggerSignalOutDelay=" + deviceTriggerSignalOutDelay +
                ", deviceTriggerSignalOutPolarity=" + deviceTriggerSignalOutPolarity +
                ", mcuTriggerFrequency=" + mcuTriggerFrequency +
                ", deviceId=" + deviceId +
                '}';
    }
}