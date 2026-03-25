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
import com.orbbec.obsensor.FormatConvertFilter;
import com.orbbec.obsensor.Frame;
import com.orbbec.obsensor.FrameSet;
import com.orbbec.obsensor.Pipeline;
import com.orbbec.obsensor.types.ConvertFormat;
import com.orbbec.obsensor.types.DeviceInfo;
import com.orbbec.obsensor.types.Format;
import com.orbbec.obsensor.types.FrameType;
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
    private FormatConvertFilter formatConvertFilter;
    private final DeviceChangedCallback mDeviceChangedCallback = new DeviceChangedCallback() {

        @Override
        public void onDeviceAttach(DeviceList deviceList) {
            try {
                if (mDevice == null) {
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

                    // 设备挂载成功后，如果 Activity 在前台，则开启流
                    runOnUiThread(() -> openStream());
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
                            closeStream();
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
        // 核心优化：在创建时就初始化 SDK，切后台不注销
        initSDK();
        formatConvertFilter = new FormatConvertFilter();
        formatConvertFilter.setFormatType(ConvertFormat.FORMAT_MJPEG_TO_RGB);
    }

    @Override
    protected void onStart() {
        super.onStart();
    }

    @Override
    protected void onResume() {
        super.onResume();
        // 从后台回到前台时，若设备已连接且未出流则快速恢复流
        if (mDevice != null && !mIsStreamRunning) {
            openStream();
        }
    }

    @Override
    protected void onPause() {
        // 核心优化：仅停止数据流，不销毁 Pipeline 句柄
        stopStreamOnly();
        super.onPause();
    }

    @Override
    protected void onStop() {
        super.onStop();
    }

    @Override
    protected void onDestroy() {
        // 只有在 Activity 真正销毁时才释放硬件资源
        closeStream();
        if(formatConvertFilter != null){
            formatConvertFilter.close();
        }
        if (mDevice != null) {
            try {
                mDevice.close();
            } catch (Exception e) {
                Log.e(TAG, "onDestroy close device: " + e.getMessage());
            }
            mDevice = null;
        }
        releaseSDK();
        super.onDestroy();
    }

    private synchronized void openStream() {
        if (mIsStreamRunning || mDevice == null) {
            return;
        }

        try {
            // 复用已有 Pipeline，跳过重建
            if (mPipeline == null) {
                mPipeline = new Pipeline(mDevice);
            }

            Config config = new Config();
            config.enableStream(StreamType.COLOR);
            config.enableStream(StreamType.DEPTH);

            mPipeline.start(config);
            config.close();

            mIsStreamRunning = true;
            if (mThread == null) {
                mThread = new Thread(mRunnable);
                mThread.start();
            }
            Log.i(TAG, "openStream success - recovered quickly");
        } catch (Exception e) {
            Log.e(TAG, "openStream failed: " + e.getMessage());
        }
    }

    private synchronized void stopStreamOnly() {
        mIsStreamRunning = false;
        if (mThread != null) {
            try {
                mThread.join(300);
            } catch (InterruptedException e) {
                Log.e(TAG, "stopStreamOnly thread join: " + e.getMessage());
            }
            mThread = null;
        }

        if (mPipeline != null) {
            try {
                // 仅停止传感器出流，保留对象以便 onResume 快速 start
                mPipeline.stop();
            } catch (Exception e) {
                Log.e(TAG, "stopStreamOnly pipeline stop: " + e.getMessage());
            }
        }
    }

    private synchronized void closeStream() {
        stopStreamOnly();
        if (mPipeline != null) {
            try {
                mPipeline.close();
            } catch (Exception ignore) {}
            mPipeline = null;
        }
        Log.i(TAG, "closeStream - resources released");
    }

    private final Runnable mRunnable = () -> {
        while (mIsStreamRunning) {
            try (FrameSet frameSet = mPipeline.waitForFrameSet(100)) {
                if (frameSet == null) continue;

                ColorFrame colorFrame = frameSet.getColorFrame();
                DepthFrame depthFrame = frameSet.getDepthFrame();

                if (colorFrame != null) {
                    if(colorFrame.getFormat() == Format.MJPG){
                        Frame newFrame = formatConvertFilter.process(colorFrame);
                        colorFrame.close();
                        colorFrame = newFrame.as(FrameType.COLOR);
                    }

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
                Log.e(TAG, "run loop error: " + e.getMessage());
            }
        }
    };

    private void showToast(String msg) {
        runOnUiThread(() -> Toast.makeText(this, msg, Toast.LENGTH_SHORT).show());
    }
}
