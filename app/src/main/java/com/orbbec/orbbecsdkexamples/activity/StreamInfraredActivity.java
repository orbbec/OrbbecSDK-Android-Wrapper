package com.orbbec.orbbecsdkexamples.activity;

import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.Toast;

import com.orbbec.obsensor.Config;
import com.orbbec.obsensor.Device;
import com.orbbec.obsensor.DeviceChangedCallback;
import com.orbbec.obsensor.DeviceList;
import com.orbbec.obsensor.FrameSet;
import com.orbbec.obsensor.IRFrame;
import com.orbbec.obsensor.Pipeline;
import com.orbbec.obsensor.Sensor;
import com.orbbec.obsensor.types.DeviceInfo;
import com.orbbec.obsensor.types.Format;
import com.orbbec.obsensor.types.FrameType;
import com.orbbec.obsensor.types.SensorType;
import com.orbbec.obsensor.types.StreamType;
import com.orbbec.orbbecsdkexamples.R;
import com.orbbec.orbbecsdkexamples.view.OBGLView;

import java.util.List;

/**
 * 红外渲染示例
 */
public class StreamInfraredActivity extends BaseActivity {
    private static final String TAG = "StreamInfraredActivity";

    private Device mDevice;
    private Pipeline mPipeline;
    private Thread mStreamThread;
    private volatile boolean mIsStreamRunning;

    private FrameLayout mIrFL;
    private FrameLayout mIrLeftFL;
    private FrameLayout mIrRightFL;

    private OBGLView mIrView;
    private OBGLView mIrLeftView;
    private OBGLView mIrRightView;

    private boolean isIrVisible = false;
    private boolean isIrLeftVisible = false;
    private boolean isIrRightVisible = false;

    private DeviceChangedCallback mDeviceChangedCallback = new DeviceChangedCallback() {
        @Override
        public void onDeviceAttach(DeviceList deviceList) {
            try {
                if (null == mDevice) {
                    mDevice = deviceList.getDevice(0);
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
                            break;
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
        setTitle("Stream-Infrared");
        setContentView(R.layout.activity_stream_infrared);
        mIrFL = findViewById(R.id.fl_ir);
        mIrLeftFL = findViewById(R.id.fl_ir_left);
        mIrRightFL = findViewById(R.id.fl_ir_right);

        mIrView = findViewById(R.id.ir_stream_view);
        mIrLeftView = findViewById(R.id.ir_left_stream_view);
        mIrRightView = findViewById(R.id.ir_right_stream_view);

        // 核心优化：在创建时初始化 SDK，切后台不注销上下文
        initSDK();
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

            List<Sensor> sensorList = mDevice.querySensors();
            Config config = new Config();
            isIrVisible = false;
            isIrLeftVisible = false;
            isIrRightVisible = false;

            for (Sensor sensor : sensorList) {
                SensorType sensorType = sensor.getType();
                if (sensorType == SensorType.IR) {
                    isIrVisible = true;
                    config.enableVideoStream(sensorType, 0, 0, 30, Format.ANY);
                } else if (sensorType == SensorType.IR_LEFT) {
                    isIrLeftVisible = true;
                    config.enableVideoStream(sensorType, 0, 0, 30, Format.ANY);
                } else if (sensorType == SensorType.IR_RIGHT) {
                    isIrRightVisible = true;
                    config.enableVideoStream(sensorType, 0, 0, 30, Format.ANY);
                }
            }

            mPipeline.start(config);
            config.close();

            runOnUiThread(() -> {
                if (isIrVisible) mIrFL.setVisibility(View.VISIBLE);
                if (isIrLeftVisible) mIrLeftFL.setVisibility(View.VISIBLE);
                if (isIrRightVisible) mIrRightFL.setVisibility(View.VISIBLE);
            });

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
        stopStreamOnly();
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
        runOnUiThread(() -> Toast.makeText(this, msg, Toast.LENGTH_SHORT).show());
    }

    private Runnable mStreamRunnable = () -> {
        while (mIsStreamRunning) {
            try (FrameSet frameSet = mPipeline.waitForFrameSet(100)) {
                if (null == frameSet) continue;

                if (isIrVisible) {
                    try (IRFrame irFrame = frameSet.getFrame(FrameType.IR)) {
                        if (null != irFrame) {
                            byte[] irFrameData = new byte[irFrame.getDataSize()];
                            irFrame.getData(irFrameData);
                            mIrView.update(irFrame.getWidth(), irFrame.getHeight(), StreamType.IR, irFrame.getFormat(), irFrameData, 1.0f);
                        }
                    }
                }
                if (isIrLeftVisible) {
                    try (IRFrame irLeftFrame = frameSet.getFrame(FrameType.IR_LEFT)) {
                        if (null != irLeftFrame) {
                            byte[] irLeftFrameData = new byte[irLeftFrame.getDataSize()];
                            irLeftFrame.getData(irLeftFrameData);
                            mIrLeftView.update(irLeftFrame.getWidth(), irLeftFrame.getHeight(), StreamType.IR, irLeftFrame.getFormat(), irLeftFrameData, 1.0f);
                        }
                    }
                }
                if (isIrRightVisible) {
                    try (IRFrame irRightFrame = frameSet.getFrame(FrameType.IR_RIGHT)) {
                        if (null != irRightFrame) {
                            byte[] irRightFrameData = new byte[irRightFrame.getDataSize()];
                            irRightFrame.getData(irRightFrameData);
                            mIrRightView.update(irRightFrame.getWidth(), irRightFrame.getHeight(), StreamType.IR, irRightFrame.getFormat(), irRightFrameData, 1.0f);
                        }
                    }
                }
            } catch (Exception e) {
                Log.e(TAG, "run thread error: " + e.getMessage());
            }
        }
    };
}
