package com.orbbec.obsensor;

import android.Manifest;
import android.content.Context;
import android.content.pm.PackageManager;
import android.content.res.AssetManager;
import android.os.Build;
import android.os.Environment;
import android.util.Log;

import com.orbbec.internal.DeviceWatcher;
import com.orbbec.obsensor.types.LogSeverity;
import com.orbbec.obsensor.types.UvcBackendType;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;

/**
 * \if English
 * Orbbec SDK context, which can perform device monitoring, query, log management and version query
 * \else
 * Orbbec Sensor SDK 上下文，可以进行设备监听、查询、日志管理、版本查询
 * \endif
 */
public class OBContext extends LobClass {
    private static final String TAG = "OBContext";

    private static DeviceWatcher mDeviceWatcher;
    private static boolean mHasConfigLogFile = false;
    private static volatile int mInstanceNum = 0;

    private interface DeviceChangedCallbackImpl {

        /**
         * \if English
         * Device addition notification and list
         *
         * @param handle 新增设备列表句柄
         *               \else
         *               设备添加通知及列表
         * @param handle add device list handle
         *               \endif
         */
        void onDeviceAttach(long handle);

        /**
         * \if English
         * Device removal notification and list
         *
         * @param handle Remove device list handle
         *               \else
         *               设备移除通知及列表
         * @param handle 移除设备列表句柄
         *               \endif
         */
        void onDeviceDetach(long handle);
    }

    private DeviceChangedCallbackImpl mDeviceChangedCallbackImpl = new DeviceChangedCallbackImpl() {
        @Override
        public void onDeviceAttach(long handle) {
            if (null != mDeviceChangedCallback) {
                mDeviceChangedCallback.onDeviceAttach(new DeviceList(handle));
            }
        }

        @Override
        public void onDeviceDetach(long handle) {
            if (null != mDeviceChangedCallback) {
                mDeviceChangedCallback.onDeviceDetach(new DeviceList(handle));
            }
        }
    };

    private DeviceChangedCallback mDeviceChangedCallback;

    /**
     * \if English
     * Create SDK context, apply for authorization, and enable device listening
     *
     * @param context  For Android environment context, it is recommended to use the context of application
     * @param callback Device listening {@link DeviceChangedCallback}
     * \else
     * 创建SDK上下文, 并申请授权，开启设备监听
     *
     * @param context  Android环境上下文，建议用Application的context
     * @param callback 设备监听 {@link DeviceChangedCallback}
     * \endif
     */
    public OBContext(Context context, DeviceChangedCallback callback) {
        initInstanceNum();
        initExtensions(context);
        initDefaultLogConfig(context);
        mHandle = nCreate();
        nSetDeviceChangedCallback(mHandle, mDeviceChangedCallbackImpl);
        setDevicesChangedCallback(callback);
        if (mDeviceWatcher == null) {
            mDeviceWatcher = new DeviceWatcher(context);
        }
    }

    /**
     * \if English
     * Create SDK context, apply for authorization, and enable device listening
     *
     * @param context    For Android environment context, it is recommended to use the context of application
     * @param configPath Profile path
     * @param callback   Device listening {@link DeviceChangedCallback}
     * \else
     * 创建SDK上下文, 并申请授权，开启设备监听
     *
     * @param context    Android环境上下文，建议用Application的context
     * @param configPath 配置文件路径
     * @param callback   设备监听 {@link DeviceChangedCallback}
     * \endif
     */
    public OBContext(Context context, String configPath, DeviceChangedCallback callback) {
        initInstanceNum();
        initExtensions(context);
        initDefaultLogConfig(context);
        mHandle = nCreateWithConfig(configPath);
        nSetDeviceChangedCallback(mHandle, mDeviceChangedCallbackImpl);
        setDevicesChangedCallback(callback);
        if (mDeviceWatcher == null) {
            mDeviceWatcher = new DeviceWatcher(context);
        }
    }

    private void initInstanceNum() {
        synchronized (OBContext.class) {
            mInstanceNum++;
        }
    }

    private void decreaseInstanceNum() {
        synchronized (OBContext.class) {
            mInstanceNum = Math.max(0, mInstanceNum - 1);
        }
    }

    private void initDefaultLogConfig(Context context) {
        Log.d(TAG, "initDefaultLogConfig..., hasConfigLogFile: " + mHasConfigLogFile);
        if (mHasConfigLogFile) {
            return;
        }

        PackageManager pm = context.getPackageManager();
        if (PackageManager.PERMISSION_GRANTED == pm.checkPermission(Manifest.permission.WRITE_EXTERNAL_STORAGE, context.getPackageName())) {
            File file = new File(Environment.getExternalStorageDirectory(), "Orbbec/OrbbecSdkExample/Log");
            if (!file.exists()) {
                try {
                    file.mkdirs();
                } catch (Exception ignore) {
                }
            }

            if (file.exists()) {
                Log.i(TAG, "OBContext config file file path: " + file.getAbsolutePath());
                mHasConfigLogFile = true;
                nSetLoggerToFile(LogSeverity.INFO.value(), file.getAbsolutePath());
                return;
            }
        }

        File cacheFile = new File(context.getExternalCacheDir(), "OrbbecLog");
        if (!cacheFile.exists()) {
            try {
                cacheFile.mkdirs();
            } catch (Exception ignore) {
            }
        }
        if (cacheFile.exists()) {
            Log.i(TAG, "OBContext config file file path: " + cacheFile.getAbsolutePath());
            mHasConfigLogFile = true;
            nSetLoggerToFile(LogSeverity.INFO.value(), cacheFile.getAbsolutePath());
            return;
        }

        File innerCacheFile = new File(context.getCacheDir(), "OrbbecLog");
        if (!innerCacheFile.exists()) {
            try {
                innerCacheFile.mkdirs();
            } catch (Exception ignore) {
            }
        }
        if (innerCacheFile.exists()) {
            Log.i(TAG, "OBContext config file file path: " + innerCacheFile.getAbsolutePath());
            mHasConfigLogFile = true;
            nSetLoggerToFile(LogSeverity.INFO.value(), innerCacheFile.getAbsolutePath());
            return;
        }

        Log.w(TAG, "OBContext config file file path: null");
    }

    private void initExtensions(Context context) {
        AssetManager assetManager = context.getAssets();

        String abi = Build.SUPPORTED_ABIS[0];
        String extensionsDir = context.getFilesDir().getAbsolutePath() + File.separator + "extensions";

        File dir = new File(extensionsDir);
        if (!dir.exists() && !dir.mkdirs()) {
            Log.d(TAG, "initExtensions: Failed to create target directory: " + extensionsDir);
            return;
        }

        String[] files = dir.list();
        if (files == null || files.length == 0) {
            Log.d(TAG, "initExtensions: Directory is empty, extracting files...");
            extractFiles(assetManager, abi + File.separator + "extensions", extensionsDir);
        } else {
            Log.d(TAG, "initExtensions: Directory already contains files, skipping extraction.");
        }
        nSetExtensionsDirectory(extensionsDir);
    }

    /**
     * \if English
     * Extract the extension libraries from assets to the application's private storage directory.
     * \else
     * 从assets中提取扩展库到应用私有存储目录。
     * \endif
     */
    private void extractFiles(AssetManager assetManager, String parentDir, String targetDir) {
        try {
            String[] files = assetManager.list(parentDir);
            if (files != null) {
                for (String file : files) {
                    if (file.equals(".gitkeep")) {
                        continue;
                    }

                    String assetPath = parentDir + File.separator + file;
                    File outFile = new File(targetDir, file);
                    if (isDirectory(assetManager, assetPath)) {
                        boolean result = outFile.mkdirs();
                        if (result) {
                            extractFiles(assetManager, assetPath, outFile.getAbsolutePath());
                        }
                    } else {
                        copyAssetsFile(assetManager, assetPath, outFile);
                    }
                }
            }
        } catch (IOException e) {
            Log.e(TAG, "Error extracting extensions：", e);
        }
    }

    /**
     * \if English
     * Query whether the assets path is a directory
     * \else
     * 判断 assets 路径是否是目录
     * \endif
     */
    private boolean isDirectory(AssetManager assetManager, String assetPath) {
        try {
            String[] files = assetManager.list(assetPath);
            return files != null && files.length != 0;
        } catch (IOException e) {
            return false;
        }
    }

    /**
     * \if English
     * Copy assets file to the specified directory
     * \else
     * 将 assets 文件复制到指定目录
     * \endif
     */
    private void copyAssetsFile(AssetManager assetManager, String assetPath, File outFile) {
        try (InputStream inputStream = assetManager.open(assetPath);
             FileOutputStream outputStream = new FileOutputStream(outFile)) {
            byte[] buffer = new byte[2048];
            int length;
            while ((length = inputStream.read(buffer)) > 0) {
                outputStream.write(buffer, 0, length);
            }
        } catch (IOException e) {
            Log.e(TAG, "Error copying file: " + assetPath, e);
        }
    }

    /**
     * \if English
     * Query device list
     *
     * @return Device List {@link DeviceList}
     * \else
     * 查询设备列表
     *
     * @return 设备列表 {@link DeviceList}
     * \endif
     */
    public DeviceList queryDevices() {
        throwInitializeException();
        return new DeviceList(nQueryDevices(mHandle));
    }

    /**
     * \if English
     * Set device plug and unplug listener callback
     *
     * @param callback Device plugging and unplugging monitor {@link DeviceChangedCallback}
     * \else
     * 设置设备插拔监听回调
     *
     * @param callback 设备插拔监听 {@link DeviceChangedCallback}
     * \endif
     */
    public synchronized void setDevicesChangedCallback(DeviceChangedCallback callback) {
        mDeviceChangedCallback = callback;
    }

    /**
     * \if English
     * Remove device plug and unplug listener callback
     * \else
     * 移除设备插拔监听回调
     * \endif
     */
    public synchronized void removeDevicesChangedCallback() {
        mDeviceChangedCallback = null;
    }

    /**
     * \if English
     * Setting the level of the global log will affect both the log level output to the console and the log output to the file
     *
     * @param severity Output log level{@link LogSeverity}
     * \else
     * 设置全局日志的等级，会同时作用于输出到console和输出到文件的日志等级
     *
     * @param severity 输出日志等级{@link LogSeverity}
     * \endif
     */
    public static void setLoggerSeverity(LogSeverity severity) {
        nSetLoggerSeverity(severity.value());
    }

    /**
     * \if English
     * Set log parameters and output to console
     *
     * @param severity Log output level {@link LogSeverity}
     * \else
     * 设置日志参数，并输出到控制台
     *
     * @param severity 日志输出等级 {@link LogSeverity}
     * \endif
     */
    public static void setLoggerToConsole(LogSeverity severity) {
        nSetLoggerToConsole(severity.value());
    }

    /**
     * \if English
     * Set log parameters and output to the specified file
     *
     * @param severity  Log output level {@link LogSeverity}
     * @param directory Save log file directory
     * \else
     * 设置日志参数，并输出到指定文件
     *
     * @param severity  日志输出等级 {@link LogSeverity}
     * @param directory 存储日志文件路径
     * \endif
     */
    public static void setLoggerToFile(LogSeverity severity, String directory) {
        nSetLoggerToFile(severity.value(), directory);
    }

    /**
     * \if English
     * Get SDK version name
     *
     * @return SDK version name
     * \else
     * 获取SDK版本名称
     * @return SDK版本名称
     * \endif
     */
    public static String getVersionName() {
        return BuildConfig.VERSION_NAME;
    }

    /**
     * \if English
     * Get SDK version number
     *
     * @return SDK version number
     * \else
     * 获取SDK版本号
     * @return SDK版本号
     * \endif
     */
    public static int getVersionCode() {
        return BuildConfig.VERSION_CODE;
    }

    /**
     * \if English
     *
     * @brief Get sdk stage version.
     * \else
     * @brief 获取SDK阶段版本号
     * \endif
     */
    public static String getStageVersion() {
        return nGetStageVersion();
    }

    /**
     * \if English
     * Get SDK kernel version name
     *
     * @return SDK kernel version, major.minor.patch
     * \else
     * 获取SDK内核版本名称
     * @return SDK内核版本号，major.minor.patch
     * \endif
     */
    public static String getCoreVersionName() {
        return nGetVersionName();
    }

    /**
     * \if English
     * Get SDK kernel version number
     *
     * @return Kernel version number
     * \else
     * 获取SDK内核版本号
     * @return 内核版本号
     * \endif
     */
    public static int getCoreVersionCode() {
        return nGetVersionCode();
    }

    /**
     * \if English
     * Activates device clock synchronization to synchronize the clock of the host and all created devices (if supported).
     *
     * @param repeatIntervalMs The interval for auto-repeated synchronization, in milliseconds. If the value is 0, synchronization is performed only once.
     * \else
     * 使能设备时钟同步以同步主机和所有已创建设备的时钟（如果支持）。
     *
     * @param repeatIntervalMs 自动重复同步的时间间隔，以毫秒为单位。如果值为0，则仅执行一次同步。
     * \endif
     */
    public void enableDeviceClockSync(long repeatIntervalMs) {
        throwInitializeException();
        nEnableDeviceClockSync(mHandle, repeatIntervalMs);
    }

    /**
     * \if English
     * For linux, there are two ways to enable the UVC backend: libuvc and libusb. This function is used to set the backend type.
     * It is effective when the new device is created.
     * @attention This interface is only available for Linux.
     *
     * @param type The backend type to be used.
     * \else
     * 在Linux中，有两种方式启用UVC后端：libuvc和libusb。本函数用于设置后端类型。仅在新设备创建时有效。
     * @attention 该接口仅在Linux下有效。
     *
     * @param type 要使用的后端类型。
     * \endif
     */
    public void setUvcBackendType(UvcBackendType type) {
        throwInitializeException();
        nSetUvcBackendType(mHandle, type.value());
    }

    /**
     * \if English
     *
     * @param enable true to enable, false to disable
     *               \else
     * @param enable true 使能，false 禁用
     *               \endif
     * @brief enable or disable net device enumeration.
     * @brief after enable, the net device will be discovered automatically and can be retrieved by @ref queryDeviceList. The default state can be set in the
     * configuration file.
     * @attention Net device enumeration by gvcp protocol, if the device is not in the same subnet as the host, it will be discovered but cannot be connected.
     * @brief 使能或禁用网络设备枚举。
     * @brief 使能后，网络设备会自动被发现，并且可以通过@ref queryDeviceList获取。默认状态可以在配置文件中设置。
     * @attention 网络设备枚举通过gvcp协议，如果设备和主机不在同一个子网，会被发现但是无法连接。
     */
    public void enableNetDeviceEnumeration(boolean enable) {
        throwInitializeException();
        nEnableNetDeviceEnumeration(mHandle, enable);
    }

    /**
     * \if English
     *
     * @param address ip address
     * @param port    port
     * @param address ip 地址
     * @param port    端口号
     * @return Target network device object
     * \else
     * @return 返回创建好的设备对象
     * \endif
     * @brief Create a network device object
     * @brief 创建网络设备对象
     */
    public Device createNetDevice(String address, int port) {
        throwInitializeException();
        long devHandle = nCreateNetDevice(mHandle, address, port);
        if (0 != devHandle) {
            return new Device(devHandle);
        }
        return null;
    }

    /**
     * \if English
     * SDK Context resource release
     * \else
     * SDK Context资源释放
     * \endif
     */
    @Override
    public void close() {
        throwInitializeException();
        nSetDeviceChangedCallback(mHandle, null);
        removeDevicesChangedCallback();
        if (mDeviceWatcher != null) {
            mDeviceWatcher.close();
            mDeviceWatcher = null;
        }
        nDelete(mHandle);
        mHandle = 0;

        decreaseInstanceNum();
        if (mInstanceNum <= 0) {
            mHasConfigLogFile = false;
        }
    }

    private static native long nCreate();

    private static native long nCreateWithConfig(String configPath);

    private static native String nGetStageVersion();

    private static native String nGetVersionName();

    private static native int nGetVersionCode();

    private static native void nDelete(long handle);

    private static native long nQueryDevices(long handle);

    private static native void nSetLoggerSeverity(int severity);

    private static native void nSetLoggerToFile(int severity, String fileName);

    private static native void nSetLoggerToConsole(int severity);

    private static native void nSetDeviceChangedCallback(long handle, DeviceChangedCallbackImpl callback);

    private static native void nEnableDeviceClockSync(long handle, long repeatInterval);

    private static native void nSetUvcBackendType(long handle, int type);

    private static native long nCreateNetDevice(long handle, String address, int port);

    private static native void nEnableNetDeviceEnumeration(long handle, boolean enable);

    private static native void nSetExtensionsDirectory(String directory);
}
