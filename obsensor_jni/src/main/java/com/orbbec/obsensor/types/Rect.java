package com.orbbec.obsensor.types;

/**
 * \if English
 * Rectangle
 * \else
 * 矩形结构
 * \endif
 */
public class Rect {
    /**
     * \if English
     * Origin coordinate X
     * \else
     * 原点坐标x
     * \endif
     */
    @StructField(offset = 0, size = 4)
    private long mX;
    /**
     * \if English
     * Origin coordinate Y
     * \else
     * 原点坐标y
     * \endif
     */
    @StructField(offset = 4, size = 4)
    private long mY;
    /**
     * \if English
     * rectangle width
     * \else
     * 矩形宽度
     * \endif
     */
    @StructField(offset = 8, size = 4)
    private long mWidth;
    /**
     * \if English
     * rectangle height
     * \else
     * 矩形高度
     * \endif
     */
    @StructField(offset = 12, size = 4)
    private long mHeight;

    private byte[] mBytes;

    public byte[] BYTES() {
        if (mBytes == null) {
            mBytes = new byte[16];
        }
        return mBytes;
    }
}
