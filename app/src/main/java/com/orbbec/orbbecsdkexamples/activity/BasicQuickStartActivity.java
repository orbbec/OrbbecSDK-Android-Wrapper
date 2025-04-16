package com.orbbec.orbbecsdkexamples.activity;

import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.widget.Toast;

import androidx.annotation.Nullable;

import com.orbbec.obsensor.ColorFrame;
import com.orbbec.obsensor.Config;
import com.orbbec.obsensor.DepthFrame;
import com.orbbec.obsensor.Device;
import com.orbbec.obsensor.DeviceChangedCallback;
import com.orbbec.obsensor.DeviceList;
import com.orbbec.obsensor.FrameSet;
import com.orbbec.obsensor.Pipeline;
import com.orbbec.obsensor.types.DeviceInfo;
import com.orbbec.obsensor.types.SensorType;
import com.orbbec.obsensor.types.StreamType;
import com.orbbec.orbbecsdkexamples.R;
import com.orbbec.orbbecsdkexamples.view.OBGLView;

public class BasicQuickStartActivity extends BaseActivity {

    private final String TAG = BasicQuickStartActivity.class.getSimpleName();

    private Device mDevice;
    private Pipeline mPipeline;
    private Thread mThread;
    private volatile boolean mIsStreamRunning;
    private OBGLView mColorView;
    private OBGLView mDepthView;

    private final DeviceChangedCallback mDeviceChangedCallback = new DeviceChangedCallback() {

        @Override
        public void onDeviceAttach(DeviceList deviceList) {
            try {
                if (mPipeline == null) {
                    // 2.Create Device and initialize Pipeline through Device
                    mDevice = deviceList.getDevice(0);
                    if (mDevice.getSensor(SensorType.COLOR) == null) {
                        showToast(getString(R.string.device_not_support_color));
                        mDevice.close();
                        mDevice = null;
                        return;
                    }
                    if (mDevice.getSensor(SensorType.DEPTH) == null) {
                        showToast(getString(R.string.device_not_support_depth));
                        mDevice.close();
                        mDevice = null;
                        return;
                    }
                    // 3. Create Device and initialize Pipeline through Device
                    mPipeline = new Pipeline(mDevice);
                    // 4.Create Pipeline configuration
                    Config config = new Config();
                    // 5.Enable depth and color stream
                    config.enableStream(StreamType.COLOR);
                    config.enableStream(StreamType.DEPTH);

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
                    for (int i = 0; i < deviceList.getDeviceCount(); i++) {
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
    protected DeviceChangedCallback getDeviceChangedCallback() {
        return mDeviceChangedCallback;
    }

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setTitle("Basic-Quick Start");
        setContentView(R.layout.activity_basic_quick_start);
        mColorView = findViewById(R.id.quick_start_color);
        mDepthView = findViewById(R.id.quick_start_depth);
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

            if (null != mPipeline) {
                mPipeline.close();
            }

            if (null != mDevice) {
                mDevice.close();
            }
        } catch (Exception e) {
            Log.e(TAG, "onStop: " + e.getMessage());
        }
        releaseSDK();
        super.onStop();
    }

    private void start() {
        mIsStreamRunning = true;
        if (null == mThread) {
            mThread = new Thread(mRunnable);
            mThread.start();
        }
    }

    private void stop() {
        mIsStreamRunning = false;
        if (null != mThread) {
            try {
                mThread.join();
            } catch (InterruptedException e) {
                Log.e(TAG, "stop: " + e.getMessage());
            }
            mThread = null;
        }
    }

    private final Runnable mRunnable = () -> {
        while (mIsStreamRunning) {
            try (FrameSet frameSet = mPipeline.waitForFrameSet(1000)) {
                if (frameSet == null) continue;

                ColorFrame colorFrame = frameSet.getColorFrame();
                DepthFrame depthFrame = frameSet.getDepthFrame();

                if (colorFrame != null) {
                    byte[] colorData = new byte[colorFrame.getDataSize()];
                    colorFrame.getData(colorData);
                    mColorView.update(colorFrame.getWidth(), colorFrame.getHeight(), StreamType.COLOR, colorFrame.getFormat(), colorData, 1.0f);
                    colorFrame.close();
                }
                if (depthFrame != null) {
                    byte[] depthData = new byte[depthFrame.getDataSize()];
                    depthFrame.getData(depthData);
                    mDepthView.update(depthFrame.getWidth(), depthFrame.getHeight(), StreamType.DEPTH, depthFrame.getFormat(), depthData, depthFrame.getValueScale());
                    depthFrame.close();
                }
            } catch (Exception e) {
                Log.e(TAG, "run: " + e.getMessage());
            }
        }
    };

    private void showToast(String msg) {
        runOnUiThread(() -> Toast.makeText(this, msg, Toast.LENGTH_SHORT).show());
    }
}
