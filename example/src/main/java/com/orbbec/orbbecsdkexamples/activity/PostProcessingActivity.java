package com.orbbec.orbbecsdkexamples.activity;

import android.annotation.SuppressLint;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.CheckBox;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.Nullable;

import com.orbbec.obsensor.Config;
import com.orbbec.obsensor.DepthFrame;
import com.orbbec.obsensor.Device;
import com.orbbec.obsensor.DeviceChangedCallback;
import com.orbbec.obsensor.DeviceInfo;
import com.orbbec.obsensor.DeviceList;
import com.orbbec.obsensor.Filter;
import com.orbbec.obsensor.Format;
import com.orbbec.obsensor.Frame;
import com.orbbec.obsensor.FrameSet;
import com.orbbec.obsensor.FrameType;
import com.orbbec.obsensor.Pipeline;
import com.orbbec.obsensor.RecommendedFilterList;
import com.orbbec.obsensor.Sensor;
import com.orbbec.obsensor.SensorType;
import com.orbbec.obsensor.StreamType;
import com.orbbec.obsensor.VideoStreamProfile;
import com.orbbec.orbbecsdkexamples.R;
import com.orbbec.orbbecsdkexamples.view.OBGLView;

import java.util.HashMap;
import java.util.Map;

public class PostProcessingActivity extends BaseActivity implements View.OnClickListener {
    private static final String TAG = "PostProcessingActivity";

    private Pipeline mPipeline;
    private Thread mStreamThread;
    private volatile boolean mIsStreamRunning;
    private OBGLView mPostProcessingView;
    private TextView mDepthDistanceTv;
    private Device mDevice;
    private RecommendedFilterList mRecommendedFilterList;
    private int mCount;
    private Map<String, Filter> filters;
    private Filter mFilter;
    private boolean resizeWindow = false;

    private CheckBox mHoleFillingCb;
    private CheckBox mTemporalCb;
    private CheckBox mSpatialAdvancedCb;
    private CheckBox mNoiseRemovalCb;
    private CheckBox mEdgeNoiseRemovalCb;
    private CheckBox mDecimationCb;
    private CheckBox mThresholdCb;
    private CheckBox mSequenceIdCb;
    private CheckBox mDisparityTransformCb;

    private DeviceChangedCallback mDeviceChangedCallback = new DeviceChangedCallback() {

        @Override
        public void onDeviceAttach(DeviceList deviceList) {
            try {
                if (mPipeline == null) {
                    // 2.Create Device and initialize Pipeline through Device
                    mDevice = deviceList.getDevice(0);
                    Sensor depthSensor = mDevice.getSensor(SensorType.DEPTH);
                    if (null == depthSensor) {
                        showToast(getString(R.string.device_not_support_post_process));
                        mDevice.close();
                        mDevice = null;
                        return;
                    }
                    // 3.Create Device and initialize Pipeline through Device
                    mPipeline = new Pipeline(mDevice);

                    // 4.Create config to configure the resolution, frame rate, and format of the depth stream
                    Config config = new Config();

                    // 5.Get the depth sensor configuration and configure it to Config
                    VideoStreamProfile streamProfile = getStreamProfile(mPipeline, SensorType.DEPTH);
                    // 6.Enable depth StreamProfile
                    if (null != streamProfile) {
                        printStreamProfile(streamProfile.as(StreamType.VIDEO));
                        config.enableStream(streamProfile);
                        streamProfile.close();
                    } else {
                        mPipeline.close();
                        mPipeline = null;
                        mDevice.close();
                        mDevice = null;

                        config.close();

                        Log.w(TAG, "No target stream profile!");
                        return;
                    }

                    // 7.Get recommended post processor filter list and get the filter count
                    mRecommendedFilterList = depthSensor.getRecommendedFilterList();
                    mCount = mRecommendedFilterList.getFilterListCount();

                    filters = new HashMap<>();
                    for (int i = 0; i < mCount; i++) {
                        Filter filter = mRecommendedFilterList.getFilter(i);

                        String name = mRecommendedFilterList.getFilterName(filter);

                        if (TextUtils.equals(name, "DecimationFilter")) {
                            mFilter = filter;
                        }

                        Log.d(TAG, "onDeviceAttach: " + name);
                        filters.put(name, filter);
                    }

//                    if (mFilter != null && mFilter.isEnable()) {
//                        resizeWindow = true;
//                    }
//
//                    runOnUiThread(() -> {
//                        filters.forEach((k, v) -> {
//                            boolean isEnable = v.isEnable();
//                            switch (k) {
//                                case "HoleFillingFilter":
//                                    mHoleFillingCb.setVisibility(View.VISIBLE);
//                                    mHoleFillingCb.setChecked(isEnable);
//                                    break;
//                                case "TemporalFilter":
//                                    mTemporalCb.setVisibility(View.VISIBLE);
//                                    mTemporalCb.setChecked(isEnable);
//                                    break;
//                                case "SpatialAdvancedFilter":
//                                    mSpatialAdvancedCb.setVisibility(View.VISIBLE);
//                                    mSpatialAdvancedCb.setChecked(isEnable);
//                                    break;
//                                case "NoiseRemovalFilter":
//                                    mNoiseRemovalCb.setVisibility(View.VISIBLE);
//                                    mNoiseRemovalCb.setChecked(isEnable);
//                                    break;
//                                case "EdgeNoiseFilter":
//                                    mEdgeNoiseRemovalCb.setVisibility(View.VISIBLE);
//                                    mEdgeNoiseRemovalCb.setChecked(isEnable);
//                                case "DecimationFilter":
//                                    mDecimationCb.setVisibility(View.VISIBLE);
//                                    mDecimationCb.setChecked(isEnable);
//                                    break;
//                                case "ThresholdFilter":
//                                    mThresholdCb.setVisibility(View.VISIBLE);
//                                    mThresholdCb.setChecked(isEnable);
//                                    break;
//                                case "SequenceIdFilter":
//                                    mSequenceIdCb.setVisibility(View.VISIBLE);
//                                    mSequenceIdCb.setChecked(isEnable);
//                                    break;
//                                case "DisparityTransform":
//                                    mDisparityTransformCb.setVisibility(View.VISIBLE);
//                                    mDisparityTransformCb.setChecked(isEnable);
//                                    break;
//                            }
//                        });
//                    });

                    // 8.Start sensor stream
                    mPipeline.start(config);

                    // 9.Release config
                    config.close();

                    // 10.Create a thread to obtain Pipeline data
                    start();
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
                    for (int i = 0; i < deviceList.getDeviceCount(); i++) {
                        String uid = deviceList.getUid(i);
                        DeviceInfo deviceInfo = mDevice.getInfo();
                        if (deviceInfo != null && TextUtils.equals(uid, deviceInfo.getUid())) {
                            stop();
                            mPipeline.stop();
                            mPipeline.close();
                            mPipeline = null;
                            mRecommendedFilterList.close();
                            filters.forEach((k, v) -> {
                                v.close();
                            });
                            filters.clear();
                            mDevice.close();
                            mDevice = null;
                        }
                    }
                }
            } catch (Exception e) {
                e.printStackTrace();
            } finally {
                deviceList.close();
            }
        }
    };

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setTitle("PostProcessing");
        setContentView(R.layout.activity_post_processing);
        initView();
    }

    @Override
    protected void onStart() {
        super.onStart();
        initSDK();
    }

    @Override
    protected void onStop() {
        try {
            stop();

            if (null != mPipeline) {
                mPipeline.stop();
                mPipeline.close();
            }

            if (null != mRecommendedFilterList) {
                mRecommendedFilterList.close();
            }

            if (null != filters) {
                filters.forEach((k, v) -> {
                    v.close();
                });
                filters.clear();
            }

            if (null != mDevice) {
                mDevice.close();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        releaseSDK();
        super.onStop();
    }

    private void initView() {
        mPostProcessingView = findViewById(R.id.postprocessing_id);
//        mDepthDistanceTv = findViewById(R.id.postprocessing_tv);
//        mDepthDistanceTv.setText(String.format(getString(R.string.depth_distance), 0f));

//        mHoleFillingCb = findViewById(R.id.cb_holefilling);
//        mHoleFillingCb.setOnClickListener(this);
//        mTemporalCb = findViewById(R.id.cb_temporal);
//        mTemporalCb.setOnClickListener(this);
//        mSpatialAdvancedCb = findViewById(R.id.cb_spatial_advanced);
//        mSpatialAdvancedCb.setOnClickListener(this);
//        mNoiseRemovalCb = findViewById(R.id.cb_noise_removal);
//        mNoiseRemovalCb.setOnClickListener(this);
//        mEdgeNoiseRemovalCb = findViewById(R.id.cb_edge_noise_removal);
//        mEdgeNoiseRemovalCb.setOnClickListener(this);
//        mDecimationCb = findViewById(R.id.cb_decimation);
//        mDecimationCb.setOnClickListener(this);
//        mThresholdCb = findViewById(R.id.cb_threshold);
//        mThresholdCb.setOnClickListener(this);
//        mSequenceIdCb = findViewById(R.id.cb_sequenceId);
//        mSequenceIdCb.setOnClickListener(this);
//        mDisparityTransformCb = findViewById(R.id.cb_disparity_transform);
//        mDisparityTransformCb.setOnClickListener(this);
    }

    private void start() {
        mIsStreamRunning = true;
        if (mStreamThread == null) {
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
                e.printStackTrace();
            }
            mStreamThread = null;
        }
    }

    private Runnable mStreamRunnable = () -> {
        while (mIsStreamRunning) {
            try {
                FrameSet frameSet = mPipeline.waitForFrameSet(100);

                if (frameSet == null) {
                    continue;
                }

                DepthFrame frame = frameSet.getDepthFrame();
                if (frame != null) {
                    for (int i = 0; i < mCount; i++) {
                        for (Filter filter : filters.values()) {
                            if (filter.isEnable()) {
                                Frame f = filter.process(frame);
                                frame.close();
                                frame = f.as(FrameType.DEPTH);
                            }
                        }
                    }

                    byte[] data = new byte[frame.getDataSize()];
                    frame.getData(data);

                    int width = frame.getWidth();
                    int height = frame.getHeight();

                    if (frame.getFrameIndex() % 30 == 0 && frame.getFormat() == Format.Y16) {
                        float scale = frame.getValueScale();

                        float centerDistance = data[width * height / 2 + width / 2] * scale;
//                        runOnUiThread(() -> {
//                            mDepthDistanceTv.setText(String.format(getString(R.string.depth_distance), centerDistance));
//                        });

                        Log.i(TAG, "Facing an object: " + centerDistance + "mm away.");
                    }

//                    if (resizeWindow) {
//                        width = 848;
//                        height = 100;
//                    }
                    Log.d(TAG, "width: " + width + ", height: " + height);
                    mPostProcessingView.update(width, height, StreamType.DEPTH, frame.getFormat(), data, frame.getValueScale());

                    frame.close();
                }

                frameSet.close();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    };

    @Override
    protected DeviceChangedCallback getDeviceChangedCallback() {
        return mDeviceChangedCallback;
    }

    private void showToast(String msg) {
        runOnUiThread(() -> Toast.makeText(PostProcessingActivity.this, msg, Toast.LENGTH_SHORT).show());
    }

    @SuppressLint("NonConstantResourceId")
    @Override
    public void onClick(View v) {
//        Filter f;
//        switch (v.getId()) {
//            case R.id.cb_holefilling:
//                f = filters.get("HoleFillingFilter");
//                if (f != null) {
//                    f.enable(mHoleFillingCb.isChecked());
//                }
//                break;
//            case R.id.cb_temporal:
//                f = filters.get("TemporalFilter");
//                if (f != null) {
//                    f.enable(mTemporalCb.isChecked());
//                }
//                break;
//            case R.id.cb_spatial_advanced:
//                f = filters.get("SpatialAdvancedFilter");
//                if (f != null) {
//                    f.enable(mSpatialAdvancedCb.isChecked());
//                }
//                break;
//            case R.id.cb_noise_removal:
//                f = filters.get("NoiseRemovalFilter");
//                if (f != null) {
//                    f.enable(mNoiseRemovalCb.isChecked());
//                }
//                break;
//            case R.id.cb_edge_noise_removal:
//                f = filters.get("EdgeNoiseRemovalFilter");
//                if (f != null) {
//                    f.enable(mEdgeNoiseRemovalCb.isChecked());
//                }
//                break;
//            case R.id.cb_decimation:
//                f = filters.get("DecimationFilter");
//                if (f != null) {
//                    f.enable(mDecimationCb.isChecked());
//                    resizeWindow = !resizeWindow;
//                }
//                break;
//            case R.id.cb_threshold:
//                f = filters.get("ThresholdFilter");
//                if (f != null) {
//                    f.enable(mThresholdCb.isChecked());
//                }
//                break;
//            case R.id.cb_sequenceId:
//                f = filters.get("SequenceIdFilter");
//                if (f != null) {
//                    f.enable(mSequenceIdCb.isChecked());
//                }
//                break;
//            case R.id.cb_disparity_transform:
//                f = filters.get("DisparityTransform");
//                if (f != null) {
//                    f.enable(mDisparityTransformCb.isChecked());
//                }
//                break;
//        }
    }
}
