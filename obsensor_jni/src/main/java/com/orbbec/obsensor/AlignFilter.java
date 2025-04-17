package com.orbbec.obsensor;

import com.orbbec.obsensor.types.StreamType;

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
    public StreamType getAlignStreamType() {
        throwInitializeException();
        int value = (int) getConfigValue("AlignType");
        return StreamType.get(value);
    }

    private void setMatchTargetResolution(boolean state) {
        throwInitializeException();
        setConfigValue("MatchTargetRes", state ? 1 : 0);
    }

    private static native long nCreate(int type);

    private static native int nGetAlignStreamType(long handle);
}
