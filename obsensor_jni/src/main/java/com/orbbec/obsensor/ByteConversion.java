package com.orbbec.obsensor;

public interface ByteConversion {
    // 解析字节数组的方法
    default boolean parseBytes() {
        // 默认实现：返回 false 或抛出异常
        throw new UnsupportedOperationException("parseBytes not implemented");
    }

    // 包装字节数组的方法
    default boolean wrapBytes(byte[] bytes) {
        // 默认实现：返回空字节数组或抛出异常
        throw new UnsupportedOperationException("wrapBytes not implemented");
    }
}
