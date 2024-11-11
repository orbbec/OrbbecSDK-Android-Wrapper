package com.orbbec.orbbecsdkexamples.activity;

import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.widget.Toast;

import com.orbbec.obsensor.Config;
import com.orbbec.obsensor.DepthFrame;
import com.orbbec.obsensor.Device;
import com.orbbec.obsensor.DeviceChangedCallback;
import com.orbbec.obsensor.DeviceInfo;
import com.orbbec.obsensor.DeviceList;
import com.orbbec.obsensor.FrameSet;
import com.orbbec.obsensor.Pipeline;
import com.orbbec.obsensor.Sensor;
import com.orbbec.obsensor.SensorType;
import com.orbbec.obsensor.StreamType;
import com.orbbec.obsensor.VideoStreamProfile;
import com.orbbec.orbbecsdkexamples.R;
import com.orbbec.orbbecsdkexamples.view.OBGLView;

/**
 * Depth Viewer
 */
public class DepthViewerActivity extends BaseActivity {
    private static final String TAG = "DepthViewerActivity";

    private Pipeline mPipeline;

    private Thread mStreamThread;
    private volatile boolean mIsStreamRunning;

    private OBGLView mDepthView;
    private Device mDevice;

    private DeviceChangedCallback mDeviceChangedCallback = new DeviceChangedCallback() {
        @Override
        public void onDeviceAttach(DeviceList deviceList) {
            try {
                if (null == mPipeline) {
                    // 2.Create Device and initialize Pipeline through Device
                    mDevice = deviceList.getDevice(0);

                    Sensor depthSensor = mDevice.getSensor(SensorType.DEPTH);
                    if (null == depthSensor) {
                        showToast(getString(R.string.device_not_support_depth));
                        mDevice.close();
                        mDevice = null;
                        return;
                    }

                    mPipeline = new Pipeline(mDevice);

                    // 3.Create Pipeline configuration
                    Config config = new Config();

                    // 4.Obtain the depth stream configuration and configure it to Config.
                    // Here, matching is based on the width and frame rate. The matching meets
                    // the configuration of a width of 640 and a frame rate of 30fps.
                    VideoStreamProfile streamProfile = getStreamProfile(mPipeline, SensorType.DEPTH);

                    // 5.Enable deep streaming configuration
                    if (null != streamProfile) {
                        printStreamProfile(streamProfile.as(StreamType.VIDEO));
                        config.enableStream(streamProfile);
                        streamProfile.close();
                    } else {
                        mPipeline.close();
                        mPipeline = null;
                        mDevice.close();
                        mDevice = null;

                        config.close();

                        Log.w(TAG, "No target stream profile!");
                        showToast(getString(R.string.init_stream_profile_failed));
                        return;
                    }

                    // 6.Start sensor stream
                    mPipeline.start(config);

                    // 7.release config
                    config.close();

                    // 8.Create a thread to obtain Pipeline data
                    start();
                }
            } catch (Exception e) {
                e.printStackTrace();
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
                        if (null != deviceInfo && TextUtils.equals(uid, deviceInfo.getUid())) {
                            stop();
                            mPipeline.stop();
                            mPipeline.close();
                            mPipeline = null;
                            mDevice.close();
                            mDevice = null;
                        }
                        deviceInfo.close();
                    }
                }
            } catch (Exception e) {
                e.printStackTrace();
            } finally {
                try {
                    deviceList.close();
                } catch (Exception ignore) {
                }
            }
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setTitle("DepthViewer");
        setContentView(R.layout.activity_depth_viewer);
        mDepthView = findViewById(R.id.depthview_id);
    }

    @Override
    protected void onStart() {
        super.onStart();
        initSDK();
    }

    @Override
    protected void onStop() {
        try {
            //Stop getting Pipeline data
            stop();

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
        super.onStop();
    }

    @Override
    protected DeviceChangedCallback getDeviceChangedCallback() {
        return mDeviceChangedCallback;
    }

    private void showToast(String msg) {
        runOnUiThread(() -> Toast.makeText(DepthViewerActivity.this, msg, Toast.LENGTH_SHORT).show());
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
                // Obtain the data set in blocking mode. If it cannot be obtained after waiting for 100ms, it will time out.
                FrameSet frameSet = mPipeline.waitForFrameSet(100);

                if (null == frameSet) {
                    continue;
                }

                // Get depth flow data
                DepthFrame frame = frameSet.getDepthFrame();
                if (frame != null) {
                    // Get data and render
                    byte[] frameData = new byte[frame.getDataSize()];
                    frame.getData(frameData);
                    mDepthView.update(frame.getWidth(), frame.getHeight(), StreamType.DEPTH, frame.getFormat(), frameData, frame.getValueScale());

                    // Release depth data frame
                    frame.close();
                }

                // 释放数据集
                frameSet.close();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    };
}