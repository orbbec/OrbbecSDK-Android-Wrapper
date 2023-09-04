package com.orbbec.obsensor.datatype;

import com.orbbec.obsensor.DataType;
import com.orbbec.internal.DataUtilities;

/**
 * \if English
 * Compression parameters
 * \else
 * 压缩参数
 * \endif
 */
public class CompressionParams extends DataType {
    //typedef struct  {
    //    int threshold;
    //}OBCompressionParams, ob_compression_params, OB_COMPRESSION_PARAMS;
    // sizeof(OBCompressionParams)
    private static final int COMPRESSION_PARAMS_BYTES = 4;

    private int mThreshold;

    /**
     * \if English
     * Setter, Lossy compression threshold, range [0~255], recommended value is 9, the higher the threshold, the higher the compression ratio.
     * \else
     * sertter 有损压缩阈值，范围 [0~255]，推荐值为 9，阈值越高压缩率越高。
     * \endif
     */
    public void setThreshold(int threshold) {
        mThreshold = threshold;
    }

    /**
     * \if English
     * Getter, Lossy compression threshold, range [0~255], recommended value is 9, the higher the threshold, the higher the compression ratio.
     * \else
     * Getter, 有损压缩阈值，范围 [0~255]，推荐值为 9，阈值越高压缩率越高。
     * \endif
     */
    public int getThreshold() {
        return mThreshold;
    }

    @Override
    public int BYTES() {
        return COMPRESSION_PARAMS_BYTES;
    }

    @Override
    protected boolean parseBytesImpl(byte[] bytes) {
        int offset = 0, length = Integer.BYTES;
        mThreshold = DataUtilities.bytesToInt(DataUtilities.subBytes(bytes, offset, length));
        return true;
    }

    @Override
    protected boolean wrapBytesImpl(byte[] bytes) {
        int offset = 0, length = Integer.BYTES;
        DataUtilities.appendBytes(DataUtilities.intToBytes(mThreshold), bytes, offset, length);
        return true;
    }

    @Override
    public String toString() {
        return "CompressionParams{" +
                "Threshold=" + mThreshold +
                '}';
    }
}
