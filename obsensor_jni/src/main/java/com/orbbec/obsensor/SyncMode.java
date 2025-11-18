package com.orbbec.obsensor;

/**
 * \if English
 * The synchronization mode of the device.
 * \else
 * 设备同步模式
 * \endif
 */
public enum SyncMode {
    /**
     * \if English
     * @brief free run mode
     * @brief The device does not synchronize with other devices,
     * @brief The Color and Depth can be set to different frame rates.
     * \else
     * @brief free run 模式
     * @brief 该设备不与其他设备同步
     * @brief “彩色”和“深度”可以设置为不同的帧速率
     * \endif
     */
    OB_MULTI_DEVICE_SYNC_MODE_FREE_RUN(1),

    /**
     * \if English
     * @brief standalone mode
     * @brief The device does not synchronize with other devices.
     * @brief The Color and Depth should be set to same frame rates, the Color and Depth will be synchronized.
     * \else
     * @brief standalone 模式
     * @brief 该设备不与其他设备同步
     * @brief “彩色”和“深度”应设置为相同的帧速率，“彩色”与“深度”将同步
     * \endif
     */
    OB_MULTI_DEVICE_SYNC_MODE_STANDALONE(1 << 1),

    /**
     * \if English
     * @brief primary mode
     * @brief The device is the primary device in the multi-device system, it will output the trigger signal via VSYNC_OUT pin on synchronization port by
     * default.
     * @brief The Color and Depth should be set to same frame rates, the Color and Depth will be synchronized and can be adjusted by @ref colorDelayUs, @ref
     * depthDelayUs or @ref trigger2ImageDelayUs.
     * \else
     * @brief primary 模式
     * @brief 该设备是多设备系统中的主要设备，默认情况下会通过同步端口上的VSYNC_OUT引脚输出触发信号
     * @brief 颜色和深度应设置为相同的帧速率，颜色和深度将同步，并可通过 @ref colorDelayUs、@refdepthDelayUs 或 @ref trigger2ImageDelayUs 进行调整
     * \endif
     */
    OB_MULTI_DEVICE_SYNC_MODE_PRIMARY(1 << 2),

    /**
     * \if English
     * @brief secondary mode
     * @brief The device is the secondary device in the multi-device system, it will receive the trigger signal via VSYNC_IN pin on synchronization port. It
     * will out the trigger signal via VSYNC_OUT pin on synchronization port by default.
     * @brief The Color and Depth should be set to same frame rates, the Color and Depth will be synchronized and can be adjusted by @ref colorDelayUs, @ref
     * depthDelayUs or @ref trigger2ImageDelayUs.
     * @brief After starting the stream, the device will wait for the trigger signal to start capturing images, and will stop capturing images when the trigger
     * signal is stopped.
     *
     * @attention The frequency of the trigger signal should be same as the frame rate of the stream profile which is set when starting the stream.
     * \else
     * @brief secondary 模式
     * @brief 该设备是多设备系统中的从设备，它将通过同步端口上的 VSYNC_IN 引脚接收触发信号。默认情况下，它将从同步端口上 VSYNC_OUT 引脚输出触发信号
     * @brief 颜色和深度应设置为相同的帧速率，颜色和深度将同步，并可通过 @ref colorDelayUs、 @ref depthDelayUss或 @ref trigger2ImageDelayUs进行调整
     * @brief 在启动流之后，设备将等待触发信号开始捕获图像，并且当触发信号停止时将停止捕获图像
     *
     * @attention 触发信号的频率应当与在启动流时设置的流简档的帧速率相同
     * \endif
     */
    OB_MULTI_DEVICE_SYNC_MODE_SECONDARY(1 << 3),

    /**
     * \if English
     * @brief secondary synced mode
     * @brief The device is the secondary device in the multi-device system, it will receive the trigger signal via VSYNC_IN pin on synchronization port. It
     * will out the trigger signal via VSYNC_OUT pin on synchronization port by default.
     * @brief The Color and Depth should be set to same frame rates, the Color and Depth will be synchronized and can be adjusted by @ref colorDelayUs, @ref
     * depthDelayUs or @ref trigger2ImageDelayUs.
     * @brief After starting the stream, the device will be immediately start capturing images, and will adjust the capture time when the trigger signal is
     * received to synchronize with the primary device. If the trigger signal is stopped, the device will still capture images.
     *
     * @attention The frequency of the trigger signal should be same as the frame rate of the stream profile which is set when starting the stream.
     * \else
     * @brief secondary synced 模式
     * @brief 该设备是多设备系统中的从设备，它将通过同步端口上的VSYNC_IN引脚接收触发信号,默认情况下，它将从同步端口上VSYNC_OUT引脚输出触发信号
     * @brief 颜色和深度应设置为相同的帧速率，颜色和深度将同步，并可通过 @ref colorDelayUs、 @ref depthDelayUss或 @ref trigger2ImageDelayUs进行调整
     * @brief 启动流后，设备将立即开始捕捉图像，并在接收到触发信号时调整捕捉时间，以与主设备同步。如果触发信号停止，设备仍将捕获图像
     *
     * @attention 触发信号的频率应当与在启动流时设置的流简档的帧速率相同
     * \endif
     */
    OB_MULTI_DEVICE_SYNC_MODE_SECONDARY_SYNCED(1 << 4),

    /**
     * \if English
     * @brief software triggering mode
     * @brief The device will start one time image capture after receiving the capture command and will output the trigger signal via VSYNC_OUT pin by default.
     * The capture command can be sent form host by call @ref ob_device_trigger_capture. The number of images captured each time can be set by @ref
     * framesPerTrigger.
     * @brief The Color and Depth should be set to same frame rates, the Color and Depth will be synchronized and can be adjusted by @ref colorDelayUs, @ref
     * depthDelayUs or @ref trigger2ImageDelayUs.
     *
     * @brief The frequency of the user call @ref ob_device_trigger_capture to send the capture command multiplied by the number of frames per trigger should be
     * less than the frame rate of the stream profile which is set when starting the stream.
     * \else
     * @brief software triggering 模式
     * @brief 该设备在接收到捕获命令后将开始一次图像捕获，并且默认情况下将通过VSYNC_OUT引脚输出触发信号。捕获命令可以
     * 通过调用 @ref ob_device_trigger_capture从主机发送。每次拍摄的图像数量可以由 @ref 设置framesPerTriggerForTriggeringMode
     * @brief 颜色和深度应设置为相同的帧速率，颜色和深度将同步，并可通过 @ref colorDelayUs、 @ref depthDelayUss或 @ref trigger2ImageDelayUs进行调整
     * @brief 用户调用@ref ob_device_trigger_capture发送捕获命令的频率乘以每个触发器的帧数应该小于启动流时设置的流配置文件的帧速率
     * \endif
     */
    OB_MULTI_DEVICE_SYNC_MODE_SOFTWARE_TRIGGERING(1 << 5),

    /**
     * \if English
     * @brief hardware triggering mode
     * @brief The device will start one time image capture after receiving the trigger signal via VSYNC_IN pin on synchronization port and will output the
     * trigger signal via VSYNC_OUT pin by default. The number of images captured each time can be set by @ref framesPerTrigger.
     * @brief The Color and Depth should be set to same frame rates, the Color and Depth will be synchronized and can be adjusted by @ref colorDelayUs, @ref
     * depthDelayUs or @ref trigger2ImageDelayUs.
     *
     * @attention The frequency of the trigger signal multiplied by the number of frames per trigger should be less than the frame rate of the stream profile
     * which is set when starting the stream.
     * @attention The trigger signal input via VSYNC_IN pin on synchronization port should be ouput by other device via VSYNC_OUT pin in hardware triggering
     * mode or software triggering mode.
     * @attention Due to different models may have different signal input requirements, please do not use different models to output trigger
     * signal as input-trigger signal.
     * \else
     * @brief hardware triggering 模式
     * @brief 该设备将在通过同步端口上的 VSYNC_IN 引脚接收到触发信号后开始一次图像捕获，并将输出默认情况下，通过 VSYNC_OUT
     * 引脚触发信号。每次捕获的图像数量可以由
     * @ref framesPerTriggerForTriggeringMode设置。
     * @brief 颜色和深度应设置为相同的帧速率，颜色和深度将同步，并可通过 @ref colorDelayUs、 @ref depthDelayUss或 @ref trigger2ImageDelayUs进行调整
     *
     * @attention 触发信号的频率乘以每个触发的帧数应该小于在启动流时设置的流配置文件的帧速率
     * @attention 在硬件触发模式或软件触发模式下，通过同步端口上的VSYNC_IN引脚输入的触发信号应由其他设备通过VSYNC_OUT引脚输出
     * @attention 由于不同的型号设备可能有不同的信号输入要求，请不要使用不同的型号设备输出触发信号作为输入触发信号
     * \endif
     */
    OB_MULTI_DEVICE_SYNC_MODE_HARDWARE_TRIGGERING(1 << 6)
    ;

    private final int mValue;

    SyncMode(int value) {
        mValue = value;
    }

    /**
	 * \if English
     * Enum value of int
	 * \else
     * 枚举对应的int值
	 * \endif
     */
    public int value() {
        return mValue;
    }

    /**
	 * \if English
     * Convert int value to SyncMode
	 * \else
     * 将int转换为SyncMode
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
