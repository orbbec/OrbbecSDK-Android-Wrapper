package com.orbbec.obsensor;

/**
 * \if English
 * Property Type Description
 * \else
 * 属性类型描述
 * \endif
 */
public enum PropertyType {
    /**
	 * \if English
	 * property of boolean data
	 * \else
     * boolean类型数据的属性
     * \endif
     */
    BOOL_PROPERTY(0),
    /**
	 * \if English
	 * property of int data
	 * \else
     * int类型数据的属性
     * \endif
     */
    INT_PROPERTY(1),
    /**
	 * \if English
	 * property of float data
	 * \else
     * float类型数据的属性
     * \endif
     */
    FLOAT_PROPERTY(2),
    /**
	 * \if English
	 * 	Properties corresponding to the underlying structure type data
	 * \else
     * 对应底层结构体类型数据的属性
     */
    STRUCT_PROPERTY(3);

    private final int mValue;

    PropertyType(int value) {
        mValue = value;
    }

    /**
	 * \if English
	 * Get the index value corresponding to a specific property enumeration
     *
     * @return index value
	 * \else
     * 获取特定属性枚举对应的索引值
     *
     * @return 索引值
	 * \endif
     */
    public int value() {
        return mValue;
    }

    /**
     * \if English
     * Get the property value type enumeration corresponding to the specified index
     *
     * @param value index value
     * @return property enumeration
     * \else
     * 获取指定索引对应的属性值类型枚举
     *
     * @param value 索引值
     * @return 属性枚举
     * \endif
     */
    public static PropertyType get(int value) {
        for (int i = 0; i < values().length; i++) {
            if (values()[i].value() == value) {
                return values()[i];
            }
        }
        return null;
    }
}
