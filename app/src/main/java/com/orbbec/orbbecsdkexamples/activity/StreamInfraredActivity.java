package com.orbbec.orbbecsdkexamples.activity;

import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.FrameLayout;

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
                if (null == mPipeline) {
                    // 2.Create Device and initialize Pipeline through Device
                    mDevice = deviceList.getDevice(0);
                    mPipeline = new Pipeline(mDevice);

                    // 3.Get the sensor list from device
                    List<Sensor> sensorList = mDevice.querySensors();

                    // 4.Create a config for pipeline
                    Config config = new Config();

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

                    // 5.Start sensor stream
                    mPipeline.start(config);

                    // 6.Release config
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
        setTitle("Stream-Infrared");
        setContentView(R.layout.activity_stream_infrared);
        mIrFL = findViewById(R.id.fl_ir);
        mIrLeftFL = findViewById(R.id.fl_ir_left);
        mIrRightFL = findViewById(R.id.fl_ir_right);

        mIrView = findViewById(R.id.ir_stream_view);
        mIrLeftView = findViewById(R.id.ir_left_stream_view);
        mIrRightView = findViewById(R.id.ir_right_stream_view);
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
                mPipeline.stop();
                mPipeline.close();
            }

            // Release Device
            if (null != mDevice) {
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

    private void start() {
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

                // Get Infrared flow data
                if (isIrVisible) {
                    IRFrame irFrame = frameSet.getFrame(FrameType.IR);
                    if (null != irFrame) {
                        byte[] irFrameData = new byte[irFrame.getDataSize()];
                        irFrame.getData(irFrameData);
                        mIrView.update(irFrame.getWidth(), irFrame.getHeight(), StreamType.IR, irFrame.getFormat(), irFrameData, 1.0f);
                        irFrame.close();
                    }
                }
                if (isIrLeftVisible) {
                    IRFrame irLeftFrame = frameSet.getFrame(FrameType.IR_LEFT);
                    if (null != irLeftFrame) {
                        byte[] irLeftFrameData = new byte[irLeftFrame.getDataSize()];
                        irLeftFrame.getData(irLeftFrameData);
                        mIrLeftView.update(irLeftFrame.getWidth(), irLeftFrame.getHeight(), StreamType.IR, irLeftFrame.getFormat(), irLeftFrameData, 1.0f);
                        irLeftFrame.close();
                    }
                }
                if (isIrRightVisible) {
                    IRFrame irRightFrame = frameSet.getFrame(FrameType.IR_RIGHT);
                    if (null != irRightFrame) {
                        byte[] irRightFrameData = new byte[irRightFrame.getDataSize()];
                        irRightFrame.getData(irRightFrameData);
                        mIrRightView.update(irRightFrame.getWidth(), irRightFrame.getHeight(), StreamType.IR, irRightFrame.getFormat(), irRightFrameData, 1.0f);
                        irRightFrame.close();
                    }
                }
            } catch (Exception e) {
                Log.e(TAG, "run: " + e.getMessage());
            }
        }
    };
}