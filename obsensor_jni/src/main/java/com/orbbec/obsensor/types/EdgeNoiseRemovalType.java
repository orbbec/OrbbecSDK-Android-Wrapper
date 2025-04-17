package com.orbbec.obsensor.types;

public enum EdgeNoiseRemovalType {
    MG_FILTER(0),
    /**
     * \if English
     * horizontal MG
     * \endif
     */
    MGH_FILTER(1),
    /**
     * \if English
     * asym MG
     * \endif
     */
    MGA_FILTER(2),

    MGC_FILTER(3);

    private final int mValue;

    EdgeNoiseRemovalType(int value) {
        mValue = value;
    }

    public int value() {
        return mValue;
    }

    public static EdgeNoiseRemovalType get(int value) {
        for (int i = 0; i < values().length; i++) {
            if (values()[i].value() == value) {
                return values()[i];
            }
        }
        return null;
    }
}
