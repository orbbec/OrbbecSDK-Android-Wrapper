package com.orbbec.orbbecsdkexamples.activity;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.util.Log;
import android.widget.Toast;

import com.orbbec.obsensor.Config;
import com.orbbec.obsensor.Device;
import com.orbbec.obsensor.DeviceChangedCallback;
import com.orbbec.obsensor.DeviceList;
import com.orbbec.obsensor.DeviceProperty;
import com.orbbec.obsensor.Format;
import com.orbbec.obsensor.FrameSet;
import com.orbbec.obsensor.IRFrame;
import com.orbbec.obsensor.OBContext;
import com.orbbec.obsensor.PermissionType;
import com.orbbec.obsensor.Pipeline;
import com.orbbec.obsensor.Sensor;
import com.orbbec.obsensor.SensorType;
import com.orbbec.obsensor.StreamProfile;
import com.orbbec.obsensor.StreamProfileList;
import com.orbbec.obsensor.StreamType;
import com.orbbec.obsensor.VideoStreamProfile;
import com.orbbec.orbbecsdkexamples.R;
import com.orbbec.orbbecsdkexamples.view.OBGLView;

/**
 * 红外渲染示例
 */
public class InfraredViewerActivity extends AppCompatActivity {
    private static final String TAG = "InfraredViewerActivity";

    private OBContext mOBContext;
    private Pipeline mPipeline;
    private Thread mStreamThread;
    private volatile boolean mIsStreamRunning;
    private OBGLView mIrView;
    private Device mDevice;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setTitle("InfraredViewer");
        setContentView(R.layout.activity_infrared_viewer);
        mIrView = findViewById(R.id.irview_id);
        // 1.Initialize the SDK Context and listen device changes
        mOBContext = new OBContext(getApplicationContext(), new DeviceChangedCallback() {
            @Override
            public void onDeviceAttach(DeviceList deviceList) {
                try {
                    if (null == mPipeline) {
                        // 2.Create Device and initialize Pipeline through Device
                        mDevice = deviceList.getDevice(0);

                        Sensor irSensor = mDevice.getSensor(SensorType.IR);
                        if (null == irSensor) {
                            showToast(getString(R.string.device_not_support_ir));
                            return;
                        }

                        mPipeline = new Pipeline(mDevice);

                        // 3.Create Pipeline configuration
                        Config config = new Config();

                        // 4.Obtain the stream configuration and configure it to Config, where the matching
                        // is performed according to the width and frame rate, and the matching satisfies
                        // the configuration with a width of 640 and a frame rate of 30fps
                        StreamProfileList irProfileList = mPipeline.getStreamProfileList(SensorType.IR);
                        StreamProfile streamProfile = getVideoStreamProfile(irProfileList, 640, 0, Format.UNKNOWN, 30);
                        if (null == streamProfile) {
                            streamProfile = getVideoStreamProfile(irProfileList, 0, 0, Format.UNKNOWN, 30);
                        }
                        if (null != irProfileList) {
                            irProfileList.close();
                        }

                        // 5.Enable infrared StreamProfile
                        if (null != streamProfile) {
                            printStreamProfile(streamProfile.as(StreamType.VIDEO));
                            config.enableStream(streamProfile);
                            streamProfile.close();
                        } else {
                            Log.w(TAG, "onDeviceAttach: No target stream profile!");
                            mPipeline.close();
                            mPipeline = null;

                            config.close();
                            return;
                        }

                        // 6.Start sensor stream
                        mPipeline.start(config);

                        // 7.Release config
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
                    deviceList.close();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        });
    }

    private void showToast(String msg) {
        runOnUiThread(() -> Toast.makeText(InfraredViewerActivity.this, msg, Toast.LENGTH_SHORT).show());
    }

    private void printStreamProfile(VideoStreamProfile vsp) {
        Log.i(TAG, "printStreamProfile: "
                + vsp.getWidth() + "×" + vsp.getHeight()
                + "@" + vsp.getFps() + "fps " + vsp.getFormat());
    }

    private VideoStreamProfile getVideoStreamProfile(StreamProfileList profileList,
                                                     int width, int height, Format format, int fps) {
        VideoStreamProfile vsp = null;
        try {
            vsp = profileList.getVideoStreamProfile(width, height, format, fps);
        } catch (Exception e) {
            Log.w(TAG, "getVideoStreamProfile: " + e.getMessage());
        }
        return vsp;
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

                // Get Infrared flow data
                IRFrame frame = frameSet.getIrFrame();

                if (frame != null) {
                    // Get infrared data and render it
                    byte[] frameData = new byte[frame.getDataSize()];
                    frame.getData(frameData);
                    mIrView.update(frame.getWidth(), frame.getHeight(), StreamType.IR, frame.getFormat(), frameData, 1.0f);

                    // Release infrared data frame
                    frame.close();
                }

                // Release FrameSet
                frameSet.close();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    };

    @Override
    protected void onDestroy() {
        super.onDestroy();
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

            // Release SDK Context
            if (null != mOBContext) {
                mOBContext.close();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

}