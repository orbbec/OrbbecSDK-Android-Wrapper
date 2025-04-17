package com.orbbec.obsensor.types;

/**
 * \if English
 * Distortion model
 * \else
 * 畸变模型
 * \endif
 */
public enum CameraDistortionModel {
    /**
     * \if English
     * Rectilinear images. No distortion compensation required
     * \else
     * 平面图像，不需要畸变校正
     * \endif
     */
    NONE(0),

    /**
     * \if English
     * Equivalent to Brown-Conrady distortion, except that tangential distortion is applied to radially distorted points
     * \else
     * 等效于Brown-Conrady畸变，但对径向畸变的点应用切向畸变
     * \endif
     */
    MODIFIED_BROWN_CONRADY(1),

    /**
     * \if English
     * Equivalent to Brown-Conrady distortion, except undistorts image instead of distorting it
     * \else
     * 等效于Brown-Conrady畸变，但对图像进行反畸变，而不是畸变
     * \endif
     */
    INVERSE_BROWN_CONRADY(2),

    /**
     * \if English
     * Unmodified Brown-Conrady distortion model
     * \else
     * 未修改的Brown-Conrady畸变模型
     * \endif
     */
    BROWN_CONRADY(3),

    /**
     * \if English
     * Unmodified Brown-Conrady distortion model with k6 supported
     * \else
     * 未修改的Brown-Conrady畸变模型，支持k6
     * \endif
     */
    BROWN_CONRADY_K6(4),

    /**
     * \if English
     * Kannala-Brandt distortion model
     * \else
     * Kannala-Brandt畸变模型
     * \endif
     */
    KANNALA_BRANDT4(5);

    private final int mValue;

    CameraDistortionModel(int value) {
        mValue = value;
    }

    public int value() {
        return mValue;
    }

    public static CameraDistortionModel get(int value) {
        for (int i = 0; i < values().length; i++) {
            if (values()[i].value() == value) {
                return values()[i];
            }
        }
        return null;
    }
}
