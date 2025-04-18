package com.orbbec.obsensor;

import com.orbbec.internal.OBLocalUtils;
import com.orbbec.obsensor.types.AlignMode;
import com.orbbec.obsensor.types.CalibrationParam;
import com.orbbec.obsensor.types.CameraParam;
import com.orbbec.obsensor.types.SensorType;

/**
 * \if English
 * pipeline: used to control data, it conflicts with the start method of the Sensor class, and the start method cannot be used to start the data stream at the same time. Additional operations can be extended, such as setting high-level interfaces such as data frame synchronization
 * \else
 * pipeline：用于控制数据，与Sensor类start方法冲突，不可以同时使用start方法开启数据流。可扩展附加操作，例如设置数据帧同步等高级接口
 * \endif
 */
public class Pipeline extends LobClass {


    private interface FrameSetCallbackImpl {
        void onFrameSet(long frameSet);
    }

    /**
     * \if English
     * Use the specified device to create a Pipeline. To create a Pipeline through this interface, you need to create the corresponding device first.
     *
     * @see DeviceList#getDevice(int)
     * \else
     * 使用指定设备，创建Pipeline,通过该接口创建Pipeline需要先创建对应的设备
     * @see DeviceList#getDevice(int)
     * \endif
     */
    public Pipeline(Device device) {
        mHandle = nCreateWithDevice(device.getHandle());
    }

    /**
     * \if English
     * Open the data stream of the specified configuration. If you need to use the configuration in the configuration file to open the stream, pass null to config
     * (if there is no configuration file in the case of config passing null, the first one in the open flow configuration list is used by default to open the stream. )
     * <p>
     * Note: If the pipeline is created by playback file, it means start playback, at this time, config can pass null
     *
     * @param config pipeline configuration {@link Config}
     *               \else
     *               开启指定配置的数据流，如果需要使用配置文件中的配置进行开流，则config传null
     *               （在config传null的情况下没有配置文件时则默认使用开流配置列表中的第一个进行开流）
     *               <p>
     *               注：如果pipeline是通过回放文件创建，则表示开始回放,此时config传null即可
     * @param config pipeline配置 {@link Config}
     *               \endif
     */
    public void start(Config config) {
        throwInitializeException();
        nStartWithConfig(mHandle, (null == config ? 0 : config.getHandle()));
    }

    /**
     * \if English
     * Open the data stream of the specified configuration, and set the callback of the data frame set. If you need to use the configuration in the configuration file to open the stream, pass null to config
     * (if there is no configuration file in the case of config passing null, the open stream configuration list is used by default. the first to open flow)
     * <p>
     * Note: If the pipeline is created by the playback file, it means that the playback starts in the callback mode. At this time, config can pass null.
     * <p>
     * Important: After the callback data frame is used up, FrameSet#close() and Frame#close() must be called to release resources.
     *
     * @param config   pipeline configuration {@link Config}
     * @param callback Data frame set callback {@link FrameSetCallback}
     *                 \else
     *                 开启指定配置的数据流，并设置数据帧集回调,如果需要使用配置文件中的配置进行开流，则config传null
     *                 （在config传null的情况下没有配置文件时则默认使用开流配置列表中的第一个进行开流）
     *                 <p>
     *                 注：如果pipeline是通过回放文件创建，则表示以回调方式开始回放,此时config传null即可
     *                 <p>
     *                 重要：回调的数据帧在使用结束后，必须调用{@link FrameSet#close()}和{@link Frame#close()}释放资源。
     * @param config   pipeline配置 {@link Config}
     * @param callback 数据帧集回调 {@link FrameSetCallback}
     *                 \endif
     */
    public void start(Config config, FrameSetCallback callback) {
        throwInitializeException();
        nStartWithCallback(mHandle, (null == config ? 0 : config.getHandle()), new FrameSetCallbackImpl() {

            @Override
            public void onFrameSet(long frameSet) {
                if (callback != null) {
                    callback.onFrameSet(new FrameSet(frameSet));
                }
            }
        });
    }

    /**
     * \if English
     * stop data stream
     * \else
     * 关闭数据流
     * \endif
     */
    public void stop() {
        throwInitializeException();
        nStop(mHandle);
    }

    /**
     * \if English
     * Query method to obtain data frame set
     * Important: frameset must be called after the data frame is used, Close() and frame Close() releases resources
     *
     * @param timeoutMilliseconds wait timeout
     * @param timeoutMilliseconds 等待超时时长
     * @return Data frameset {@link FrameSet}
     * \else
     * 查询方式获取数据帧集
     * 重要：数据帧在使用结束后，必须调用FrameSet.close()和Frame.close()释放资源。
     * @return 数据帧集 {@link FrameSet}
     * \endif
     */
    public FrameSet waitForFrameSet(long timeoutMilliseconds) {
        throwInitializeException();
        long handle = nWaitForFrameSet(mHandle, timeoutMilliseconds);
        if (0 == handle) {
            return null;
        }
        return new FrameSet(handle);
    }

    /**
     * \if English
     * Get the current pipeline configuration, which is configured by the start method
     *
     * @return pipeline configuration {@link Config}
     * \else
     * 获取当前pipeline配置，该配置通过start方法配置的
     * @return pipeline配置 {@link Config}
     * \endif
     */
    public Config getConfig() {
        throwInitializeException();
        long handle = nGetConfig(mHandle);
        if (0 == handle) {
            return null;
        }
        return new Config(handle);
    }

    /**
     * \if English
     * Enable frame sync
     * \else
     * 开启帧同步
     * \endif
     */
    public void enableFrameSync() {
        throwInitializeException();
        nEnableFrameSync(mHandle);
    }

    /**
     * \if English
     * disable frame sync
     * \else
     * 关闭帧同步
     * \endif
     */
    public void disableFrameSync() {
        throwInitializeException();
        nDisableFrameSync(mHandle);
    }

    /**
     * \if English
     * Get the dataflow configuration supported by the specified sensor type
     *
     * @param sensorType Sensor Type{@link SensorType}
     * @param sensorType 传感器类型 {@link SensorType}
     * @return stream profile list {@link StreamProfileList}
     * \else
     * 获取指定传感器类型支持的数据流配置
     * @return 数据流配置列表 {@link StreamProfileList}
     * \endif
     */
    public StreamProfileList getStreamProfileList(SensorType sensorType) {
        throwInitializeException();
        long handle = nGetStreamProfileList(mHandle, sensorType.value());
        return handle != 0 ? new StreamProfileList(handle) : null;
    }

    /**
     * \if English
     * Dynamically switch the corresponding config configuration
     *
     * @param config pipeline configuration {@link Config}
     *               \else
     *               动态切换对应的config配置
     * @param config pipeline的配置 {@link Config}
     */
    public void switchConfig(Config config) {
        throwInitializeException();
        nSwitchConfig(mHandle, config.getHandle());
    }

    /**
     * \if English
     * Returns a list of D2C-enabled depth sensor resolutions corresponding to the input color sensor resolution
     *
     * @param colorProfile Input color sensor resolution  {@link StreamProfile}
     * @param mode         The align mode of D2C {@link AlignMode}
     * @param colorProfile 输入的Color Sensor的分辨率 {@link StreamProfile}
     * @param mode         D2C模式 {@link AlignMode}
     * @return a configuration list of matching depths
     * \else
     * 返回与输入的彩色传感器分辨率对应的支持D2C的深度传感器分辨率列表
     * @return 匹配的深度的配置列表
     * \endif
     */
    public StreamProfileList getD2CDepthProfileList(StreamProfile colorProfile, AlignMode mode) {
        throwInitializeException();
        long handle = nGetD2CDepthProfileList(mHandle, colorProfile.getHandle(), mode.value());
        return handle != 0 ? new StreamProfileList(handle) : null;
    }

    /**
     * \if English
     * Get the camera parameters after D2C. When the pipeline is created by the playback file, the camera internal parameters of the playback device are obtained
     *
     * @return Returns the aligned camera internal parameter {@link CameraParam}
     * \else
     * 获取D2C后的相机参数，当pipeline通过回放文件创建时，则获取的是回放设备的相机内参
     * @return 返回对齐后的相机内参 {@link CameraParam}
     * \endif
     */
    @Deprecated
    public CameraParam getCameraParam() {
        throwInitializeException();

        CameraParam params = new CameraParam();
        nGetCameraParam(mHandle, params.BYTES());
        boolean result = params.parseBytes();
        if (!result) {
            throw new OBException("getCameraParam parse bytes error!");
        }
        return params;
    }

    /**
     * \if English
     *
     * @param colorWidth  Width of color resolution
     * @param colorHeight High of color resolution
     * @param depthWidth  Width of depth resolution
     * @param depthHeight High of depth resolution
     * @return OBCameraParam returns camera parameters
     * \else
     * TODO lumiaozi
     * \endif
     * @brief Get camera parameters by entering color and depth resolution
     * @attention If D2C is enabled, it will return the camera parameters after D2C, if not, it will return to the default parameters
     */
    @Deprecated
    public CameraParam getCameraParamWithProfile(int colorWidth, int colorHeight, int depthWidth, int depthHeight) {
        throwInitializeException();

        CameraParam params = new CameraParam();
        nGetCameraParamWithProfile(mHandle, colorWidth, colorHeight, depthWidth, depthHeight, params.BYTES());
        boolean result = params.parseBytes();
        if (!result) {
            throw new OBException("getCameraParamWithProfile parse bytes error!");
        }
        return params;
    }

    /**
     * \if English
     *
     * @param config The pipeline configuration
     * @param config pipeline配置
     * @return CalibrationParam The calibration parameters
     * \else
     * @return CalibrationParam 校准参数
     * \endif
     * @brief Get device calibration parameters with the specified configuration
     * @brief 获取指定配置的设备校准参数
     */
    @Deprecated
    public CalibrationParam getCalibrationParam(Config config) {
        throwInitializeException();

        CalibrationParam params = new CalibrationParam();
        nGetCalibrationParam(mHandle, config.getHandle(), params.BYTES());
        boolean result = params.parseBytes();
        if (!result) {
            throw new OBException("getCalibrationParam parse bytes error!");
        }
        return params;
    }

    /**
     * \if English
     * pipeline resource release
     * \else
     * Pipeline资源释放
     * \endif
     */
    @Override
    public void close() {
        throwInitializeException();
        nDelete(mHandle);
        mHandle = 0;
    }

    private static native long nCreateWithDevice(long deviceHandle);

    private static native void nDelete(long handle);

    private static native void nStartWithConfig(long handle, long configHandle);

    private static native void nStartWithCallback(long handle, long configHandle, FrameSetCallbackImpl callback);

    private static native void nStop(long handle);

    private static native long nGetConfig(long handle);

    private static native long nGetStreamProfileList(long handle, int sensorType);

    private static native long nWaitForFrameSet(long handle, long timeoutMilliseconds);

    private static native void nEnableFrameSync(long handle);

    private static native void nDisableFrameSync(long handle);

    private static native void nSwitchConfig(long handle, long configHandle);

    private static native long nGetD2CDepthProfileList(long handle, long colorProfileHandle, int mode);

    private static native void nGetCameraParam(long handle, byte[] cameraParamBytes);

    private static native void nGetCameraParamWithProfile(long handle, int colorWidth, int colorHeight, int depthWidth, int depthHeight, byte[] cameraParamBytes);

    private static native void nGetCalibrationParam(long handle, long configHandle, byte[] CalibrationParamBytes);
}
