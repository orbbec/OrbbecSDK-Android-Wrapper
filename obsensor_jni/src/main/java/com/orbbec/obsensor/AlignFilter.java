package com.orbbec.obsensor;

public class AlignFilter extends Filter {

    /**
     * \if English
     * Create a align filter.
     * \else
     * 创建一个对齐滤波器
     * \endif
     */
    public AlignFilter(StreamType streamType) {
        super(nCreate(streamType.value()));
    }

    /**
     * \if English
     * Get the algin stream type.
     * \else
     * 获取对齐的流类型
     * \endif
     */
    public int getAlignStreamType() {
        return nGetAlignStreamType(mHandle);
    }

    private static native long nCreate(int streamType);

    private static native int nGetAlignStreamType(long handle);
}
