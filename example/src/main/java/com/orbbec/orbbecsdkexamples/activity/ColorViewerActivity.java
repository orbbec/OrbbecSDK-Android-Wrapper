package com.orbbec.orbbecsdkexamples.activity;

import android.os.Bundle;
import android.util.Log;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.orbbec.obsensor.ColorFrame;
import com.orbbec.obsensor.Config;
import com.orbbec.obsensor.Device;
import com.orbbec.obsensor.DeviceChangedCallback;
import com.orbbec.obsensor.DeviceList;
import com.orbbec.obsensor.Format;
import com.orbbec.obsensor.FrameSet;
import com.orbbec.obsensor.OBContext;
import com.orbbec.obsensor.Pipeline;
import com.orbbec.obsensor.Sensor;
import com.orbbec.obsensor.SensorType;
import com.orbbec.obsensor.StreamProfile;
import com.orbbec.obsensor.StreamProfileList;
import com.orbbec.obsensor.StreamType;
import com.orbbec.obsensor.VideoStreamProfile;
import com.orbbec.orbbecsdkexamples.R;
import com.orbbec.orbbecsdkexamples.view.OBGLView;

import java.nio.ByteBuffer;

/**
 * Color Viewer
 */
public class ColorViewerActivity extends AppCompatActivity {
    private static final String TAG = "ColorViewerActivity";

    private OBContext mOBContext;
    private Pipeline mPipeline;
    private Thread mStreamThread;
    private volatile boolean mIsStreamRunning;
    private OBGLView mColorView;
    private Device mDevice;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setTitle("ColorView");
        setContentView(R.layout.activity_color_viewer);
        mColorView = findViewById(R.id.colorview_id);
        // 1.Initialize the SDK Context and listen device changes
        mOBContext = new OBContext(getApplicationContext(), new DeviceChangedCallback() {
            @Override
            public void onDeviceAttach(DeviceList deviceList) {
                try {
                    if (null == mPipeline) {
                        // 2.Create Device and initialize Pipeline through Device
                        mDevice = deviceList.getDevice(0);
                        Sensor colorSensor = mDevice.getSensor(SensorType.COLOR);
                        if (null == colorSensor) {
                            showToast(getString(R.string.device_not_support_color));
                            return;
                        }
                        // 3. Create Device and initialize Pipeline through Device
                        mPipeline = new Pipeline(mDevice);

                        // 4.Create Pipeline configuration
                        Config config = new Config();

                        // 5.Get the color sensor configuration and configure it to Config
                        // Here, matching is performed based on the width, frame rate and RGB888 format.
                        // If the configuration is not changed, the matching meets the configuration
                        // of a width of 640 and a frame rate of 30fps.
                        StreamProfileList colorProfileList = mPipeline.getStreamProfileList(SensorType.COLOR);
                        StreamProfile streamProfile = getVideoStreamProfile(colorProfileList, 640, 0, Format.RGB888, 30);
                        if (null == streamProfile) {
                            streamProfile = getVideoStreamProfile(colorProfileList, 0, 0, Format.RGB888, 30);
                        }
                        if (null != colorProfileList) {
                            colorProfileList.close();
                        }

                        // 6.Enable color StreamProfile
                        if (null != streamProfile) {
                            printStreamProfile(streamProfile.as(StreamType.VIDEO));
                            config.enableStream(streamProfile);
                            streamProfile.close();
                        } else {
                            Log.w(TAG, "No target stream profile!");
                        }

                        // 7.Start sensor stream
                        mPipeline.start(config);

                        // 8.Release config
                        config.close();

                        // 9.Create a thread to obtain Pipeline data
                        start();
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                } finally {
                    // 10.Release device list resources
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
        runOnUiThread(() -> Toast.makeText(ColorViewerActivity.this, msg, Toast.LENGTH_SHORT).show());
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
        ByteBuffer buffer = null;
        while (mIsStreamRunning) {
            try {
                // Obtain the data set in blocking mode. If it cannot be obtained after waiting for 100ms, it will time out.
                FrameSet frameSet = mPipeline.waitForFrameSet(100);

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
                // Release FrameSet
                frameSet.close();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    };

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
            if (mDevice != null) {
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