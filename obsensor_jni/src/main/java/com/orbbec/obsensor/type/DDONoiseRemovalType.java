package com.orbbec.obsensor.type;

public enum DDONoiseRemovalType {
    /**
     * \if English
     * SPLIT
     * \endif
     */
    NR_LUT(0),
    /**
     * \if English
     * NON_SPLIT
     * \endif
     */
    NR_OVERALL(1);

    private final int mValue;

    DDONoiseRemovalType(int value) {
        mValue = value;
    }

    public int value() {
        return mValue;
    }

    public static DDONoiseRemovalType get(int value) {
        for (DDONoiseRemovalType type : DDONoiseRemovalType.values()) {
            if (type.value() == value) {
                return type;
            }
        }
        return null;
    }
}
