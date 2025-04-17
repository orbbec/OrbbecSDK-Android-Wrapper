package com.orbbec.obsensor.types;

/**
 * \if English
 * For Linux, there are two ways to access the UVC device, libuvc and v4l2. The backend type is used to select the backend to access the device.
 * \else
 * 在Linux下，有两种方式访问UVC设备，libuvc和v4l2。后端类型用于选择访问设备使用的后端。
 * \endif
 */
public enum UvcBackendType {
    /**
     * \if English
     * Auto detect system capabilities and device hint to select backend
     * \else
     * 自动检测系统功能和设备提示以选择后端
     * \endif
     */
    OB_UVC_BACKEND_TYPE_AUTO(0),

    /**
     * \if English
     * Use libuvc backend to access the UVC device
     * \else
     * 使用libuvc后端访问UVC设备
     * \endif
     */
    OB_UVC_BACKEND_TYPE_LIBUVC(1),

    /**
     * \if English
     * Use v4l2 backend to access the UVC device
     * \else
     * 使用v4l2后端访问UVC设备
     * \endif
     */
    OB_UVC_BACKEND_TYPE_V4L2(2);

    private final int mValue;

    UvcBackendType(int value) {
        mValue = value;
    }

    public int value() {
        return mValue;
    }

    public static UvcBackendType get(int value) {
        for (UvcBackendType type : values()) {
            if (type.mValue == value) {
                return type;
            }
        }
        return OB_UVC_BACKEND_TYPE_AUTO;
    }
}
