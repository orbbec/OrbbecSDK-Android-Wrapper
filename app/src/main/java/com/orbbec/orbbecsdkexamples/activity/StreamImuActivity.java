package com.orbbec.orbbecsdkexamples.activity;

import android.content.res.Resources;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

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
    private volatile boolean mIsIMURunning;

    private TextView mImuPromptView;
    private TextView mAccelContentView;
    private TextView mGyroContentView;

    private final Locale locale = Locale.getDefault();

    private DeviceChangedCallback mDeviceChangedCallback = new DeviceChangedCallback() {
        @Override
        public void onDeviceAttach(DeviceList deviceList) {
            try {
                if (mPipeline == null) {
                    // 2.Create Device and initialize Pipeline through Device
                    mDevice = deviceList.getDevice(0);
                    mPipeline = new Pipeline(mDevice);

                    Config config = getConfig();

                    // 6.Start sensor stream
                    mPipeline.start(config);

                    // 7.Release config
                    config.close();

                    // 8.Create a thread to obtain Pipeline data
                    start();
                }
            } catch (Exception e) {
                Log.e(TAG, "onDeviceAttach: " + e.getMessage());
            } finally {
                // 9.Release DeviceList
                deviceList.close();
            }
        }

        @Override
        public void onDeviceDetach(DeviceList deviceList) {
            try {
                showToast(getString(R.string.please_access_device));
                if (mDevice != null) {
                    for (int i = 0, N = deviceList.getDeviceCount(); i < N; i++) {
                        String uid = deviceList.getUid(i);
                        DeviceInfo deviceInfo = mDevice.getInfo();
                        if (null != deviceInfo && TextUtils.equals(uid, deviceInfo.getUid())) {
                            stop();
                            mPipeline.close();
                            mPipeline = null;
                            mDevice.close();
                            mDevice = null;
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
    }

    @Override
    protected void onStart() {
        super.onStart();
        initSDK();
    }

    @Override
    protected void onStop() {
        try {
            stop();

            // Stop the Pipeline and release
            if (null != mPipeline) {
                mPipeline.stop();
                mPipeline.close();
            }

            // Release Device
            if (null != mDevice) {
                mDevice.close();
                mDevice = null;
            }
        } catch (Exception e) {
            Log.e(TAG, "onStop: " + e.getMessage());
        }
        releaseSDK();
        super.onStop();
    }

    @Override
    protected DeviceChangedCallback getDeviceChangedCallback() {
        return mDeviceChangedCallback;
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
        // 3.Configure which streams to enable or disable for the Pipeline by creating a Config
        Config config = new Config();
        // 4.1.Enable Accel stream
        config.enableAccelStream();
        // 4.2.Enable Gyro stream
        config.enableGyroStream();
        // 5.Only FrameSet that contains all types of data frames will be output
        config.setFrameAggregateOutputMode(FrameAggregateOutputMode.OB_FRAME_AGGREGATE_OUTPUT_ALL_TYPE_FRAME_REQUIRE);
        return config;
    }

    private void start() {
        mIsIMURunning = true;
        if (null == mIMUThread) {
            mIMUThread = new Thread(mIMURunnable);
            mIMUThread.start();
        }
    }

    private void stop() {
        mIsIMURunning = false;
        if (null != mIMUThread) {
            try {
                mIMUThread.join(300);
            } catch (InterruptedException e) {
                Log.e(TAG, "stop: " + e.getMessage());
            }
            mIMUThread = null;
        }
    }

    private Runnable mIMURunnable = () -> {
        boolean isPromptHidden = false;
        boolean isAccelVisible = false;
        boolean isGyroVisible = false;
        while (mIsIMURunning) {
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

                AccelFrame accelFrame = frameSet.getFrame(FrameType.ACCEL);
                GyroFrame gyroFrame = frameSet.getFrame(FrameType.GYRO);

                if (accelFrame != null) {
                    long accelIndex = accelFrame.getIndex();
                    BigInteger accelTimeStampUs = accelFrame.getTimeStampUs();
                    float accelTemperature = accelFrame.getTemperature();
                    FrameType accelType = accelFrame.getType();
                    if (accelIndex % 20 == 0) {
                        float[] accelValue = accelFrame.getAccelData();
                        printImuValue(accelValue, accelIndex, accelTimeStampUs, accelTemperature, accelType, "m/s^2");

                        if (!isAccelVisible) {
                            runOnUiThread(() -> mAccelContentView.setVisibility(View.VISIBLE));
                            isAccelVisible = true;
                        }
                    }
                    accelFrame.close();
                }

                if (gyroFrame != null) {
                    long gyroIndex = gyroFrame.getIndex();
                    BigInteger gyroTimeStampUs = gyroFrame.getTimeStampUs();
                    float gyroTemperature = gyroFrame.getTemperature();
                    FrameType gyroType = gyroFrame.getType();
                    if (gyroIndex % 20 == 0) {
                        float[] gyroValue = gyroFrame.getGyroData();
                        printImuValue(gyroValue, gyroIndex, gyroTimeStampUs, gyroTemperature, gyroType, "rad/s");

                        if (!isGyroVisible) {
                            runOnUiThread(() -> mGyroContentView.setVisibility(View.VISIBLE));
                            isGyroVisible = true;
                        }
                    }
                    gyroFrame.close();
                }
            } catch (Exception e) {
                Log.e(TAG, "run: " + e.getMessage());
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

    private void showToast(String msg) {
        runOnUiThread(() -> Toast.makeText(StreamImuActivity.this, msg, Toast.LENGTH_SHORT).show());
    }
}