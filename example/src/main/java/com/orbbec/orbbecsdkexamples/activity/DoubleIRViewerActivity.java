package com.orbbec.orbbecsdkexamples.activity;

import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.widget.Toast;

import com.orbbec.obsensor.Config;
import com.orbbec.obsensor.Device;
import com.orbbec.obsensor.DeviceChangedCallback;
import com.orbbec.obsensor.DeviceInfo;
import com.orbbec.obsensor.DeviceList;
import com.orbbec.obsensor.Format;
import com.orbbec.obsensor.FrameSet;
import com.orbbec.obsensor.FrameType;
import com.orbbec.obsensor.IRFrame;
import com.orbbec.obsensor.OBContext;
import com.orbbec.obsensor.Pipeline;
import com.orbbec.obsensor.Sensor;
import com.orbbec.obsensor.SensorType;
import com.orbbec.obsensor.StreamProfileList;
import com.orbbec.obsensor.StreamType;
import com.orbbec.obsensor.VideoStreamProfile;
import com.orbbec.orbbecsdkexamples.R;
import com.orbbec.orbbecsdkexamples.view.OBGLView;

/**
 * 红外渲染示例
 */
public class DoubleIRViewerActivity extends BaseActivity {
    private static final String TAG = "InfraredViewerActivity";

    private Pipeline mPipeline;
    private Thread mStreamThread;
    private volatile boolean mIsStreamRunning;
    private OBGLView mIrLeftView;
    private OBGLView mIrRightView;
    private Device mDevice;

    private DeviceChangedCallback mDeviceChangedCallback = new DeviceChangedCallback() {
        @Override
        public void onDeviceAttach(DeviceList deviceList) {
            try {
                if (null == mPipeline) {
                    // 2.Create Device and initialize Pipeline through Device
                    mDevice = deviceList.getDevice(0);

                    Sensor irLeftSensor = mDevice.getSensor(SensorType.IR_LEFT);
                    if (null == irLeftSensor) {
                        showToast(getString(R.string.device_not_support_ir));
                        mDevice.close();
                        mDevice = null;
                        return;
                    }

                    Sensor irRightSensor = mDevice.getSensor(SensorType.IR_RIGHT);
                    if (null == irRightSensor) {
                        showToast(getString(R.string.device_not_support_ir_right));
                        mDevice.close();
                        mDevice = null;
                        return;
                    }

                    mPipeline = new Pipeline(mDevice);

                    // 3.Initialize stream profile
                    Config config = initStreamProfile(mPipeline);
                    if (null == config) {
                        showToast(getString(R.string.init_stream_profile_failed));
                        mPipeline.close();
                        mPipeline = null;
                        mDevice.close();
                        mDevice = null;
                        config.close();
                        return;
                    }

                    // 4.Start sensor stream
                    mPipeline.start(config);

                    // 5.Release config
                    config.close();

                    // 6.Create a thread to obtain Pipeline data
                    start();
                }
            } catch (Exception e) {
                e.printStackTrace();
            } finally {
                // 7.Release device list resources
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
        setTitle("InfraredViewer");
        setContentView(R.layout.activity_double_ir_viewer);
        mIrLeftView = findViewById(R.id.view_ir_left);
        mIrRightView = findViewById(R.id.view_ir_right);
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
        runOnUiThread(() -> Toast.makeText(DoubleIRViewerActivity.this, msg, Toast.LENGTH_SHORT).show());
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

    private Config initStreamProfile(Pipeline pipeline) {
        // 1.Create Pipeline configuration
        Config config = new Config();

        SensorType sensorTypes[] = {SensorType.IR_LEFT, SensorType.IR_RIGHT};
        for (SensorType sensorType : sensorTypes) {
            // Obtain the stream configuration and configure it to Config, where the matching
            // is performed according to the width and frame rate, and the matching satisfies
            // the configuration with a width of 640 and a frame rate of 30fps
            VideoStreamProfile irStreamProfile = getStreamProfile(pipeline, sensorType);
            // Enable ir left StreamProfile
            if (null != irStreamProfile) {
                Log.d(TAG, "irStreamProfile: " + sensorType);
                printStreamProfile(irStreamProfile.as(StreamType.VIDEO));
                config.enableStream(irStreamProfile);
                irStreamProfile.close();
            } else {
                Log.w(TAG, ": No target stream profile! ir left stream profile is null");
                config.close();
                return null;
            }
        }
        return config;
    }

    private Runnable mStreamRunnable = () -> {
        FrameType frameTypes[] = {FrameType.IR_LEFT, FrameType.IR_RIGHT};
        while (mIsStreamRunning) {
            try {
                // Obtain the data set in blocking mode. If it cannot be obtained after waiting for 100ms, it will time out.
                FrameSet frameSet = mPipeline.waitForFrameSet(100);

                if (null == frameSet) {
                    continue;
                }

                // Get Infrared flow data
                for (int i = 0; i < frameTypes.length; i++) {
                    IRFrame frame = frameSet.getFrame(frameTypes[i]);

                    if (frame != null) {
                        // Get infrared data and render it
                        byte[] frameData = new byte[frame.getDataSize()];
                        frame.getData(frameData);

                        OBGLView glView = frameTypes[i] == FrameType.IR_LEFT ? mIrLeftView : mIrRightView;
                        glView.update(frame.getWidth(), frame.getHeight(), StreamType.IR, frame.getFormat(), frameData, 1.0f);

                        // Release infrared data frame
                        frame.close();
                    }
                }

                // Release FrameSet
                frameSet.close();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    };

}