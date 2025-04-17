package com.orbbec.obsensor;

import com.orbbec.obsensor.types.FrameType;
import com.orbbec.obsensor.types.Format;

public class FrameFactory {

    /**
     * \if English
     * Create a Frame object of a specific type with a given format and data size.
     *
     * @param frameType The type of the frame.
     * @param format The format of the frame.
     * @param dataSize The size of the data in bytes.
     *
     * @return The created frame object.
     * \else
     * 创建指定类型的帧对象，指定格式和数据大小
     *
     * @param frameType 帧类型
     * @param format 帧格式
     * @param dataSize 数据大小，单位为字节
     *
     * @return 创建好的帧对象
     */
    public static Frame createFrame(FrameType frameType, Format format, int dataSize) {
        long handle = nCreateFrame(frameType.value(), format.value(), dataSize);
        return handle != 0 ? new Frame(handle) : null;
    }

    /**
     * \if English
     * Create a VideoFrame object of a specific type with a given format, width, height, and stride.
     * If stride is not specified, it will be calculated based on the width and format.
     *
     * @param frameType The type of the frame.
     * @param format The format of the frame.
     * @param width The width of the frame.
     * @param height The height of the frame.
     * @param stride The stride of the frame.
     *
     * @return The created video frame object.
     * \else
     * 创建指定类型的视频帧对象，指定格式、宽度、高度和步长
     * 如果步长未指定，则会根据宽度和格式计算出步长
     *
     * @param frameType 帧类型
     * @param format 帧格式
     * @param width 帧宽度
     * @param height 帧高度
     * @param stride 帧步长
     *
     * @return 创建好的视频帧对象
     */
    public static VideoFrame createVideoFrame(FrameType frameType, Format format, int width, int height, int stride) {
        long handle = nCreateVideoFrame(frameType.value(), format.value(), width, height, stride);
        return handle != 0 ? new VideoFrame(handle) : null;
    }

    /**
     * \if English
     * Create (clone) a frame object based on the specified other frame object.
     * The new frame object will have the same properties as the other frame object, but the data buffer is newly allocated.
     *
     * @param shouldCopyData If true, the data of the source frame object will be copied to the new frame object. If false, the new frame object will
     * have a data buffer with random data. The default value is true.
     *
     * @return The new frame object.
     * \else
     * 基于指定其他帧对象创建一个新的帧对象。新帧对象将拥有与源帧对象相同的属性，但数据缓冲区是新申请的。
     *
     * @param shouldCopyData 为true，则源帧对象的数据将被拷贝到新帧对象；为false，新帧对象将拥有随机数据。默认值为true。
     *
     * @return 新的帧对象
     */
    public static Frame createFrameFromOtherFrame(Frame otherFrame, boolean shouldCopyData) {
        long handle = nCreateFrameFromOtherFrame(otherFrame.getHandle(), shouldCopyData);
        return handle != 0 ? new Frame(handle) : null;
    }

    /**
     * \if English
     * Create a Frame From (according to)Stream Profile object
     *
     * @param profile The stream profile object to create the frame from.
     *
     * @return std::shared_ptr<Frame>  The created frame object.
     * \else
     * 基于StreamProfile对象创建帧对象
     *
     * @param profile 创建帧的对象
     *
     * @return 创建好的帧对象
     */
    public static Frame createFrameFromStreamProfile(StreamProfile profile) {
        long handle = nCreateFrameFromStreamProfile(profile.getHandle());
        return handle != 0 ? new Frame(handle) : null;
    }

    /**
     * \if English
     * Create a frame object based on an externally created buffer.
     * The buffer is owned by the caller, and will not be destroyed by the frame object. The user should ensure that the buffer is valid and not
     * modified.
     *
     * @param frameType Frame object type.
     * @param format Frame object format.
     * @param buffer Frame object buffer.
     *
     * @return The created frame object.
     * \else
     * 基于外部创建的缓冲区创建帧对象。
     * 缓冲区由调用者拥有，不会被帧对象销毁。用户需要确保缓冲区有效且未修改。
     *
     * @param frameType 帧对象类型
     * @param format 帧对象格式
     * @param buffer 帧对象缓冲区
     *
     * @return 创建好的帧对象
     * \endif
     */
    public static Frame createFrameFromBuffer(FrameType frameType, Format format, byte[] buffer) {
        long frameHandle = nCreateFrameFromBuffer(frameType.value(), format.value(), buffer);
        return frameHandle != 0 ? new Frame(frameHandle) : null;
    }

    /**
     * \if English
     * Create a video frame object based on an externally created buffer.
     * The buffer is owned by the user and will not be destroyed by the frame object. The user should ensure that the buffer is valid and not
     * modified.
     * The frame object is created with a reference count of 1, and the reference count should be decreased by calling @ref ob_delete_frame() when it
     * is no longer needed.
     *
     * @param frameType Frame object type.
     * @param format Frame object format.
     * @param width Frame object width.
     * @param height Frame object height.
     * @param buffer Frame object buffer.
     * @param stride Row span in bytes. If 0, the stride is calculated based on the width and format.
     *
     * @return The created video frame object.
     * \else
     * 基于外部创建的缓冲区创建视频帧对象。
     * 缓冲区由用户拥有，不会被帧对象销毁。用户需要确保缓冲区有效且未修改。
     * 帧对象被创建时有一个引用计数为1，当不再使用时，需要调用@ref ob_delete_frame()减少引用计数。
     *
     * @param frameType 帧对象类型
     * @param format 帧对象格式
     * @param width 帧对象宽度
     * @param height 帧对象高度
     * @param buffer 帧对象缓冲区
     * @param stride 行跨度，单位为字节。如果为0，则根据宽度和格式计算出步长。
     *
     * @return 创建好的视频帧对象
     */
    public static VideoFrame createVideoFrameFromBuffer(FrameType frameType, Format format, int width, int height,
                                                        byte[] buffer, int stride) {
        long frameHandle = nCreateVideoFrameFromBuffer(frameType.value(), format.value(), width, height, buffer, stride);
        return frameHandle != 0 ? new VideoFrame(frameHandle) : null;
    }

    private static native long nCreateFrame(int frameType, int format, int dataSize);

    private static native long nCreateVideoFrame(int frameType, int format, int width, int height, int stride);

    private static native long nCreateFrameFromOtherFrame(long otherFrame, boolean shouldCopyData);

    private static native long nCreateFrameFromStreamProfile(long profile);

    private static native long nCreateFrameFromBuffer(int frameType, int format, byte[] buffer);

    private static native long nCreateVideoFrameFromBuffer(int frameType, int format, int width, int height, byte[] buffer, int stride);
}
