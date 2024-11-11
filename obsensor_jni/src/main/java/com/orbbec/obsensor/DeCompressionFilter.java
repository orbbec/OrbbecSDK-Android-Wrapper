package com.orbbec.obsensor;

public class DeCompressionFilter extends Filter {

    /**
     * \if English
     * Create decompression filter
     *
     * @return decompression filter object
     * \else
     * 创建解压缩过滤器
     *
     * @return 解压缩过滤器
     * \endif
     */
    public DeCompressionFilter() {
        super(nCreate());
    }

    private static native long nCreate();
}
