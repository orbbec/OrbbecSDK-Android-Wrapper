package com.orbbec.obsensor.types;

public enum FilterConfigValueType {
    OB_FILTER_CONFIG_VALUE_TYPE_INVALID(-1),
    OB_FILTER_CONFIG_VALUE_TYPE_INT(0),
    OB_FILTER_CONFIG_VALUE_TYPE_FLOAT(1),
    OB_FILTER_CONFIG_VALUE_TYPE_BOOLEAN(2);

    private final int mValue;

    FilterConfigValueType(int value) {
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
     * 将int转换为FilterConfigValueType
     * \endif
     */
    public static FilterConfigValueType get(int value) {
        for (FilterConfigValueType item : values()) {
            if (item.value() == value) {
                return item;
            }
        }
        return null;
    }
}
