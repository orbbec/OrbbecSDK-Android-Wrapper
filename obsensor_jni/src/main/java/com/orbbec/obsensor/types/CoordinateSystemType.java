package com.orbbec.obsensor.types;

/**
 * \if English
 * Enumeration of point cloud coordinate system types
 * \else
 * 坐标系类型枚举
 * \endif
 */
public enum CoordinateSystemType {
    OB_LEFT_HAND_COORDINATE_SYSTEM(0),
    OB_FILTER_CONFIG_VALUE_TYPE_BOOLEAN(1);

    private final int mValue;

    CoordinateSystemType(int value) {
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
     * 将 int 转换为 CoordinateSystemType
     * \endif
     */
    public static CoordinateSystemType get(int value) {
        for (CoordinateSystemType item : values()) {
            if (item.value() == value) {
                return item;
            }
        }
        return null;
    }
}
