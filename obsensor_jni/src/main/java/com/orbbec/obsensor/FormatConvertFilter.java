package com.orbbec.obsensor;

/**
 * \if English
 * FormatConvertFilter For data stream format conversion
 * \else
 * FormatConvertFilter 用于数据流格式转换
 * \endif
 **/
public class FormatConvertFilter extends Filter {
    /**
	 * \if English
	 * Create a format conversion filter
	 * \else
     * 创建格式转换filter
     * \endif
     */
    public FormatConvertFilter() {
        super(nCreate());
    }

    /**
	 * \if English
	 * Set the data stream conversion format
     *
     * @param type Data Stream Transformation Format
	 * \else
     * 设置数据流转换格式
     *
     * @param type 数据流转换格式
	 * \endif
     */
    public void setFormatType(FormatConvertType type) {
        throwInitializeException();
        nSetFormatConvertType(type.value(), mHandle);
    }

    private static native long nCreate();

    private static native void nSetFormatConvertType(int type, long filterPtr);
}
