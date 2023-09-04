package com.orbbec.obsensor;

import java.nio.ByteBuffer;

public class OBDataBundle {
    // byte数组
    private ByteBuffer data;
    // 包含数据结构的个数
    private int itemCount;

    public ByteBuffer getData() {
        return data;
    }

    public void setData(ByteBuffer data) {
        this.data = data;
    }

    public int getItemCount() {
        return itemCount;
    }

    public void setItemCount(int itemCount) {
        this.itemCount = itemCount;
    }
}
