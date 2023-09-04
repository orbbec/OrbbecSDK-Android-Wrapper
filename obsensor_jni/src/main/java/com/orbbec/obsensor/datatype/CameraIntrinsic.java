package com.orbbec.obsensor.datatype;

import com.orbbec.obsensor.DataType;
import com.orbbec.internal.DataUtilities;

/**
 * \if English
 * Camera internal parameters
 * \else
 * 相机内参
 * \endif
 */
public class CameraIntrinsic extends DataType {
    /**
	 * \if English
	 * the focal length in the x direction, unit: pixel
	 * \else
     * x方向焦距，单位：像素
     * \endif
     */
    private float mFx;

    /**
	 * \if English
	 * the focal length in the y direction, unit: pixel
	 * \else
     * y方向焦距，单位：像素
     * \endif
     */
    private float mFy;

    /**
	 * \if English
     * the abscissa of the optical center
	 * \else
     * 光心横坐标
     * \endif
     */
    private float mCx;

    /**
	 * \if English
	 * the ordinate of the optical center
	 * \else
     * 光心纵坐标
     * \endif
     */
    private float mCy;

    /**
	 * \if English
	 * image width
	 * \else
     * 图像宽度
     * \endif
     */
    private short mWidth;

    /**
	 * \if English
	 * image height
	 * \else
     * 图像高度
     * \endif
     */
    private short mHeight;

    /**
	 * \if English
	 * Get the focal length in the x direction, unit: pixel
     *
     * @return returns the focal length in the x-direction
	 * \else
     * 获取x方向焦距，单位：像素
     *
     * @return 返回x方向焦距
	 * \endif
     */
    public float getFx() {
        throwInitializeException();
        return mFx;
    }

    /**
	 * \if English
	 * Get the focal length in the y direction, unit: pixel
     *
     * @return returns the y-direction focal length
	 * \else
     * 获取y方向焦距，单位：像素
     *
     * @return 返回y方向焦距
	 * \endif
     */
    public float getFy() {
        throwInitializeException();
        return mFy;
    }

    /**
	 * \if English
	 * Get the abscissa of the optical center
     *
     * @return returns the abscissa of the optical center
	 * \else
     * 获取光心横坐标
     *
     * @return 返回光心横坐标
	 * \endif
     */
    public float getCx() {
        throwInitializeException();
        return mCx;
    }

    /**
	 * \if English
     * Get the ordinate of the optical center
     *
     * @return returns the ordinate of the optical center
	 * \else
     * 获取光心纵坐标
     *
     * @return 返回光心纵坐标
	 * \endif
     */
    public float getCy() {
        throwInitializeException();
        return mCy;
    }

    /**
	 * \if English
	 * get image width
     *
     * @return returns image width
	 * \else
     * 获取图像宽度
     *
     * @return 返回图像宽度
	 * \endif
     */
    public short getWidth() {
        throwInitializeException();
        return mWidth;
    }

    /**
	 * \if English
	 * get image height
     *
     * @return returns image height
	 * \else
     * 获取图像高度
     *
     * @return 返回图像高度
	 * \endif
     */
    public short getHeight() {
        throwInitializeException();
        return mHeight;
    }

    @Override
    public int BYTES() {
        // Float.BYTES * 4 + Short.BYTES * 2
        return 20;
    }

    @Override
    protected boolean parseBytesImpl(byte[] bytes) {
        int offset = 0, length = Float.BYTES;
        mFx = DataUtilities.bytesToFloat(DataUtilities.subBytes(bytes, offset, length));
        offset += length;
        mFy = DataUtilities.bytesToFloat(DataUtilities.subBytes(bytes, offset, length));
        offset += length;
        mCx = DataUtilities.bytesToFloat(DataUtilities.subBytes(bytes, offset, length));
        offset += length;
        mCy = DataUtilities.bytesToFloat(DataUtilities.subBytes(bytes, offset, length));
        offset += length;
        length = Short.BYTES;
        mWidth = DataUtilities.bytesToShort(DataUtilities.subBytes(bytes, offset, length));
        offset += length;
        mHeight = DataUtilities.bytesToShort(DataUtilities.subBytes(bytes, offset, length));
        return true;
    }

    @Override
    protected boolean wrapBytesImpl(byte[] bytes) {
        int offset = 0, length = Float.BYTES;
        DataUtilities.appendBytes(DataUtilities.floatToBytes(mFx), bytes, offset, length);
        offset += length;
        DataUtilities.appendBytes(DataUtilities.floatToBytes(mFy), bytes, offset, length);
        offset += length;
        DataUtilities.appendBytes(DataUtilities.floatToBytes(mCx), bytes, offset, length);
        offset += length;
        DataUtilities.appendBytes(DataUtilities.floatToBytes(mCy), bytes, offset, length);
        offset += length;
        length = Short.BYTES;
        DataUtilities.appendBytes(DataUtilities.shortToBytes(mWidth), bytes, offset, length);
        offset += length;
        DataUtilities.appendBytes(DataUtilities.shortToBytes(mHeight), bytes, offset, length);
        return true;
    }

    @Override
    public String toString() {
        return "CameraIntrinsic{Fx:" + mFx
                + ", Fy:" + mFy
                + ", Cx:" + mCx
                + ", Cy:" + mCy
                + ", Width:" + mWidth
                + ", Height:" + mHeight
                + "}";
    }
}
