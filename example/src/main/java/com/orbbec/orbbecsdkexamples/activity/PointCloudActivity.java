package com.orbbec.orbbecsdkexamples.activity;

import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.orbbec.obsensor.AlignMode;
import com.orbbec.obsensor.CameraParam;
import com.orbbec.obsensor.Config;
import com.orbbec.obsensor.DepthFrame;
import com.orbbec.obsensor.Device;
import com.orbbec.obsensor.DeviceChangedCallback;
import com.orbbec.obsensor.DeviceInfo;
import com.orbbec.obsensor.DeviceList;
import com.orbbec.obsensor.Format;
import com.orbbec.obsensor.Frame;
import com.orbbec.obsensor.FrameSet;
import com.orbbec.obsensor.FrameSetCallback;
import com.orbbec.obsensor.FrameType;
import com.orbbec.obsensor.Pipeline;
import com.orbbec.obsensor.PointCloudFilter;
import com.orbbec.obsensor.PointFrame;
import com.orbbec.obsensor.SensorType;
import com.orbbec.orbbecsdkexamples.R;
import com.orbbec.orbbecsdkexamples.utils.FileUtils;

import java.io.File;

/**
 * PointCloud Example
 */
public class PointCloudActivity extends BaseActivity implements View.OnClickListener {
    private static final String TAG = "PointCloudActivity";

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

    private DeviceChangedCallback mDeviceChangedCallback = new DeviceChangedCallback() {
        @Override
        public void onDeviceAttach(DeviceList deviceList) {
            try {
                if (null == mPipeline) {
                    // 2.Create Device and initialize Pipeline through Device
                    mDevice = deviceList.getDevice(0);

                    if (null == mDevice.getSensor(SensorType.COLOR)) {
                        mDevice.close();
                        mDevice = null;
                        showToast(getString(R.string.device_not_support_color));
                        runOnUiThread(() -> {
                            mSaveColorPointsBtn.setEnabled(false);
                        });
                    }

                    if (null == mDevice.getSensor(SensorType.DEPTH)) {
                        mDevice.close();
                        mDevice = null;
                        showToast(getString(R.string.device_not_support_depth));
                        return;
                    }

                    // 3.Create Device and initialize Pipeline through Device
                    mPipeline = new Pipeline(mDevice);

                    // 4.Create Config to configure pipeline opening sensors
                    Config config = genD2CConfig(mPipeline, AlignMode.ALIGN_D2C_HW_ENABLE);
                    if (null == config) {
                        mPipeline.close();
                        mPipeline = null;
                        mDevice.close();
                        mDevice = null;
                        Log.w(TAG, "onDeviceAttach: No target depth and color stream profile!");
                        showToast(getString(R.string.init_stream_profile_failed));
                        return;
                    }

                    // 5.Start sensors stream
                    mPipeline.start(config, mPointCloudFrameSetCallback);

                    // 6.Start the point cloud asynchronous processing thread
                    start();

                    // 7.Create point cloud filter
                    mPointCloudFilter = new PointCloudFilter();

                    // 8.Set the format of the point cloud filter
                    mPointCloudFilter.setPointFormat(mPointFormat);

                    // 9.Obtain camera intrinsic parameters and set parameters to point cloud filter
                    CameraParam cameraParam = mPipeline.getCameraParam();
                    mPointCloudFilter.setCameraParam(cameraParam);
                    Log.i(TAG, "onDeviceAttach: cameraParam:" + cameraParam);

                    // 10.Release config resources
                    config.close();

                    runOnUiThread(() -> {
                        mSaveColorPointsBtn.setEnabled(true);
                        mSaveDepthPointsBtn.setEnabled(true);
                    });
                }
            } catch (Exception e) {
                e.printStackTrace();
            } finally {
                // 11.Release device list resources
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
                            runOnUiThread(() -> {
                                mSaveColorPointsBtn.setEnabled(false);
                                mSaveDepthPointsBtn.setEnabled(false);
                            });
                            stop();
                            if (null != mPointCloudFilter) {
                                mPointCloudFilter.close();
                                mPointCloudFilter = null;
                            }
                            if (null != mPipeline) {
                                mPipeline.stop();
                                mPipeline.close();
                            }
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
        setTitle("PointCloud");
        setContentView(R.layout.activity_point_cloud);

        mInfoTv = findViewById(R.id.tv_information);
        mSaveDepthPointsBtn = findViewById(R.id.btn_save_depth_points);
        mSaveDepthPointsBtn.setOnClickListener(this);
        mSaveColorPointsBtn = findViewById(R.id.btn_save_rgb_points);
        mSaveColorPointsBtn.setOnClickListener(this);
    }

    @Override
    protected void onStart() {
        super.onStart();
        initSDK();
    }

    @Override
    protected void onStop() {
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

    private Config genD2CConfig(Pipeline pipeline, AlignMode alignMode) {
        BaseActivity.D2CStreamProfile d2CStreamProfile = genD2CStreamProfile(pipeline, alignMode);
        if (null == d2CStreamProfile) {
            return null;
        }

        Config config = new Config();
        config.setAlignMode(alignMode);
        config.enableStream(d2CStreamProfile.getColorProfile());
        config.enableStream(d2CStreamProfile.getDepthProfile());
        d2CStreamProfile.close();
        return config;
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
                        DepthFrame depthFrame = mPointFrameSet.getDepthFrame();
                        if (null != depthFrame) {
                            mPointCloudFilter.setPositionDataScale(depthFrame.getValueScale());
                            depthFrame.close();
                        }
                        // Point cloud filter processing generates corresponding point cloud data
                        frame = mPointCloudFilter.process(mPointFrameSet);

                        if (null != frame) {
                            // Get point cloud frames
                            PointFrame pointFrame = frame.as(FrameType.POINTS);

                            String rootSaveDirPath = FileUtils.getExternalSaveDir();
                            if (mIsSavePoints && !TextUtils.isEmpty(rootSaveDirPath)) {
                                File saveDirPath = new File(rootSaveDirPath + File.separator + "point_cloud");
                                if (!saveDirPath.exists()) {
                                    saveDirPath.mkdirs();
                                }

                                if (mPointFormat == Format.POINT) {
                                    // Get the depth point cloud data and save it. The data size of the depth point cloud is w * h * 3
                                    float[] depthPoints = new float[pointFrame.getDataSize() / Float.BYTES];
                                    pointFrame.getPointCloudData(depthPoints);
                                    String depthPointsPath = saveDirPath.getAbsolutePath() + "/point.ply";
                                    FileUtils.savePointCloud(depthPointsPath, depthPoints);
                                    runOnUiThread(new Runnable() {
                                        @Override
                                        public void run() {
                                            mInfoTv.append("Save Path:" + FileUtils.convertSDCardPath(depthPointsPath) + "\n");
                                        }
                                    });
                                } else {
                                    // Get the color point cloud data and save it, the data size of the color point cloud is w * h * 6
                                    float[] colorPoints = new float[pointFrame.getDataSize() / Float.BYTES];
                                    pointFrame.getPointCloudData(colorPoints);
                                    String colorPointsPath = saveDirPath.getAbsolutePath() + "/point_rgb.ply";
                                    FileUtils.saveRGBPointCloud(colorPointsPath, colorPoints);
                                    runOnUiThread(new Runnable() {
                                        @Override
                                        public void run() {
                                            mInfoTv.append("Save Path:" + FileUtils.convertSDCardPath(colorPointsPath) + "\n");
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

}