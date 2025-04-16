package com.orbbec.orbbecsdkexamples.activity;

import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.widget.Toast;

import com.orbbec.obsensor.ColorFrame;
import com.orbbec.obsensor.Config;
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

import java.nio.ByteBuffer;

/**
 * Color Viewer
 */
public class StreamColorActivity extends BaseActivity {
    private static final String TAG = "StreamColorActivity";

    private Pipeline mPipeline;
    private Thread mStreamThread;
    private volatile boolean mIsStreamRunning;
    private OBGLView mColorView;
    private Device mDevice;

    private DeviceChangedCallback mDeviceChangedCallback = new DeviceChangedCallback() {
        @Override
        public void onDeviceAttach(DeviceList deviceList) {
            try {
                if (null == mPipeline) {
                    // 2.Create Device and initialize Pipeline through Device
                    mDevice = deviceList.getDevice(0);
                    Sensor colorSensor = mDevice.getSensor(SensorType.COLOR);
                    if (null == colorSensor) {
                        showToast(getString(R.string.device_not_support_color));
                        mDevice.close();
                        mDevice = null;
                        return;
                    }
                    // 3. Create Device and initialize Pipeline through Device
                    mPipeline = new Pipeline(mDevice);

                    // 4.Create Pipeline configuration
                    Config config = new Config();
                    // 5.Enable color stream
                    config.enableStream(SensorType.COLOR);

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
        setTitle("Stream-Color");
        setContentView(R.layout.activity_stream_color);
        mColorView = findViewById(R.id.colorview_id);
    }

    @Override
    protected void onStart() {
        super.onStart();
        initSDK();
    }

    @Override
    protected void onStop() {
        try {
            // Stop getting Pipeline data
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
        runOnUiThread(() -> Toast.makeText(StreamColorActivity.this, msg, Toast.LENGTH_SHORT).show());
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
        ByteBuffer buffer = null;
        while (mIsStreamRunning) {
            // Obtain the data set in blocking mode. If it cannot be obtained after waiting for 100ms, it will time out.
            try (FrameSet frameSet = mPipeline.waitForFrameSet(100)) {
                if (null == frameSet) {
                    continue;
                }

                // Get color flow data
                ColorFrame colorFrame = frameSet.getColorFrame();
                if (null != buffer) {
                    buffer.clear();
                }

                if (null != colorFrame) {
                    // Initialize buffer
                    int dataSize = colorFrame.getDataSize();
                    if (null == buffer || buffer.capacity() != dataSize) {
                        buffer = ByteBuffer.allocateDirect(dataSize);
                    }
                    // Get data and render
                    colorFrame.getData(buffer);
                    mColorView.update(colorFrame.getWidth(), colorFrame.getHeight(), StreamType.COLOR, colorFrame.getFormat(), buffer, 1.0f);

                    // Release color data frame
                    colorFrame.close();
                }
            } catch (Exception e) {
                Log.e(TAG, "run: " + e.getMessage());
            }
        }
    };
}