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
    private int syncModeValue = SyncMode.OB_MULTI_DEVICE_SYNC_MODE_FREE_RUN.value();
    private int depthDelayUs;
    private int colorDelayUs;
    private int trigger2ImageDelayUs;
    private boolean triggerOutEnable;
    private int triggerOutDelayUs;
    private int framesPerTrigger;

    /**
     * \if English
     * @brief The sync mode of the device.
     * \else
     * @brief 设备的同步模式
     * \endif
     */
    public SyncMode getSyncMode() {
        return SyncMode.get(syncModeValue);
    }

    /**
     * \if English
     * @brief The sync mode of the device.
     * \else
     * @brief 设备的同步模式
     * \endif
     */
    public void setSyncMode(SyncMode mode) {
        this.syncModeValue = mode.value();
    }

    /**
     * \if English
     * @brief The delay time of the depth image capture after receiving the capture command or trigger signal in microseconds.
     *
     * @attention This parameter is only valid for some models， please refer to the product manual for details.
     * \else
     * @brief 深度图像捕获在接收到捕获命令或触发信号之后的延迟时间（以微秒为单位）
     *
     * @attention 此参数仅对某些型号有效，请参阅产品手册了解详细信息
     * \endif
     */
    public int getDepthDelayUs() {
        return depthDelayUs;
    }

    /**
     * \if English
     * @brief The delay time of the depth image capture after receiving the capture command or trigger signal in microseconds.
     *
     * @attention This parameter is only valid for some models， please refer to the product manual for details.
     * \else
     * @brief 深度图像捕获在接收到捕获命令或触发信号之后的延迟时间（以微秒为单位）
     *
     * @attention 此参数仅对某些型号有效，请参阅产品手册了解详细信息
     * \endif
     */
    public void setDepthDelayUs(int delayUs) {
        this.depthDelayUs = delayUs;
    }

    /**
     * \if English
     * @brief The delay time of the color image capture after receiving the capture command or trigger signal in microseconds.
     *
     * @attention This parameter is only valid for some models， please refer to the product manual for details.
     * \else
     * @brief 彩色图像捕获在接收到捕获命令或触发信号之后的延迟时间（以微秒为单位）
     *
     * @attention 此参数仅对某些型号有效，请参阅产品手册了解详细信息
     * \endif
     */
    public int getColorDelayUs() {
        return colorDelayUs;
    }

    /**
     * \if English
     * @brief The delay time of the color image capture after receiving the capture command or trigger signal in microseconds.
     *
     * @attention This parameter is only valid for some models， please refer to the product manual for details.
     * \else
     * @brief 彩色图像捕获在接收到捕获命令或触发信号之后的延迟时间（以微秒为单位）
     *
     * @attention 此参数仅对某些型号有效，请参阅产品手册了解详细信息
     * \endif
     */
    public void setColorDelayUs(int delayUs) {
        this.colorDelayUs = delayUs;
    }

    /**
     * \if English
     * @brief The delay time of the image capture after receiving the capture command or trigger signal in microseconds.
     * @brief The depth and color images are captured synchronously as the product design and can not change the delay between the depth and color images.
     *
     * @attention For Orbbec Astra 2 device, this parameter is valid only when the @ref triggerOutDelayUs is set to 0.
     * @attention This parameter is only valid for some models to replace @ref depthDelayUs and @ref colorDelayUs, please refer to the product manual for
     * details.
     * \else
     * @brief 在接收到捕获命令或触发信号之后的图像捕获的延迟时间（以微秒为单位）
     * @brief 深度图像和彩色图像是作为产品设计同步捕获的，并且不能改变深度图像和色彩图像之间的延迟
     *
     * @attention 此参数仅对某些型号有效，以替换 @ref depthDelayUs和 @ref colorDelayUs，请参阅产品手册了解详细信息
     * \endif
     */
    public int getTrigger2ImageDelayUs() {
        return trigger2ImageDelayUs;
    }

    /**
     * \if English
     * @brief The delay time of the image capture after receiving the capture command or trigger signal in microseconds.
     * @brief The depth and color images are captured synchronously as the product design and can not change the delay between the depth and color images.
     *
     * @attention For Orbbec Astra 2 device, this parameter is valid only when the @ref triggerOutDelayUs is set to 0.
     * @attention This parameter is only valid for some models to replace @ref depthDelayUs and @ref colorDelayUs, please refer to the product manual for
     * details.
     * \else
     * @brief 在接收到捕获命令或触发信号之后的图像捕获的延迟时间（以微秒为单位）
     * @brief 深度图像和彩色图像是作为产品设计同步捕获的，并且不能改变深度图像和色彩图像之间的延迟
     *
     * @attention 此参数仅对某些型号有效，以替换 @ref depthDelayUs和 @ref colorDelayUs，请参阅产品手册了解详细信息
     * \endif
     */
    public void setTrigger2ImageDelayUs(int delayUs) {
        this.trigger2ImageDelayUs = delayUs;
    }

    /**
     * \if English
     * @brief Trigger signal output enable flag.
     * @brief After the trigger signal output is enabled, the trigger signal will be output when the capture command or trigger signal is received. User can
     * adjust the delay time of the trigger signal output by @ref triggerOutDelayUs.
     *
     * @attention For some models, the trigger signal output is always enabled and cannot be disabled.
     * @attention If device is in the @ref OB_MULTI_DEVICE_SYNC_MODE_FREE_RUN or @ref OB_MULTI_DEVICE_SYNC_MODE_STANDALONE mode, the trigger signal output is
     * always disabled. Set this parameter to true will not take effect.
     * \else
     * @brief 触发信号输出启用标志
     * @brief 在触发信号输出被使能之后，当接收到捕获命令或触发信号时，触发信号将被输出。用户可以调整 @ref triggerSignalOutputDelayUs输出的触发信号的延迟时间
     *
     * @attention 对于某些型号，触发信号输出始终处于启用状态，在某些模式下无法禁用
     * @attention 如果设备处于 @ref OB_MULTI_DEVICE_SYNC_MODE_FREE_RUN 或 @ref
     * OB_MULTI_DEVICE_SYNC_MODE_STANDALONE模式，则触发信号输出始终被禁用。将此参数设置为true将不会生效
     * \endif
     */
    public boolean isTriggerOutEnable() {
        return triggerOutEnable;
    }

    /**
     * \if English
     * @brief Trigger signal output enable flag.
     * @brief After the trigger signal output is enabled, the trigger signal will be output when the capture command or trigger signal is received. User can
     * adjust the delay time of the trigger signal output by @ref triggerOutDelayUs.
     *
     * @attention For some models, the trigger signal output is always enabled and cannot be disabled.
     * @attention If device is in the @ref OB_MULTI_DEVICE_SYNC_MODE_FREE_RUN or @ref OB_MULTI_DEVICE_SYNC_MODE_STANDALONE mode, the trigger signal output is
     * always disabled. Set this parameter to true will not take effect.
     * \else
     * @brief 触发信号输出启用标志
     * @brief 在触发信号输出被使能之后，当接收到捕获命令或触发信号时，触发信号将被输出。用户可以调整 @ref triggerSignalOutputDelayUs输出的触发信号的延迟时间
     *
     * @attention 对于某些型号，触发信号输出始终处于启用状态，在某些模式下无法禁用
     * @attention 如果设备处于 @ref OB_MULTI_DEVICE_SYNC_MODE_FREE_RUN 或 @ref
     * OB_MULTI_DEVICE_SYNC_MODE_STANDALONE模式，则触发信号输出始终被禁用。将此参数设置为true将不会生效
     * \endif
     */
    public void setTriggerOutEnable(boolean triggerOutEnable) {
        this.triggerOutEnable = triggerOutEnable;
    }

    /**
     * \if English
     * @brief The delay time of the trigger signal output after receiving the capture command or trigger signal in microseconds.
     *
     * @attention For Orbbec Astra 2 device, only supported -1 and 0. -1 means the trigger signal output delay is automatically adjusted by the device, 0 means
     * the trigger signal output is disabled.
     * \else
     * @brief 接收到捕获命令或触发信号后输出的触发信号的延迟时间，单位为微秒
     * \endif
     */
    public int getTriggerOutDelayUs() {
        return triggerOutDelayUs;
    }

    /**
     * \if English
     * @brief The delay time of the trigger signal output after receiving the capture command or trigger signal in microseconds.
     *
     * @attention For Orbbec Astra 2 device, only supported -1 and 0. -1 means the trigger signal output delay is automatically adjusted by the device, 0 means
     * the trigger signal output is disabled.
     * \else
     * @brief 接收到捕获命令或触发信号后输出的触发信号的延迟时间，单位为微秒
     * \endif
     */
    public void setTriggerOutDelayUs(int delayUs) {
        this.triggerOutDelayUs = delayUs;
    }

    /**
     * \if English
     * @brief The frame number of each stream after each trigger in triggering mode.
     *
     * @attention This parameter is only valid when the triggering mode is set to @ref OB_MULTI_DEVICE_SYNC_MODE_HARDWARE_TRIGGERING or @ref
     * OB_MULTI_DEVICE_SYNC_MODE_SOFTWARE_TRIGGERING.
     * @attention The trigger frequency multiplied by the number of frames per trigger cannot exceed the maximum frame rate of the stream profile which is set
     * when starting the stream.
     * \else
     * @brief 触发模式中每个触发器的帧数
     *
     * @attention 仅当触发模式设置为 @ref OB_MULTI_DEVICE_SYNC_MODE_HARDWARE_TRIGGERING 或 @ref OB_MULTI_DEVICE_SYNC_MODE_SOFTWARE_TRIGGERING时，此参数才有效
     * @attention 触发频率乘以每个触发的帧数不能超过在启动流时设置的流配置文件的最大帧速率
     * \endif
     */
    public int getFramesPerTrigger() {
        return framesPerTrigger;
    }

    /**
     * \if English
     * @brief The frame number of each stream after each trigger in triggering mode.
     *
     * @attention This parameter is only valid when the triggering mode is set to @ref OB_MULTI_DEVICE_SYNC_MODE_HARDWARE_TRIGGERING or @ref
     * OB_MULTI_DEVICE_SYNC_MODE_SOFTWARE_TRIGGERING.
     * @attention The trigger frequency multiplied by the number of frames per trigger cannot exceed the maximum frame rate of the stream profile which is set
     * when starting the stream.
     * \else
     * @brief 触发模式中每个触发器的帧数
     *
     * @attention 仅当触发模式设置为 @ref OB_MULTI_DEVICE_SYNC_MODE_HARDWARE_TRIGGERING 或 @ref OB_MULTI_DEVICE_SYNC_MODE_SOFTWARE_TRIGGERING时，此参数才有效
     * @attention 触发频率乘以每个触发的帧数不能超过在启动流时设置的流配置文件的最大帧速率
     * \endif
     */
    public void setFramesPerTrigger(int frameNumPerTrigger) {
        this.framesPerTrigger = frameNumPerTrigger;
    }
}