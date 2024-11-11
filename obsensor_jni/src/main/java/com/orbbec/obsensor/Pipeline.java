package com.orbbec.obsensor;

import android.util.Log;

import com.orbbec.internal.DataUtilities;
import com.orbbec.obsensor.datatype.CameraDistortion;
import com.orbbec.obsensor.datatype.CameraIntrinsic;
import com.orbbec.obsensor.datatype.D2CTransform;
import com.orbbec.obsensor.datatype.OBRect;
import com.orbbec.internal.OBLocalUtils;

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
     * Use the playback file to create a pipeline object
     *
     * @param playbackFile The playback file path used to create the pipeline
     *                     \else
     *                     使用回放文件来创建pipeline对象
     * @param playbackFile 用于创建pipeline的回放文件路径
     *                     \endif
     */
    public Pipeline(String playbackFile) {
        OBLocalUtils.checkFileAndThrow(playbackFile, "Create Pipeline failed");
        mHandle = nCreateWithPlaybackFile(playbackFile);
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

//    /**
//     * 获取支持的所有传感器的数据流配置
//     *
//     * @return 数据流配置 {@link StreamProfileList}
//     */
//    public StreamProfileList getAllStreamProfileList() {
//        long handle = nGetAllStreamProfileList(mHandle);
//        return handle != 0 ? new StreamProfileList(handle) : null;
//    }

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
     * Valid area after getting D2C
     *
     * @param distance Working distance(mm)
     * @param distance 工作距离(mm)
     * @return Returns the area information valid after D2c at the working distance {@link OBRect}
     * \else
     * 获取D2C后有效的区域
     * @return 返回在工作距离下D2C后有效的区域信息 {@link OBRect}
     * \endif
     */
    @Deprecated
    public OBRect getD2CValidArea(int distance) {
        throwInitializeException();
        OBRect rect = new OBRect();
        nGetD2CValidArea(mHandle, distance, rect.getBytes());
        boolean result = rect.parseBytes();
        if (!result) {
            throw new OBException("getD2CValidArea parse bytes error!");
        }
        return rect;
    }

    /**
     * \if English
     * Get valid area between minimum distance and maximum distance after D2C
     *
     * @param minimumDistance minimum working distance(mm)
     * @param maximumDistance maximum working distance(mm)
     * @param minimumDistance 最小工作距离(mm)
     * @param maximumDistance 最大工作距离(mm)
     * @return returns the area information valid after D2C at working distance
     * \else
     * 获取D2C后有效的区域。
     * 如果需要获取指定距离D2C后的ROI区域，将minimum_distance与maximum_distance设置成一样或者将maximum_distance设置成0
     * @return 返回在工作距离下D2C后有效的区域信息
     * \endif
     */
    public OBRect getD2CRangeValidArea(int minimumDistance, int maximumDistance) {
        throwInitializeException();
        OBRect rect = new OBRect();
        nGetD2CRangeValidArea(mHandle, minimumDistance, maximumDistance, rect.getBytes());
        boolean result = rect.parseBytes();
        if (!result) {
            throw new OBException("getD2CValidArea parse bytes error!");
        }
        return rect;
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
    public CameraParam getCameraParam() {
        throwInitializeException();
        CameraIntrinsic depthIntrinsic = new CameraIntrinsic();
        CameraIntrinsic colorIntrinsic = new CameraIntrinsic();
        CameraDistortion depthDistortion = new CameraDistortion();
        CameraDistortion colorDistortion = new CameraDistortion();
        D2CTransform d2CTransform = new D2CTransform();
        CameraParam params = new CameraParam();

        nGetCameraParam(mHandle, depthIntrinsic.getBytes(), colorIntrinsic.getBytes(),
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
    public CameraParam getCameraParamWithProfile(int colorWidth, int colorHeight, int depthWidth, int depthHeight) {
        throwInitializeException();
        CameraIntrinsic depthIntrinsic = new CameraIntrinsic();
        CameraIntrinsic colorIntrinsic = new CameraIntrinsic();
        CameraDistortion depthDistortion = new CameraDistortion();
        CameraDistortion colorDistortion = new CameraDistortion();
        D2CTransform d2CTransform = new D2CTransform();
        CameraParam params = new CameraParam();

        nGetCameraParamWithProfile(mHandle, colorWidth, colorHeight, depthWidth, depthHeight, depthIntrinsic.getBytes(), colorIntrinsic.getBytes(),
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
     * Start recording
     *
     * @param filePath Recorded file path
     *                 \else
     *                 开启录制
     * @param filePath 录制的文件路径
     *                 \endif
     */
    public void startRecord(String filePath) {
        throwInitializeException();
        nStartRecord(mHandle, filePath);
    }

    /**
     * \if English
     * stop recording
     * \else
     * 关闭录制
     * \endif
     */
    public void stopRecord() {
        throwInitializeException();
        nStopRecord(mHandle);
    }

    /**
     * \if English
     * Get playback object from pipeline
     *
     * @return Returns the playback object {@link Playback}
     * \else
     * 从pipeline中获取回放对象
     * @return 返回回放对象 {@link Playback}
     * \endif
     */
    public Playback getPlayback() {
        throwInitializeException();
        long handle = nGetPlayback(mHandle);
        if (0 == handle) {
            return null;
        }
        return new Playback(handle);
    }

    /**
     * \if English
     * @brief Get device calibration parameters with the specified configuration
     *
     * @param config The pipeline configuration
     *
     * @return CalibrationParam The calibration parameters
     * \else
     * @brief 获取指定配置的设备校准参数
     *
     * @param config pipeline配置
     *
     * @return CalibrationParam 校准参数
     * \endif
     */
    public CalibrationParam getCalibrationParam(Config config) {
        /*throwInitializeException();
        CameraIntrinsic[] intrinsics = new CameraIntrinsic[SensorType.COUNT.value()];
        CameraDistortion[] distortions = new CameraDistortion[SensorType.COUNT.value()];
        D2CTransform[][] extrinsics = new D2CTransform[SensorType.COUNT.value()][SensorType.COUNT.value()];

        byte[] intrinsicBytes = new byte[SensorType.COUNT.value() * intrinsics[0].BYTES()];
        byte[] distortionBytes = new byte[SensorType.COUNT.value() * distortions[0].BYTES()];
        byte[] extrinsicBytes = new byte[SensorType.COUNT.value() * SensorType.COUNT.value() * extrinsics[0][0].BYTES()];

        nGetCalibrationParam(mHandle, config.getHandle(), intrinsicBytes, distortionBytes, extrinsicBytes);

        for (int i = 0; i < SensorType.COUNT.value(); i++) {
            intrinsics[i] = new CameraIntrinsic();
            distortions[i] = new CameraDistortion();
        }
        for (int i = 0; i < SensorType.COUNT.value(); i++) {
            for (int j = 0; j < SensorType.COUNT.value(); j++) {
                extrinsics[i][j] = new D2CTransform();
            }
        }
        CalibrationParam param = new CalibrationParam();

        boolean isSuccess = true;
        for (int i = 0; i < SensorType.COUNT.value(); i++) {
            byte[] bytes = DataUtilities.subBytes(intrinsicBytes, i * intrinsics[i].BYTES(), intrinsics[i].BYTES());
            isSuccess &= param.parseIntrinsicBytes(intrinsics[i], bytes);
        }
        for (int i = 0; i < SensorType.COUNT.value(); i++) {
            byte[] bytes = DataUtilities.subBytes(distortionBytes, i * distortions[i].BYTES(), distortions[i].BYTES());
            isSuccess &= param.parseDistortionBytes(distortions[i], bytes);
        }
        for (int i = 0; i < SensorType.COUNT.value(); i++) {
            for (int j = 0; j < SensorType.COUNT.value(); j++) {
                byte[] bytes = DataUtilities.subBytes(extrinsicBytes, (i * SensorType.COUNT.value() + j) * extrinsics[i][j].BYTES(), extrinsics[i][j].BYTES());
                isSuccess &= param.parseExtrinsicBytes(extrinsics[i][j], bytes);
            }
        }

        if (isSuccess) {
            param.setIntrinsics(intrinsics);
            param.setDistortions(distortions);
            param.setExtrinsics(extrinsics);

            return param;
        }
        return null;*/
        throwInitializeException();
        CameraIntrinsic[] intrinsics = new CameraIntrinsic[SensorType.COUNT.value()];
        CameraDistortion[] distortions = new CameraDistortion[SensorType.COUNT.value()];
        D2CTransform[][] extrinsics = new D2CTransform[SensorType.COUNT.value()][SensorType.COUNT.value()];

        for (int i = 0; i < SensorType.COUNT.value(); i++) {
            intrinsics[i] = new CameraIntrinsic();
            distortions[i] = new CameraDistortion();
        }
        for (int i = 0; i < SensorType.COUNT.value(); i++) {
            for (int j = 0; j < SensorType.COUNT.value(); j++) {
                extrinsics[i][j] = new D2CTransform();
            }
        }
        CalibrationParam param = new CalibrationParam();

        byte[] intrinsicBytes = new byte[SensorType.COUNT.value() * intrinsics[0].BYTES()];
        byte[] distortionBytes = new byte[SensorType.COUNT.value() * distortions[0].BYTES()];
        byte[] extrinsicBytes = new byte[SensorType.COUNT.value() * SensorType.COUNT.value() * extrinsics[0][0].BYTES()];

        nGetCalibrationParam(mHandle, config.getHandle(), intrinsicBytes, distortionBytes, extrinsicBytes);

        boolean isSuccess = true;
        for (int i = 0; i < SensorType.COUNT.value(); i++) {
            intrinsics[i] = new CameraIntrinsic();
            DataUtilities.copyBytes(intrinsicBytes, intrinsics[i].getBytes(), i * intrinsics[i].BYTES(), intrinsics[i].BYTES());
            isSuccess &= intrinsics[i].parseBytes();
        }
        for (int i = 0; i < SensorType.COUNT.value(); i++) {
            distortions[i] = new CameraDistortion();
            DataUtilities.copyBytes(distortionBytes, distortions[i].getBytes(), i * distortions[i].BYTES(), distortions[i].BYTES());
            isSuccess &= distortions[i].parseBytes();
        }
        for (int i = 0; i < SensorType.COUNT.value(); i++) {
            for (int j = 0; j < SensorType.COUNT.value(); j++) {
                extrinsics[i][j] = new D2CTransform();
                DataUtilities.copyBytes(extrinsicBytes, extrinsics[i][j].getBytes(),
                        (i * SensorType.COUNT.value() + j) * extrinsics[i][j].BYTES(), extrinsics[i][j].BYTES());
                isSuccess &= extrinsics[i][j].parseBytes();
            }
        }

        if (isSuccess) {
            param.intrinsics = intrinsics;
            param.distortions = distortions;
            param.extrinsics = extrinsics;
            return param;
        }
        return null;
    }

    /**
     * \if English
     * @brief Enable a video stream to be used in the configuration.
     *
     * @param config     Pointer to the configuration structure.
     * @param streamType The video stream type.
     * @param width      The video stream width.
     * @param height     The video stream height.
     * @param fps        The video stream frame rate.
     * @param format     The video stream format.
     * \else
     * @brief 启用一个视频流以用于配置。
     *
     * @param config     指向配置结构体的指针。
     * @param streamType 视频流类型。
     * @param width      视频流宽度.
     * @param height     视频流高度.
     * @param fps        视频流帧率.
     * @param format     视频流格式.
     * \endif
     */
    public void enableVideoStream(Config config, StreamType streamType, int width, int height,
                                  int fps, Format format) {
        throwInitializeException();
        nEnableVideoStream(config.getHandle(), streamType.value(), width, height, fps, format.value());
    }

    /**
     * \if English
     * @brief Enable an accelerometer stream to be used in the configuration.
     *
     * @param config         Pointer to the configuration structure.
     * @param fullScaleRange The full-scale range of the accelerometer.
     * @param sampleRate     The sample rate of the accelerometer.
     * \else
     * @brief 启用一个加速度计流以用于配置。
     *
     * @param config         指向配置结构体的指针。
     * @param fullScaleRange 加速度计的全标程范围。
     * @param sampleRate     加速度计的采样率。
     * \endif
     */
    public void enableAccelStream(Config config, AccelFullScaleRange fullScaleRange, SampleRate sampleRate) {
        throwInitializeException();
        nEnableAccelStream(config.getHandle(), fullScaleRange.value(), sampleRate.value());
    }

    /**
     * \if English
     * @brief Enable a gyroscope stream to be used in the configuration.
     *
     * @param config         Pointer to the configuration structure.
     * @param fullScaleRange The full-scale range of the gyroscope.
     * @param sampleRate     The sample rate of the gyroscope.
     * \else
     * @brief 启用一个陀螺仪流以用于配置。
     *
     * @param config         指向配置结构体的指针。
     * @param fullScaleRange 陀螺仪的全标程范围。
     * @param sampleRate     陀螺仪的采样率。
     * \endif
     */
    public void enableGyroStream(Config config, GyroFullScaleRange fullScaleRange, SampleRate sampleRate) {
        throwInitializeException();
        nEnableGyroStream(config.getHandle(), fullScaleRange.value(), sampleRate.value());
    }

    /**
     * \if English
     * @brief Get the enabled stream profile list in the pipeline configuration.
     *
     * @param config Pointer to the configuration structure.
     * \else
     * @brief 获取pipeline配置中启用的流配置列表。
     *
     * @param config 指向配置结构体的指针。
     * \endif
     */
    public StreamProfileList getEnabledStreamProfileList(Config config) {
        throwInitializeException();
        long handle = nGetEnabledStreamProfileList(config.getHandle());
        return handle != 0 ? new StreamProfileList(handle) : null;
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

    private static native long nCreateWithPlaybackFile(String playbackFile);

    private static native void nDelete(long handle);

    private static native void nStartWithConfig(long handle, long configHandle);

    private static native void nStartWithCallback(long handle, long configHandle, FrameSetCallbackImpl callback);

    private static native void nStop(long handle);

    private static native long nGetConfig(long handle);

    private static native long nGetStreamProfileList(long handle, int sensorType);

//    private static native long nGetAllStreamProfileList(long handle);

    private static native long nWaitForFrameSet(long handle, long timeoutMilliseconds);

    private static native void nEnableFrameSync(long handle);

    private static native void nDisableFrameSync(long handle);

    private static native void nSwitchConfig(long handle, long configHandle);

    private static native long nGetD2CDepthProfileList(long handle, long colorProfileHandle, int mode);

    private static native void nGetD2CValidArea(long handle, int distance, byte[] rect);

    private static native void nGetD2CRangeValidArea(long handle, int minimumDistance, int maximumDistance, byte[] rect);

    private static native void nGetCameraParam(long handle, byte[] depthIntr, byte[] colorIntr, byte[] depthDisto, byte[] colorDisto, byte[] trans, CameraParam cameraParam);

    private static native void nGetCameraParamWithProfile(long handle, int colorWidth, int colorHeight, int depthWidth, int depthHeight, byte[] depthIntr, byte[] colorIntr, byte[] depthDisto, byte[] colorDisto, byte[] trans, CameraParam cameraParam);

    private static native void nStartRecord(long handle, String filePath);

    private static native void nStopRecord(long handle);

    private static native long nGetPlayback(long handle);

    private static native void nGetCalibrationParam(long handle, long configHandle, byte[] intrinsicBytes, byte[] distortionBytes, byte[] extrinsicBytes);

    private static native void nEnableVideoStream(long configHandle, int streamType, int width, int height, int fps, int format);

    private static native void nEnableAccelStream(long configHandle, int fullScaleRange, int sampleRate);

    private static native void nEnableGyroStream(long configHandle, int fullScaleRange, int sampleRate);

    private static native long nGetEnabledStreamProfileList(long configHandle);
}
