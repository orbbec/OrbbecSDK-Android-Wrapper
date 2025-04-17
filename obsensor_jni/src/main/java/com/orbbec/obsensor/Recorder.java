package com.orbbec.obsensor;

/**
 * \if English
 * data recorder
 * \else
 * 数据录制器
 * \endif
 */
public class Recorder extends LobClass {
    private static final String TAG = "Recorder";

    /**
	 * \if English
	 * Create a recorder
	 * \else
     * 创建录制器
	 * \endif
     */
    public Recorder() {
        mHandle = nCreateRecorder();
    }

    /**
	 * \if English
	 * Create a recorder by specifying a device
     *
     * @param device device {@link Device}
	 * \else
     * 通过指定设备创建录制器
     *
     * @param device 设备 {@link Device}
	 * \endif
     */
    public Recorder(Device device) {
        mHandle = nCreateRecorderWithDevice(device.getHandle());
    }

    /**
	 * \if English
	 * start recording
     *
     * @param fileName Recorded file name
     * @param async    	Whether to record asynchronously
	 * \else
     * 开始录制
     *
     * @param fileName 录制的文件名称
     * @param async    是否异步录制
	 * \endif
     */
    public void start(String fileName, boolean async) {
        throwInitializeException();
        nStart(mHandle, fileName, async);
    }

    /**
	 * \if English
	 * Stop recording
	 * \else
     * 停止录制
     * \endif
     */
    public void stop() {
        throwInitializeException();
        nStop(mHandle);
    }

    /**
	 * \if English
	 * Write frame data to the recorder
     *
     * @param frame Written frame data {@link Frame}
	 * \else
     * 向录制器内写入帧数据
     *
     * @param frame 写入的帧数据 {@link Frame}
	 * \endif
     */
    public void writeFrame(Frame frame) {
        throwInitializeException();
        nWriteFrame(mHandle, frame.getHandle());
    }

    /**
	 * \if English
	 * release recorder resources
	 * \else
     * 释放录制器资源
     * \endif
     */
    @Override
    public void close() {
        throwInitializeException();
        nDelete(mHandle);
        mHandle = 0;
    }

    private static native long nCreateRecorder();

    private static native long nCreateRecorderWithDevice(long deviceHandle);

    private static native void nStart(long handle, String fileName, boolean async);

    private static native void nStop(long handle);

    private static native void nWriteFrame(long handle, long frameHandle);

    private static native void nDelete(long handle);
}
