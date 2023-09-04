package com.orbbec.internal;

import android.text.TextUtils;

import java.io.File;

import com.orbbec.obsensor.OBException;

/**
 * @author lumiaozi
 * @date 2022/03/17
 * orbbec SDK internal utils class, not publish.
 */
public class OBLocalUtils {
    /**
     * Check whether the file is valid and throw an exception if it is not
     *
     * @param filename    File path
     * @param baseMessage The caller needs additional information
     */
    public static void checkFileAndThrow(String filename, String baseMessage) {
        String prefix = (null != baseMessage ? baseMessage + ", " : "");
        if (TextUtils.isEmpty(filename)) {
            throw new OBException(prefix + "Bad argument, filename is empty.");
        }

        File file = new File(filename);
        if (!file.exists() || !(file.canRead() || file.canWrite() || file.canExecute())) {
            throw new OBException(prefix + ", file('" + filename + "') not exists or has no permission.");
        }
    }
}
