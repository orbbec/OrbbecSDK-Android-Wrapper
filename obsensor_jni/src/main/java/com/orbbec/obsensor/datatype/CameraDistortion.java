package com.orbbec.obsensor.datatype;

import com.orbbec.obsensor.DataType;
import com.orbbec.internal.DataUtilities;

/**
 * \if English
 * Distortion Parameters
 * \else
 * 畸变参数
 * \endif
 */
public class CameraDistortion extends DataType {
    private float mK1;
    private float mK2;
    private float mK3;
    private float mK4;
    private float mK5;
    private float mK6;
    private float mP1;
    private float mP2;

    /**
	 * \if English
	 * 	Get Radial Distortion Factor 1
     *
     * @return Radial distortion factor 1
	 * \else
     * 获取径向畸变系数1
     *
     * @return 径向畸变系数1
	 * \endif
     */
    public float getK1() {
        throwInitializeException();
        return mK1;
    }

    /**
	 * \if English
	 * 	Get Radial Distortion Factor 2
     *
     * @return Radial distortion factor 2
	 * \else
     * 获取径向畸变系数2
     *
     * @return 径向畸变系数2
	 * \endif
     */
    public float getK2() {
        throwInitializeException();
        return mK2;
    }

    /**
	 * \if English
	 * 	Get Radial Distortion Factor 3
     *
     * @return Radial distortion factor 3
	 * \else
     * 获取径向畸变系数3
     *
     * @return 径向畸变系数3
	 * \endif
     */
    public float getK3() {
        throwInitializeException();
        return mK3;
    }

    /**
	 * \if English
	 * 	Get Radial Distortion Factor 4
     *
     * @return Radial distortion factor 4
	 * \else
     * 获取径向畸变系数4
     *
     * @return 径向畸变系数4
	 * \endif
     */
    public float getK4() {
        throwInitializeException();
        return mK4;
    }

    /**
	 * \if English
	 * 	Get Radial Distortion Factor 5
     *
     * @return Radial distortion factor 5
	 * \else
     * 获取径向畸变系数5
     *
     * @return 径向畸变系数5
	 * \endif
     */
    public float getK5() {
        throwInitializeException();
        return mK5;
    }

    /**
	 * \if English
	 * 	Get Radial Distortion Factor 6
     *
     * @return Radial distortion factor 6
	 * \else
     * 获取径向畸变系数6
     *
     * @return 径向畸变系数6
	 * \endif
     */
    public float getK6() {
        throwInitializeException();
        return mK6;
    }

    /**
	 * \if English
	 * Get the tangential distortion factor 1
     *
     * @return Tangential distortion factor 1
	 * \else
     * 获取切向畸变系数1
     *
     * @return 切向畸变系数1
	 * \endif
     */
    public float getP1() {
        throwInitializeException();
        return mP1;
    }

    /**
	 * \if English
	 * Get the tangential distortion factor 2
     *
     * @return Tangential distortion factor 2
	 * \else
     * 获取切向畸变系数2
     *
     * @return 切向畸变系数2
	 * \endif
     */
    public float getP2() {
        throwInitializeException();
        return mP2;
    }

    @Override
    public int BYTES() {
        // Float.BYTES * 8
        return 32;
    }

    @Override
    protected boolean parseBytesImpl(byte[] bytes) {
        int offset = 0, length = Float.BYTES;
        mK1 = DataUtilities.bytesToFloat(DataUtilities.subBytes(bytes, offset, length));
        offset += length;
        mK2 = DataUtilities.bytesToFloat(DataUtilities.subBytes(bytes, offset, length));
        offset += length;
        mK3 = DataUtilities.bytesToFloat(DataUtilities.subBytes(bytes, offset, length));
        offset += length;
        mK4 = DataUtilities.bytesToFloat(DataUtilities.subBytes(bytes, offset, length));
        offset += length;
        mK5 = DataUtilities.bytesToFloat(DataUtilities.subBytes(bytes, offset, length));
        offset += length;
        mK6 = DataUtilities.bytesToFloat(DataUtilities.subBytes(bytes, offset, length));
        offset += length;
        mP1 = DataUtilities.bytesToFloat(DataUtilities.subBytes(bytes, offset, length));
        offset += length;
        mP2 = DataUtilities.bytesToFloat(DataUtilities.subBytes(bytes, offset, length));
        return true;
    }

    @Override
    protected boolean wrapBytesImpl(byte[] bytes) {
        int offset = 0, length = Float.BYTES;
        DataUtilities.appendBytes(DataUtilities.floatToBytes(mK1), bytes, offset, length);
        offset += length;
        DataUtilities.appendBytes(DataUtilities.floatToBytes(mK2), bytes, offset, length);
        offset += length;
        DataUtilities.appendBytes(DataUtilities.floatToBytes(mK3), bytes, offset, length);
        offset += length;
        DataUtilities.appendBytes(DataUtilities.floatToBytes(mK4), bytes, offset, length);
        offset += length;
        DataUtilities.appendBytes(DataUtilities.floatToBytes(mK5), bytes, offset, length);
        offset += length;
        DataUtilities.appendBytes(DataUtilities.floatToBytes(mK6), bytes, offset, length);
        offset += length;
        DataUtilities.appendBytes(DataUtilities.floatToBytes(mP1), bytes, offset, length);
        offset += length;
        DataUtilities.appendBytes(DataUtilities.floatToBytes(mP2), bytes, offset, length);
        return true;
    }

    @Override
    public String toString() {
        return "CameraDistortion{K1:" + mK1
                + ", K2:" + mK2
                + ", K3:" + mK3
                + ", K4:" + mK4
                + ", K5:" + mK5
                + ", K6:" + mK6
                + ", P1:" + mP1
                + ", P2:" + mP2
                + "}";
    }
}
