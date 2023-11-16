package com.orbbec.orbbecsdkexamples.utils;

import android.content.Context;
import android.content.res.AssetManager;
import android.util.Log;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.nio.charset.Charset;

public class LocalUtils {
    private static final String TAG = "LocalUtils";
    public static String formatHex04(int value) {
        return String.format("0x%04x", value);
    }

    public static File initConfigXMLFromAsset(Context context) {
        final String fileName = "OrbbecSDKConfig_v1.0.xml";
        File appFiles = context.getFilesDir();
        if (!appFiles.exists()) {
            appFiles.mkdirs();
        }
        File configFile = new File(appFiles, fileName);
        if (!configFile.exists()) {
            try {
                configFile.createNewFile();
            } catch (IOException e) {
                Log.e(TAG, "init xml configFile failed. create file: '" + configFile.getAbsolutePath() + "' failed.");
                e.printStackTrace();
                return null;
            }
        }

        OutputStreamWriter writer = null;
        InputStreamReader reader = null;
        try {
            InputStream is = context.getAssets().open(fileName, AssetManager.ACCESS_STREAMING);
            reader = new InputStreamReader(is, Charset.forName("UTF-8"));

            FileOutputStream fos = new FileOutputStream(configFile, false);
            writer = new OutputStreamWriter(fos, Charset.forName("UTF-8"));

            char buf[] = new char[4096];
            int len = 0;
            while (-1 != (len = reader.read(buf, 0, buf.length))) {
                writer.write(buf, 0, len);
            }
            writer.flush();
            Log.d(TAG, "init config file success. config file: " + configFile.getAbsolutePath());
            return configFile;
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            if (null != reader) {
                try {
                    reader.close();
                } catch (IOException e) {
                    Log.w(TAG, "init xml config file. close asset config file failed. file: "
                            + ", error: " + e.getMessage());
                    e.printStackTrace();
                }
            }
            if (null != writer) {
                try {
                    writer.close();
                } catch (IOException e) {
                    Log.w(TAG, "init xml config file. close appConfigFile file failed. file: " + configFile.getAbsolutePath()
                            + ", error: " + e.getMessage());
                    e.printStackTrace();
                }
            }
        }
        return null;
    }
}
