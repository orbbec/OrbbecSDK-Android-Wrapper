package com.orbbec.obsensor;

import android.util.Log;

import static com.orbbec.obsensor.SensorType.ACCEL;
import static com.orbbec.obsensor.SensorType.GYRO;

/**
 * \if English
 * Expose orbbec sensor function
 * \else
 * 公开orbbec传感器功能
 * \endif
 */
public class Sensor extends LobClass {
    private static final String TAG = "Sensor";

    private interface FrameCallbackImpl {
        void onFrame(long frame);
    }

    Sensor(long h) {
        mHandle = h;
    }

    /**
	 * \if English
	 * Get sensor type
     *
     * @return Sensor Type {@link SensorType}
	 * \else
     * 获取传感器类型
     *
     * @return 传感器类型 {@link SensorType}
	 * \endif
     */
    public SensorType getType() {
        throwInitializeException();
        return SensorType.get(nGetType(mHandle));
    }

    /**
	 * \if English
	 * Get the data stream configuration information supported by the sensor
     *
     * @return The list of data stream configuration information supported by the sensor {@link StreamProfileList}
	 * \else
     * 获取传感器支持的数据流配置信息
     *
     * @return 传感器支持的数据流配置信息列表 {@link StreamProfileList}
	 * \endif
     */
    public StreamProfileList getStreamProfileList() {
        throwInitializeException();
        long handle = nGetStreamProfileList(mHandle);
        return handle != 0 ? new StreamProfileList(handle) : null;
    }

    /**
	 * \if English
	 * Open the sensor data stream, it cannot be used with the pipeline at the same time. If the configuration in the configuration file is used to open the stream, the profile will pass null
     * (if there is no configuration file when the null is passed, the first configuration in the configuration file list will be used by default. )
     * <p>
     * Important: After the callback data frame is used up, Frame.close() must be called to release resources.
     *
     * @param profile  Data stream configuration {@link StreamProfile}
     * @param callback Data callback {@link FrameCallback}
	 * \else
     * 开启传感器数据流，不可与pipeline同时使用,如果使用配置文件中的配置开流，则profile传null
     * （如果在传null时又没有配置文件的情况下则默认使用配置文件列表中的第一个配置）
     * <p>
     * 重要：回调的数据帧在使用结束后，必须调用Frame.close()释放资源。
     *
     * @param profile  数据流配置 {@link StreamProfile}
     * @param callback 数据回调 {@link FrameCallback}
	 * \endif
     */
    public void start(StreamProfile profile, FrameCallback callback) {
        throwInitializeException();
        nStart(mHandle, (null == profile ? 0 : profile.getHandle()), new FrameCallbackImpl() {
            @Override
            public void onFrame(long frame) {
                if (callback != null) {
                    callback.onFrame(new Frame(frame));
                }
            }
        });
    }

    /**
	 * \if English
	 * turn off sensor data stream
	 * \else
     * 关闭传感器数据流
	 * \endif
     */
    public void stop() {
        throwInitializeException();
        nStop(mHandle);
    }

    /**
	 * \if English
	 * Dynamically switch resolutions
     *
     * @param profile resolution that needs to be switched {@link StreamProfile}
	 * \else
     * 动态切换分辨率
     *
     * @param profile 需要切换的分辨率 {@link StreamProfile}
	 * \endif
     */
    public void switchProfile(StreamProfile profile) {
        throwInitializeException();
        nSwitchProfile(mHandle, profile.getHandle());
    }

    /**
	 * \if English
	 * release sensor resources
	 * \else
     * 释放传感器资源
     * \endif
     */
    @Override
    public void close() {
        // do nothing
    }

    /**
     * Because {@link Device} hold all sensors and manage
     */
    /*package*/ void release() {
        throwInitializeException();
        Log.d(TAG, "close sensor(" + getType() + ") !");
        nDelete(mHandle);
        mHandle = 0;
    }

    private static native void nDelete(long handle);

    private static native int nGetType(long handle);

    private static native long nGetStreamProfileList(long handle);

    private static native void nSwitchProfile(long handle, long streamProfileHandle);

    private static native void nStart(long handle, long streamProfileHandle, FrameCallbackImpl callback);

    private static native void nStop(long handle);
}
