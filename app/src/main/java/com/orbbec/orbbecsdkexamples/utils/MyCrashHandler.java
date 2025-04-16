package com.orbbec.orbbecsdkexamples.utils;

import android.os.Environment;
import android.util.Log;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.io.Writer;
import java.text.SimpleDateFormat;
import java.util.Date;

public class MyCrashHandler implements Thread.UncaughtExceptionHandler{
    @Override
    public void uncaughtException(Thread t, Throwable e) {
        saveCrashInfoToFile(e);
    }

    /**
     * Save error message to file
     * @param ex
     */
    private void saveCrashInfoToFile(Throwable ex) {
        Writer writer = new StringWriter();
        PrintWriter printWriter = new PrintWriter(writer);
        ex.printStackTrace(printWriter);
        Throwable exCause = ex.getCause();
        while (exCause != null) {
            exCause.printStackTrace(printWriter);
            exCause =exCause.getCause();
        }
        printWriter.close();

        // Error log file name
        String fileName = getTimeStamp() + ".txt";
        // Determine whether the sd card can be used normally
        if(Environment.getExternalStorageState().equals(Environment.MEDIA_MOUNTED)) {
            // Log file storage location
            String path = FileUtils.getExternalSaveDir() + "/crash/";
            File file = new File(path);
            // Create folder
            Log.d("TAG", "file.exists() :" + file.exists());
            if(!file.exists()) {
                boolean isMkdirs = file.mkdirs();
                Log.d("TAG", "isMkdirs :" + isMkdirs);
            }
            try {
                FileOutputStream fileOutputStream = new FileOutputStream(path + fileName);
                fileOutputStream.write(writer.toString().getBytes());
                fileOutputStream.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    private String getTimeStamp(){
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        return sdf.format(new Date(Long.valueOf(System.currentTimeMillis())));
    }

}
