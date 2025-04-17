package com.orbbec.obsensor;

public class XYTables extends LobClass {

    XYTables(long handle) {
        mHandle = handle;
    }

    @Override
    public void close() throws Exception {

    }

//    private float[] mXTable;
//    private float[] mYTable;
//    private int mWidth;
//    private int mHeight;
//
//    public float[] getXTable() {
//        throwInitializeException();
//        return mXTable;
//    }
//
//    public float[] getYTable() {
//        throwInitializeException();
//        return mYTable;
//    }
//
//    public int getWidth() {
//        throwInitializeException();
//        return mWidth;
//    }
//
//    public int getHeight() {
//        throwInitializeException();
//        return mHeight;
//    }
//
//    @Override
//    public int BYTES() {
//        return 0;
//    }
//
//    @Override
//    protected boolean parseBytesImpl(byte[] bytes) {
//        return true;
//    }
//
//    @Override
//    protected boolean wrapBytesImpl(byte[] bytes) {
//        return true;
//    }
//
//    @Override
//    public String toString() {
//        return "XYTables{" +
//                "mXTable=" + Arrays.toString(mXTable) +
//                ", mYTable=" + Arrays.toString(mYTable) +
//                ", mWidth=" + mWidth +
//                ", mHeight=" + mHeight +
//                '}';
//    }
}
