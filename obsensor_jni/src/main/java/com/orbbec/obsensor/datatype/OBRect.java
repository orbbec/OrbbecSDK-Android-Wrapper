package com.orbbec.obsensor.datatype;

import com.orbbec.obsensor.DataType;
import com.orbbec.internal.DataUtilities;

/**
 * \if English
 * Rectangle description
 * \else
 * 矩形描述
 * \endif
 */
public class OBRect extends DataType {
    /**
	 * \if English
	 * Origin coordinate X
	 * \else
     * 原点坐标x
     * \endif
     */
    private int mX;

    /**
	 * \if English
	 * Origin coordinate Y
	 * \else
     * 原点坐标y
     * \endif
     */
    private int mY;

    /**
	 * \if English
	 * rectangle width
	 * \else
     * 矩形宽度
     * \endif
     */
    private int mWidth;

    /**
	 * \if English
	 * rectangle height
	 * \else
     * 矩形高度
     * \endif
     */
    private int mHeight;

    /**
	 * \if English
	 * Get the x-coordinate of the origin of the rectangle
     *
     * @return returns the x-coordinate of the origin of the rectangle
	 * \else
     * 获取矩形原点x坐标
     *
     * @return 返回矩形原点x坐标
	 * \endif
     */
    public int getX() {
        throwInitializeException();
        return mX;
    }

    /**
	 * \if English
	 * Get the y-coordinate of the origin of the rectangle
     *
     * @return returns the y coordinate of the origin of the rectangle
	 * \else
     * 获取矩形原点y坐标
     *
     * @return 返回矩形原点y坐标
	 * \endif
     */
    public int getY() {
        throwInitializeException();
        return mY;
    }

    /**
	 * \if English
	 * get rectangle width
     *
     * @return returns the width of the rectangle
	 * \else
     * 获取矩形宽度
     *
     * @return 返回矩形宽度
	 * \endif
     */
    public int getWidth() {
        throwInitializeException();
        return mWidth;
    }

    /**
	 * \if English
	 * get rectangle height
     *
     * @return returns the height of the rectangle
	 * \else
     * 获取矩形高度
     *
     * @return 返回矩形高度
	 * \endif
     */
    public int getHeight() {
        throwInitializeException();
        return mHeight;
    }

    @Override
    public int BYTES() {
        return 16;
    }

    @Override
    protected boolean parseBytesImpl(byte[] bytes) {
        int offset = 0, length = Integer.BYTES;
        mX = DataUtilities.bytesToInt(DataUtilities.subBytes(bytes, offset, length));
        offset += length;
        mY = DataUtilities.bytesToInt(DataUtilities.subBytes(bytes, offset, length));
        offset += length;
        mWidth = DataUtilities.bytesToInt(DataUtilities.subBytes(bytes, offset, length));
        offset += length;
        mHeight = DataUtilities.bytesToInt(DataUtilities.subBytes(bytes, offset, length));
        return true;
    }

    @Override
    protected boolean wrapBytesImpl(byte[] bytes) {
        return true;
    }

    @Override
    public String toString() {
        return "OBRect{x:" + mX
                + ", y:" + mY
                + ", width:" + mWidth
                + ", height:" + mHeight + "}";
    }
}
