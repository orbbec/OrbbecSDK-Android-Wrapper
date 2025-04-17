package com.orbbec.obsensor;

import com.orbbec.internal.OBLocalUtils;
import com.orbbec.obsensor.types.DeviceInfo;
import com.orbbec.obsensor.types.MediaState;
import com.orbbec.obsensor.types.MediaType;
import com.orbbec.obsensor.types.CameraParam;

/**
 * \if English
 * Data Playback
 * \else
 * 数据回放器
 * \endif
 */
public class Playback extends LobClass {
    private static final String TAG = "Playback";

    private MediaStateCallbackImpl mMediaStateCallbackImpl = new MediaStateCallbackImpl() {
        @Override
        public void onState(int state) {
            if (null != mMediaStateCallback) {
                mMediaStateCallback.onState(MediaState.get(state));
            }
        }
    };

    private MediaStateCallback mMediaStateCallback;

    /**
	 * \if English
	 * Create playback
     *
     * @param filePath playback file path
	 * \else
     * 创建回放器
     *
     * @param filePath 回放文件路径
	 * \endif
     */
    public Playback(String filePath) {
        OBLocalUtils.checkFileAndThrow(filePath, "Create Pipeline failed");
        mHandle = nCreatePlayback(filePath);
        nSetMediaStateCallback(mHandle, mMediaStateCallbackImpl);
    }

    /**
	 * \if English
	 * Create a callback with the corresponding handle
     *
     * @param handle callback handle
	 * \else
     * 通过对应的句柄创建回放器
     *
     * @param handle 回放器句柄
	 * \endif
     */
    Playback(long handle) {
        mHandle = handle;
        nSetMediaStateCallback(mHandle, mMediaStateCallbackImpl);
    }

    /**
	 * \if English
	 * Start playback, playback data is returned from the callback
     *
     * @param callback  Callback for playback data  {@link PlaybackCallback}
     * @param mediaType Type of playback data {@link MediaType}
	 * \else
     * 开启回放，回放数据从回调中返回
     *
     * @param callback  回放数据的回调 {@link PlaybackCallback}
     * @param mediaType 回放数据的类型 {@link MediaType}
	 * \endif
     */
    public void start(PlaybackCallback callback, MediaType mediaType) {
        throwInitializeException();
        nStart(mHandle, new PlaybackCallbackImpl() {
            @Override
            public void onPlayback(long frame) {
                if (callback != null) {
                    callback.onPlayback(new Frame(frame));
                }
            }
        }, mediaType.value());
    }

    /**
	 * \if English
	 * stop playback
	 * \else
     * 停止回放
     * \endif
     */
    public void stop() {
        throwInitializeException();
        nStop(mHandle);
    }

    /**
	 * \if English
	 * Get device information
     *
     * @return returns device information {@link DeviceInfo}
	 * \else
     * 获取设备信息
     *
     * @return 返回设备信息 {@link DeviceInfo}
	 * \endif
     */
    public DeviceInfo getDeviceInfo() {
        throwInitializeException();
        return nGetDeviceInfo(mHandle);
    }

    /**
	 * \if English
	 * Get the intrinsic and extrinsic parameter information in the recording file
     *
     * @return returns internal and external parameter information {@link CameraParam}
	 * \else
     * 获取录制文件内的内外参信息
     *
     * @return 返回的内外参信息 {@link CameraParam}
	 * \endif
     */
//    public CameraParam getCameraParam() {
//        throwInitializeException();
//        CameraIntrinsic depthIntrinsic = new CameraIntrinsic();
//        CameraIntrinsic colorIntrinsic = new CameraIntrinsic();
//        CameraDistortion depthDistortion = new CameraDistortion();
//        CameraDistortion colorDistortion = new CameraDistortion();
//        Extrinsic d2CTransform = new Extrinsic();
//        CameraParam params = new CameraParam();
//
//        nGetCameraParam(mHandle, depthIntrinsic.BYTES(), colorIntrinsic.BYTES(),
//                depthDistortion.BYTES(), colorDistortion.BYTES(), d2CTransform.BYTES(), params);
//
//        if (depthIntrinsic.parseBytes() && colorIntrinsic.parseBytes()
//                && depthDistortion.parseBytes() && colorDistortion.parseBytes() && d2CTransform.parseBytes()) {
//            params.mDepthIntrinsic = depthIntrinsic;
//            params.mColorIntrinsic = colorIntrinsic;
//            params.mDepthDistortion = depthDistortion;
//            params.mColorDistortion = colorDistortion;
//            params.mTransform = d2CTransform;
//
//            return params;
//        }
//        return null;
//    }

    /**
	 * \if English
     * Set media state callback
     *
     * @param callback media state callback
	 * \else
     * 设置回放状态回调
     *
     * @param callback 回放状态回调
	 * \endif
     */
    public void setMediaStateCallback(MediaStateCallback callback) {
        mMediaStateCallback = callback;
    }

    /**
	 * \if English
	 * Release callback resource 
	 * \else
     * 释放回放器资源
     * \endif
     */
    @Override
    public void close() {
        throwInitializeException();
        nSetMediaStateCallback(mHandle, null);
        nDelete(mHandle);
        mHandle = 0;
    }

    private interface PlaybackCallbackImpl {
        void onPlayback(long frame);
    }

    private interface MediaStateCallbackImpl {
        void onState(int state);
    }

    private static native long nCreatePlayback(String filePath);

    private static native void nStart(long handle, PlaybackCallbackImpl callback, int mediaType);

    private static native void nStop(long handle);

    private static native DeviceInfo nGetDeviceInfo(long handle);

    private static native void nGetCameraParam(long handle, byte[] depthIntr, byte[] colorIntr, byte[] depthDisto, byte[] colorDisto, byte[] trans, CameraParam cameraParam);

    private static native void nSetMediaStateCallback(long handle, MediaStateCallbackImpl callback);

    private static native void nDelete(long handle);
}
