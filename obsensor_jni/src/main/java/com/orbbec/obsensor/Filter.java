package com.orbbec.obsensor;

import com.orbbec.obsensor.types.FilterConfigSchemaItem;

import java.util.List;

/**
 * \if English
 * Base class for all filters
 * \else
 * 所有filter的基类
 * \endif
 */
public class Filter extends LobClass {
    private FilterCallback mFilterCallback;
    protected String name_;
    protected List<FilterConfigSchemaItem> configSchemaList_;

    Filter(long handle) {
        this.mHandle = handle;
        nInit(mHandle);
        name_ = nGetName(mHandle);
    }

    public <T extends Filter> T as() {
        switch (getName()) {
            case "DecimationFilter":
                return (T) new DecimationFilter(mHandle);
            case "HDRMerge":
                return (T) new HdrMerge(mHandle);
            case "SequenceIdFilter":
                return (T) new SequenceIdFilter(mHandle);
            case "NoiseRemovalFilter":
                return (T) new NoiseRemovalFilter(mHandle);
            case "EdgeNoiseRemovalFilter":
                return (T) new EdgeNoiseRemovalFilter(mHandle);
            case "SpatialFilter":
                return (T) new SpatialAdvancedFilter(mHandle);
            case "TemporalFilter":
                return (T) new TemporalFilter(mHandle);
            case "HoleFillingFilter":
                return (T) new HoleFillingFilter(mHandle);
            case "DisparityTransform":
                return (T) new DisparityTransform(mHandle);
            case "ThresholdFilter":
                return (T) new ThresholdFilter(mHandle);
        }
        throw new OBException("this filter is not extendable to " + getName());
    }

    public String getName() {
        throwInitializeException();
        return name_;
    }

    /**
     * \if English
     * Process the data frame to get the format converted data frame
     *
     * @param frame The data frame to be converted
     * @param frame 待转换的数据帧
     * @return Formatted Frame
     * \else
     * 处理数据帧得到格式转换后的数据帧
     * @return 格式转换后的Frame
     * \endif
     */
    public Frame process(Frame frame) {
        throwInitializeException();
        long handle = nProcess(mHandle, frame.getHandle());
        return handle != 0 ? new Frame(handle) : null;
    }

    /**
     * \if English
     * Filter reset, after reset, you can reset the format of the filter for the processing of another format
     * \else
     * Filter重置,重置后可重新设置filter的格式,用于另外一中格式的处理
     * \endif
     */
    public void reset() {
        throwInitializeException();
        nReset(mHandle);
    }

    /**
     * \if English
     * Filter sets the processing result callback function (asynchronous callback interface)
     *
     * @param callback filter callback
     * \else
     * Filter 设置处理结果回调函数(异步回调接口)
     *
     * @param callback filter回调
     * \endif
     */
    public void setCallback(FilterCallback callback) {
        throwInitializeException();
        mFilterCallback = callback;
        nSetCallback(mHandle, new FilterCallbackImpl() {
            @Override
            public void onFrame(long frameHandle) {
                if (null != mFilterCallback) {
                    mFilterCallback.onFrame((frameHandle != 0 ? new Frame(frameHandle) : null));
                }
            }
        });
    }

    /**
     * \if English
     * filter pushes frame_set into the pending buffer (asynchronous callback interface)
     *
     * @param frame data frame
     * \else
     * filter 压入frame_set到待处理缓存(异步回调接口)
     *
     * @param frame 数据帧
     * \endif
     */
    public void pushFrame(Frame frame) {
        throwInitializeException();
        nPushFrame(mHandle, frame.getHandle());
    }

    /**
     * \if English
     * Enable the frame post processing
     *
     * @param isEnable enable status
     * \else
     * 启用帧后处理
     *
     * @param isEnable 启用状态
     * endif
     */
    public void enable(boolean isEnable) {
        throwInitializeException();
        nEnable(mHandle, isEnable);
    }

    /**
     * \if English
     * Return Enable State
     * \else
     * 返回启用状态
     * \endif
     */
    public boolean isEnabled() {
        throwInitializeException();
        return nIsEnable(mHandle);
    }

    /**
     * \if English
     * Get config schema of the filter
     *
     * @return The config schema of the filter
     * \else
     * 获取滤波器的配置模式
     * @return 滤波器的配置模式
     * \endif
     */
    public String getConfigSchema() {
        throwInitializeException();
        return nGetConfigSchema(mHandle);
    }

    /**
     * \if English
     * Get the Config Schema List object
     *
     * @return The List of the filter config schema
     * \else
     * 获取配置模式列表对象
     * @return 滤波的配置模式列表
     * \endif
     */
    public List<FilterConfigSchemaItem> getConfigSchemaList() {
        return nGetConfigSchemaList(name_);
    }

    /**
     * \if English
     * Set the filter config value by name
     *
     * @param configName The name of the config
     * @param value The value of the config
     * \else
     * 设置滤波器的配置值
     *
     * @param configName 配置名称
     * @param value 配置值
     */
    public void setConfigValue(String configName, double value) {
        throwInitializeException();
        nSetConfigValue(mHandle, configName, value);
    }

    /**
     * \if English
     * Get the Config Value object by name.
     *
     * @param configName  The name of the config.
     * @return double The value of the config.
     * \else
     * 通过名称获取配置对象
     *
     * @param configName 配置名称
     * @return double 配置值
     */
    public double getConfigValue(String configName) {
        throwInitializeException();
        return nGetConfigValue(mHandle, configName);
    }

    /**
     * \if English
     * release data frame resources
     * \else
     * 释放数据帧资源
     * \endif
     */
    @Override
    public void close() {
        synchronized (this) {
            throwInitializeException();
            nSetCallback(mHandle, null);
            nDelete(mHandle);
            mHandle = 0;
        }
    }

    private interface FilterCallbackImpl {
        void onFrame(long frameHandle);
    }

    private static native String nGetName(long handle);

//    private static native long nGetConfigSchemaList(long handle);

    private static native int nConfigSchemaListGetCount(long handle);

    private static native List<FilterConfigSchemaItem> nGetConfigSchemaList(String name);

    private static native void nDeleteConfigSchemaList(long handle);

    private static native void nInit(long handle);

    private static native void nDelete(long handle);

    private static native long nProcess(long handle, long frameHandle);

    private static native void nReset(long handle);

    private static native void nSetCallback(long handle, FilterCallbackImpl callback);

    private static native void nPushFrame(long handle, long frameHandle);

    private static native void nEnable(long handle, boolean isEnable);

    private static native boolean nIsEnable(long handle);

    private static native String nGetConfigSchema(long handle);

    private static native void nSetConfigValue(long handle, String configName, double value);

    private static native double nGetConfigValue(long handle, String configName);
}
