package com.orbbec.orbbecsdkexamples.activity;

import android.os.Build;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.orbbec.obsensor.AlignFilter;
import com.orbbec.obsensor.Config;
import com.orbbec.obsensor.DepthFrame;
import com.orbbec.obsensor.Device;
import com.orbbec.obsensor.DeviceChangedCallback;
import com.orbbec.obsensor.DeviceList;
import com.orbbec.obsensor.Frame;
import com.orbbec.obsensor.FrameSet;
import com.orbbec.obsensor.Pipeline;
import com.orbbec.obsensor.PointCloudFilter;
import com.orbbec.obsensor.PointFrame;
import com.orbbec.obsensor.types.DeviceInfo;
import com.orbbec.obsensor.types.Format;
import com.orbbec.obsensor.types.FrameAggregateOutputMode;
import com.orbbec.obsensor.types.FrameType;
import com.orbbec.obsensor.types.SensorType;
import com.orbbec.obsensor.types.StreamType;
import com.orbbec.orbbecsdkexamples.R;
import com.orbbec.orbbecsdkexamples.utils.FileUtils;

import java.io.File;
import java.text.SimpleDateFormat;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Date;
import java.util.Locale;

/**
 * PointCloud Example
 */
public class AdvancedPointCloudActivity extends BaseActivity implements View.OnClickListener {
    private static final String TAG = "AdvancedPointCloudActivity";

    private Device mDevice;
    private Pipeline mPipeline;
    private Thread mPointFilterThread;
    private PointCloudFilter mPointCloudFilter;
    private AlignFilter mAlignFilter;
    private Button mSaveDepthPointsBtn;
    private Button mSaveColorPointsBtn;
    private TextView mInfoTv;
    private boolean mIsSavePoints = false;
    private boolean mIsPointCloudRunning = false;

    private String mRootSaveDirPath;

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
                        runOnUiThread(() -> mSaveColorPointsBtn.setEnabled(false));
                    }

                    if (null == mDevice.getSensor(SensorType.DEPTH)) {
                        mDevice.close();
                        mDevice = null;
                        showToast(getString(R.string.device_not_support_depth));
                        return;
                    }

                    // 3.Create Device and initialize Pipeline through Device
                    mPipeline = new Pipeline(mDevice);

                    // 4.create config to configure the pipeline streams
                    Config config = new Config();
                    // 5.enable depth and color streams with specified format
                    config.enableVideoStream(SensorType.COLOR, 0, 0, 0, Format.RGB);
                    config.enableVideoStream(SensorType.DEPTH, 0, 0, 0, Format.Y16);

                    // 6.set frame aggregate output mode to all type frame require. therefor, the output frameset will contain all type of frames
                    config.setFrameAggregateOutputMode(FrameAggregateOutputMode.OB_FRAME_AGGREGATE_OUTPUT_ALL_TYPE_FRAME_REQUIRE);

                    // 7.Enable frame synchronization to ensure depth frame and color frame on output frameset are synchronized
                    mPipeline.enableFrameSync();
                    // 8.Start pipeline with config
                    mPipeline.start(config);

                    // 9.Create a point cloud Filter, which will be used to generate pointcloud frame from depth and color frames
                    mPointCloudFilter = new PointCloudFilter();
                    // 10.Create a Align Filter, which will be used to align depth frame and color frame
                    mAlignFilter = new AlignFilter(StreamType.COLOR);

                    // 11.Release config resources
                    config.close();

                    // 12.Create a thread to obtain Pipeline data
                    start();

                    runOnUiThread(() -> {
                        mSaveColorPointsBtn.setEnabled(true);
                        mSaveDepthPointsBtn.setEnabled(true);
                    });
                }
            } catch (Exception e) {
                Log.e(TAG, "onDeviceAttach: " + e.getMessage());
            } finally {
                // 13.Release device list resources
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
                            if (null != mAlignFilter) {
                                mAlignFilter.close();
                                mAlignFilter = null;
                            }
                            if (null != mPointCloudFilter) {
                                mPointCloudFilter.close();
                                mPointCloudFilter = null;
                            }
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
        setTitle("Advanced-Point Cloud");
        setContentView(R.layout.activity_advanced_point_cloud);

        mInfoTv = findViewById(R.id.tv_information);
        mSaveDepthPointsBtn = findViewById(R.id.btn_save_depth_points);
        mSaveDepthPointsBtn.setOnClickListener(this);
        mSaveColorPointsBtn = findViewById(R.id.btn_save_rgb_points);
        mSaveColorPointsBtn.setOnClickListener(this);

        mRootSaveDirPath = FileUtils.getExternalSaveDir();
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

            // Release align filter
            if (null != mAlignFilter) {
                mAlignFilter.close();
                mAlignFilter = null;
            }

            // Release point cloud filter
            if (null != mPointCloudFilter) {
                mPointCloudFilter.close();
                mPointCloudFilter = null;
            }

            // Release Device
            if (mDevice != null) {
                mDevice.close();
                mDevice = null;
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

    @Override
    public void onClick(View v) {
        int id = v.getId();
        if (id == R.id.btn_save_depth_points) {
            mInfoTv.setText("Saving depth points...\n");
            mPointCloudFilter.setPointFormat(Format.POINT);
            mIsSavePoints = true;
        } else if (id == R.id.btn_save_rgb_points) {
            mInfoTv.setText("Saving color points...\n");
            mPointCloudFilter.setPointFormat(Format.RGB_POINT);
            mIsSavePoints = true;
        }
    }

    private void showToast(String msg) {
        runOnUiThread(() -> Toast.makeText(AdvancedPointCloudActivity.this, msg, Toast.LENGTH_SHORT).show());
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
                Log.e(TAG, "stop: " + e.getMessage());
            }
            mPointFilterThread = null;
        }
    }

    private void savePointCloudToPly(PointFrame frame, boolean isRGB) {
        if (TextUtils.isEmpty(mRootSaveDirPath)) {
            Log.w(TAG, "savePointCloudToPly: Save directory path as empty");
            return;
        }

        File saveDirPath = new File(mRootSaveDirPath + File.separator + "point_cloud");
        if (!saveDirPath.exists()) {
            boolean mkRes = saveDirPath.mkdirs();
            if (!mkRes) {
                Log.w(TAG, "savePointCloudToPly: Create directory failed");
            }
        }

        String timestamp;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss");
            timestamp = LocalDateTime.now().format(formatter);
        } else {
            SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault());
            timestamp = sdf.format(new Date());
        }

        float[] pointCloudData = new float[frame.getDataSize() / Float.BYTES];
        frame.getPointCloudData(pointCloudData);
        String pointCloudPath;
        if (!isRGB) {
            pointCloudPath = saveDirPath.getAbsolutePath() + "/" + timestamp + "_point.ply";
            FileUtils.savePointCloud(pointCloudPath, pointCloudData);
        } else {
            pointCloudPath = saveDirPath.getAbsolutePath() + "/" + timestamp + "_point_rgb.ply";
            FileUtils.saveRGBPointCloud(pointCloudPath, pointCloudData);
        }
        runOnUiThread(() -> mInfoTv.append("Save Path:" + FileUtils.convertSDCardPath(pointCloudPath) + "\n"));
    }

    private final Runnable mPointFilterRunnable = new Runnable() {
        @Override
        public void run() {
            while (mIsPointCloudRunning) {
                try (FrameSet frameSet = mPipeline.waitForFrameSet(1000)) {
                    if (null == frameSet) continue;

                    if (mIsSavePoints) {
                        DepthFrame depthFrame = frameSet.getDepthFrame();
                        if (null != depthFrame) {
                            mPointCloudFilter.setCoordinateDataScaled(depthFrame.getValueScale());
                            depthFrame.close();
                        }
                        Frame alignedFrameSet = mAlignFilter.process(frameSet);
                        if (alignedFrameSet == null) continue;
                        Frame frame = mPointCloudFilter.process(alignedFrameSet);

                        if (frame != null) {
                            PointFrame pointFrame = frame.as(FrameType.POINTS);
                            // Get the depth point cloud data and save it. The data size of the depth point cloud is w * h * 3
                            // Get the color point cloud data and save it, the data size of the color point cloud is w * h * 6
                            savePointCloudToPly(pointFrame, pointFrame.getFormat() != Format.POINT);
                            pointFrame.close();
                        }
                        mIsSavePoints = false;
                        alignedFrameSet.close();
                    }
                } catch (Exception e) {
                    Log.e(TAG, "run: " + e.getMessage());
                }
            }
        }
    };
}