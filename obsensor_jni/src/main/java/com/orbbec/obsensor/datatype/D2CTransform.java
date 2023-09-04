package com.orbbec.obsensor.datatype;

import com.orbbec.obsensor.DataType;
import com.orbbec.internal.DataUtilities;

/**
 * \if English
 * Rotation/Transformation Matrix
 * \else
 * 旋转/变换矩阵
 * \endif
 */
public class D2CTransform extends DataType {
    private float[] mRot; // length:9
    private float[] mTrans; // length:3

    /**
	 * \if English
	 * Get rotation matrix, row-major
     *
     * @return rotation matrix
	 * \else
     * 获取旋转矩阵，行优先
     *
     * @return 旋转矩阵
	 * \endif
     */
    public float[] getRot() {
        throwInitializeException();
        return mRot;
    }

    /**
	 * \if English
	 * get transformation matrix
     *
     * @return transformation matrix
	 * \else
     * 获取变换矩阵
     *
     * @return 变换矩阵
	 * \endif
     */
    public float[] getTrans() {
        throwInitializeException();
        return mTrans;
    }

    @Override
    public int BYTES() {
        // Float.BYTES * 12
        return 48;
    }

    @Override
    protected boolean parseBytesImpl(byte[] bytes) {
        int offset = 0, length = Float.BYTES * 9;
        mRot = DataUtilities.bytesToFloats(DataUtilities.subBytes(bytes, offset, length), 9);
        offset += length;
        length = Float.BYTES * 3;
        mTrans = DataUtilities.bytesToFloats(DataUtilities.subBytes(bytes, offset, length), 3);
        return true;
    }

    @Override
    protected boolean wrapBytesImpl(byte[] bytes) {
        int offset = 0, length = Float.BYTES * mRot.length;
        DataUtilities.appendBytes(DataUtilities.floatsToBytes(mRot), bytes, offset, length);
        offset += length;
        length = Float.BYTES * mTrans.length;
        DataUtilities.appendBytes(DataUtilities.floatsToBytes(mTrans), bytes, offset, length);
        return true;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder("D2CTransform{Rot{");
        for (int i = 0; i < mRot.length; i++) {
            if (i == (mRot.length - 1)) {
                sb.append(mRot[i] + "}, Trans{");
            } else {
                sb.append(mRot[i] + ", ");
            }
        }

        for (int i = 0; i < mTrans.length; i++) {
            if (i == (mTrans.length - 1)) {
                sb.append(mTrans[i] + "}}");
            } else {
                sb.append(mTrans[i] + ", ");
            }
        }

        return sb.toString();
    }
}
