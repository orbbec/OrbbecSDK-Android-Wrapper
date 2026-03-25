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
import com.orbbec.obsensor.FormatConvertFilter;
import com.orbbec.obsensor.Frame;
import com.orbbec.obsensor.FrameSet;
import com.orbbec.obsensor.Pipeline;
import com.orbbec.obsensor.Sensor;
import com.orbbec.obsensor.types.ConvertFormat;
import com.orbbec.obsensor.types.DeviceInfo;
import com.orbbec.obsensor.types.Format;
import com.orbbec.obsensor.types.FrameType;
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
    private FormatConvertFilter formatConvertFilter;

    private DeviceChangedCallback mDeviceChangedCallback = new DeviceChangedCallback() {
        @Override
        public void onDeviceAttach(DeviceList deviceList) {
            try {
                if (null == mDevice) {
                    mDevice = deviceList.getDevice(0);
                    Sensor colorSensor = mDevice.getSensor(SensorType.COLOR);
                    if (null == colorSensor) {
                        showToast(getString(R.string.device_not_support_color));
                        mDevice.close();
                        mDevice = null;
                        return;
                    }
                    Log.i(TAG, "onDeviceAttach: device init");
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
                    for (int i = 0, N = deviceList.getDeviceCount(); i < N; i++) {
                        String uid = deviceList.getUid(i);
                        DeviceInfo deviceInfo = mDevice.getInfo();
                        if (null != deviceInfo && TextUtils.equals(uid, deviceInfo.getUid())) {
                            Log.i(TAG, "onDeviceDetach: device released");
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
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setTitle("Stream-Color");
        setContentView(R.layout.activity_stream_color);
        mColorView = findViewById(R.id.colorview_id);
        // 核心优化：创建时初始化 SDK，切后台不注销上下文
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
        // 彻底退出页面时才释放硬件资源
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
            // 复用已有 Pipeline，跳过最耗时的重建过程
            if (mPipeline == null) {
                mPipeline = new Pipeline(mDevice);
            }
            Config config = new Config();
            config.enableStream(SensorType.COLOR);
            mPipeline.start(config);
            config.close();

            mIsStreamRunning = true;
            if (null == mStreamThread) {
                mStreamThread = new Thread(mStreamRunnable);
                mStreamThread.start();
            }
            Log.i(TAG, "openStream success - quickly recovered");
        } catch (Exception e) {
            Log.e(TAG, "openStream failed: " + e.getMessage());
        }
    }

    private void stopStreamThread() {
        mIsStreamRunning = false;
        if (null != mStreamThread) {
            try {
                mStreamThread.join(300);
            } catch (InterruptedException e) {
                Log.e(TAG, "stopStreamThread join error: " + e.getMessage());
            }
            mStreamThread = null;
        }
    }

    private synchronized void stopStreamOnly() {
        stopStreamThread();
        if (null != mPipeline) {
            try {
                // 仅停止传感器出流，保留对象以便 onResume 快速 start
                mPipeline.stop();
            } catch (Exception e) {
                Log.e(TAG, "stopStreamOnly pipeline: " + e.getMessage());
            }
        }
    }

    private synchronized void closeStream() {
        stopStreamThread();
        if (null != mPipeline) {
            try {
                mPipeline.stop();
                mPipeline.close();
            } catch (Exception e) {
                Log.e(TAG, "closeStream pipeline: " + e.getMessage());
            }
            mPipeline = null;
        }
        Log.i(TAG, "closeStream - resources released");
    }

    @Override
    protected DeviceChangedCallback getDeviceChangedCallback() {
        return mDeviceChangedCallback;
    }

    private void showToast(String msg) {
        runOnUiThread(() -> Toast.makeText(StreamColorActivity.this, msg, Toast.LENGTH_SHORT).show());
    }

    private Runnable mStreamRunnable = () -> {
        ByteBuffer buffer = null;
        while (mIsStreamRunning) {
            try (FrameSet frameSet = mPipeline.waitForFrameSet(100)) {
                if (null == frameSet) continue;

                ColorFrame colorFrame = frameSet.getColorFrame();
                if (null != colorFrame) {
                    if(colorFrame.getFormat() == Format.MJPG){
                        Frame newFrame = formatConvertFilter.process(colorFrame);
                        colorFrame.close();
                        colorFrame = newFrame.as(FrameType.COLOR);
                    }

                    int dataSize = colorFrame.getDataSize();
                    if (null == buffer || buffer.capacity() != dataSize) {
                        buffer = ByteBuffer.allocateDirect(dataSize);
                    }
                    buffer.clear();
                    colorFrame.getData(buffer);
                    mColorView.update(colorFrame.getWidth(), colorFrame.getHeight(), StreamType.COLOR, colorFrame.getFormat(), buffer, 1.0f);
                    colorFrame.close();
                }
            } catch (Exception e) {
                Log.e(TAG, "run thread error: " + e.getMessage());
            }
        }
    };
}
