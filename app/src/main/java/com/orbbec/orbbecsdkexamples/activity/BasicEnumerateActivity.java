package com.orbbec.orbbecsdkexamples.activity;

import android.graphics.Typeface;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.widget.TextView;

import androidx.annotation.Nullable;

import com.google.android.material.tabs.TabLayout;
import com.orbbec.obsensor.AccelStreamProfile;
import com.orbbec.obsensor.Device;
import com.orbbec.obsensor.DeviceChangedCallback;
import com.orbbec.obsensor.DeviceList;
import com.orbbec.obsensor.GyroStreamProfile;
import com.orbbec.obsensor.Pipeline;
import com.orbbec.obsensor.Sensor;
import com.orbbec.obsensor.StreamProfile;
import com.orbbec.obsensor.StreamProfileList;
import com.orbbec.obsensor.TypeHelper;
import com.orbbec.obsensor.VideoStreamProfile;
import com.orbbec.obsensor.types.DeviceInfo;
import com.orbbec.obsensor.types.Format;
import com.orbbec.obsensor.types.IMUSampleRate;
import com.orbbec.obsensor.types.SensorType;
import com.orbbec.obsensor.types.StreamType;
import com.orbbec.orbbecsdkexamples.R;

import java.util.List;
import java.util.Locale;

public class BasicEnumerateActivity extends BaseActivity {

    private static final String TAG = BasicEnumerateActivity.class.getSimpleName();

    private Device mDevice;
    private Pipeline mPipeline;
    private List<Sensor> sensors;
    private TabLayout mTabLayout;
    private TextView mProfilesContent;
    private final StringBuilder sb = new StringBuilder();

    private final DeviceChangedCallback mDeviceChangedCallback = new DeviceChangedCallback() {
        @Override
        public void onDeviceAttach(DeviceList deviceList) {
            try {
                if (mPipeline == null) {
                    // 2.Create Device and initialize Pipeline through Device
                    mDevice = deviceList.getDevice(0);

                    sensors = mDevice.querySensors();
                    for (Sensor sensor : sensors) {
                        runOnUiThread(() -> {
                            TextView item = new TextView(BasicEnumerateActivity.this);
                            item.setText(TypeHelper.convertOBSensorTypeToString(sensor.getType()));
                            item.setTypeface(null, Typeface.BOLD);
                            item.setTextSize(20);
                            TabLayout.Tab tab = mTabLayout.newTab();
                            tab.setCustomView(item);
                            mTabLayout.addTab(tab);
                        });
                    }
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
                    for (int i = 0; i < deviceList.getDeviceCount(); i++) {
                        String uid = deviceList.getUid(i);
                        DeviceInfo deviceInfo = mDevice.getInfo();
                        if (null != deviceInfo && TextUtils.equals(uid, deviceInfo.getUid())) {
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
    protected DeviceChangedCallback getDeviceChangedCallback() {
        return mDeviceChangedCallback;
    }

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setTitle("Basic-Enumerate");
        setContentView(R.layout.activity_basic_enumerate);
        mTabLayout = findViewById(R.id.enumerate_tab_layout);
        mTabLayout.addOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
            @Override
            public void onTabSelected(TabLayout.Tab tab) {
                int pos = tab.getPosition();
                Sensor selectedSensor = sensors.get(pos);
                enumerateStreamProfiles(selectedSensor);
            }

            @Override
            public void onTabUnselected(TabLayout.Tab tab) {

            }

            @Override
            public void onTabReselected(TabLayout.Tab tab) {

            }
        });
        mProfilesContent = findViewById(R.id.enumerate_profiles_content);
        mProfilesContent.setTextSize(20);
    }

    @Override
    protected void onStart() {
        super.onStart();
        initSDK();
    }

    private void enumerateStreamProfiles(Sensor sensor) {
        StreamProfileList streamProfileList = sensor.getStreamProfileList();
        SensorType sensorType = sensor.getType();
        sb.delete(0, sb.length());
        for (int index = 0; index < streamProfileList.getCount(); index++) {
            StreamProfile profile = streamProfileList.getProfile(index);
            if (sensorType == SensorType.IR || sensorType == SensorType.COLOR || sensorType == SensorType.DEPTH || sensorType == SensorType.IR_LEFT
                    || sensorType == SensorType.IR_RIGHT) {
                printStreamProfile(profile, index);
            } else if (sensorType == SensorType.ACCEL) {
                printAccelProfile(profile, index);
            } else if (sensorType == SensorType.GYRO) {
                printGyroProfile(profile, index);
            } else {
                break;
            }
        }
        mProfilesContent.setText(sb.toString());
    }

    private void printStreamProfile(StreamProfile profile, int index) {
        VideoStreamProfile videoProfile = profile.as(StreamType.VIDEO);
        Format format = videoProfile.getFormat();
        int width = videoProfile.getWidth();
        int height = videoProfile.getHeight();
        int fps = videoProfile.getFps();
        String s = String.format(Locale.getDefault(), "index = %d, format = %s, width = %d, height = %d, fps = %d\n",
                index, TypeHelper.convertOBFormatTypeToString(format), width, height, fps);
        sb.append(s);
    }

    private void printAccelProfile(StreamProfile profile, int index) {
        AccelStreamProfile accelProfile = profile.as(StreamType.ACCEL);
        IMUSampleRate accRate = accelProfile.getSampleRate();
        String s = String.format(Locale.getDefault(),
                "index = %d, acc rate = %s\n", index, TypeHelper.convertOBIMUSampleRateTypeToString(accRate));
        sb.append(s);
    }

    private void printGyroProfile(StreamProfile profile, int index) {
        GyroStreamProfile gyroProfile = profile.as(StreamType.GYRO);
        IMUSampleRate gyroRate = gyroProfile.getSampleRate();
        String s = String.format(Locale.getDefault(),
                "index = %d, gyro rate = %s\n", index, TypeHelper.convertOBIMUSampleRateTypeToString(gyroRate));
        sb.append(s);
    }

    @Override
    protected void onStop() {
        super.onStop();
        try {
            if (null != mPipeline) {
                mPipeline.stop();
                mPipeline.close();
            }

            if (null != mDevice) {
                mDevice.close();
            }
        } catch (Exception e) {
            Log.e(TAG, "onStop: " + e.getMessage());
        }
        releaseSDK();
    }
}
