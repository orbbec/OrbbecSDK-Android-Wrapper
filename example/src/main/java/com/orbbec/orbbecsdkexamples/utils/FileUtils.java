package com.orbbec.orbbecsdkexamples.utils;

import android.graphics.Bitmap;
import android.os.Environment;
import android.util.Log;
import com.orbbec.obsensor.FrameType;
import com.orbbec.orbbecsdkexamples.bean.FrameCopy;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.PrintWriter;

public class FileUtils {
    private static final String TAG = "FileUtils";

    private static String imageDirFile = "/sdcard/imagePng/";

    private static String imageDirFileSync = "/sdcard/imagePngSync/";

    /**
     * Determine whether the file exists
     * @param filePath file path
     * @return true：file exits, false：not exists
     */
    public static boolean isFileExists(String filePath) {
        File file = new File(filePath);
        return file.exists();
    }

    public static void savePointCloud(String fileName, float[] data) {
        try {
            File file = new File(fileName);
            FileOutputStream fos = new FileOutputStream(file);
            PrintWriter writer = new PrintWriter(fos);
            writer.write("ply\n");
            writer.write("format ascii 1.0\n");
            writer.write("element vertex " + data.length / 3 + "\n");
            writer.write("property float x\n");
            writer.write("property float y\n");
            writer.write("property float z\n");
            writer.write("end_header\n");
            writer.flush();

            for (int i = 0; i < data.length; i += 3) {
                writer.print(data[i]);
                writer.print(" ");
                writer.print(data[i + 1]);
                writer.print(" ");
                writer.print(data[i + 2]);
                writer.print("\n");
            }
            writer.close();
            fos.close();
        } catch (Exception e) {
            Log.e(TAG, "exception: " + e.getMessage());
        }
    }

    public static void saveRGBPointCloud(String fileName, float[] data) {

        try {
            File file = new File(fileName);
            FileOutputStream fos = new FileOutputStream(file);
            PrintWriter writer = new PrintWriter(fos);
            writer.write("ply\n");
            writer.write("format ascii 1.0\n");
            writer.write("element vertex " + data.length / 6 + "\n");
            writer.write("property float x\n");
            writer.write("property float y\n");
            writer.write("property float z\n");
            writer.write("property uchar red\n");
            writer.write("property uchar green\n");
            writer.write("property uchar blue\n");
            writer.write("end_header\n");
            writer.flush();

            for (int i = 0; i < data.length; i += 6) {
                writer.print(data[i]);
                writer.print(" ");
                writer.print(data[i + 1]);
                writer.print(" ");
                writer.print(data[i + 2]);
                writer.print(" ");
                writer.print((int) data[i + 3]);
                writer.print(" ");
                writer.print((int) data[i + 4]);
                writer.print(" ");
                writer.print((int) data[i + 5]);
                writer.print("\n");
            }
            writer.close();
            fos.close();
        } catch (Exception e) {
            Log.e(TAG, "exception: " + e.getMessage());
        }
    }

    public static void saveImage(FrameCopy frame) {
        try {
            // Determine whether the sd card can be used normally
            if (!Environment.getExternalStorageState().equals(Environment.MEDIA_MOUNTED)) {
                return;
            }
            File imageFile = new File(imageDirFile);
            if (!imageFile.exists()) {
                boolean isMkdirs = imageFile.mkdirs();
                Log.d(TAG, "isMkdirs :" + isMkdirs);
            }

            FrameType type = frame.getStreamType();
            Log.d(TAG, "type :" + type);
            if (type == FrameType.DEPTH) {
                saveRawImage(imageDirFile + "Depth_" + frame.getWidth() + "x" + frame.getHeight() + "_" + frame.timeStamp + ".raw", frame.getData());
            } else if (type == FrameType.COLOR) {
                try {
                    FileOutputStream fileOutputStream = new FileOutputStream(imageDirFile + "COLOR_" + frame.getWidth() + "x" + frame.getHeight() + "_" + frame.timeStamp + ".png");
                    Bitmap bitmap = ImageUtils.createBitmap(frame.getData(), frame.getWidth(), frame.getHeight());
                    bitmap.compress(Bitmap.CompressFormat.PNG, 90, fileOutputStream);
                } catch (IOException e) {
                    e.printStackTrace();
                    Log.d(TAG, "saveRawImage catch :" + e.getMessage());
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
            Log.d(TAG, "saveImage catch :" + e.getMessage());
        }
    }

    public static void saveImageSync(byte[] data, String type) {
        try {
            // Determine whether the sd card can be used normally
            if (!Environment.getExternalStorageState().equals(Environment.MEDIA_MOUNTED)) {
                return;
            }
            File imageFile = new File(imageDirFileSync);
            if (!imageFile.exists()) {
                boolean isMkdirs = imageFile.mkdirs();
                Log.d(TAG, "isMkdirs :" + isMkdirs);
            }
            Log.d(TAG, "type :" + type);
            saveRawImage(imageDirFileSync + type + System.currentTimeMillis() + ".raw", data);
            //saveRawImage(imageDirFileSync + type + System.currentTimeMillis() + ".png", data);
        } catch (Exception e) {
            e.printStackTrace();
            Log.d(TAG, "saveImage catch :" + e.getMessage());
        }
    }


    public static void saveRawImage(String fileName, byte[] rawData) {
        FileOutputStream bitFos = null;
        try {
            bitFos = new FileOutputStream(fileName);
            bitFos.write(rawData);
            bitFos.flush();
        } catch (IOException e) {
            e.printStackTrace();
            Log.d(TAG, "saveRawImage catch :" + e.getMessage());
        } finally {
            if (bitFos != null) {
                try {
                    bitFos.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }
}
