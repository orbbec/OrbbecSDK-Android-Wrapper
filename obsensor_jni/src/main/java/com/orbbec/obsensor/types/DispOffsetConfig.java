package com.orbbec.obsensor.types;

public class DispOffsetConfig {
    @StructField(offset = 0, size = 1)
    private short enable;

    @StructField(offset = 1, size = 1)
    private short offset0;

    @StructField(offset = 2, size = 1)
    private short offset1;

    @StructField(offset = 3, size = 1)
    private short reserved;

    private byte[] mBytes;

    public byte[] BYTES() {
        if (mBytes == null) {
            mBytes = new byte[4];
        }
        return mBytes;
    }
}
