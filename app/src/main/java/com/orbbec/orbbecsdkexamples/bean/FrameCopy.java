package com.orbbec.orbbecsdkexamples.bean;

import com.orbbec.obsensor.types.Format;
import com.orbbec.obsensor.types.FrameType;

public class FrameCopy {
    public int width;
    public int height;
    public long systemTimeStamp;
    public long timeStamp;
    public long frameIndex;
    public byte[] data;
    public float[] point;
    public Format format;
    public int size;
    public FrameType frameType;


    public long getFrameIndex() {
        return frameIndex;
    }

    public Format getFormat() {
        return format;
    }

    public int getWidth() {
        return width;
    }

    public int getHeight() {
        return height;
    }

    public int getDataSize() {
        return size;
    }

    public byte[] getData() {
        return data;
    }

    public FrameType getStreamType() {
        return frameType;
    }

    public float[] getPoint() {
        return point;
    }

}
