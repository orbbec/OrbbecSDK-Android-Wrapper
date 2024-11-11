package com.orbbec.orbbecsdkexamples.activity;

import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;

import com.orbbec.obsensor.AccelFrame;
import com.orbbec.obsensor.AccelStreamProfile;
import com.orbbec.obsensor.Device;
import com.orbbec.obsensor.DeviceChangedCallback;
import com.orbbec.obsensor.DeviceList;
import com.orbbec.obsensor.Frame;
import com.orbbec.obsensor.FrameCallback;
import com.orbbec.obsensor.FrameType;
import com.orbbec.obsensor.GyroFrame;
import com.orbbec.obsensor.GyroStreamProfile;
import com.orbbec.obsensor.Sensor;
import com.orbbec.obsensor.SensorType;
import com.orbbec.obsensor.StreamProfileList;
import com.orbbec.obsensor.StreamType;
import com.orbbec.orbbecsdkexamples.R;

/**
 * Imu Viewer
 */
public class ImuActivity extends BaseActivity {
    private static final String TAG = "ImuActivity";

    private int MSG_UPDATE_IMU_INFO = 1;

    private Device mDevice;
    private Sensor mSensorAccel;
    private Sensor mSensorGyro;
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

    private boolean mIsActivityStarted = false;

    private boolean mIsAccelStarted = false;
    private boolean mIsGyroStarted = false;

    private Handler mHandler = new Handler() {
        @Override
        public void handleMessage(@NonNull Message msg) {
            if (msg.what == MSG_UPDATE_IMU_INFO) {
                drawImuInfo();
                sendEmptyMessageDelayed(MSG_UPDATE_IMU_INFO, 20);
            }
        }
    };

    private DeviceChangedCallback mDeviceChangedCallback = new DeviceChangedCallback() {
        @Override
        public void onDeviceAttach(DeviceList deviceList) {
            try {
                if (deviceList == null || deviceList.getDeviceCount() == 0) {
                    showToast(getString(R.string.please_access_device));
                } else {
                    // 2.Create Device
                    mDevice = deviceList.getDevice(0);

                    // 3.Get Acceleration Sensor through Device
                    mSensorAccel = mDevice.getSensor(SensorType.ACCEL);

                    // 4.Get Gyroscope Sensor through Device
                    mSensorGyro = mDevice.getSensor(SensorType.GYRO);

                    if (mSensorAccel == null || mSensorGyro == null) {
                        showToast(getString(R.string.device_not_support_imu));
                        return;
                    }

                    if (mSensorAccel != null && mSensorGyro != null) {
                        // 5.Get accelerometer StreamProfile List
                        StreamProfileList accelProfileList = mSensorAccel.getStreamProfileList();
                        if (null != accelProfileList) {
                            mAccelStreamProfile = accelProfileList.getStreamProfile(0).as(StreamType.ACCEL);
                            accelProfileList.close();
                        }

                        // 6.Get gyroscope StreamProfile List
                        StreamProfileList gyroProfileList = mSensorGyro.getStreamProfileList();
                        if (null != gyroProfileList) {
                            mGyroStreamProfile = gyroProfileList.getStreamProfile(0).as(StreamType.GYRO);
                            gyroProfileList.close();
                        }

                        // 7. start IMU
                        if (mIsActivityStarted) {
                            startIMU();
                        }
                    }
                }
            } catch (Exception e) {
                e.printStackTrace();
            } finally {
                // 8.Release DeviceList
                deviceList.close();
            }
        }

        @Override
        public void onDeviceDetach(DeviceList deviceList) {
            try {
                showToast(getString(R.string.please_access_device));
                deviceList.close();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setTitle("ImuViewer");
        setContentView(R.layout.activity_imu);
        initView();
    }

    @Override
    protected void onStart() {
        super.onStart();
        mIsActivityStarted = true;
        mHandler.sendEmptyMessage(MSG_UPDATE_IMU_INFO);
        initSDK();
    }


    @Override
    protected void onStop() {
        mIsActivityStarted = false;
        mHandler.removeMessages(MSG_UPDATE_IMU_INFO);
        try {
            stopIMU();

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

            // Release Device
            if (null != mDevice) {
                mDevice.close();
                mDevice = null;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        releaseSDK();
        super.onStop();
    }

    @Override
    protected DeviceChangedCallback getDeviceChangedCallback() {
        return mDeviceChangedCallback;
    }

    private void startAccelStream() {
        try {
            // Turn on accelerometer sampling
            if (null != mAccelStreamProfile) {
                mSensorAccel.start(mAccelStreamProfile, new FrameCallback() {
                    @Override
                    public void onFrame(Frame frame) {
                        AccelFrame accelFrame = frame.as(FrameType.ACCEL);

                        Log.d(TAG, "AccelFrame onFrame");
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
            // Turn on gyroscope sampling
            if (null != mGyroStreamProfile) {
                mSensorGyro.start(mGyroStreamProfile, new FrameCallback() {
                    @Override
                    public void onFrame(Frame frame) {
                        GyroFrame gyroFrame = frame.as(FrameType.GYRO);

                        Log.d(TAG, "GyroFrame onFrame");
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

    private void startIMU() {
        // 7.1.Start gyroscope sampling
        startGyroStream();

        // 7.2.Start accelerometer sampling
        startAccelStream();
    }

    private void stopIMU() {
        try {
            // Stop accelerometer sampling
            if (null != mSensorAccel) {
                mSensorAccel.stop();
            }
            mIsAccelStarted = false;

            // Stop gyroscope sampling
            if (null != mSensorGyro) {
                mSensorGyro.stop();
            }
            mIsGyroStarted = false;
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void showToast(String msg) {
        runOnUiThread(() -> Toast.makeText(ImuActivity.this, msg, Toast.LENGTH_SHORT).show());
    }

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
        } // if accel or gyro started
    }

    private void initView() {
        mAccelTimeStampView = findViewById(R.id.view_accel_timestamp);
        mAccelTemperatureView = findViewById(R.id.view_accel_temperature);
        mAccelXView = findViewById(R.id.view_accel_x);
        mAccelYView = findViewById(R.id.view_accel_y);
        mAccelZView = findViewById(R.id.view_accel_z);

        mGyroTimeStampView = findViewById(R.id.view_gyro_timestamp);
        mGyroTemperatureView = findViewById(R.id.view_gyro_temperature);
        mGyroXView = findViewById(R.id.view_gyro_x);
        mGyroYView = findViewById(R.id.view_gyro_y);
        mGyroZView = findViewById(R.id.view_gyro_z);
    }
}