package com.orbbec.orbbecsdkexamples.activity;

import android.content.res.Resources;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.View;
import android.widget.TextView;

import com.orbbec.obsensor.AccelFrame;
import com.orbbec.obsensor.Config;
import com.orbbec.obsensor.Device;
import com.orbbec.obsensor.DeviceChangedCallback;
import com.orbbec.obsensor.DeviceList;
import com.orbbec.obsensor.FrameSet;
import com.orbbec.obsensor.GyroFrame;
import com.orbbec.obsensor.Pipeline;
import com.orbbec.obsensor.TypeHelper;
import com.orbbec.obsensor.types.DeviceInfo;
import com.orbbec.obsensor.types.FrameAggregateOutputMode;
import com.orbbec.obsensor.types.FrameType;
import com.orbbec.orbbecsdkexamples.R;

import java.math.BigInteger;
import java.util.Locale;

/**
 * Imu Viewer
 */
public class StreamImuActivity extends BaseActivity {
    private static final String TAG = "StreamImuActivity";

    private Device mDevice;
    private Pipeline mPipeline;
    private Thread mIMUThread;
    private volatile boolean mIsStreamRunning; // 统一变量名

    private TextView mImuPromptView;
    private TextView mAccelContentView;
    private TextView mGyroContentView;

    private final Locale locale = Locale.getDefault();

    private DeviceChangedCallback mDeviceChangedCallback = new DeviceChangedCallback() {
        @Override
        public void onDeviceAttach(DeviceList deviceList) {
            try {
                if (null == mDevice) {
                    mDevice = deviceList.getDevice(0);
                    Log.i(TAG, "onDeviceAttach: device connected");
                    runOnUiThread(() -> openStream());
                }
            } catch (Exception e) {
                Log.e(TAG, "onDeviceAttach: " + e.getMessage());
            } finally {
                deviceList.close();
            }
        }

        @Override
        public void onDeviceDetach(DeviceList deviceList) {
            try {
                if (mDevice != null) {
                    DeviceInfo deviceInfo = mDevice.getInfo();
                    if (deviceInfo != null) {
                        String currentUid = deviceInfo.getUid();
                        for (int i = 0, N = deviceList.getDeviceCount(); i < N; i++) {
                            String uid = deviceList.getUid(i);
                            if (TextUtils.equals(uid, currentUid)) {
                                Log.i(TAG, "onDeviceDetach: device released");
                                closeStream();
                                mDevice.close();
                                mDevice = null;
                                break;
                            }
                        }
                    }
                }
            } catch (Exception e) {
                Log.e(TAG, "onDeviceDetach: " + e.getMessage());
            } finally {
                deviceList.close();
            }
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setTitle("Stream-Imu");
        setContentView(R.layout.activity_stream_imu);
        initView();

        // 核心优化：在创建时初始化 SDK，确保切后台时不注销上下文
        initSDK();
    }

    @Override
    protected void onResume() {
        super.onResume();
        // 从后台回到前台时，若设备已连接且未出流则快速恢复流
        if (mDevice != null && !mIsStreamRunning) {
            openStream();
        }
    }

    @Override
    protected void onPause() {
        // 核心优化：仅停止数据流，不销毁 Pipeline 句柄
        stopStreamOnly();
        super.onPause();
    }

    @Override
    protected void onDestroy() {
        // 彻底退出页面时才释放硬件资源
        closeStream();
        if (mDevice != null) {
            try {
                mDevice.close();
            } catch (Exception e) {
                Log.e(TAG, "onDestroy close device: " + e.getMessage());
            }
            mDevice = null;
        }
        releaseSDK();
        super.onDestroy();
    }

    @Override
    protected DeviceChangedCallback getDeviceChangedCallback() {
        return mDeviceChangedCallback;
    }

    private synchronized void openStream() {
        if (mIsStreamRunning || mDevice == null) {
            return;
        }
        try {
            // 复用已有 Pipeline
            if (mPipeline == null) {
                mPipeline = new Pipeline(mDevice);
            }
            Config config = getConfig();
            mPipeline.start(config);
            config.close();

            mIsStreamRunning = true;
            if (null == mIMUThread) {
                mIMUThread = new Thread(mIMURunnable);
                mIMUThread.start();
            }
            Log.i(TAG, "openStream: recovered success");
        } catch (Exception e) {
            Log.e(TAG, "openStream failed: " + e.getMessage());
        }
    }

    private void stopStreamThread() {
        mIsStreamRunning = false;
        if (null != mIMUThread) {
            try {
                mIMUThread.join(300);
            } catch (InterruptedException e) {
                Log.e(TAG, "stopStreamThread join error: " + e.getMessage());
            }
            mIMUThread = null;
        }
    }

    private synchronized void stopStreamOnly() {
        stopStreamThread();
        if (null != mPipeline) {
            try {
                // 仅停止出流，保留对象
                mPipeline.stop();
            } catch (Exception e) {
                Log.e(TAG, "stopStreamOnly pipeline stop: " + e.getMessage());
            }
        }
    }

    private synchronized void closeStream() {
        stopStreamOnly();
        if (null != mPipeline) {
            try {
                mPipeline.close();
            } catch (Exception ignore) {}
            mPipeline = null;
        }
    }

    private void initView() {
        mImuPromptView = findViewById(R.id.imu_prompt);
        mAccelContentView = findViewById(R.id.imu_accel_content);
        mAccelContentView.setLineSpacing(1.0f, 1.2f);
        mGyroContentView = findViewById(R.id.imu_gyro_content);
        mGyroContentView.setLineSpacing(1.0f, 1.2f);

        DisplayMetrics displayMetrics = Resources.getSystem().getDisplayMetrics();
        float density = displayMetrics.density;
        int screenWidth = displayMetrics.widthPixels;
        int screenHeight = displayMetrics.heightPixels;

        int minSize = Math.min(screenWidth, screenHeight);
        float scaleFactor = 1f / 40f;
        float textSize = minSize * scaleFactor / density;

        mAccelContentView.setTextSize(textSize);
        mGyroContentView.setTextSize(textSize);
    }

    private Config getConfig() {
        Config config = new Config();
        config.enableAccelStream();
        config.enableGyroStream();
        config.setFrameAggregateOutputMode(FrameAggregateOutputMode.OB_FRAME_AGGREGATE_OUTPUT_ALL_TYPE_FRAME_REQUIRE);
        return config;
    }

    private Runnable mIMURunnable = () -> {
        boolean isPromptHidden = false;
        boolean isAccelVisible = false;
        boolean isGyroVisible = false;
        while (mIsStreamRunning) {
            try (FrameSet frameSet = mPipeline.waitForFrameSet(100)) {

                if (frameSet == null) {
                    if (isPromptHidden) {
                        runOnUiThread(() -> {
                            mImuPromptView.setVisibility(View.VISIBLE);
                            mAccelContentView.setVisibility(View.GONE);
                            mGyroContentView.setVisibility(View.GONE);
                        });
                        isPromptHidden = false;
                        isAccelVisible = false;
                        isGyroVisible = false;
                    }
                    continue;
                }
                if (!isPromptHidden) {
                    runOnUiThread(() -> mImuPromptView.setVisibility(View.GONE));
                    isPromptHidden = true;
                }

                try (AccelFrame accelFrame = frameSet.getFrame(FrameType.ACCEL)) {
                    if (accelFrame != null) {
                        long accelIndex = accelFrame.getIndex();
                        if (accelIndex % 20 == 0) {
                            float[] accelValue = accelFrame.getAccelData();
                            printImuValue(accelValue, accelIndex, accelFrame.getTimeStampUs(), accelFrame.getTemperature(), FrameType.ACCEL, "m/s^2");
                            if (!isAccelVisible) {
                                runOnUiThread(() -> mAccelContentView.setVisibility(View.VISIBLE));
                                isAccelVisible = true;
                            }
                        }
                    }
                }

                try (GyroFrame gyroFrame = frameSet.getFrame(FrameType.GYRO)) {
                    if (gyroFrame != null) {
                        long gyroIndex = gyroFrame.getIndex();
                        if (gyroIndex % 20 == 0) {
                            float[] gyroValue = gyroFrame.getGyroData();
                            printImuValue(gyroValue, gyroIndex, gyroFrame.getTimeStampUs(), gyroFrame.getTemperature(), FrameType.GYRO, "rad/s");
                            if (!isGyroVisible) {
                                runOnUiThread(() -> mGyroContentView.setVisibility(View.VISIBLE));
                                isGyroVisible = true;
                            }
                        }
                    }
                }
            } catch (Exception e) {
                Log.e(TAG, "run thread error: " + e.getMessage());
            }
        }
    };

    private void printImuValue(float[] obFloat3d, long index, BigInteger timeStampUs,
                               float temperature, FrameType type, String unitStr) {
        String typeStr = TypeHelper.convertOBFrameTypeToString(type);
        String imuData = String.format(locale, "Frame index: %d\n" +
                        "%s Frame: \n{\n" +
                        "\t\ttsp = %s us\n" +
                        "\t\ttemperature = %.2f °C\n" +
                        "\t\t%s.x = %.6f %s\n" +
                        "\t\t%s.y = %.6f %s\n" +
                        "\t\t%s.z = %.6f %s\n" +
                        "}\n",
                index, typeStr, timeStampUs, temperature,
                typeStr, obFloat3d[0], unitStr,
                typeStr, obFloat3d[1], unitStr,
                typeStr, obFloat3d[2], unitStr);
        runOnUiThread(() -> {
            if (type == FrameType.ACCEL) {
                mAccelContentView.setText(imuData);
            } else if (type == FrameType.GYRO) {
                mGyroContentView.setText(imuData);
            }
        });
    }

}