package com.orbbec.orbbecsdkexamples.utils;

import android.graphics.Bitmap;
import android.util.Log;

import org.opencv.core.CvType;
import org.opencv.core.Mat;
import org.opencv.imgcodecs.Imgcodecs;
import org.opencv.imgproc.Imgproc;

import java.nio.ByteBuffer;

public class ImageUtils {
    private static final String TAG = "ImageUtils";

    public static Bitmap createBitmap(byte[] data, int width, int height) {
        int[] colors = rgbToArgb(data);
        if (colors == null) {
            return null;
        }
        Bitmap bmp = Bitmap.createBitmap(colors, width, height, Bitmap.Config.ARGB_8888);
        return bmp;
    }

    private static int byteToInt(byte data) {
        int highBit = (int) ((data >> 4) & 0x0F);
        int lowBit = (int) (0x0F & data);
        return highBit * 16 + lowBit;
    }


    /**
     * RGB format data to ARGB format
     *
     * @param data RGB data
     * @return ARGB data
     */
    public static int[] rgbToArgb(byte[] data) {
        int size = data.length;
        if (size == 0) {
            return null;
        }

        int arg = 0;
        if (size % 3 != 0) {
            arg = 1;
        }

        int[] color = new int[size / 3 + arg];
        int r, g, b;

        if (arg == 0) {
            for (int i = 0; i < color.length; ++i) {
                r = byteToInt(data[i * 3]);
                g = byteToInt(data[i * 3 + 1]);
                b = byteToInt(data[i * 3 + 2]);

                color[i] = (r << 16) | (g << 8) | b | 0xFF000000;
            }
        } else {
            for (int i = 0; i < color.length - 1; ++i) {
                r = byteToInt(data[i * 3]);
                g = byteToInt(data[i * 3 + 1]);
                b = byteToInt(data[i * 3 + 2]);
                color[i] = (r << 16) | (g << 8) | b | 0xFF000000;
            }
            color[color.length - 1] = 0xFF000000;
        }
        return color;
    }

    /**
     * Convert depth data to RGB
     *
     * @param src Depth frame data
     * @param dst Rgb data
     */
    public static void depthToRgb(ByteBuffer src, ByteBuffer dst) {
        nDepthToRgb(src, dst);
    }

    /**
     * Overlay the color map and the depth map
     *
     * @param colorData Color frame data
     * @param depthData Depth frame data
     * @param colorW    Color width
     * @param colorH    Color height
     * @param depthW    Depth width
     * @param depthH    Depth height
     * @param alpha     Depth map transparency
     * @return Overlaid data
     */
    public static byte[] depthAlignToColor(ByteBuffer colorData, ByteBuffer depthData, int colorW, int colorH,
                                           int depthW, int depthH, float alpha) {
        return nDepthAlignToColor(colorData, depthData, colorW, colorH, depthW, depthH, alpha);
    }

    /**
     * yuyv format to rgb
     *
     * @param srcBuffer yuyv frame data
     * @param dstBuffer RGB data
     * @param width     frame width
     * @param height    frame height
     */
    public static void yuyvToRgb(ByteBuffer srcBuffer, ByteBuffer dstBuffer, int width, int height) {
        nYuyvToRgb(srcBuffer, dstBuffer, width, height);
    }

    /**
     * uyvy format to rgb
     *
     * @param srcBuffer uyvy frame data
     * @param dstBuffer RGB data
     * @param width     frame width
     * @param height    frame height
     */
    public static void uyvyToRgb(ByteBuffer srcBuffer, ByteBuffer dstBuffer, int width, int height) {
        nUyvyToRgb(srcBuffer, dstBuffer, width, height);
    }

    public static void y8ToRgb(ByteBuffer srcBuffer, ByteBuffer dstBuffer, int width, int height) {
        nY8ToRgb(srcBuffer, dstBuffer, width, height);
    }

    /**
     * mjpg format to rgb
     *
     * @param srcBuffer mjpg frame data
     * @param dstBuffer RGB data
     * @param width     frame width
     * @param height    frame height
     */
    public static void mjpgToRgb(ByteBuffer srcBuffer, ByteBuffer dstBuffer, int width, int height) {
        try {
            byte[] mjpgData = new byte[srcBuffer.remaining()];
            srcBuffer.get(mjpgData);

            Mat rawData = new Mat(1, mjpgData.length, CvType.CV_8UC1);
            rawData.put(0, 0, mjpgData);

            Mat bgrMat = Imgcodecs.imdecode(rawData, Imgcodecs.IMREAD_COLOR);
            if (bgrMat.empty()) {
                Log.w(TAG, "Decoded image size does not match expected: expected " + width + "x" + height +
                        ", but got " + bgrMat.cols() + "x" + bgrMat.rows() +
                        ". Subsequent processing will use the decoded size.");
                // Imgproc.resize(bgrMat, bgrMat, new org.opencv.core.Size(width, height));
            }

            Mat rgbMat = new Mat();
            Imgproc.cvtColor(bgrMat, rgbMat, Imgproc.COLOR_BGR2RGB);

            int rgbDataSize = rgbMat.cols() * rgbMat.rows() * rgbMat.channels();
            byte[] rgbData = new byte[rgbDataSize];
            rgbMat.get(0, 0, rgbData);

            dstBuffer.clear();
            dstBuffer.put(rgbData);
            dstBuffer.flip();

            rawData.release();
            bgrMat.release();
            rgbMat.release();
        } catch (Exception e) {
            Log.e(TAG, "Error in mjpgToRGB: ", e);
        }
    }

    private static native void nDepthToRgb(ByteBuffer srcBuffer, ByteBuffer dstBuffer);

    private static native byte[] nDepthAlignToColor(ByteBuffer colorData, ByteBuffer depthData, int colorW, int colorH,
                                                    int depthW, int depthH, float alpha);

    private static native void nYuyvToRgb(ByteBuffer srcBuffer, ByteBuffer dstBuffer, int width, int height);

    private static native void nUyvyToRgb(ByteBuffer srcBuffer, ByteBuffer dstBuffer, int width, int height);

    private static native void nY8ToRgb(ByteBuffer srcBuffer, ByteBuffer dstBuffer, int width, int height);

    public static native void nScalePrecisionToDepthPixel(ByteBuffer depthBuffer,
                                                          int w, int h, int size, float scale);
}
