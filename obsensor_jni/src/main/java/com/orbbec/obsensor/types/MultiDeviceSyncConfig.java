package com.orbbec.obsensor.types;

import com.orbbec.obsensor.ByteConversion;

/**
 * \if English
 * The synchronization configuration of the device
 * \else
 * 设备的时钟同步配置
 * \endif
 */
public class MultiDeviceSyncConfig implements ByteConversion {
    /**
     * \if English
     * The sync mode of the device.
     * \else
     * 设备的同步模式。
     * \endif
     */
    @StructField(offset = 0, size = 4)
    private int mSyncMode;
    /**
     * \if English
     * The delay time of the depth image capture after receiving the capture command or trigger signal in microseconds.
     *
     * @attention This parameter is only valid for some models， please refer to the product manual for details.
     * \else
     * 触发信号或捕获命令接收后，深度图像捕获的延时时间，单位为微秒。
     * @attention 此参数仅对部分型号设备有效，详情请参考产品手册。
     * \endif
     */
    @StructField(offset = 4, size = 4)
    private int depthDelayUs;
    /**
     * \if English
     * The delay time of the color image capture after receiving the capture command or trigger signal in microseconds.
     *
     * @attention This parameter is only valid for some models， please refer to the product manual for details.
     * \else
     * 触发信号或捕获命令接收后，彩色图像捕获的延时时间，单位为微秒。
     * @attention 此参数仅对部分型号设备有效，详情请参考产品手册。
     * \endif
     */
    @StructField(offset = 8, size = 4)
    private int colorDelayUs;
    /**
     * \if English
     * The delay time of the image capture after receiving the capture command or trigger signal in microseconds.
     * The depth and color images are captured synchronously as the product design and can not change the delay between the depth and color images.
     *
     * @attention For Orbbec Astra 2 device, this parameter is valid only when the @ref triggerOutDelayUs is set to 0.
     * @attention This parameter is only valid for some models to replace @ref depthDelayUs and @ref colorDelayUs, please refer to the product manual for
     * details.
     * \else
     * 触发信号或捕获命令接收后，图像捕获的延时时间，单位为微秒。
     * 深度图像和彩色图像被同步捕获，因此无法修改深度图像和彩色图像之间的延时。
     * @attention Orbbec Astra 2设备，当@ref triggerOutDelayUs设置为0时，此参数才有效。
     * @attention 此参数仅对某些型号有效，详情请参考产品手册。
     * \endif
     */
    @StructField(offset = 12, size = 4)
    private int trigger2ImageDelayUs;
    /**
     * \if English
     * Trigger signal output enable flag.
     * After the trigger signal output is enabled, the trigger signal will be output when the capture command or trigger signal is received. User can
     * adjust the delay time of the trigger signal output by @ref triggerOutDelayUs.
     *
     * @attention For some models, the trigger signal output is always enabled and cannot be disabled.
     * @attention If device is in the @ref OB_MULTI_DEVICE_SYNC_MODE_FREE_RUN or @ref OB_MULTI_DEVICE_SYNC_MODE_STANDALONE mode, the trigger signal output is
     * always disabled. Set this parameter to true will not take effect.
     * \else
     * 触发信号输出使能标志。
     * 触发信号输出被启用后，当捕获命令或触发信号被接收时，会输出触发信号。用户可以通过@ref triggerOutDelayUs来调整触发信号输出的延时。
     * @attention 此参数仅对某些型号有效，详情请参考产品手册。
     * @attention 如果设备处于@ref OB_MULTI_DEVICE_SYNC_MODE_FREE_RUN或@ref OB_MULTI_DEVICE_SYNC_MODE_STANDALONE模式，则触发信号输出始终被禁用。设置此参数为true无效。
     * \endif
     */
    @StructField(offset = 16, size = 1)
    private byte triggerOutEnable;
    /**
     * \if English
     * The delay time of the trigger signal output after receiving the capture command or trigger signal in microseconds.
     *
     * @attention For Orbbec Astra 2 device, only supported -1 and 0. -1 means the trigger signal output delay is automatically adjusted by the device, 0 means
     * the trigger signal output is disabled.
     * \else
     * 触发信号输出延时时间，单位为微秒。
     * @attention Orbbec Astra 2设备，仅支持-1和0。-1表示触发信号输出延时由设备自动调整，0表示触发信号输出被禁用。
     * \endif
     */
    @StructField(offset = 17, size = 4)
    private int triggerOutDelayUs;
    /**
     * \if English
     * The frame number of each stream after each trigger in triggering mode.
     *
     * @attention This parameter is only valid when the triggering mode is set to @ref OB_MULTI_DEVICE_SYNC_MODE_HARDWARE_TRIGGERING or @ref
     * OB_MULTI_DEVICE_SYNC_MODE_SOFTWARE_TRIGGERING.
     * @attention The trigger frequency multiplied by the number of frames per trigger cannot exceed the maximum frame rate of the stream profile which is set
     * when starting the stream.
     * \else
     * 触发模式下，每个触发后捕获的帧数。
     * @attention 此参数仅对触发模式为@ref OB_MULTI_DEVICE_SYNC_MODE_HARDWARE_TRIGGERING或@ref OB_MULTI_DEVICE_SYNC_MODE_SOFTWARE_TRIGGERING时有效。
     * @attention 触发频率乘以帧数不能超过在启动流时设置的流配置的最大帧率。
     * \endif
     */
    @StructField(offset = 21, size = 4)
    private int framesPerTrigger;

    public void setSyncMode(MultiDeviceSyncMode mSyncMode) {
        this.mSyncMode = mSyncMode.value();
    }

    public MultiDeviceSyncMode getSyncMode() {
        return MultiDeviceSyncMode.get(mSyncMode);
    }

    public int getDepthDelayUs() {
        return depthDelayUs;
    }

    public void setDepthDelayUs(int depthDelayUs) {
        this.depthDelayUs = depthDelayUs;
    }

    public int getColorDelayUs() {
        return colorDelayUs;
    }

    public void setColorDelayUs(int colorDelayUs) {
        this.colorDelayUs = colorDelayUs;
    }

    public int getTrigger2ImageDelayUs() {
        return trigger2ImageDelayUs;
    }

    public void setTrigger2ImageDelayUs(int trigger2ImageDelayUs) {
        this.trigger2ImageDelayUs = trigger2ImageDelayUs;
    }

    public byte getTriggerOutEnable() {
        return triggerOutEnable;
    }

    public void setTriggerOutEnable(byte triggerOutEnable) {
        this.triggerOutEnable = triggerOutEnable;
    }

    public int getTriggerOutDelayUs() {
        return triggerOutDelayUs;
    }

    public void setTriggerOutDelayUs(int triggerOutDelayUs) {
        this.triggerOutDelayUs = triggerOutDelayUs;
    }

    public int getFramesPerTrigger() {
        return framesPerTrigger;
    }

    public void setFramesPerTrigger(int framesPerTrigger) {
        this.framesPerTrigger = framesPerTrigger;
    }

    private byte[] mBytes;

    @Override
    public byte[] getBytes() {
        if (mBytes == null) {
            mBytes = new byte[25];
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
            mBytes = new byte[25];
        }
        return StructParser.wrapBytes(this, mBytes);
    }

    @Override
    public String toString() {
        return "MultiDeviceSyncConfig{" +
                "mSyncMode=" + mSyncMode +
                ", depthDelayUs=" + depthDelayUs +
                ", colorDelayUs=" + colorDelayUs +
                ", trigger2ImageDelayUs=" + trigger2ImageDelayUs +
                ", triggerOutEnable=" + triggerOutEnable +
                ", triggerOutDelayUs=" + triggerOutDelayUs +
                ", framesPerTrigger=" + framesPerTrigger +
                '}';
    }
}
