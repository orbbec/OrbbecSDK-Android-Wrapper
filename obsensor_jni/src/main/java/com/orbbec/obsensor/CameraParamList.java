package com.orbbec.obsensor;

import com.orbbec.obsensor.datatype.CameraDistortion;
import com.orbbec.obsensor.datatype.CameraIntrinsic;
import com.orbbec.obsensor.datatype.D2CTransform;

/**
 * \if English
 * Camera parameter list
 * \else
 * 相机参数列表
 * \endif
 */
public class CameraParamList extends LobClass {

    CameraParamList(long handle) {
        mHandle = handle;
    }

    /**
	 * \if English
	 * Get the number of camera parameters
     *
     * @return The number of camera parameters
	 * \else
     * 获取相机参数数量
     *
     * @return 相机参数数量
	 * \endif
     */
    public int getCameraParamCount() {
        throwInitializeException();
        return nGetCameraParamCount(mHandle);
    }

    /**
	 * \if English
	 * Obtain camera parameters according to the specified index in the camera parameter list
     *
     * @param index index value
     * @return Camera parameter {@link CameraParam}
	 * \else
     * 根据相机参数列表中指定索引，获取相机参数
     *
     * @param index 索引值
     * @return 相机参数 {@link CameraParam}
	 * \endif
     */
    public CameraParam getCameraParam(int index) {
        throwInitializeException();
        CameraIntrinsic depthIntrinsic = new CameraIntrinsic();
        CameraIntrinsic colorIntrinsic = new CameraIntrinsic();
        CameraDistortion depthDistortion = new CameraDistortion();
        CameraDistortion colorDistortion = new CameraDistortion();
        D2CTransform d2CTransform = new D2CTransform();
        CameraParam params = new CameraParam();

        nGetCameraParam(mHandle, index, depthIntrinsic.getBytes(), colorIntrinsic.getBytes(),
                depthDistortion.getBytes(), colorDistortion.getBytes(), d2CTransform.getBytes(), params);

        if (depthIntrinsic.parseBytes() && colorIntrinsic.parseBytes()
                && depthDistortion.parseBytes() && colorDistortion.parseBytes() && d2CTransform.parseBytes()) {
            params.mDepthIntrinsic = depthIntrinsic;
            params.mColorIntrinsic = colorIntrinsic;
            params.mDepthDistortion = depthDistortion;
            params.mColorDistortion = colorDistortion;
            params.mTransform = d2CTransform;

            return params;
        }
        return null;
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
        throwInitializeException();
        nDelete(mHandle);
        mHandle = 0;
    }

    private static native int nGetCameraParamCount(long handle);

    private static native void nGetCameraParam(long handle, int index, byte[] depthIntr, byte[] colorIntr, byte[] depthDisto, byte[] colorDisto, byte[] trans, CameraParam cameraParam);

    private static native void nDelete(long handle);
}
