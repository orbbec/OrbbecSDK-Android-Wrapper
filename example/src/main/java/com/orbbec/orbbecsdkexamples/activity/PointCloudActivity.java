package com.orbbec.orbbecsdkexamples.activity;

import android.os.Bundle;
import android.os.Environment;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.orbbec.obsensor.AlignMode;
import com.orbbec.obsensor.CameraParam;
import com.orbbec.obsensor.Config;
import com.orbbec.obsensor.Device;
import com.orbbec.obsensor.DeviceChangedCallback;
import com.orbbec.obsensor.DeviceList;
import com.orbbec.obsensor.Format;
import com.orbbec.obsensor.Frame;
import com.orbbec.obsensor.FrameSet;
import com.orbbec.obsensor.FrameSetCallback;
import com.orbbec.obsensor.FrameType;
import com.orbbec.obsensor.OBContext;
import com.orbbec.obsensor.Pipeline;
import com.orbbec.obsensor.PointCloudFilter;
import com.orbbec.obsensor.PointFrame;
import com.orbbec.obsensor.SensorType;
import com.orbbec.obsensor.StreamProfileList;
import com.orbbec.obsensor.VideoStreamProfile;
import com.orbbec.orbbecsdkexamples.R;
import com.orbbec.orbbecsdkexamples.utils.FileUtils;

import java.io.File;

/**
 * PointCloud Example
 */
public class PointCloudActivity extends AppCompatActivity implements View.OnClickListener {
    private static final String TAG = "PointCloudActivity";

    private File mSdcardDir = Environment.getExternalStorageDirectory();
    private OBContext mOBContext;
    private Pipeline mPipeline;
    private Device mDevice;
    private Thread mPointFilterThread;
    private PointCloudFilter mPointCloudFilter;
    private Button mSaveDepthPointsBtn;
    private Button mSaveColorPointsBtn;
    private TextView mInfoTv;
    private Format mPointFormat = Format.POINT;
    private FrameSet mPointFrameSet;
    private boolean mIsSavePoints = false;
    private boolean mIsPointCloudRunning = false;

    // Pipeline open sensors stream callback
    private FrameSetCallback mPointCloudFrameSetCallback = frameSet -> {
        if (null != frameSet) {
            if (mIsPointCloudRunning) {
                if (null == mPointFrameSet) {
                    mPointFrameSet = frameSet;
                    return;
                }
            }

            frameSet.close();
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setTitle("PointCloud");
        setContentView(R.layout.activity_point_cloud);

        mInfoTv = findViewById(R.id.tv_information);
        mSaveDepthPointsBtn = findViewById(R.id.btn_save_depth_points);
        mSaveDepthPointsBtn.setOnClickListener(this);
        mSaveColorPointsBtn = findViewById(R.id.btn_save_rgb_points);
        mSaveColorPointsBtn.setOnClickListener(this);

        // 1.Initialize the SDK Context and listen device changes
        mOBContext = new OBContext(getApplicationContext(), new DeviceChangedCallback() {
            @Override
            public void onDeviceAttach(DeviceList deviceList) {
                try {
                    if (null == mPipeline) {
                        // 2.Create Device and initialize Pipeline through Device
                        mDevice = deviceList.getDevice(0);

                        if (null == mDevice.getSensor(SensorType.COLOR)) {
                            showToast(getString(R.string.device_not_support_color));
                            runOnUiThread(() -> {
                                mSaveColorPointsBtn.setEnabled(false);
                            });
                        }

                        if (null == mDevice.getSensor(SensorType.DEPTH)) {
                            showToast(getString(R.string.device_not_support_depth));
                            return;
                        }

                        // 3.Create Device and initialize Pipeline through Device
                        mPipeline = new Pipeline(mDevice);

                        // 4.Create Config to configure pipeline opening sensors
                        Config config = new Config();

                        // 5.Get depth VideoStreamProfile List
                        StreamProfileList depthProfileList = mPipeline.getStreamProfileList(SensorType.DEPTH);
                        VideoStreamProfile depthProfileTarget = getVideoStreamProfile(depthProfileList, 640, 0, Format.UNKNOWN, 30);
                        if (null == depthProfileTarget) {
                            depthProfileTarget = getVideoStreamProfile(depthProfileList, 0, 0, Format.UNKNOWN, 30);
                        }
                        if (null != depthProfileList) {
                            depthProfileList.close();
                        }

                        // 6.Enable the configuration after obtaining the specified depth VideoStreamProfile.
                        int depthProfileW = 0;
                        int depthProfileH = 0;
                        if (null != depthProfileTarget) {
                            printStreamProfile(depthProfileTarget);
                            config.enableStream(depthProfileTarget);
                            depthProfileW = depthProfileTarget.getWidth();
                            depthProfileH = depthProfileTarget.getHeight();
                            depthProfileTarget.close();
                        } else {
                            Log.w(TAG, "onDeviceAttach: No target depth stream profile!");
                        }

                        // 7.Get color VideoStreamProfile list
                        try {
                            StreamProfileList colorProfileList = mPipeline.getStreamProfileList(SensorType.COLOR);
                            VideoStreamProfile colorProfileTarget = getVideoStreamProfile(colorProfileList, 640, 0, Format.RGB888, 30);
                            if (null == colorProfileTarget) {
                                colorProfileTarget = getVideoStreamProfile(colorProfileList, 0, 0, Format.UNKNOWN, 30);
                            }
                            if (null != colorProfileList) {
                                colorProfileList.close();
                            }

                            // 8.Enable the configuration after obtaining the specified color VideoStreamProfile
                            if (null != colorProfileTarget) {
                                printStreamProfile(colorProfileTarget);
                                config.enableStream(colorProfileTarget);
                                colorProfileTarget.close();
                            } else {
                                Log.w(TAG, "onDeviceAttach: No target color stream profile!");
                            }
                        } catch (Exception e) {
                            // If the color sensor is not supported, an exception will be thrown when
                            // obtaining the profile. In order to ensure the normal operation of the
                            // depth point cloud, it is necessary to configure the depth zoom to not zoom,
                            // and configure the D2C target resolution to be the same as the depth.
                            e.printStackTrace();
                            config.setDepthScaleRequire(false);
                            config.setD2CTargetResolution(depthProfileW, depthProfileH);
                        }

                        // 9.Enable hardware D2C
                        config.setAlignMode(AlignMode.ALIGN_D2C_HW_ENABLE);

                        // 10.Start sensors stream
                        mPipeline.start(config, mPointCloudFrameSetCallback);

                        // 11.Start the point cloud asynchronous processing thread
                        start();

                        // 12.Create point cloud filter
                        mPointCloudFilter = new PointCloudFilter();

                        // 13.Set the format of the point cloud filter
                        mPointCloudFilter.setPointFormat(mPointFormat);

                        // 14.Obtain camera intrinsic parameters and set parameters to point cloud filter
                        CameraParam cameraParam = mPipeline.getCameraParam();
                        mPointCloudFilter.setCameraParam(cameraParam);
                        Log.i(TAG, "onDeviceAttach: cameraParam:" + cameraParam);

                        // 15.Release config resources
                        config.close();
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                } finally {
                    // 16.Release device list resources
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

    @Override
    public void onClick(View v) {
        int id = v.getId();
        if (id == R.id.btn_save_depth_points) {
            mInfoTv.setText("Saving depth points...\n");
            mPointFormat = Format.POINT;
            mIsSavePoints = true;
        } else if (id == R.id.btn_save_rgb_points) {
            mInfoTv.setText("Saving color points...\n");
            mPointFormat = Format.RGB_POINT;
            mIsSavePoints = true;
        }
    }

    private void showToast(String msg) {
        runOnUiThread(() -> Toast.makeText(PointCloudActivity.this, msg, Toast.LENGTH_SHORT).show());
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
        mIsPointCloudRunning = true;
        if (null == mPointFilterThread) {
            mPointFilterThread = new Thread(mPointFilterRunnable);
            mPointFilterThread.start();
        }
    }

    private void stop() {
        mIsPointCloudRunning = false;
        if (null != mPointFilterThread) {
            try {
                mPointFilterThread.join(300);
            } catch (InterruptedException e) {
            }
            mPointFilterThread = null;
        }
    }

    private final Runnable mPointFilterRunnable = new Runnable() {
        @Override
        public void run() {
            while (mIsPointCloudRunning) {
                try {
                    if (null != mPointFrameSet) {
                        Frame frame = null;
                        if (mPointFormat == Format.POINT) {
                            // Set the save format to depth point cloud
                            mPointCloudFilter.setPointFormat(Format.POINT);
                        } else {
                            // Set the save format to color point cloud
                            mPointCloudFilter.setPointFormat(Format.RGB_POINT);
                        }
                        // Point cloud filter processing generates corresponding point cloud data
                        frame = mPointCloudFilter.process(mPointFrameSet);

                        if (null != frame) {
                            // Get point cloud frames
                            PointFrame pointFrame = frame.as(FrameType.POINTS);

                            if (mIsSavePoints) {
                                if (mPointFormat == Format.POINT) {
                                    // Get the depth point cloud data and save it. The data size of the depth point cloud is w * h * 3
                                    float[] depthPoints = new float[pointFrame.getDataSize() / Float.BYTES];
                                    pointFrame.getPointCloudData(depthPoints);
                                    String depthPointsPath = mSdcardDir.toString() + "/Orbbec/point.ply";
                                    FileUtils.savePointCloud(depthPointsPath, depthPoints);
                                    runOnUiThread(new Runnable() {
                                        @Override
                                        public void run() {
                                            mInfoTv.append("Save Path:" + depthPointsPath + "\n");
                                        }
                                    });
                                } else {
                                    // Get the color point cloud data and save it, the data size of the color point cloud is w * h * 6
                                    float[] colorPoints = new float[pointFrame.getDataSize() / Float.BYTES];
                                    pointFrame.getPointCloudData(colorPoints);
                                    String colorPointsPath = mSdcardDir.toString() + "/Orbbec/point_rgb.ply";
                                    FileUtils.saveRGBPointCloud(colorPointsPath, colorPoints);
                                    runOnUiThread(new Runnable() {
                                        @Override
                                        public void run() {
                                            mInfoTv.append("Save Path:" + colorPointsPath + "\n");
                                        }
                                    });
                                }

                                mIsSavePoints = false;
                            }

                            // Release the newly generated frame
                            frame.close();
                        }

                        // Release the original data frameSet
                        mPointFrameSet.close();
                        mPointFrameSet = null;
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }
    };

    @Override
    protected void onDestroy() {
        super.onDestroy();
        try {
            // Stop and release the point cloud filter processing thread
            stop();

            // Stop the Pipeline and close it
            if (null != mPipeline) {
                mPipeline.stop();
                mPipeline.close();
                mPipeline = null;
            }

            // Release point cloud filter
            if (null != mPointCloudFilter) {
                try {
                    mPointCloudFilter.close();
                } catch (Exception e) {
                }
                mPointCloudFilter = null;
            }

            // Release Device
            if (mDevice != null) {
                mDevice.close();
                mDevice = null;
            }

            // Release SDK Context
            if (null != mOBContext) {
                mOBContext.close();
                mOBContext = null;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}