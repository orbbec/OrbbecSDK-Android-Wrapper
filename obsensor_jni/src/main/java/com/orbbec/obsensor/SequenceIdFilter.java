package com.orbbec.obsensor;

import com.orbbec.obsensor.types.SequenceIdItem;

import java.util.HashMap;
import java.util.Map;

public class SequenceIdFilter extends Filter {

    private final Map<Float, String> mSequenceIdList = new HashMap<Float, String>() {{
        put(0.f, "all");
        put(1.f, "1");
    }};
    private SequenceIdItem[] mOutputSequenceIdList;

    private void initSequenceIdList() {
        mOutputSequenceIdList = new SequenceIdItem[mSequenceIdList.size()];

        int i = 0;
        for (Map.Entry<Float, String> pair: mSequenceIdList.entrySet()) {
            if (i < mOutputSequenceIdList.length) {
                mOutputSequenceIdList[i] = new SequenceIdItem();
                mOutputSequenceIdList[i].setSequenceSelectId(pair.getKey().intValue());
                mOutputSequenceIdList[i].setName(pair.getValue());
                i++;
            }
        }
    }

    /**
     * \if English
     * Create a SequenceId filter.
     * \else
     * 创建一个序列化ID过滤器
     * \endif
     */
    public SequenceIdFilter() {
        super(nCreate());
        initSequenceIdList();
    }

    SequenceIdFilter(long handle) {
        super(handle);
        initSequenceIdList();
    }

    /**
     * \if English
     * Set the sequence id filter select sequence id.
     *
     * @param sequenceId sequence id to pass the filter.
     * \else
     * 设置序列化ID过滤器选择序列ID
     *
     * @param sequenceId 要通过过滤的序列ID
     * \endif
     */
    public void selectSequenceId(int sequenceId) {
        throwInitializeException();
//        nSelectSequenceId(mHandle, sequenceId);
        setConfigValue("sequenceid", sequenceId);
    }

    /**
     * \if English
     * Get the current sequence id.
     *
     * @return sequence id to pass the filter.
     * \else
     * 获取当前序列ID
     *
     * @return 要通过过滤的序列ID
     * \endif
     */
    public int getSequenceId() {
        throwInitializeException();
        return (int) getConfigValue("sequenceid");
    }

    /**
     * \if English
     * Get the current sequence id list.
     * \else
     * 获取当前序列ID列表
     * \endif
     */
    public SequenceIdItem[] getSequenceIdList() {
        throwInitializeException();
//        SequenceIdItem idList = new SequenceIdItem();
//        nGetSequenceIdList(mHandle, idList.getBytes());
//        boolean result = idList.parseBytes();
//        if (!result) {
//            throw new OBException("getSequenceIdList parse bytes error!");
//        }
        return mOutputSequenceIdList;
    }

    /**
     * \if English
     * Get the current sequence id list size.
     * \else
     * 获取当前序列ID列表大小
     * \endif
     */
    public int getSequenceIdListSize() {
        throwInitializeException();
//        return nGetSequenceIdListSize(mHandle);
        return mSequenceIdList.size();
    }

    @Override
    public void close() {
        super.close();
        if (mOutputSequenceIdList != null) {
            mOutputSequenceIdList = null;
        }
    }

    private static native long nCreate();

    private static native void nSelectSequenceId(long handle, int sequenceId);

    private static native int nGetSequenceId(long handle);

    private static native void nGetSequenceIdList(long handle, byte[] idList);

    private static native int nGetSequenceIdListSize(long handle);
}
