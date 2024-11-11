package com.orbbec.orbbecsdkexamples.activity;

import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.text.TextUtils;
import android.util.Log;
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
import com.orbbec.obsensor.DeviceInfo;
import com.orbbec.obsensor.DeviceList;
import com.orbbec.obsensor.Frame;
import com.orbbec.obsensor.FrameCallback;
import com.orbbec.obsensor.FrameSet;
import com.orbbec.obsensor.FrameType;
import com.orbbec.obsensor.GyroFrame;
import com.orbbec.obsensor.GyroStreamProfile;
import com.orbbec.obsensor.IRFrame;
import com.orbbec.obsensor.Pipeline;
import com.orbbec.obsensor.Sensor;
import com.orbbec.obsensor.SensorType;
import com.orbbec.obsensor.StreamProfileList;
import com.orbbec.obsensor.StreamType;
import com.orbbec.obsensor.VideoStreamProfile;
import com.orbbec.orbbecsdkexamples.R;
import com.orbbec.orbbecsdkexamples.view.OBGLView;

public class MultiStreamActivity extends BaseActivity {

    private static final String TAG = "MultiActivity";

    private Pipeline mPipeline;
    private Thread mStreamThread;
    private volatile boolean mIsStreamRunning;
    private OBGLView mColorView;
    private OBGLView mDepthView;
    private OBGLView mIrLeftView;
    private OBGLView mIrRightView;
    private Device mDevice;

    private int MSG_UPDATE_IMU_INFO = 1;
    private Sensor mAccelSensor;
    private Sensor mGyroSensor;
    private AccelStreamProfile mAccelStreamProfile;
    private GyroStreamProfile mGyroStreamProfile;

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

    private Object mAccelLock = new Object();
    private AccelFrame mAccelFrame;

    private Object mGyroLock = new Object();
    private GyroFrame mGyroFrame;

    private boolean mIsAccelStarted = false;
    private boolean mIsGyroStarted = false;

    private DeviceChangedCallback mDeviceChangedCallback = new DeviceChangedCallback() {

        @Override
        public void onDeviceAttach(DeviceList deviceList) {
            try {
                if (mPipeline == null) {
                    // 2.Create Device and initialize Pipeline through Device
                    mDevice = deviceList.getDevice(0);

                    Sensor colorSensor = mDevice.getSensor(SensorType.COLOR);
                    if (colorSensor == null) {
                        showToast(getString(R.string.device_not_support_color));
                        mDevice.close();
                        mDevice = null;
                        return;
                    }

                    Sensor depthSensor = mDevice.getSensor(SensorType.DEPTH);
                    if (depthSensor == null) {
                        showToast(getString(R.string.device_not_support_depth));
                        mDevice.close();
                        mDevice = null;
                        return;
                    }

                    Sensor irLeftSensor = mDevice.getSensor(SensorType.IR_LEFT);
                    if (irLeftSensor == null) {
                        showToast(getString(R.string.device_not_support_ir_left));
                        mDevice.close();
                        mDevice = null;
                        return;
                    }

                    Sensor irRightSensor = mDevice.getSensor(SensorType.IR_RIGHT);
                    if (irRightSensor == null) {
                        showToast(getString(R.string.device_not_support_ir_right));
                        mDevice.close();
                        mDevice = null;
                        return;
                    }

                    // 3.Get Acceleration and Gyroscope Sensor through Device
                    mAccelSensor = mDevice.getSensor(SensorType.ACCEL);
                    mGyroSensor = mDevice.getSensor(SensorType.GYRO);

                    if (mAccelSensor == null || mGyroSensor == null) {
                        showToast(getString(R.string.device_not_support_imu));
                        return;
                    } else {
                        // 4.Get accelerometer and gyroscope StreamProfile List
                        StreamProfileList accelProfileList = mAccelSensor.getStreamProfileList();
                        if (null != accelProfileList) {
                            mAccelStreamProfile = accelProfileList.getStreamProfile(0).as(StreamType.ACCEL);
                            accelProfileList.close();
                        }

                        StreamProfileList gyroProfileList = mGyroSensor.getStreamProfileList();
                        if (null != gyroProfileList) {
                            mGyroStreamProfile = gyroProfileList.getStreamProfile(0).as(StreamType.GYRO);
                            gyroProfileList.close();
                        }
                    }

                    mPipeline = new Pipeline(mDevice);

                    // 5.Initialize stream profile
                    Config config = initStreamProfile(mPipeline);
                    if (config == null) {
                        showToast(getString(R.string.init_stream_profile_failed));
                        mPipeline.close();
                        mPipeline = null;
                        mDevice.close();
                        mDevice = null;
                        return;
                    }

                    // 6.Start sensor stream
                    mPipeline.start(config);

                    // 7.Release config
                    config.close();

                    // 8.Create a thread to obtain Pipeline data
                    start();
                }
            } catch (Exception e) {
                e.printStackTrace();
            } finally {
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
                            mPipeline.stop();
                            mPipeline.close();
                            mPipeline = null;
                            mDevice.close();
                            mDevice = null;
                        }
                    }
                }
            } catch (Exception e) {
                e.printStackTrace();
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
                sendEmptyMessageDelayed(MSG_UPDATE_IMU_INFO, 50);
            }
        }
    };

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setTitle("MultiStreamView");
        setContentView(R.layout.activity_multi_stream_viewer);
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
        super.onStop();
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
            e.printStackTrace();
        }

        releaseSDK();
    }

    private void initView() {
        mColorView = findViewById(R.id.multi_stream_color);
        mDepthView = findViewById(R.id.multi_stream_depth);
        mIrLeftView = findViewById(R.id.multi_stream_ir_left);
        mIrRightView = findViewById(R.id.multi_stream_ir_right);

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
    }

    private Config initStreamProfile(Pipeline pipeline) {
        Config config = new Config();

        SensorType types[] = {SensorType.DEPTH, SensorType.COLOR, SensorType.IR_LEFT, SensorType.IR_RIGHT};
        for (SensorType type : types) {
            VideoStreamProfile streamProfile = getStreamProfile(pipeline, type);
            if (streamProfile != null) {
                Log.d(TAG, "initStreamProfile: " + type);
                printStreamProfile(streamProfile.as(StreamType.VIDEO));
                config.enableStream(streamProfile);
                streamProfile.close();
            } else {
                Log.w(TAG, "initStreamProfile: stream profile is null");
                config.close();
                return null;
            }
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
                mAccelSensor.start(mAccelStreamProfile, new FrameCallback() {
                    @Override
                    public void onFrame(Frame frame) {
                        AccelFrame accelFrame = frame.as(FrameType.ACCEL);

                        synchronized (mAccelLock) {
                            if (null != mAccelFrame) {
                                mAccelFrame.close();
                                mAccelFrame = null;
                            }
                            mAccelFrame = accelFrame;
                        }
                    }
                });
                mIsAccelStarted = true;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void startGyroStream() {
        try {
            if (mGyroStreamProfile != null) {
                mGyroSensor.start(mGyroStreamProfile, new FrameCallback() {
                    @Override
                    public void onFrame(Frame frame) {
                        GyroFrame gyroFrame = frame.as(FrameType.GYRO);

                        synchronized (mGyroLock) {
                            if (null != mGyroFrame) {
                                mGyroFrame.close();
                                mGyroFrame = null;
                            }
                            mGyroFrame = gyroFrame;
                        }
                    }
                });
                mIsGyroStarted = true;
            }
        } catch (Exception e) {
            e.printStackTrace();
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
            e.printStackTrace();
        }
    }

    private Runnable mStreamRunnable = () -> {
        while (mIsStreamRunning) {
            try {
                FrameSet frameSet = mPipeline.waitForFrameSet(100);

                if (frameSet == null) continue;

                DepthFrame depthFrame = frameSet.getFrame(FrameType.DEPTH);
                ColorFrame colorFrame = frameSet.getFrame(FrameType.COLOR);
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
                    mDepthView.update(depthFrame.getWidth(), depthFrame.getHeight(), StreamType.DEPTH, depthFrame.getFormat(), depthFrameData, 1.0f);
                    depthFrame.close();
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

                frameSet.close();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    };

    private void drawImuInfo() {
        if (mIsAccelStarted || mIsGyroStarted) {
            synchronized (mAccelLock) {
                if (null != mAccelFrame) {
                    mAccelTimeStampView.setText("AccelTimestamp:" + mAccelFrame.getTimeStamp());
                    mAccelTemperatureView.setText(String.format("AccelTemperature:%.2f°C", mAccelFrame.getTemperature())); // TODO unit
                    mAccelXView.setText(String.format("Accel.x: %.6fm/s^2", mAccelFrame.getAccelData()[0]));
                    mAccelYView.setText(String.format("Accel.y:%.6fm/s^2", mAccelFrame.getAccelData()[1]));
                    mAccelZView.setText(String.format("Accel.z: %.6fm/s^2", mAccelFrame.getAccelData()[2]));
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
                    mGyroTimeStampView.setText("GyroTimestamp:" + mGyroFrame.getTimeStamp());
                    mGyroTemperatureView.setText(String.format("GyroTemperature:%.2f°C", mGyroFrame.getTemperature())); // TODO unit
                    mGyroXView.setText(String.format("Gyro.x: %.6frad/s", mGyroFrame.getGyroData()[0]));
                    mGyroYView.setText(String.format("Gyro.y:%.6frad/s", mGyroFrame.getGyroData()[1]));
                    mGyroZView.setText(String.format("Gyro.z: %.6frad/s", mGyroFrame.getGyroData()[2]));
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
        runOnUiThread(() -> Toast.makeText(MultiStreamActivity.this, msg, Toast.LENGTH_SHORT).show());
    }
}
