package com.orbbec.orbbecsdkexamples.activity;

import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.widget.Toast;

import com.orbbec.obsensor.Config;
import com.orbbec.obsensor.DepthFrame;
import com.orbbec.obsensor.Device;
import com.orbbec.obsensor.DeviceChangedCallback;
import com.orbbec.obsensor.DeviceList;
import com.orbbec.obsensor.FrameSet;
import com.orbbec.obsensor.Pipeline;
import com.orbbec.obsensor.Sensor;
import com.orbbec.obsensor.types.DeviceInfo;
import com.orbbec.obsensor.types.SensorType;
import com.orbbec.obsensor.types.StreamType;
import com.orbbec.orbbecsdkexamples.R;
import com.orbbec.orbbecsdkexamples.view.OBGLView;

/**
 * Depth Viewer
 */
public class StreamDepthActivity extends BaseActivity {
    private static final String TAG = "StreamDepthActivity";

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
                    // 4.Enable depth stream
                    config.enableStream(StreamType.DEPTH);

                    // 5.Start sensor stream
                    mPipeline.start(config);

                    // 6.release config
                    config.close();

                    // 7.Create a thread to obtain Pipeline data
                    start();
                }
            } catch (Exception e) {
                Log.e(TAG, "onDeviceAttach: " + e.getMessage());
            } finally {
                // 8.Release device list resources
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
        setTitle("Stream-Depth");
        setContentView(R.layout.activity_stream_depth);
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

    @Override
    protected DeviceChangedCallback getDeviceChangedCallback() {
        return mDeviceChangedCallback;
    }

    private void showToast(String msg) {
        runOnUiThread(() -> Toast.makeText(StreamDepthActivity.this, msg, Toast.LENGTH_SHORT).show());
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
                Log.e(TAG, "stop: " + e.getMessage());
            }
            mStreamThread = null;
        }
    }

    private Runnable mStreamRunnable = () -> {
        while (mIsStreamRunning) {
            // Obtain the data set in blocking mode. If it cannot be obtained after waiting for 100ms, it will time out.
            try (FrameSet frameSet = mPipeline.waitForFrameSet(100)) {
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
            } catch (Exception e) {
                Log.e(TAG, "run: " + e.getMessage());
            }
        }
    };
}