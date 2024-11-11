package com.orbbec.obsensor;

/**
 * \if English
 * Base class for all filters
 * \else
 * 所有filter的基类
 * \endif
 */
public class Filter extends LobClass implements Cloneable {
    private FilterCallback mFilterCallback;

    Filter(long handle) {
        this.mHandle = handle;
    }

    public <T extends Filter> T as(String filterName) {
        switch (filterName) {
            case "DecimationFilter":
                return (T) new DecimationFilter(mHandle);
            case "HDRMerge":
                return (T) new HdrMergeFilter(mHandle);
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
                return (T) new DisparityTransform(mHandle, false);
            case "ThresholdFilter":
                return (T) new ThresholdFilter(mHandle);
        }
        throw new OBException("this filter is not extendable to " + filterName);
    }

    /**
	 * \if English
	 * Process the data frame to get the format converted data frame
     *
     * @param frame The data frame to be converted
     * @return Formatted Frame
	 * \else
     * 处理数据帧得到格式转换后的数据帧
     *
     * @param frame 待转换的数据帧
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
     * \endif
     */
    public void enable(boolean isEnable) {
        throwInitializeException();
        nEnable(mHandle, isEnable);
    }

    public boolean isEnable() {
        throwInitializeException();
        return nIsEnable(mHandle);
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

    private static native void nDelete(long handle);

    private static native long nProcess(long handle, long frameHandle);

    private static native void nReset(long handle);

    private static native void nSetCallback(long handle, FilterCallbackImpl callback);

    private static native void nPushFrame(long handle, long frameHandle);

    private static native void nEnable(long handle, boolean isEnable);

    private static native boolean nIsEnable(long handle);
}
