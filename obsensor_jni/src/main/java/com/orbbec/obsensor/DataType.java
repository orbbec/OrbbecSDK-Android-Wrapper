package com.orbbec.obsensor;

/**
 * \if English
 * Data type base class, used to represent the type of Native structure mapped to Java
 * \else
 * 数据类型基类，用于表示Native结构体映射到Java的类型
 * \endif
 */
public abstract class DataType {

    private byte[] mBytes;
    private boolean initialized;

    /**
	 * \if English
	 * Get the memory required by the Native structure
     *
     * @return The memory required by the Native structure
	 * \else
     * 获取Native结构体需要的内存
     *
     * @return Native结构体需要的内存
	 * \endif
     */
    protected byte[] getBytes() {
        if (null == mBytes) {
            mBytes = new byte[BYTES()];
        }
        return mBytes;
    }

    /**
	 * \if English
	 * Get the number of bytes occupied by the Native structure
     *
     * @return The number of bytes occupied by the Native structure
	 * \else
     * 获取Native结构体占用的字节数
     *
     * @return Native结构体占用的字节数
	 * \endif
     */
    public abstract int BYTES();

    /**
	 * \if English
	 * Parse the content of the Native structure and initialize the mapped Java class
     *
     * @return true, parsing succeeded; false , parsing failed
	 * \else
     * 解析Native结构体的内容，初始化映射的Java类
     *
     * @return true, 解析成功; false , 解析失败
	 * \endif
     */
    protected boolean parseBytes() {
        if (null == mBytes) {
            return false;
        }
        initialized = parseBytesImpl(mBytes);
        return initialized;
    }

    /**
	 * \if English
	 * Encapsulate the properties in the Java object as a Byte array to map to the structure corresponding to Native
     *
     * @return true, wrapping succeeded; false, wrapping failed
	 * \else
     * 将Java对象中的属性封装为Byte数组，从而映射到Native对应的结构体
     *
     * @return true, 包装成功; false, 包装失败
	 * \endif
     */
    protected boolean wrapBytes() {
        mBytes = new byte[BYTES()];
        initialized = wrapBytesImpl(mBytes);
        return initialized;
    }

    /**
	 * \if English
	 * Specific class implementation parsing, pay attention to the internal order and length of the structure
     *
     * @param bytes byte array to be parsed
     * @return Parsing result, false: parsing failed, true: parsing succeeded
	 * \else
     * 具体类实现解析, 注意结构体内部顺序和长度
     *
     * @param bytes 待解析的字节数组
     * @return 解析结果，false：解析失败，true：解析成功
	 * \endif
     */
    protected abstract boolean parseBytesImpl(byte[] bytes);

    /**
	 * \if English
	 * Specific class attribute encapsulation, pay attention to the internal order and length of the structure
     *
     * @param bytes The generated byte array to be encapsulated
     * @return Encapsulation result, false: Encapsulation failed, true: Encapsulation succeeded
	 * \else
     * 具体类属性封装，注意结构体内部顺序和长度
     *
     * @param bytes 待封装的生成的字节数组
     * @return 封装结果，false：封装失败，true：封装成功
	 * \endif
     */
    protected abstract boolean wrapBytesImpl(byte[] bytes);

    /**
	 * \if English
	 * Check whether the corresponding Java class is initialized successfully
     *
     * @return Whether the Java class is initialized, false: failure, true: success
	 * \else
     * 检查对应的Java类是否初始化成功
     *
     * @return Java类是否初始化，false：失败，true：成功
	 * \endif
     */
    public boolean checkInitialized() {
        return initialized;
    }

    /**
	 * \if English
	 * 	If not initialized, throw an exception
	 * \else
     * 如果未初始化，抛出异常
     * \endif
     */
    protected void throwInitializeException() {
        if (!checkInitialized()) {
            throw new OBException(getClass().getName() + " uninitialized!");
        }
    }
}
