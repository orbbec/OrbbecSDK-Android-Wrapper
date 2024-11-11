package com.orbbec.obsensor;

import com.orbbec.obsensor.type.FrameMetadataType;

import java.math.BigInteger;
import java.nio.ByteBuffer;

/**
 * \if English
 * The data frame in the data stream is the base class of ColorFrame, DepthFrame, IRFrame, VideoFrame, FrameSet
 * \else
 * 数据流中的数据帧，是ColorFrame、DepthFrame、IRFrame、VideoFrame、FrameSet的基类
 * \endif
 */
public class Frame extends LobClass implements Cloneable {

    Frame(long handle) {
        mHandle = handle;
    }

    /**
	 * \if English
	 * Frame type conversion
     *
     * @param frameType frame type to be converted
     * @param <T>       Subclass of frame
     * @return New type of frame generated after conversion
	 * \else
     * 帧的类型转换
     *
     * @param frameType 待转换的帧类型
     * @param <T>       Frame的子类
     * @return 转换后生成的新的类型的帧
	 * \endif
     */
    public <T extends Frame> T as(FrameType frameType) {
        switch (frameType) {
            case VIDEO:
                return (T) new VideoFrame(mHandle);
            case DEPTH:
                return (T) new DepthFrame(mHandle);
            case COLOR:
                return (T) new ColorFrame(mHandle);
            case IR: // follow
            case IR_LEFT: // follow
            case IR_RIGHT: // follow
                return (T) new IRFrame(mHandle);
            case ACCEL:
                return (T) new AccelFrame(mHandle);
            case GYRO:
                return (T) new GyroFrame(mHandle);
            case FRAME_SET:
                return (T) new FrameSet(mHandle);
            case POINTS:
                return (T) new PointFrame(mHandle);
            case RAW_PHASE:
                return (T) new RawPhaseFrame(mHandle);
        }
        throw new OBException("this frame is not extendable to " + frameType.name());
    }

    /**
	 * \if English
	 * Get the system timestamp of the data frame, unit: ms
     *
     * @return system timestamp
	 * \else
     * 获取数据帧的系统时间戳，单位：ms
     *
     * @return 系统时间戳
	 * \endif
     */
    public long getSystemTimeStamp() {
        throwInitializeException();
        return nGetSystemTimeStamp(mHandle);
    }

    /**
	 * \if English
	 * Get the device timestamp of the data frame, unit: ms
     *
     * @return device timestamp
	 * \else
     * 获取数据帧的设备时间戳，单位：ms
     *
     * @return 设备时间戳
	 * \endif
     */
    public long getTimeStamp() {
        throwInitializeException();
        return nGetTimeStamp(mHandle);
    }

    /**
	 * \if English
	 * Get the device timestamp of the data frame, unit: us
     *
     * @return device timestamp
     *
     * @history delete 'long  getTimeStampUs()' because it wrong if encounter java long return from C native unsigned long long is overflow
     *
	 * \else
     * 获取数据帧的设备时间戳，单位：us
     *
     * @return 设备时间戳
     *
     * @历史 删除旧函数'long  getTimeStampUs()'，因为native层的unsigned long long返回Java时会因为造成Java返回值溢出；
     *
	 * \endif
     */
    public BigInteger getTimeStampUs() {
        throwInitializeException();
        final String text = nGetTimeStampUs(mHandle);
        if (null == text || !text.trim().matches("[0-9]+")) {
            throw new OBException("getTimeStampUs failed. text=" + text);
        }
        return new BigInteger(text);
    }

    /**
	 * \if English
	 * Get the data stream type to which the data frame belongs
     *
     * @return Data stream type {@link FrameType}
	 * \else
     * 获取数据帧所属的数据流类型
     *
     * @return 数据流类型 {@link FrameType}
	 * \endif
     */
    public FrameType getStreamType() {
        throwInitializeException();
        int type = nGetType(mHandle);
        return FrameType.get(type);
    }

    /**
	 * \if English
	 * Get the data stream format to which the data frame belongs
     *
     * @return Data Stream {@link Format}
	 * \else
     * 获取数据帧所属的数据流格式
     *
     * @return 数据流格式 {@link Format}
	 * \endif
     */
    public Format getFormat() {
        throwInitializeException();
        int format = nGetFormat(mHandle);
        return Format.get(format);
    }

    /**
	 * \if English
	 * Get the sequence number of the data frame in the data stream
     *
     * @return data frame number
	 * \else
     * 获取数据帧在数据流的序号
     *
     * @return 数据帧序号
	 * \endif
     */
    public long getFrameIndex() {
        throwInitializeException();
        return nGetIndex(mHandle);
    }

    /**
	 * \if English
	 * Get the data size of the data frame
     *
     * @return data size
	 * \else
     * 获取数据帧的数据大小
     *
     * @return 数据大小
	 * \endif
     */
    public int getDataSize() {
        throwInitializeException();
        return nGetDataSize(mHandle);
    }

    /**
	 * \if English
	 * Get the data of the dataframe
     *
     * @param buf byte array to fill frame data
     *
     * @return success, return the size of data had fill to buf;
     *         failed, return -1;
     *
	 * \else
     * 获取数据帧的数据
     *
     * @param buf 用于填充数据帧的byte数组
	 *
     * @return 成功，返回复制的数据大小；
     *         失败，返回-1
     * \endif
     */
    public int getData(byte[] buf) {
        throwInitializeException();
        return nGetData(mHandle, buf);
    }

    /**
     * \if English
     * Get the data of the dataFrame
     *
     * @param directByteBuffer ByteBuffer to fill frame data.
     *
     * @return success, return the size of data had fill to buf;
     *         failed, return -1;
     * \else
     * 获取数据帧的内容
     *
     * @param directByteBuffer 用于填充数据帧的ByteBuffer
     * @return 成功，返回复制的数据大小；
     *         失败，返回-1
     * \endif
     */
    public int getData(ByteBuffer directByteBuffer) {
        throwInitializeException();
        return nGetData(mHandle, directByteBuffer);
    }

    /**
     * \if English
     * Get the buffer of frame data
     *
     * The buffer should be consumed before the frame closed
     *
     * @return return the buffer of the frame data
     * \else
     * 获取数据帧的buffer
     *
     * 该buffer需要在frame被close之前消费掉，否则可能会有问题
     *
     * @return 帧数据的buffer
     * \endif
     */
    public ByteBuffer getDirectBuffer() {
        throwInitializeException();
        return nGetDirectBuffer(mHandle);
    }

    /**
     * \if English
     * Get the system timestamp of the frame in microseconds.
     *
     * The system timestamp is the time point when the frame was received by the host, on host clock domain.
     *
     * @return return the frame system timestamp in microseconds
     * \else
     * 获取数据帧的系统时间戳，单位：us
     *
     * 系统时间戳是数据帧被接收到主机的时间点，在主机时钟域
     *
     * @return 系统时间戳，单位：us
     * \endif
     */
    public long getSystemTimeStampUs() {
        throwInitializeException();
        return nGetSystemTimeStampUs(mHandle);
    }

    /**
     * \if English
     * Get the global timestamp of the frame in microseconds.
     *
     * The global timestamp is the time point when the frame was was captured by the device, and has been converted to the host clock domain. The
     * conversion process base on the device timestamp and can eliminate the timer drift of the device
     *
     * @return The global timestamp of the frame in microseconds.
     * \else
     * 获取数据帧的全局时间戳，单位：us
     *
     * 全局时间戳是数据帧被捕获到设备时，并且已经转换到主机时钟域的时间点，转换过程基于设备时间戳，可以消除设备时钟的漂移
     *
     * @return 全局时间戳，单位：us
     * \endif
     */
    public long getGlobalTimeStampUs() {
        throwInitializeException();
        return nGetGlobalTimeStampUs(mHandle);
    }

    /**
	 * \if English
	 * Get the metadata size of the frame
     *
     * @return Returns the metadata size of the frame
	 * \else
     * 获取帧的元数据大小
     *
     * @return 返回帧的元数据大小
	 * \endif
     */
    public int getMetadataSize() {
        throwInitializeException();
        return nGetMetadataSize(mHandle);
    }

    /**
	 * \if English
	 * Get the metadata of the frame
     *
     * @param metadata frame metadata
	 * \else
     * 获取帧的元数据
     *
     * @param metadata 帧的元数据
	 * \endif
     */
    public void getMetadata(byte[] metadata) {
        throwInitializeException();
        nGetMetadata(mHandle, metadata);
    }

    /**
     * \if English
     * check if the frame contains the specified metadata
     *
     * @param frameMetadataType frame metadata
     * \else
     * 检查帧是否包含指定的元数据
     *
     * @param frameMetadataType 帧的元数据
     * \endif
     */
    public boolean hasMetadata(FrameMetadataType frameMetadataType) {
        throwInitializeException();
        return nHasMetadata(mHandle, frameMetadataType.value());
    }

    /**
     * \if English
     * Get the metadata value of the frame
     *
     * @param frameMetadataType frame metadata
     * \else
     * 获取帧的元数据值
     *
     * @param frameMetadataType 帧的元数据
     * \endif
     */
    public long getMetaValue(FrameMetadataType frameMetadataType) {
        throwInitializeException();
        return nGetMetaValue(mHandle, frameMetadataType.value());
    }

    /**
     * \if English
     * Get the stream profile of the frame
     *
     * @return Return the stream profile of the frame, if the frame is not captured by a sensor stream, it will return NULL
     * \else
     * 获取数据帧的流配置
     *
     * @return 数据帧的流配置，如果数据帧没有被传感器捕获或者传感器流已经被销毁，则返回NULL
     * \endif
     */
    public StreamProfile getStreamProfile() {
        throwInitializeException();
        long handle = nGetStreamProfile(mHandle);
        return handle != 0 ? new StreamProfile(mHandle) : null;
    }

    /**
     * \if English
     * Get the sensor of the frame
     *
     * @return return the sensor of the frame, if the frame is not captured by a sensor or the sensor stream has been destroyed, it will return NULL
     * \else
     * 获取数据帧的传感器
     *
     * @return 数据帧的传感器，如果数据帧没有被传感器捕获或者传感器流已经被销毁，则返回NULL
     * \endif
     */
    public Sensor getSensor() {
        throwInitializeException();
        long handle = nGetSensor(mHandle);
        return handle != 0 ? new Sensor(handle) : null;
    }

    /**
     * \if English
     * Get the device of the frame
     *
     * @return return the device of the frame, if the frame is not captured by a sensor stream or the device has been destroyed, it will return NULL
     * \else
     * 获取数据帧的设备
     *
     * @return 数据帧的设备，如果数据帧没有被传感器捕获或者设备被销毁，则返回NULL
     * \endif
     */
    public Device getDevice() {
        throwInitializeException();
        long handle = nGetDevice(mHandle);
        return handle != 0 ? new Device(handle) : null;
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
            nDelete(mHandle);
            mHandle = 0;
        }

    }

    private static native long nGetTimeStamp(long handle);

    private static native long nGetSystemTimeStamp(long handle);

    private static native long nGetIndex(long handle);

    private static native int nGetType(long handle);

    private static native int nGetFormat(long handle);

    private static native void nDelete(long handle);

    private native int nGetData(long handle, ByteBuffer directByteBuffer);

    private static native ByteBuffer nGetDirectBuffer(long handle);

    private static native int nGetData(long handle, byte[] buf);

    private static native long nGetSystemTimeStampUs(long handle);

    private static native long nGetGlobalTimeStampUs(long handle);

    private static native int nGetDataSize(long handle);

    private static native void nGetMetadata(long handle, byte[] data);

    private static native int nGetMetadataSize(long handle);

    private static native String nGetTimeStampUs(long handle);

    private static native boolean nHasMetadata(long handle, int frameMetadataType);

    private static native long nGetMetaValue(long handle, int frameMetadataType);

    private static native long nGetStreamProfile(long handle);

    private static native long nGetSensor(long handle);

    private static native long nGetDevice(long handle);
}
