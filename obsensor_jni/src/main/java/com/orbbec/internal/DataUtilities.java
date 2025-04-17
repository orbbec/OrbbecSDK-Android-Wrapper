package com.orbbec.internal;

import android.util.Log;

import com.orbbec.obsensor.OBException;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.DoubleBuffer;
import java.nio.FloatBuffer;
import java.nio.IntBuffer;
import java.nio.ShortBuffer;

/**
 * data processing tools
 */
public final class DataUtilities {

    /**
     * copy byte array
     *
     * @param src    source byte array
     * @param dst    destination byte array
     * @param offset start position of interception
     * @param length intercepted length
     */
    public static void copyBytes(byte[] src, byte[] dst, int offset, int length) {
        if (null == src || dst == src) {
            throw new OBException("src or dst is invalid!");
        }
        if (offset < 0 || length < 1) {
            throw new OBException("offset = " + offset + "; length = " + length);
        }
        if (dst.length < length) {
            throw new OBException("Out of dst range!");
        }
        System.arraycopy(src, offset, dst, offset, length);
    }

    /**
     * Intercept byte array
     *
     * @param bytes  array to be intercepted
     * @param offset start position of interception
     * @param length intercepted length
     * @return Return the truncated array
     */
    public static byte[] subBytes(byte[] bytes, int offset, int length) {
        if (offset < 0 || length < 1) {
            throw new OBException("length=" + length + "; index=" + offset);
        }
        if (offset + length > bytes.length) {
            throw new OBException("length=" + length + "; index=" + offset);
        }

        byte[] sub = new byte[length];
        System.arraycopy(bytes, offset, sub, 0, length);

        return sub;
    }

    /**
     * byte array concatenation
     *
     * @param src    array to be concatenated
     * @param dst    concatenated array
     * @param offset splicing start position
     * @param length splice length
     */
    public static void appendBytes(byte[] src, byte[] dst, int offset, int length) {
        if (offset < 0 || length < 1) {
            throw new OBException("length=" + length + "; index=" + offset);
        }
        if (offset + length > dst.length) {
            throw new OBException("length=" + length + "; index=" + offset);
        }

        System.arraycopy(src, 0, dst, offset, length);
    }

    private static void checkBytesValidity(int length, int stride, int count) {
        if (0 == length || 0 != (length % stride) || count > (length / stride)) {
            throw new OBException("bytes length error!");
        }
    }

    /**
     * Convert short data to byte array
     *
     * @param s data to be converted
     * @return Converted byte array
     */
    public static byte[] shortToBytes(short s) {
        ByteBuffer byteBuffer = ByteBuffer.allocate(Short.BYTES);
        byteBuffer.order(ByteOrder.nativeOrder());
        ShortBuffer shortBuffer = byteBuffer.asShortBuffer();
        shortBuffer.put(s);
        return byteBuffer.array();
    }

    /**
     * Convert short array to byte array
     *
     * @param ss array of shorts to convert
     * @return Converted byte array
     */
    public static byte[] shortsToBytes(short[] ss) {
        ByteBuffer byteBuffer = ByteBuffer.allocate(Short.BYTES * ss.length);
        byteBuffer.order(ByteOrder.nativeOrder());
        ShortBuffer shortBuffer = byteBuffer.asShortBuffer();
        shortBuffer.put(ss);
        return byteBuffer.array();
    }

    /**
     * Convert byte array to short data
     *
     * @param bytes The byte array to be converted
     * @return converted short data
     */
    public static short bytesToShort(byte[] bytes) {
        checkBytesValidity(bytes.length, Short.BYTES, 1);
        ByteBuffer buffer = ByteBuffer.wrap(bytes);
        buffer.order(ByteOrder.nativeOrder());
        return buffer.getShort();
    }

    /**
     * Convert byte array to short array
     *
     * @param bytes The byte array to be converted
     * @param count the length of the short array
     * @return converted array of shorts
     */
    public static short[] bytesToShorts(byte[] bytes, int count) {
        checkBytesValidity(bytes.length, Short.BYTES, count);
        short[] array = new short[bytes.length / Short.BYTES];
        ByteBuffer buffer = ByteBuffer.wrap(bytes);
        buffer.order(ByteOrder.nativeOrder());
        ShortBuffer shortBuffer = buffer.asShortBuffer();
        shortBuffer.get(array);
        return array;
    }

    /**
     * Convert int type data to byte array
     *
     * @param i int data to be converted
     * @return converted byte array
     */
    public static byte[] intToBytes(int i) {
        ByteBuffer byteBuffer = ByteBuffer.allocate(Integer.BYTES);
        byteBuffer.order(ByteOrder.nativeOrder());
        IntBuffer intBuffer = byteBuffer.asIntBuffer();
        intBuffer.put(i);
        return byteBuffer.array();
    }

    /**
     * Convert int array to byte array
     *
     * @param is the array of ints to convert
     * @return converted byte array
     */
    public static byte[] intsToBytes(int[] is) {
        ByteBuffer byteBuffer = ByteBuffer.allocate(Integer.BYTES * is.length);
        byteBuffer.order(ByteOrder.nativeOrder());
        IntBuffer intBuffer = byteBuffer.asIntBuffer();
        intBuffer.put(is);
        return byteBuffer.array();
    }

    /**
     * Convert byte array to int data
     *
     * @param bytes The byte array to be converted
     * @return converted int type data
     */
    public static int bytesToInt(byte[] bytes) {
        checkBytesValidity(bytes.length, Integer.BYTES, 1);
        ByteBuffer buffer = ByteBuffer.wrap(bytes);
        buffer.order(ByteOrder.nativeOrder());
        return buffer.getInt();
    }

    /**
     * Convert byte array to int array
     *
     * @param bytes The byte array to be converted
     * @param count the length of the converted int array
     * @return converted int array
     */
    public static int[] bytesToInts(byte[] bytes, int count) {
        checkBytesValidity(bytes.length, Integer.BYTES, count);
        int[] array = new int[bytes.length / Integer.BYTES];
        ByteBuffer buffer = ByteBuffer.wrap(bytes);
        buffer.order(ByteOrder.nativeOrder());
        IntBuffer intBuffer = buffer.asIntBuffer();
        intBuffer.get(array);
        return array;
    }

    /**
     * Convert float data type to byte array
     *
     * @param f float data type to convert
     * @return converted byte array
     */
    public static byte[] floatToBytes(float f) {
        ByteBuffer byteBuffer = ByteBuffer.allocate(Float.BYTES);
        byteBuffer.order(ByteOrder.nativeOrder());
        FloatBuffer floatBuffer = byteBuffer.asFloatBuffer();
        floatBuffer.put(f);
        return byteBuffer.array();
    }

    /**
     * Convert float array to byte array
     *
     * @param fs array of floats to convert
     * @return converted byte array
     */
    public static byte[] floatsToBytes(float[] fs) {
        ByteBuffer byteBuffer = ByteBuffer.allocate(Float.BYTES * fs.length);
        byteBuffer.order(ByteOrder.nativeOrder());
        FloatBuffer floatBuffer = byteBuffer.asFloatBuffer();
        floatBuffer.put(fs);
        return byteBuffer.array();
    }

    /**
     * Convert double data type to byte array
     *
     * @param d double data type to convert
     * @return converted byte array
     */
    public static byte[] doubleToBytes(double d) {
        ByteBuffer byteBuffer = ByteBuffer.allocate(Double.BYTES);
        byteBuffer.order(ByteOrder.nativeOrder());
        DoubleBuffer doubleBuffer = byteBuffer.asDoubleBuffer();
        doubleBuffer.put(d);
        return byteBuffer.array();
    }

    /**
     * Convert double array to byte array
     *
     * @param ds array of doubles to convert
     * @return converted byte array
     */
    public static byte[] doublesToBytes(double[] ds) {
        ByteBuffer byteBuffer = ByteBuffer.allocate(Double.BYTES * ds.length);
        byteBuffer.order(ByteOrder.nativeOrder());
        DoubleBuffer doubleBuffer = byteBuffer.asDoubleBuffer();
        doubleBuffer.put(ds);
        return byteBuffer.array();
    }

    /**
     * Convert byte array to float array
     *
     * @param bytes The byte array to be converted
     * @return the length of the converted float array
     */
    public static float bytesToFloat(byte[] bytes) {
        checkBytesValidity(bytes.length, Float.BYTES, 1);
        ByteBuffer buffer = ByteBuffer.wrap(bytes);
        buffer.order(ByteOrder.nativeOrder());
        return buffer.getFloat();
    }

    /**
     * Convert byte array to float array
     *
     * @param bytes The byte array to be converted
     * @param count the length of the converted float array
     * @return converted float array
     */
    public static float[] bytesToFloats(byte[] bytes, int count) {
        checkBytesValidity(bytes.length, Float.BYTES, count);
        float[] array = new float[bytes.length / Float.BYTES];
        ByteBuffer buffer = ByteBuffer.wrap(bytes);
        buffer.order(ByteOrder.nativeOrder());
        FloatBuffer floatBuffer = buffer.asFloatBuffer();
        floatBuffer.get(array);
        return array;
    }

    /**
     * Convert byte array to double array
     *
     * @param bytes The byte array to be converted
     * @return the length of the converted double array
     */
    public static double bytesToDouble(byte[] bytes) {
        checkBytesValidity(bytes.length, Double.BYTES, 1);
        ByteBuffer buffer = ByteBuffer.wrap(bytes);
        buffer.order(ByteOrder.nativeOrder());
        return buffer.getDouble();
    }

    /**
     * Convert byte array to double array
     *
     * @param bytes The byte array to be converted
     * @param count the length of the converted double array
     * @return converted double array
     */
    public static double[] bytesToDoubles(byte[] bytes, int count) {
        checkBytesValidity(bytes.length, Double.BYTES, count);
        double[] array = new double[bytes.length / Double.BYTES];
        ByteBuffer buffer = ByteBuffer.wrap(bytes);
        buffer.order(ByteOrder.nativeOrder());
        DoubleBuffer doubleBuffer = buffer.asDoubleBuffer();
        doubleBuffer.get(array);
        return array;
    }

    /**
     * Convert String to byte array
     *
     * @param s String to convert
     * @return converted byte array
     */
    public static byte[] stringToBytes(String s) {
        return s.getBytes();
    }

    /**
     * Convert byte array to String
     *
     * @param bytes The byte array to be converted
     * @return converted string
     */
    public static String bytesToString(byte[] bytes) {
        int length = bytes.length;
        for (int i = 0; i < bytes.length; i++) {
            if ('\0' == bytes[i]) {
                length = i;
                break;
            }
        }
        return new String(bytes, 0, length);
    }

    /**
     * Convert float array to string
     *
     * @param floats array of floats to convert
     * @return converted string
     */
    public static String floatsToString(float[] floats) {
        StringBuilder sb = new StringBuilder("{");
        for (int i = 0; i < floats.length; i++) {
            if (i == (floats.length - 1)) {
                sb.append(floats[i] + "}");
            } else {
                sb.append(floats[i] + ", ");
            }
        }
        return sb.toString();
    }

    /**
     * int array converted to string
     *
     * @param ints array of ints to convert
     * @return converted string
     */
    public static String intsToString(int[] ints) {
        StringBuilder sb = new StringBuilder("{");
        for (int i = 0; i < ints.length; i++) {
            if (i == (ints.length - 1)) {
                sb.append(ints[i] + "}");
            } else {
                sb.append(ints[i] + ", ");
            }
        }
        return sb.toString();
    }

    public static short bytesToUint8(byte[] bytes) {
        checkBytesValidity(bytes.length, Byte.BYTES, 1);
        ByteBuffer buffer = ByteBuffer.wrap(bytes);
        buffer.order(ByteOrder.nativeOrder());
        return (short) (buffer.get() & 0xFF);
    }

    public static byte[] uint8ToBytes(short value) {
        return new byte[]{(byte) ((value >> 8) & 0xFF)};
    }

    public static int bytesToUnit16(byte[] bytes) {
        checkBytesValidity(bytes.length, Short.BYTES, 1);
        ByteBuffer buffer = ByteBuffer.wrap(bytes);
        buffer.order(ByteOrder.nativeOrder());
        return buffer.getShort() & 0xFFFF;
    }

    public static byte[] uint16ToBytes(int value) {
        if (value < 0 || value > 0xFFFF) {
            throw new IllegalArgumentException("Value out of range for uint16: " + value);
        }
        ByteBuffer buffer = ByteBuffer.allocate(Short.BYTES);
        buffer.order(ByteOrder.nativeOrder());
        buffer.put((byte) ((value >> 8) & 0xFF));
        buffer.put((byte) ((value & 0xFF)));
        return buffer.array();
    }
}
