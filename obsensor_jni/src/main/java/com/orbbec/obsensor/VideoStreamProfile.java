package com.orbbec.obsensor;

import com.orbbec.obsensor.datatype.CameraDistortion;
import com.orbbec.obsensor.datatype.CameraIntrinsic;

/**
 * \if English
 * Data stream configuration for Color, Depth, IR stream
 * \else
 * 数据流配置,用于Color, Depth, IR 流
 * \endif
 */
public class VideoStreamProfile extends StreamProfile {

    VideoStreamProfile(long handle) {
        super(handle);
        mHandle = handle;
    }

    /**
	 * \if English
	 * Get data stream width
     *
     * @return data stream width
	 * \else
     * 获取数据流宽
     *
     * @return 数据流宽
	 * \endif
     */
    public int getWidth() {
        throwInitializeException();
        return nGetWidth(mHandle);
    }

    /**
     * \if English
	 * Get data flow height
     *
     * @return data flow height
	 * \else
     * 获取数据流高
     *
     * @return 数据流高
	 * \endif
     */
    public int getHeight() {
        throwInitializeException();
        return nGetHeight(mHandle);
    }

    /**
	 * \if English
	 * Get data stream frame rate per second
     *
     * @return streaming frame rate
	 * \else
     * 获取数据流每秒帧率
     *
     * @return 数据流帧率
	 * \endif
     */
    public int getFps() {
        throwInitializeException();
        return nGetFps(mHandle);
    }

    /**
     * \if English
     * Get the intrinsic of the video stream
     *
     * @return the intrinsic of the stream
     * \else
     * 获取视频流内参
     *
     * @return 流内参
     * \endif
     */
    public CameraIntrinsic getIntrinsic() {
        throwInitializeException();
        CameraIntrinsic intrinsic = new CameraIntrinsic();
        nGetIntrinsic(mHandle, intrinsic.getBytes());
        return intrinsic;
    }

    /**
     * \if English
     * Get the distortion of the video stream
     *
     * @return the distortion of the stream
     * \else
     * 获取视频流畸变
     *
     * @return 流畸变
     * \endif
     */
    public CameraDistortion getDistortion() {
        throwInitializeException();
        CameraDistortion distortion = new CameraDistortion();
        nGetCameraDistortion(mHandle, distortion.getBytes());
        return distortion;
    }

    /**
	 * \if English
	 * resource release
	 * \else
     * 资源释放
	 * \endif
     */
    @Override
    public void close() {
        super.close();
    }

    private static native int nGetFps(long handle);

    private static native int nGetWidth(long handle);

    private static native int nGetHeight(long handle);

    private static native void nGetIntrinsic(long handle, byte[] intrinsic);

    private static native void nGetCameraDistortion(long handle, byte[] distortion);
}
