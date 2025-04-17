package com.orbbec.obsensor.types;

/**
 * \if English
 * Enumeration used to describe access rights for properties
 * \else
 * 用于描述属性的访问权限的枚举
 * \endif
 */
public enum PermissionType {
    /**
     * \if English
     * no access
     * \else
     * 无访问权限
     * \endif
     */
    OB_PERMISSION_DENY(0),
    /**
     * \if English
     * readable
     * \else
     * 可读
     * \endif
     */
    OB_PERMISSION_READ(1),
    /**
     * \if English
     * writable
     * \else
     * 可写
     * \endif
     */
    OB_PERMISSION_WRITE(2),
    /**
     * \if English
     * can read and write
     * \else
     * 可读写
     * \endif
     */
    OB_PERMISSION_READ_WRITE(3),
    /**
     * \if English
     * any situation above
     * \else
     * 任意一种情况
     * \endif
     */
    OB_PERMISSION_ANY(255);

    private final int mValue;

    PermissionType(int value) {
        mValue = value;
    }

    /**
     * \if English
     * Get the index value corresponding to a specific property enumeration
     *
     * @return index value
     * \else
     * 获取特定属性枚举对应的索引值
     * @return 索引值
     * \endif
     */
    public int value() {
        return mValue;
    }

    /**
     * \if English
     * Get the attribute value corresponding to the specified index, read and write permission type enumeration
     *
     * @param value index value
     * @param value 索引值
     * @return property enumeration
     * \else
     * 获取指定索引对应的属性值读写权限类型枚举
     * @return 属性枚举
     * \endif
     */
    public static PermissionType get(int value) {
        for (int i = 0; i < values().length; i++) {
            if (values()[i].value() == value) {
                return values()[i];
            }
        }
        return null;
    }
}
