package com.orbbec.obsensor;

/**
 * \if English
 * Point cloud frame
 * \else
 * 点云帧数据
 * \endif
 */
public class PointFrame extends Frame {
    PointFrame(long handle) {
        super(handle);
    }

    /**
	 * \if English
	 * Return point cloud frame data
     *
     * @param data Data returned by point cloud frame
	 * \else
     * 返回点云帧数据
     *
     * @param data 点云帧返回的数据
	 * \endif
     */
    public void getPointCloudData(float[] data) {
        throwInitializeException();
        nGetPointCloudData(mHandle, data);
    }

    private static native void nGetPointCloudData(long handle, float[] data);
}
