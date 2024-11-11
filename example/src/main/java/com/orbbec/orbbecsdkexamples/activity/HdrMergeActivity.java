package com.orbbec.orbbecsdkexamples.activity;

import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.widget.CheckBox;
import android.widget.Toast;

import androidx.annotation.Nullable;

import com.orbbec.obsensor.Config;
import com.orbbec.obsensor.DepthFrame;
import com.orbbec.obsensor.Device;
import com.orbbec.obsensor.DeviceChangedCallback;
import com.orbbec.obsensor.DeviceInfo;
import com.orbbec.obsensor.DeviceList;
import com.orbbec.obsensor.DeviceProperty;
import com.orbbec.obsensor.FrameSet;
import com.orbbec.obsensor.FrameType;
import com.orbbec.obsensor.HdrConfig;
import com.orbbec.obsensor.HdrMergeFilter;
import com.orbbec.obsensor.PermissionType;
import com.orbbec.obsensor.Pipeline;
import com.orbbec.obsensor.Sensor;
import com.orbbec.obsensor.SensorType;
import com.orbbec.obsensor.StreamType;
import com.orbbec.obsensor.VideoStreamProfile;
import com.orbbec.orbbecsdkexamples.R;
import com.orbbec.orbbecsdkexamples.view.OBGLView;

public class HdrMergeActivity extends BaseActivity {
    private static final String TAG = "HdrMergeActivity";

    private Pipeline mPipeline;
    private Thread mStreamThread;
    private volatile boolean mIsStreamRunning;
    private HdrConfig mHdrConfig;
    private OBGLView mHdrMergeView;
    private Device mDevice;
    private HdrMergeFilter mHdrFilter;

    private CheckBox mEnableHdrMerge;
    private boolean mergeRequired;

    private DeviceChangedCallback mDeviceChangedCallback = new DeviceChangedCallback() {

        @Override
        public void onDeviceAttach(DeviceList deviceList) {
            try {
                if (mPipeline == null) {
                    // 2.Create Device and initialize Pipeline through Device
                    mDevice = deviceList.getDevice(0);
                    Sensor depthSensor = mDevice.getSensor(SensorType.DEPTH);
                    if (depthSensor == null) {
                        showToast(getString(R.string.device_not_support_depth));
                        mDevice.close();
                        mDevice = null;
                        return;
                    }

                    // 3.c
                    boolean isSupported = mDevice.isPropertySupported(DeviceProperty.OB_STRUCT_DEPTH_HDR_CONFIG, PermissionType.OB_PERMISSION_READ_WRITE);
                    if (!isSupported) {
                        Log.w(TAG, "The device does not support Hdr feature.");
                        showToast(getString(R.string.device_not_support_hdr));
                        mDevice.close();
                        mDevice = null;
                        return;
                    }

                    // 4.Create Device and initialize Pipeline through Device
                    mPipeline = new Pipeline(mDevice);

                    // 5.configure Hdr
                    mHdrConfig = new HdrConfig();
                    mHdrConfig.setEnable((byte) 1);
                    mHdrConfig.setSequenceName((byte) 0);
                    mHdrConfig.setExposure1(7500);
                    mHdrConfig.setGain1(16);
                    mHdrConfig.setExposure2(1);
                    mHdrConfig.setGain2(16);
                    mDevice.setPropertyValueDataType(DeviceProperty.OB_STRUCT_DEPTH_HDR_CONFIG, mHdrConfig);

                    // 6.Create HdrMerge post processor
                    mHdrFilter = new HdrMergeFilter();
                    mergeRequired = mHdrFilter.isEnable();
                    mEnableHdrMerge.setChecked(mergeRequired);

                    // 7.Create Pipeline configuration
                    Config config = new Config();

                    // 8.Get the depth sensor configuration and configure it to Config
                    VideoStreamProfile streamProfile = getStreamProfile(mPipeline, SensorType.DEPTH);
                    // 9.Enable depth StreamProfile
                    if (null != streamProfile) {
                        printStreamProfile(streamProfile.as(StreamType.VIDEO));
                        config.enableStream(streamProfile);
                        streamProfile.close();
                    } else {
                        mPipeline.close();
                        mPipeline = null;
                        mDevice.close();
                        mDevice = null;

                        mHdrFilter.close();
                        mHdrFilter = null;
                        config.close();

                        Log.w(TAG, "No target stream profile!");
                        return;
                    }

                    // 10.Start sensor stream
                    mPipeline.start(config);

                    // 11.Release config
                    config.close();

                    // 12.Create a thread to obtain Pipeline data
                    start();
                }
            } catch (Exception e) {
                e.printStackTrace();
            } finally {
                // 13.Release device list resources
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
                        if (deviceInfo != null && TextUtils.equals(uid, deviceInfo.getUid())) {
                            stop();
                            mPipeline.stop();
                            mPipeline.close();
                            mPipeline = null;
                            mHdrFilter.close();
                            mHdrFilter = null;
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

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setTitle("HdrMerge");
        setContentView(R.layout.activity_hdr_merge);
        mHdrMergeView = findViewById(R.id.hdrmerge_id);

        mEnableHdrMerge = findViewById(R.id.hdr_merge_required);
        mEnableHdrMerge.setOnClickListener(v -> {
            mergeRequired = !mergeRequired;
        });
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

            if (mPipeline != null) {
                mPipeline.stop();
                mPipeline.close();
            }

            if (mHdrFilter != null) {
                mHdrFilter.close();
            }

            if (mDevice != null) {
                mHdrConfig.setEnable((byte) 0);
                mDevice.setPropertyValueDataType(DeviceProperty.OB_STRUCT_DEPTH_HDR_CONFIG, mHdrConfig);
                mDevice.close();
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

    private void start() {
        mIsStreamRunning = true;
        if (null == mStreamThread) {
            mStreamThread = new Thread(mStreamRunnable);
            mStreamThread.start();
        }
    }

    private void stop() {
        mIsStreamRunning = false;
        if (null != mStreamThread) {
            try {
                mStreamThread.join(300);
            } catch (InterruptedException e) {
            }
            mStreamThread = null;
        }
    }

    private Runnable mStreamRunnable = () -> {
        while (mIsStreamRunning) {
            try {
                FrameSet frameSet = mPipeline.waitForFrameSet(100);

                Log.d(TAG, "frameSet=" + frameSet);
                if (frameSet == null) {
                    continue;
                }

                DepthFrame depthFrame = frameSet.getDepthFrame();

                if (depthFrame == null) {
                    frameSet.close();
                    continue;
                }
                Log.d(TAG, "frameSet=" + frameSet + ", depthFrame=" + depthFrame);

                if (mergeRequired) {
                    DepthFrame mergeDepthFrame = mHdrFilter.process(depthFrame).as(FrameType.DEPTH);
                    Log.d(TAG, "depth frame: " + mergeDepthFrame);
                    if (mergeDepthFrame != null) {
                        byte[] frameData = new byte[mergeDepthFrame.getDataSize()];
                        mergeDepthFrame.getData(frameData);
                        mHdrMergeView.update(mergeDepthFrame.getWidth(), mergeDepthFrame.getHeight(), StreamType.DEPTH, mergeDepthFrame.getFormat(), frameData, mergeDepthFrame.getValueScale());
                        mergeDepthFrame.close();
                    }
                } else {
                    byte[] frameData = new byte[depthFrame.getDataSize()];
                    depthFrame.getData(frameData);
                    mHdrMergeView.update(depthFrame.getWidth(), depthFrame.getHeight(), StreamType.DEPTH, depthFrame.getFormat(), frameData, depthFrame.getValueScale());
                }

                depthFrame.close();
                frameSet.close();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    };

    private void showToast(String msg) {
        runOnUiThread(() -> Toast.makeText(HdrMergeActivity.this, msg, Toast.LENGTH_SHORT).show());
    }
}
