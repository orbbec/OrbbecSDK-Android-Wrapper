package com.orbbec.obsensor;

import android.util.Log;
import com.orbbec.obsensor.types.StreamType;

public class StreamProfileFactory {
    private static final String TAG = "StreamProfileFactory";

    public static StreamProfile create(long handle) {
        StreamType type = StreamType.get(nGetType(handle));
        if (type != null) {
            switch (type) {
                case IR:
                case IR_LEFT:
                case IR_RIGHT:
                case DEPTH:
                case COLOR:
                case VIDEO:
                    return new VideoStreamProfile(handle);
                case ACCEL:
                    return new AccelStreamProfile(handle);
                case GYRO:
                    return new GyroStreamProfile(handle);
                default:
                    Log.w(TAG, "create：Unsupported stream type.");
                    return null;
            }
        }
        Log.w(TAG, "create：Non-existent stream type.");
        return null;
    }

    private static native int nGetType(long handle);
}