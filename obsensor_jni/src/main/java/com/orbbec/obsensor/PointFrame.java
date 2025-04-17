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

    /**
	 * \if English
     * Get the point coordinate value scale of the points frame. The point position value of the points frame is multiplied by the scale to give a
     * position value in millimeters. For example, if scale=0.1, the x-coordinate value of a point is x = 10000, which means that the actual x-coordinate value
     * = x*scale = 10000*0.1 = 1000mm.
     *
	 * @return float The coordinate value scale
	 * \else
     * 获取点云帧的点坐标值缩放比例，点云帧中的点坐标值乘以缩放比例可以得到以毫米为单位的坐标值。例如，如果缩放比例=0.1，则点坐标值为x=10000，则表示实际x坐标值为x*scale=10000*0.1=1000mm。
     *
     * @return float 坐标值缩放比例
	 * \endif
     */
    public float getCoordinateValueScale() {
        throwInitializeException();
        return nGetCoordinateValueScale(mHandle);
    }

    private static native void nGetPointCloudData(long handle, float[] data);

    private static native float nGetCoordinateValueScale(long handle);
}
