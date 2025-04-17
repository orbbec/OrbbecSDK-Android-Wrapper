package com.orbbec.obsensor;

import com.orbbec.obsensor.types.PixelType;

/**
 * \if English
 * Base class for ColorFrame, DepthFrame, IRFrame
 * \else
 * ColorFrame、DepthFrame、IRFrame的基类
 * \endif
 */
public class VideoFrame extends Frame {
    VideoFrame(long handle) {
        super(handle);
    }

    /**
	 * \if English
	 * Get the height of the data frame
     *
     * @return height of the data frame
	 * \else
     * 获取数据帧的宽
     *
     * @return 数据帧的宽
	 * \endif
     */
    public int getWidth() {
        throwInitializeException();
        return nGetWidth(mHandle);
    }

    /**
	 * \if English
	 * Get the width of the data frame
     *
     * @return width of the data frame
	 * \else
     * 获取数据帧的高
     *
     * @return 数据帧的高
	 * \endif
     */
    public int getHeight() {
        throwInitializeException();
        return nGetHeight(mHandle);
    }

    /**
     * \if English
     * Get the Pixel Type object
     * Usually used to determine the pixel type of depth frame (depth, disparity, raw phase, etc.)
     *
     * @attention Always return OB_PIXEL_UNKNOWN for non-depth frame currently
     *
     * @return OBPixelType
     * \else
     * 获取像素类型
     * 通常用于判断深度帧的像素类型（深度、视差、原始相位等）
     *
     * @attention 目前非深度帧都返回OB_PIXEL_UNKNOWN
     *
     * @return OBPixelType
     * \endif
     */
    public PixelType getPixelType() {
        throwInitializeException();
        return PixelType.get(nGetPixelType(mHandle));
    }

    /**
	 * \if English
	 * Get the effective number of pixels (such as Y16 format frame, but only the lower 10 bits are valid bits, and the upper 6 bits are filled with 0)
     * Only valid for Y8/Y10/Y11/Y12/Y14/Y16 format
     *
     * @return Returns the effective number of pixels in pixels, or 0 if it is an unsupported format
	 * \else
     * 获取像素有效位数（如Y16格式帧，每个像素占16bit，但实际只有低10位是有效位，高6位填充0）
     * 仅对Y8/Y10/Y11/Y12/Y14/Y16格式有效
     *
     * @return 返回像素有效位数，如果是不支持的格式，返回0
	 * \endif
     */
    public int getPixelAvailableBitSize() {
        throwInitializeException();
        return nGetPixelAvailableBitSize(mHandle);
    }

    private static native int nGetWidth(long handle);

    private static native int nGetHeight(long handle);

    private static native int nGetPixelType(long handle);

    private static native int nGetPixelAvailableBitSize(long handle);
}
