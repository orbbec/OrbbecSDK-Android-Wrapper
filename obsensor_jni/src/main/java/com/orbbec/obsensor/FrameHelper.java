package com.orbbec.obsensor;

import com.orbbec.obsensor.types.Format;
import com.orbbec.obsensor.types.FrameType;

public class FrameHelper extends FrameFactory {
    /**
     * \if English
     * @brief Create a frame object based on an externally created Buffer
     *
     * @param[in] frame_type Frame object type.
     * @param format frame object format
     * @param buffer frame object buffer
     * @return frame object
     * \else
     * @brief 根据外部创建的Buffer创建帧对象
     *
     * @param[in] frame_type 帧对象类型
     * @param format 帧对象格式
     * @param buffer 帧对象数据
     * @return 返回帧对象
     * \endif
     */
    @Deprecated
    public static Frame createFrameFromBuffer(FrameType frameType, Format format, byte[] buffer) {
        long frameHandle = nCreateFrameFromBuffer(frameType.value(), format.value(), buffer);
        if (0 != frameHandle) {
            return new Frame(frameHandle);
        }
        return null;
    }

    /**
     * \if English
     * @brief Creates an empty frame collection object
     *
     * @return frameset object
     * \else
     * @brief 创建空的帧集合对象
     *
     * @return 返回帧集合对象
     * \endif
     */
    @Deprecated
    public static FrameSet createFrameSet() {
        long frameSetHandle = nCreateFrameSet();
        if (0 != frameSetHandle) {
            return new FrameSet(frameSetHandle);
        }
        return null;
    }


    /**
     * \if English
     * @brief Populate the frame collection with frames of the corresponding type
     *
     * @param frameSet frameset object
     * @param frame the object that fills the frame
     * \else
     * @brief 往帧集合中填入对应类型的帧
     *
     * @param frameSet 帧集合对象
     * @param frame 填入帧的对象
     * \endif
     */
    @Deprecated
    public static void pushFrame(Frame frameSet, Frame frame) {
        nPushFrame(frameSet.getHandle(), frame.getHandle());
    }

    /**
     * \if English
     * @brief Set the system timestamp of the frame
     *
     * @param frame object for frame settings
     * @param systemTimestamp set by systemTimestamp
     * \else
     * @brief 设置帧的系统时间戳
     *
     * @param frame 设置的帧对象
     * @param systemTimestamp 设置的系统时间戳
     * \endif
     */
    @Deprecated
    public static void setFrameSystemTimestamp(Frame frame, long systemTimestamp) {
        nSetFrameSystemTimestamp(frame.getHandle(), systemTimestamp);
    }

    /**
     * \if English
     * @brief Set the device timestamp of the frame
     *
     * @param frame object for frame settings
     * @param deviceTimestamp set by deviceTimestamp
     * \else
     * @brief 设置帧的设备时间戳
     *
     * @param frame 设置的帧对象
     * @param deviceTimestamp 设置的设备时间戳
     * \endif
     */
    @Deprecated
    public static void setFrameDeviceTimestamp(Frame frame, long deviceTimestamp) {
        nSetFrameDeviceTimestamp(frame.getHandle(), deviceTimestamp);
    }

    /**
     * \if English
     * @brief Set the device timestamp of the frame
     *
     * @param frame object for frame settings
     * @param deviceTimestampUs the device timestamp set (Us)
     * \else
     * @brief 设置帧的设备时间戳
     *
     * @param frame 设置的帧对象
     * @param deviceTimestampUs 设置的设备时间戳（Us）
     * \endif
     */
    public static void setFrameDeviceTimestampUs(Frame frame, long deviceTimestampUs) {
        nSetFrameDeviceTimestampUs(frame.getHandle(), deviceTimestampUs);
    }

    private static native long nCreateFrameFromBuffer(int frameType, int format, byte[] buffer);

    private static native long nCreateFrameSet();

    private static native void nPushFrame(long frameSet, long frame);

    private static native void nSetFrameSystemTimestamp(long frame, long systemTimestamp);

    private static native void nSetFrameDeviceTimestamp(long frame, long deviceTimestamp);

    private static native void nSetFrameDeviceTimestampUs(long frame, long deviceTimestampUs);
}
