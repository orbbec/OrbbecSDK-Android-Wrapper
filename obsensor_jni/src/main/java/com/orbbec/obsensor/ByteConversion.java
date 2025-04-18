package com.orbbec.obsensor;

public interface ByteConversion {
    byte[] getBytes();

    // Parse Byte Arrays
    default boolean parseBytes() {
        throw new UnsupportedOperationException("parseBytes not implemented");
    }

    // Wrap Byte Arrays
    default boolean wrapBytes() {
        throw new UnsupportedOperationException("wrapBytes not implemented");
    }
}
