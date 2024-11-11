package com.orbbec.obsensor;

import com.orbbec.obsensor.datatype.D2CTransform;

/**
 * \if English
 * Data stream configuration base class
 * \else
 * 数据流配置基类
 * \endif
 */
public class StreamProfile extends LobClass {

    StreamProfile(long handle) {
        mHandle = handle;
    }

    /**
	 * \if English
	 * Type conversion of data stream configuration
     *
     * @param <T>        Subclass of streamProfile
     * @param streamType data flow configuration type to be converted {@link StreamType}
	 * \else
     * 数据流配置的类型转换
     *
     * @param <T>        StreamProfile的子类
     * @param streamType 待转换的数据流配置类型 {@link StreamType}
	 * \endif
     */
    public <T extends StreamProfile> T as(StreamType streamType) {
        switch (streamType) {
            case VIDEO:
            case IR:
            case IR_LEFT:
            case IR_RIGHT:
            case COLOR:
            case DEPTH:
            case RAW_PHASE:
                return (T) new VideoStreamProfile(mHandle);
            case ACCEL:
                return (T) new AccelStreamProfile(mHandle);
            case GYRO:
                return (T) new GyroStreamProfile(mHandle);
        }
        throw new OBException("this profile is not supported " + streamType.name());
    }

    /**
	 * \if English
	 * Get data stream format
     *
     * @return stream format {@link Format}
	 * \else
     * 获取数据流格式
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
	 * Get data stream type
     *
     * @return stream type {@link StreamType}
	 * \else
     * 获取数据流类型
     *
     * @return 数据流类型 {@link StreamType}
	 * \endif
     */
    public StreamType getType() {
        throwInitializeException();
        int type = nGetType(mHandle);
        return StreamType.get(type);
    }

    /**
     * \if English
     * Get the extrinsic for source stream to target stream
     *
     * @return The extrinsic {@link D2CTransform}
     * \else
     * 获取源数据流到目标数据流的外参
     *
     * @return 外参 {@link D2CTransform}
     * \endif
     */
    public D2CTransform getExtrinsicTo(StreamProfile targetStreamProfile) {
        throwInitializeException();
        D2CTransform extrinsic = new D2CTransform();
        long targetHandle = targetStreamProfile.getHandle();
        nGetExtrinsicTo(mHandle, targetHandle, extrinsic.getBytes());
        return extrinsic;
    }

    /**
	 * \if English
	 * resources release
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

    private static native void nDelete(long handle);

    private static native int nGetFormat(long handle);

    private static native int nGetType(long handle);

    private static native void nGetExtrinsicTo(long sourceHandle, long targetHandle, byte[] extrinsic_ptr);
}
