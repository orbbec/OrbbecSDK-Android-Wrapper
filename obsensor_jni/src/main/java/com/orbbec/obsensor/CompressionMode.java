package com.orbbec.obsensor;

/**
 * \if English
 * Compression mode
 * \else
 * 压缩模式
 * \endif
 */
public enum CompressionMode {
    OB_COMPRESSION_LOSSLESS(0),

    OB_COMPRESSION_LOSSY(1);

    private final int mValue;

    CompressionMode(int value) {
        mValue = value;
    }

    /**
	 * \if English
	 * Get the index corresponding to the compression mode
     *
     * @return index value
	 * \else
     * 获取压缩模式对应的索引
     *
     * @return 索引值
	 * \endif
     */
    public int value() {
        return mValue;
    }

    /**
	 * \if English
	 * Get the compression mode corresponding to the specified index
     *
     * @param value index value
     * @return compression mode
	 * \else
     * 获取指定索引对应的压缩模式
     *
     * @param value 索引值
     * @return 压缩模式
	 * \endif
     */
    public static CompressionMode get(int value) {
        for (int i = 0; i < values().length; i++) {
            if (values()[i].value() == value) {
                return values()[i];
            }
        }
        return null;
    }
}
