package com.orbbec.obsensor;

public class FrameHelper {
    /**
     * \if English
     * @brief Create a frame object based on an externally created Buffer
     *
     * @param format frame object format
     * @param width frame object width
     * @param height frame object height
     * @param buffer frame object buffer
     * @return frame object
     * \else
     * @brief 根据外部创建的Buffer创建帧对象
     *
     * @param format 帧对象格式
     * @param width 帧对象宽
     * @param height 帧对象高
     * @param buffer 帧对象数据
     * @return 返回帧对象
     * \endif
     */
    public static Frame createFrameFromBuffer(Format format, int width, int height, byte[] buffer) {
        long frameHandle = nCreateFrameFromBuffer(format.value(), width, height, buffer);
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
     * @param frameType the type of frame filled in
     * @param frame the object that fills the frame
     * \else
     * @brief 往帧集合中填入对应类型的帧
     *
     * @param frameSet 帧集合对象
     * @param frameType 填入帧的类型
     * @param frame 填入帧的对象
     * \endif
     */
    public static void pushFrame(Frame frameSet, FrameType frameType, Frame frame) {
        nPushFrame(frameSet.getHandle(), frameType.value(), frame.getHandle());
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

    private static native long nCreateFrameFromBuffer(int format, int width, int height, byte[] buffer);

    private static native long nCreateFrameSet();

    private static native void nPushFrame(long frameSet, int frameType, long frame);

    private static native void nSetFrameSystemTimestamp(long frame, long systemTimestamp);

    private static native void nSetFrameDeviceTimestamp(long frame, long deviceTimestamp);

    private static native void nSetFrameDeviceTimestampUs(long frame, long deviceTimestampUs);
}
