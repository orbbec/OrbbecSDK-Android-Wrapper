package com.orbbec.obsensor;

import com.orbbec.obsensor.datatype.CompressionParams;

public class CompressionFilter extends Filter {

    /**
     * \if English
     * Create compression filter
     *
     * @return compression filter object
     * \else
     * 创建compression filter
     *
     * @return 压缩对象
     * \endif
     */
    public CompressionFilter() {
        super(nCreate());
    }

    /**
     * \if English
     * Set compression parameters
     *
     * @param mode Compression mode {@link CompressionMode#OB_COMPRESSION_LOSSLESS} or {@link CompressionMode#OB_COMPRESSION_LOSSY}
     * @param params Compression params, struct ob_compression_params, when mode is OB_COMPRESSION_LOSSLESS, params is NULL
     * \else
     * 设置压缩类型
     *
     * @param mode 压缩模式{@link CompressionMode#OB_COMPRESSION_LOSSLESS} or {@link CompressionMode#OB_COMPRESSION_LOSSY}
     * @param params 压缩参数，结构体 ob_compression_params，当mode为OB_COMPRESSION_LOSSLESS时，params为NULL
     * \endif
     */
    public void setCompressionParams(CompressionMode mode, CompressionParams params) {
        throwInitializeException();
        boolean result = params.wrapBytes();
        if (result) {
            nSetCompressionParams(mHandle, mode.value(), params.getBytes());
        } else {
            throw new OBException("setCompressionParams wrap bytes error!");
        }
    }

    private static native long nCreate();

    private static native void nSetCompressionParams(long handle, int mode, byte[] paramBytes);
}
