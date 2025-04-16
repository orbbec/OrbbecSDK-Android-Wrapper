package com.orbbec.orbbecsdkexamples.activity;

import android.content.res.Resources;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.text.TextUtils;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.orbbec.obsensor.AccelFrame;
import com.orbbec.obsensor.AccelStreamProfile;
import com.orbbec.obsensor.ColorFrame;
import com.orbbec.obsensor.Config;
import com.orbbec.obsensor.DepthFrame;
import com.orbbec.obsensor.Device;
import com.orbbec.obsensor.DeviceChangedCallback;
import com.orbbec.obsensor.DeviceList;
import com.orbbec.obsensor.FrameSet;
import com.orbbec.obsensor.GyroFrame;
import com.orbbec.obsensor.GyroStreamProfile;
import com.orbbec.obsensor.IRFrame;
import com.orbbec.obsensor.Pipeline;
import com.orbbec.obsensor.Sensor;
import com.orbbec.obsensor.StreamProfileList;
import com.orbbec.obsensor.types.DeviceInfo;
import com.orbbec.obsensor.types.FrameType;
import com.orbbec.obsensor.types.SensorType;
import com.orbbec.obsensor.types.StreamType;
import com.orbbec.orbbecsdkexamples.R;
import com.orbbec.orbbecsdkexamples.view.OBGLView;

import java.util.List;
import java.util.Locale;

public class StreamMultiStreamsActivity extends BaseActivity {

    private static final String TAG = "StreamMultiStreamsActivity";

    private Device mDevice;
    private Pipeline mPipeline;
    private Thread mStreamThread;
    private volatile boolean mIsStreamRunning;
    private OBGLView mColorView;
    private OBGLView mDepthView;
    private OBGLView mIrView;
    private OBGLView mIrLeftView;
    private OBGLView mIrRightView;

    private final int MSG_UPDATE_IMU_INFO = 1;
    private Sensor mAccelSensor;
    private Sensor mGyroSensor;
    private AccelStreamProfile mAccelStreamProfile;
    private GyroStreamProfile mGyroStreamProfile;

    private FrameLayout mIrFL;
    private FrameLayout mIrLeftFL;
    private FrameLayout mIrRightFL;

    private TextView mAccelTimeStampView;
    private TextView mAccelTemperatureView;
    private TextView mAccelXView;
    private TextView mAccelYView;
    private TextView mAccelZView;

    private TextView mGyroTimeStampView;
    private TextView mGyroTemperatureView;
    private TextView mGyroXView;
    private TextView mGyroYView;
    private TextView mGyroZView;

    private final Object mAccelLock = new Object();
    private AccelFrame mAccelFrame;

    private final Object mGyroLock = new Object();
    private GyroFrame mGyroFrame;

    private boolean mIsAccelStarted = false;
    private boolean mIsGyroStarted = false;

    private final Locale locale = Locale.getDefault();

    private DeviceChangedCallback mDeviceChangedCallback = new DeviceChangedCallback() {

        @Override
        public void onDeviceAttach(DeviceList deviceList) {
            try {
                if (mPipeline == null) {
                    // 2.Create Device and initialize Pipeline through Device
                    mDevice = deviceList.getDevice(0);
                    mPipeline = new Pipeline(mDevice);

                    // 3.Enumerate and config all sensors
                    Config config = initStreamProfile();
                    if (config == null) {
                        showToast(getString(R.string.init_stream_profile_failed));
                        mPipeline.close();
                        mPipeline = null;
                        mDevice.close();
                        mDevice = null;
                        return;
                    }

                    // 4.Get Acceleration and Gyroscope Sensor through Device
                    mAccelSensor = mDevice.getSensor(SensorType.ACCEL);
                    mGyroSensor = mDevice.getSensor(SensorType.GYRO);

                    if (mAccelSensor == null || mGyroSensor == null) {
                        showToast(getString(R.string.device_not_support_imu));
                        return;
                    } else {
                        // 5.Get accelerometer and gyroscope StreamProfile List
                        StreamProfileList accelProfileList = mAccelSensor.getStreamProfileList();
                        if (null != accelProfileList) {
                            mAccelStreamProfile = accelProfileList.getProfile(0).as(StreamType.ACCEL);
                            accelProfileList.close();
                        }

                        StreamProfileList gyroProfileList = mGyroSensor.getStreamProfileList();
                        if (null != gyroProfileList) {
                            mGyroStreamProfile = gyroProfileList.getProfile(0).as(StreamType.GYRO);
                            gyroProfileList.close();
                        }
                    }

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
                // 9.Release device list resources
                deviceList.close();
            }
        }

        @Override
        public void onDeviceDetach(DeviceList deviceList) {
            try {
                if (mDevice != null) {
                    for (int i = 0, N = deviceList.getDeviceCount(); i < N; i++) {
                        String uid = deviceList.getUid(i);
                        DeviceInfo deviceInfo = mDevice.getInfo();
                        if (deviceInfo != null && TextUtils.equals(uid, deviceInfo.getUid())) {
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

    private Handler mHandler = new Handler(Looper.getMainLooper()) {
        @Override
        public void handleMessage(@NonNull Message msg) {
            if (msg.what == MSG_UPDATE_IMU_INFO) {
                drawImuInfo();
                sendEmptyMessageDelayed(MSG_UPDATE_IMU_INFO, 100);
            }
        }
    };

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setTitle("Stream-Multi Streams");
        setContentView(R.layout.activity_stream_multi_streams);
        initView();
    }

    @Override
    protected void onStart() {
        super.onStart();
        mHandler.sendEmptyMessage(MSG_UPDATE_IMU_INFO);
        initSDK();
    }

    @Override
    protected void onStop() {
        mHandler.removeMessages(MSG_UPDATE_IMU_INFO);
        try {
            // Stop getting Pipeline data
            stop();

            // Release Frame
            synchronized (mAccelLock) {
                if (null != mAccelFrame) {
                    mAccelFrame.close();
                    mAccelFrame = null;
                }
            }

            // Release Frame
            synchronized (mGyroLock) {
                if (null != mGyroFrame) {
                    mGyroFrame.close();
                    mGyroFrame = null;
                }
            }

            // Release accelerometer StreamProfile
            if (null != mAccelStreamProfile) {
                mAccelStreamProfile.close();
                mAccelStreamProfile = null;
            }

            // Release gyroscope StreamProfile
            if (null != mGyroStreamProfile) {
                mGyroStreamProfile.close();
                mGyroStreamProfile = null;
            }

            // Stop the Pipeline and release
            if (null != mPipeline) {
                mPipeline.stop();
                mPipeline.close();
            }

            // Release Device
            if (mDevice != null) {
                mDevice.close();
            }
        } catch (Exception e) {
            Log.e(TAG, "onStop: " + e.getMessage());
        }
        releaseSDK();
        super.onStop();
    }

    private void initView() {
        mColorView = findViewById(R.id.multi_stream_color);
        mDepthView = findViewById(R.id.multi_stream_depth);
        mIrView = findViewById(R.id.multi_stream_ir);
        mIrLeftView = findViewById(R.id.multi_stream_ir_left);
        mIrRightView = findViewById(R.id.multi_stream_ir_right);

        mIrFL = findViewById(R.id.fl_multi_stream_ir);
        mIrLeftFL = findViewById(R.id.fl_multi_stream_ir_left);
        mIrRightFL = findViewById(R.id.fl_multi_stream_ir_right);

        mAccelTimeStampView = findViewById(R.id.multi_stream_accel_timestamp);
        mAccelTemperatureView = findViewById(R.id.multi_stream_accel_temperature);
        mAccelXView = findViewById(R.id.multi_stream_accel_x);
        mAccelYView = findViewById(R.id.multi_stream_accel_y);
        mAccelZView = findViewById(R.id.multi_stream_accel_z);

        mGyroTimeStampView = findViewById(R.id.multi_stream_gyro_timestamp);
        mGyroTemperatureView = findViewById(R.id.multi_stream_gyro_temperature);
        mGyroXView = findViewById(R.id.multi_stream_gyro_x);
        mGyroYView = findViewById(R.id.multi_stream_gyro_y);
        mGyroZView = findViewById(R.id.multi_stream_gyro_z);

        DisplayMetrics displayMetrics = Resources.getSystem().getDisplayMetrics();
        float density = displayMetrics.density;
        int screenWidth = displayMetrics.widthPixels;

        float textSize = screenWidth * 0.02f / density;
        mAccelTimeStampView.setTextSize(textSize);
        mAccelTemperatureView.setTextSize(textSize);
        mAccelXView.setTextSize(textSize);
        mAccelYView.setTextSize(textSize);
        mAccelZView.setTextSize(textSize);
        mGyroTimeStampView.setTextSize(textSize);
        mGyroTemperatureView.setTextSize(textSize);
        mGyroXView.setTextSize(textSize);
        mGyroYView.setTextSize(textSize);
        mGyroZView.setTextSize(textSize);
    }

    private Config initStreamProfile() {
        Config config = new Config();

        List<Sensor> sensorList = mDevice.querySensors();
        for (Sensor sensor : sensorList) {
            SensorType type = sensor.getType();
            if (type == SensorType.ACCEL || type == SensorType.GYRO) {
                continue;
            }
            runOnUiThread(() -> {
                if (type == SensorType.IR) {
                    mIrFL.setVisibility(View.VISIBLE);
                } else if (type == SensorType.IR_LEFT) {
                    mIrLeftFL.setVisibility(View.VISIBLE);
                } else if (type == SensorType.IR_RIGHT) {
                    mIrRightFL.setVisibility(View.VISIBLE);
                }
            });
            config.enableStream(type);
        }
        return config;
    }

    private void start() {
        mIsStreamRunning = true;
        if (mStreamThread == null) {
            mStreamThread = new Thread(mStreamRunnable);
            mStreamThread.start();
        }

        startAccelStream();
        startGyroStream();
    }

    private void startAccelStream() {
        try {
            if (mAccelStreamProfile != null) {
                mAccelSensor.start(mAccelStreamProfile, frame -> {
                    AccelFrame accelFrame = frame.as(FrameType.ACCEL);

                    synchronized (mAccelLock) {
                        if (null != mAccelFrame) {
                            mAccelFrame.close();
                            mAccelFrame = null;
                        }
                        mAccelFrame = accelFrame;
                    }
                });
                mIsAccelStarted = true;
            }
        } catch (Exception e) {
            Log.e(TAG, "startAccelStream: " + e.getMessage());
        }
    }

    private void startGyroStream() {
        try {
            if (mGyroStreamProfile != null) {
                mGyroSensor.start(mGyroStreamProfile, frame -> {
                    GyroFrame gyroFrame = frame.as(FrameType.GYRO);

                    synchronized (mGyroLock) {
                        if (null != mGyroFrame) {
                            mGyroFrame.close();
                            mGyroFrame = null;
                        }
                        mGyroFrame = gyroFrame;
                    }
                });
                mIsGyroStarted = true;
            }
        } catch (Exception e) {
            Log.e(TAG, "startGyroStream: " + e.getMessage());
        }
    }

    private void stop() {
        mIsStreamRunning = false;
        try {
            if (mStreamThread != null) {
                mStreamThread.join(300);
                mStreamThread = null;
            }

            if (mAccelSensor != null) {
                mAccelSensor.stop();
            }
            mIsAccelStarted = false;

            if (mGyroSensor != null) {
                mGyroSensor.stop();
            }
            mIsGyroStarted = false;
        } catch (Exception e) {
            Log.e(TAG, "stop: " + e.getMessage());
        }
    }

    private Runnable mStreamRunnable = () -> {
        while (mIsStreamRunning) {
            try (FrameSet frameSet = mPipeline.waitForFrameSet(100)) {
                if (frameSet == null) continue;

                ColorFrame colorFrame = frameSet.getFrame(FrameType.COLOR);
                DepthFrame depthFrame = frameSet.getFrame(FrameType.DEPTH);
                IRFrame irFrame = frameSet.getFrame(FrameType.IR);
                IRFrame irLeftFrame = frameSet.getFrame(FrameType.IR_LEFT);
                IRFrame irRightFrame = frameSet.getFrame(FrameType.IR_RIGHT);

                if (colorFrame != null) {
                    byte[] colorFrameData = new byte[colorFrame.getDataSize()];
                    colorFrame.getData(colorFrameData);
                    mColorView.update(colorFrame.getWidth(), colorFrame.getHeight(), StreamType.COLOR, colorFrame.getFormat(), colorFrameData, 1.0f);
                    colorFrame.close();
                }
                if (depthFrame != null) {
                    byte[] depthFrameData = new byte[depthFrame.getDataSize()];
                    depthFrame.getData(depthFrameData);
                    mDepthView.update(depthFrame.getWidth(), depthFrame.getHeight(), StreamType.DEPTH, depthFrame.getFormat(), depthFrameData, depthFrame.getValueScale());
                    depthFrame.close();
                }
                if (irFrame != null) {
                    byte[] irFrameData = new byte[irFrame.getDataSize()];
                    irFrame.getData(irFrameData);
                    mIrView.update(irFrame.getWidth(), irFrame.getHeight(), StreamType.IR, irFrame.getFormat(), irFrameData, 1.0f);
                    irFrame.close();
                }
                if (irLeftFrame != null) {
                    byte[] leftFrameData = new byte[irLeftFrame.getDataSize()];
                    irLeftFrame.getData(leftFrameData);
                    mIrLeftView.update(irLeftFrame.getWidth(), irLeftFrame.getHeight(), StreamType.IR, irLeftFrame.getFormat(), leftFrameData, 1.0f);
                    irLeftFrame.close();
                }
                if (irRightFrame != null) {
                    byte[] rightFrameData = new byte[irRightFrame.getDataSize()];
                    irRightFrame.getData(rightFrameData);
                    mIrRightView.update(irRightFrame.getWidth(), irRightFrame.getHeight(), StreamType.IR, irRightFrame.getFormat(), rightFrameData, 1.0f);
                    irRightFrame.close();
                }
            } catch (Exception e) {
                Log.w(TAG, "run: " + e.getMessage());
            }
        }
    };

    private void drawImuInfo() {
        if (mIsAccelStarted || mIsGyroStarted) {
            synchronized (mAccelLock) {
                if (null != mAccelFrame) {
                    mAccelTimeStampView.setText(String.format(locale, "AccelTimestamp:" + mAccelFrame.getTimeStampUs()));
                    mAccelTemperatureView.setText(String.format(locale, "AccelTemperature:%.2f°C", mAccelFrame.getTemperature()));
                    mAccelXView.setText(String.format(locale, "Accel.x: %.6f m/s^2", mAccelFrame.getAccelData()[0]));
                    mAccelYView.setText(String.format(locale, "Accel.y:%.6f m/s^2", mAccelFrame.getAccelData()[1]));
                    mAccelZView.setText(String.format(locale, "Accel.z: %.6f m/s^2", mAccelFrame.getAccelData()[2]));
                } else {
                    mAccelTimeStampView.setText("AccelTimestamp: null");
                    mAccelTemperatureView.setText("AccelTemperature: null");
                    mAccelXView.setText("Accel.x: null");
                    mAccelYView.setText("Accel.y: null");
                    mAccelZView.setText("Accel.z: null");
                }
            }

            synchronized (mGyroLock) {
                if (null != mGyroFrame) {
                    mGyroTimeStampView.setText(String.format(locale, "GyroTimestamp:" + mGyroFrame.getTimeStampUs()));
                    mGyroTemperatureView.setText(String.format(locale, "GyroTemperature:%.2f°C", mGyroFrame.getTemperature()));
                    mGyroXView.setText(String.format(locale, "Gyro.x: %.6f rad/s", mGyroFrame.getGyroData()[0]));
                    mGyroYView.setText(String.format(locale, "Gyro.y:%.6f rad/s", mGyroFrame.getGyroData()[1]));
                    mGyroZView.setText(String.format(locale, "Gyro.z: %.6f rad/s", mGyroFrame.getGyroData()[2]));
                } else {
                    mGyroTimeStampView.setText("GyroTimestamp: null");
                    mGyroTemperatureView.setText("GyroTemperature: null");
                    mGyroXView.setText("Gyro.x: null");
                    mGyroYView.setText("Gyro.y: null");
                    mGyroZView.setText("Gyro.z: null");
                }
            }
        }
    }

    @Override
    protected DeviceChangedCallback getDeviceChangedCallback() {
        return mDeviceChangedCallback;
    }

    private void showToast(String msg) {
        runOnUiThread(() -> Toast.makeText(StreamMultiStreamsActivity.this, msg, Toast.LENGTH_SHORT).show());
    }
}
