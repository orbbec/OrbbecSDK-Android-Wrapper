package com.orbbec.obsensor;

/**
 * \if English
 * SDK base class, loading core SDK Library
 * \else
 * SDK基类，加载核心SDK库
 * \else
 */
public abstract class LobClass implements AutoCloseable {

    static {
        System.loadLibrary("obsensor_jni");
    }

    protected long mHandle = 0;

    /**
	 * \if English
	 * Get the current class handle
     * @return handle
	 * \else
     * 获取当前类句柄
     *
     * @return 句柄
	 * \endif
     */
    public long getHandle() {
        throwInitializeException();
        return mHandle;
    }

    protected void throwInitializeException() {
        if (mHandle == 0) {
            throw new OBException(getClass().getName() + " uninitialized!");
        }
    }
}
