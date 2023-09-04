package com.orbbec.obsensor;

/**
 * \if English
 * Data frame set, including 0~N data frames
 * \else
 * 数据帧集，包含0~N个数据帧
 * \endif
 */
public class FrameSet extends Frame {

    FrameSet(long handle) {
        super(handle);
    }

    /**
	 * \if English
	 * Get the number of frames in a dataframe set
     *
     * @return number of frames
	 * \else
     * 获取数据帧集包含帧的数量
     *
     * @return 帧的数量
	 * \endif
     */
    public int getFrameCount() {
        throwInitializeException();
        return nGetFrameCount(mHandle);
    }

    /**
	 * \if English
	 * get depth data frame
     *
     * @return depth data frame {@link DepthFrame}
	 * \else
     * 获取深度数据帧
     *
     * @return 深度数据帧 {@link DepthFrame}
	 * \endif
     */
    public DepthFrame getDepthFrame() {
        throwInitializeException();
        long handle = nGetDepthFrame(mHandle);
        return handle != 0 ? new DepthFrame(handle) : null;
    }

    /**
	 * \if English
	 * Get color data frame
     *
     * @return color data frame {@link ColorFrame}
	 * \else
     * 获取彩色数据帧
     *
     * @return 彩色数据帧 {@link ColorFrame}
     */
    public ColorFrame getColorFrame() {
        throwInitializeException();
        long handle = nGetColorFrame(mHandle);
        return handle != 0 ? new ColorFrame(handle) : null;
    }

    /**
	 * \if English
	 * Get infrared data frame
     *
     * @return Infrared data frame {@link IRFrame}
	 * \else
     * 获取红外数据帧
     *
     * @return 红外数据帧 {@link IRFrame}
	 * \endif
     */
    public IRFrame getIrFrame() {
        throwInitializeException();
        long handle = nGetInfraredFrame(mHandle);
        return handle != 0 ? new IRFrame(handle) : null;
    }

    /**
	 * \if English
	 * Get point cloud data from a collection of frames
     *
     * @return Return point cloud frame {@link PointFrame}
	 * \else
     * 从帧集合中获取点云数据
     *
     * @return 返回点云帧 {@link PointFrame}
	 * \endif
     */
    public PointFrame getPointFrame() {
        throwInitializeException();
        long handle = nGetPointFrame(mHandle);
        return handle != 0 ? new PointFrame(handle) : null;
    }

    public <T extends Frame> T getFrame(FrameType frameType) {
        throwInitializeException();
        long handle = nGetFrame(mHandle, frameType.value());
        if (handle == 0) {
            return null;
        }

        switch (frameType) {
            case COLOR:
                return (T) new ColorFrame(handle);
            case IR_LEFT:  // follow
            case IR_RIGHT: // follow
            case IR:
                return (T) new IRFrame(handle);
            case DEPTH:
                return (T) new DepthFrame(handle);
            case POINTS:
                return (T) new PointFrame(handle);
            default:
                // handle 内存泄漏
                throw new RuntimeException("getFrame failed. not support frameType: " + frameType);
        }
    }

    /**
	 * \if English
	 * release dataframe set resources
	 * \else
     * 释放数据帧集资源
     * \endif
     */
    @Override
    public void close() {
        throwInitializeException();
        nDelete(mHandle);
        mHandle = 0;
    }

    private static native int nGetFrameCount(long handle);

    private static native long nGetDepthFrame(long handle);

    private static native long nGetColorFrame(long handle);

    private static native long nGetInfraredFrame(long handle);

    private static native long nGetFrame(long handle, int frameType);

    private static native long nGetPointFrame(long handle);

    private static native void nDelete(long handle);
}
