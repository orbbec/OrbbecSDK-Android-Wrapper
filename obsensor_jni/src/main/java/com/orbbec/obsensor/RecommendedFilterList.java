package com.orbbec.obsensor;

public class RecommendedFilterList extends LobClass {

    RecommendedFilterList(long handle) {
        mHandle = handle;
    }

    /**
     * \if English
     * Get the number of recommended filter list
     * \else
     * 获取推荐滤波器列表的个数
     * \endif
     */
    public int getFilterListCount() {
        throwInitializeException();
        return nGetFilterListCount(mHandle);
    }

    /**
     * \if English
     * Get the filter corresponding to the index
     * \else
     * 获取对应索引的滤波器
     * \endif
     */
    public Filter getFilter(int index) {
        throwInitializeException();
        long handle = nGetFilter(mHandle, index);
        return new Filter(handle);
    }

    /**
     * \if English
     * Get the name of ob_filter
     * \else
     * 获取滤波器的名称
     * \endif
     */
    public String getFilterName(Filter filter) {
        throwInitializeException();
        return nGetFilterName(filter.getHandle());
    }

    /**
     * \if English
     * Release recommended filter list resources
     * \else
     * 释放推荐滤波器列表资源
     * \endif
     */
    @Override
    public void close() {
        throwInitializeException();
        nDelete(mHandle);
        mHandle = 0;
    }

    private static native int nGetFilterListCount(long handle);

    private static native long nGetFilter(long handle, int index);

    private static native String nGetFilterName(long handle);

    private static native void nDelete(long handle);
}
