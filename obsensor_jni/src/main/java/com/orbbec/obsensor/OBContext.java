package com.orbbec.obsensor;

import android.Manifest;
import android.content.Context;
import android.content.pm.PackageManager;
import android.os.Environment;
import android.text.TextUtils;
import android.util.Log;

import com.orbbec.internal.DeviceWatcher;

import java.io.File;

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
		 * \else
         * 设备添加通知及列表
         *
         * @param handle add device list handle
		 * \endif
         */
        void onDeviceAttach(long handle);

        /**
		 * \if English
	     * Device removal notification and list
         *
         * @param handle Remove device list handle
	     * \else
         * 设备移除通知及列表
         *
         * @param handle 移除设备列表句柄
		 * \endif
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
            File file = new File(Environment.getExternalStorageDirectory().getAbsolutePath() + "/Orbbec/" + context.getPackageName());
            if (!file.exists()) {
                try {
                    file.mkdirs();
                } catch (Exception ignore) {
                }
            }

            if (file.exists()) {
                Log.i(TAG, "OBContext config file file path: " + file.getAbsolutePath());
                mHasConfigLogFile = true;
                nSetLoggerToFile(LogSeverity.INFO.value(), file.getAbsolutePath(), 20, 150);
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
            nSetLoggerToFile(LogSeverity.INFO.value(), cacheFile.getAbsolutePath(), 20, 150);
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
            nSetLoggerToFile(LogSeverity.INFO.value(), innerCacheFile.getAbsolutePath(), 20, 50);
            return;
        }

        Log.w(TAG, "OBContext config file file path: null");
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
     * @param severity 	Output log level{@link LogSeverity}
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
     * @param severity 	Log output level {@link LogSeverity}
     * @param directory Save log file directory
	 * \else
     * 设置日志参数，并输出到指定文件
     *
     * @param severity 日志输出等级 {@link LogSeverity}
     * @param directory 存储日志文件路径
	 * \endif
     */
    public static void setLoggerToFile(LogSeverity severity, String directory) {
        nSetLoggerToFile(severity.value(), directory);
    }

    /**
     * \if English
     * Set log parameters and output to the specified file
     *
     * @param severity 	Log output level {@link LogSeverity}
     * @param directory Save log file directory
     * @param maxFileSize Max size of each log file, unit: MB
     * @param maxFileNum  Max log file number
     * \else
     * 设置日志参数，并输出到指定文件
     *
     * @param severity 日志输出等级 {@link LogSeverity}
     * @param directory 存储日志文件路径
     * @param maxFileSize 单个日志文件大小，单位：MB
     * @param maxFileNum 日志文件保存的最大数量
     * \endif
     */
    public static void setLoggerToFile(LogSeverity severity, String directory, long maxFileSize, long maxFileNum) {
        Log.d(TAG, "setLoggerToFile directory: " + directory + ", maxFileSize: " + maxFileSize + ", maxFileNum: " + maxFileNum);
        if (mHasConfigLogFile) {
            throw new OBException("Config log file path only just one time.");
        }
        if (mInstanceNum > 0) {
            throw new OBException("Config log file path must before OBContext create and static invoke.");
        }
        mHasConfigLogFile = true;
        nSetLoggerToFile(severity.value(), directory, maxFileSize, maxFileNum);
    }

    /**
     * \if English
     * Internal API:
     * Config SDK license path
     *
     * @param licenseFilePath license file path
     * @param key decrypt key,"OB_DEFAULT_DECRYPT_KEY" can be used to represent the default key
     * \else
     * 内部接口：
     * 配置SDK license文件
     *
     * @param licenseFilePath license文件路径
     * @param key 解密的key,可使用"OB_DEFAULT_DECRYPT_KEY"表示默认key
     *
     * \endif
     */
    public static void loadLicense(String licenseFilePath, String key) {
        if (TextUtils.isEmpty(licenseFilePath)) {
            throw new OBException("Invalid Argument. licenseFilePath is empty");
        }

        File file = new File(licenseFilePath);
        if (!file.exists() || !file.isFile() || !file.canRead() || 0 == file.length()) {
            throw new OBException("File invalid state. filePath: " + licenseFilePath
                    + ", exists: " + file.exists() + ", isFile: " + file.isFile()
                    + ", readable: " + file.canRead() + ", file.length: " + file.length());
        }

        nLoadLicense(licenseFilePath, key);
    }

    /**
	 * \if English
	 * Get SDK version name
     *
     * @return SDK version name
	 * \else
     * 获取SDK版本名称
     *
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
     *
     * @return SDK版本号
	 * \endif
     */
    public static int getVersionCode() {
        return BuildConfig.VERSION_CODE;
    }

    /**
     * \if English
     * @brief Get sdk stage version.
     * \else
     * @brief 获取SDK阶段版本号
     * \endif
     *
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
     *
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
     *
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
     * @brief enable or disable net device enumeration.
     * @brief after enable, the net device will be discovered automatically and can be retrieved by @ref queryDeviceList. The default state can be set in the
     * configuration file.
     *
     * @attention Net device enumeration by gvcp protocol, if the device is not in the same subnet as the host, it will be discovered but cannot be connected.
     *
     * @param enable true to enable, false to disable
     * \else
     * @brief 使能或禁用网络设备枚举。
     * @brief 使能后，网络设备会自动被发现，并且可以通过@ref queryDeviceList获取。默认状态可以在配置文件中设置。
     *
     * @attention 网络设备枚举通过gvcp协议，如果设备和主机不在同一个子网，会被发现但是无法连接。
     *
     * @param enable true 使能，false 禁用
     * \endif
     */
    public void enableNetDeviceEnumeration(boolean enable) {
        throwInitializeException();
        nEnableNetDeviceEnumeration(mHandle, enable);
    }

    /**
     * \if English
     * Network device enumeration is enable
     * \else
     * 网络设备枚举是否启用
     * \endif
     */
    public boolean isNetDeviceEnumerationEnable() {
        throwInitializeException();
        return nIsNetDeviceEnumerationEnable(mHandle);
    }

    /**
     * \if English
     * @brief Create a network device object
     *
     * @param address  ip address
     * @param port port
     * @return Target network device object
     * \else
     * @brief 创建网络设备对象
     *
     * @param address  ip 地址
     * @param port 端口号
     * @return 返回创建好的设备对象
     * \endif
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

    private static native void nSetLoggerToFile(int severity, String fileName, long maxFileSize, long maxFileNum);

    private static native void nSetLoggerToConsole(int severity);

    private static native void nLoadLicense(String filePath, String key);

    private static native void nSetDeviceChangedCallback(long handle, DeviceChangedCallbackImpl callback);

    private static native void nEnableDeviceClockSync(long handle, long repeatInterval);

    private static native long nCreateNetDevice(long handle, String address, int port);

    private static native void nEnableNetDeviceEnumeration(long handle, boolean enable);

    private static native boolean nIsNetDeviceEnumerationEnable(long handle);
}
