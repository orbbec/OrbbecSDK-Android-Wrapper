package com.orbbec.orbbecsdkexamples.activity;

import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.DisplayMetrics;
import android.util.Log;
import android.util.TypedValue;
import android.widget.CheckBox;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

import com.orbbec.obsensor.ColorFrame;
import com.orbbec.obsensor.Config;
import com.orbbec.obsensor.DepthFrame;
import com.orbbec.obsensor.Device;
import com.orbbec.obsensor.DeviceChangedCallback;
import com.orbbec.obsensor.DeviceList;
import com.orbbec.obsensor.FormatConvertFilter;
import com.orbbec.obsensor.Frame;
import com.orbbec.obsensor.FrameSet;
import com.orbbec.obsensor.Pipeline;
import com.orbbec.obsensor.StreamProfile;
import com.orbbec.obsensor.StreamProfileList;
import com.orbbec.obsensor.VideoStreamProfile;
import com.orbbec.obsensor.types.AlignMode;
import com.orbbec.obsensor.types.ConvertFormat;
import com.orbbec.obsensor.types.DeviceInfo;
import com.orbbec.obsensor.types.Format;
import com.orbbec.obsensor.types.FrameAggregateOutputMode;
import com.orbbec.obsensor.types.FrameType;
import com.orbbec.obsensor.types.SensorType;
import com.orbbec.obsensor.types.StreamType;
import com.orbbec.orbbecsdkexamples.R;
import com.orbbec.orbbecsdkexamples.utils.ImageUtils;
import com.orbbec.orbbecsdkexamples.view.OBGLView;

import java.nio.ByteBuffer;
import java.util.Locale;

public class AdvancedHwD2CAlignActivity extends BaseActivity {
    private static final String TAG = "AdvancedHwD2CAlignActivity";

    private Device mDevice;
    private Pipeline mPipeline;
    private Config mConfig;
    private Thread mStreamThread;
    private volatile boolean mIsStreamRunning;
    private OBGLView mHwD2CView;
    private CheckBox mHardwareD2CCb;
    private boolean mIsHwD2CEnabled = true;

    private TextView mColorProfileInfoTv;
    private TextView mDepthProfileInfoTv;
    private TextView mTransparencyTv;
    private float mAlpha = 0.5f;

    private ByteBuffer mDepthSrcBuffer;
    private ByteBuffer mDepthDstBuffer;

    private ByteBuffer mColorSrcBuffer;
    private ByteBuffer mColorDstBuffer;

    private FormatConvertFilter formatConvertFilter;

    private DeviceChangedCallback mDeviceChangedCallback = new DeviceChangedCallback() {
        @Override
        public void onDeviceAttach(DeviceList deviceList) {
            try {
                if (null == mPipeline) {
                    // 2.Create Device and initialize Pipeline through Device
                    mDevice = deviceList.getDevice(0);

                    if (null == mDevice.getSensor(SensorType.DEPTH)) {
                        mDevice.close();
                        mDevice = null;
                        showToast(getString(R.string.device_not_support_depth));
                        return;
                    }

                    if (null == mDevice.getSensor(SensorType.COLOR)) {
                        mDevice.close();
                        mDevice = null;
                        showToast(getString(R.string.device_not_support_color));
                        return;
                    }

                    mPipeline = new Pipeline(mDevice);

                    // 3.Create a config for hardware depth-to-color alignment
                    // Method 1: Implement according to the C++example
                    // Method 2: You can refer to the implementation of a closed source wrapper
                    mConfig = createHwD2CAlignConfig();

                    // 4.Enable frame sync inside the pipeline, which is synchronized by frame timestamp
                    mPipeline.enableFrameSync();

                    // 5.Start the pipeline with config and create a thread to obtain Pipeline data
                    startStream();
                }
            } catch (Exception e) {
                Log.e(TAG, "onDeviceAttach: " + e.getMessage());
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
        setTitle("Advanced-HwD2C Align");
        setContentView(R.layout.activity_advanced_hw_d2c_align);
        initView();
    }

    @Override
    protected void onStart() {
        super.onStart();
        initSDK();
        if(formatConvertFilter == null){
            formatConvertFilter = new FormatConvertFilter();
            formatConvertFilter.setFormatType(ConvertFormat.FORMAT_MJPEG_TO_RGB);
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        // 从后台回到前台时，若 pipeline 已存在则恢复出流
        if (mPipeline != null && !mIsStreamRunning) {
            startStream();
        }
    }

    @Override
    protected void onPause() {
        // 退到后台时停止出流，但保留 pipeline/config/device 资源
        stop();
        if (mPipeline != null) {
            try {
                mPipeline.stop();
            } catch (Exception e) {
                Log.e(TAG, "onPause pipeline stop: " + e.getMessage());
            }
        }
        super.onPause();
    }

    @Override
    protected void onStop() {
        // 释放全部资源必须在 releaseSDK 之前
        try {
            if (mPipeline != null) {
                mPipeline.close();
                mPipeline = null;
            }
            if (mConfig != null) {
                mConfig.close();
                mConfig = null;
            }
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
    protected void onDestroy() {
        // 安全兜底：正常情况 onStop 已释放
        try {
            if(formatConvertFilter != null){
                formatConvertFilter.close();
            }
            if (mPipeline != null) {
                mPipeline.close();
                mPipeline = null;
            }
            if (mConfig != null) {
                mConfig.close();
                mConfig = null;
            }
            if (mDevice != null) {
                mDevice.close();
                mDevice = null;
            }
        } catch (Exception e) {
            Log.e(TAG, "onDestroy: " + e.getMessage());
        }
        super.onDestroy();
    }

    @Override
    protected DeviceChangedCallback getDeviceChangedCallback() {
        return mDeviceChangedCallback;
    }

    private void showToast(String msg) {
        runOnUiThread(() -> Toast.makeText(AdvancedHwD2CAlignActivity.this, msg, Toast.LENGTH_SHORT).show());
    }

    private int getContrastColor(int backgroundColor) {
        // Calculate relative luminance.
        double darkness = 1 - (0.299 * Color.red(backgroundColor) + 0.587 * Color.green(backgroundColor) + 0.114 * Color.blue(backgroundColor)) / 255;
        if (darkness < 0.5) {
            return Color.BLACK; // Light background, use black text
        } else {
            return Color.WHITE; // Dark background, use white text
        }
    }

    private void updateTextColor() {
        // Get background color of activity to determine text color
        int bgColor = Color.BLACK; // Default
        TypedValue a = new TypedValue();
        getTheme().resolveAttribute(android.R.attr.windowBackground, a, true);
        if (a.type >= TypedValue.TYPE_FIRST_COLOR_INT && a.type <= TypedValue.TYPE_LAST_COLOR_INT) {
            bgColor = a.data;
        } else {
            try {
                Drawable background = getWindow().getDecorView().getBackground();
                if (background instanceof ColorDrawable) {
                    bgColor = ((ColorDrawable) background).getColor();
                }
            } catch (Exception ignore) {}
        }
        int contrastColor = getContrastColor(bgColor);
        mHardwareD2CCb.setTextColor(contrastColor);
    }

    private void initView() {
        mHwD2CView = findViewById(R.id.hw_d2c_view);
        SeekBar mTransparencySb = findViewById(R.id.sb_transparency);
        mTransparencyTv = findViewById(R.id.tv_transparency);
        mColorProfileInfoTv = findViewById(R.id.tv_color_profile_info);
        mDepthProfileInfoTv = findViewById(R.id.tv_depth_profile_info);
        mHardwareD2CCb = findViewById(R.id.cb_hw_d2c);

        mHardwareD2CCb.setOnClickListener(v -> {
            mIsHwD2CEnabled = mHardwareD2CCb.isChecked();
            setAlign(mIsHwD2CEnabled);
        });
        DisplayMetrics dm = getResources().getDisplayMetrics();
        int screenWidth = dm.widthPixels;
        mTransparencySb.getLayoutParams().width = (int) (screenWidth * 0.4);
        mTransparencySb.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                if (seekBar.getId() == R.id.sb_transparency) {
                    float progressF = progress / 100f;
                    mTransparencyTv.setText(String.format(Locale.getDefault(), "%.2f", progressF));
                    mAlpha = progressF;
                }
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {

            }
        });

        mTransparencyTv.setText(String.format(Locale.getDefault(), "%.2f", mTransparencySb.getProgress() / 100f));
        mColorProfileInfoTv.setText("color: null");
        mDepthProfileInfoTv.setText("depth: null");

        updateTextColor();
    }

    private void startStream() {
        try {
            mPipeline.start(mConfig);
            start();
        } catch (Exception e) {
            Log.e(TAG, "startStream: " + e.getMessage());
        }
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
                Log.e(TAG, "stop: " + e.getMessage());
            }
            mStreamThread = null;
        }
    }

    private void decodeColorFrame(ColorFrame colorFrame) {
        if (null == colorFrame) {
            return;
        }

        int colorW = colorFrame.getWidth();
        int colorH = colorFrame.getHeight();
        if (null == mColorSrcBuffer || mColorSrcBuffer.capacity() != colorFrame.getDataSize()) {
            mColorSrcBuffer = ByteBuffer.allocateDirect(colorFrame.getDataSize());
        }
        mColorSrcBuffer.clear();
        colorFrame.getData(mColorSrcBuffer);

        int colorDstSize = colorW * colorH * 3;
        if (null == mColorDstBuffer || mColorDstBuffer.capacity() != colorDstSize) {
            mColorDstBuffer = ByteBuffer.allocateDirect(colorDstSize);
        }
        mColorDstBuffer.clear();

        switch (colorFrame.getFormat()) {
            case RGB:
                mColorDstBuffer.put(mColorSrcBuffer);
                mColorDstBuffer.flip();
                break;
            case YUYV:
                ImageUtils.yuyvToRgb(mColorSrcBuffer, mColorDstBuffer, colorW, colorH);
                break;
            case UYVY:
                ImageUtils.uyvyToRgb(mColorSrcBuffer, mColorDstBuffer, colorW, colorH);
                break;
            case MJPG:
                //ImageUtils.mjpgToRgb(mColorSrcBuffer, mColorDstBuffer, colorW, colorH);
                break;
            default:
                Log.w(TAG, "decodeColorFrame: unsupported format!");
                break;
        }
    }

    private void decodeDepthFrame(DepthFrame depthFrame) {
        if (null == depthFrame) {
            return;
        }
        int depthW = depthFrame.getWidth();
        int depthH = depthFrame.getHeight();
        // Depth to RGB conversion
        if (null == mDepthSrcBuffer || mDepthSrcBuffer.capacity() != depthFrame.getDataSize()) {
            mDepthSrcBuffer = ByteBuffer.allocateDirect(depthFrame.getDataSize());
        }
        mDepthSrcBuffer.clear();
        depthFrame.getData(mDepthSrcBuffer);

        int depthDstSize = depthW * depthH * 3;
        if (null == mDepthDstBuffer || mDepthDstBuffer.capacity() != depthDstSize) {
            mDepthDstBuffer = ByteBuffer.allocateDirect(depthDstSize);
        }
        mDepthDstBuffer.clear();

        ImageUtils.nScalePrecisionToDepthPixel(mDepthSrcBuffer, depthW, depthH, depthFrame.getDataSize(), depthFrame.getValueScale());
        ImageUtils.depthToRgb(mDepthSrcBuffer, mDepthDstBuffer);
    }

    private void depthOverlayColorProcess(DepthFrame depthFrame, ColorFrame colorFrame) {
        if (null != depthFrame && null != colorFrame) {
            int colorW = colorFrame.getWidth();
            int colorH = colorFrame.getHeight();
            int depthW = depthFrame.getWidth();
            int depthH = depthFrame.getHeight();

            // Convert depth data to RGB format
            decodeDepthFrame(depthFrame);

            // Convert color data to RGB format
            decodeColorFrame(colorFrame);

            if (mDepthDstBuffer != null && mColorDstBuffer != null) {
                byte[] depthColorData = ImageUtils.depthAlignToColor(mColorDstBuffer, mDepthDstBuffer,
                        colorW, colorH, depthW, depthH, mAlpha);
                mHwD2CView.update(colorW, colorH, StreamType.COLOR, Format.RGB, depthColorData, 1.0f);
            }
        }
    }

    private Runnable mStreamRunnable = () -> {
        while (mIsStreamRunning) {
            // Obtain the data set in blocking mode. If it cannot be obtained after waiting for 100ms, it will time out.
            try (FrameSet frameSet = mPipeline.waitForFrameSet(100)) {
                if (null == frameSet) {
                    continue;
                }

                DepthFrame depthFrame = frameSet.getDepthFrame();
                ColorFrame colorFrame = frameSet.getColorFrame();

                ColorFrame newColorFrame = colorFrame;
                ColorFrame processedFrame = null;
                if(colorFrame != null && colorFrame.getFormat() == Format.MJPG){
                    Frame res = formatConvertFilter.process(colorFrame);
                    if (res != null) {
                        processedFrame = res.as(FrameType.COLOR);
                        newColorFrame = processedFrame;
                    }
                }

                // Depth and color overlay rendering
                depthOverlayColorProcess(depthFrame, newColorFrame);

                if (null != depthFrame) {
                    depthFrame.close();
                }
                if (null != colorFrame) {
                    colorFrame.close();
                }
            } catch (Exception e) {
                Log.e(TAG, "run: " + e.getMessage());
            }
        }
    };

    // Set D2C
    private void setAlign(boolean isChecked) {
        if (mPipeline == null || mDevice == null) {
            return;
        }
        try {
            mPipeline.stop();
            Thread.sleep(100);
            if (isChecked) {
                mConfig.setAlignMode(AlignMode.ALIGN_D2C_HW_MODE);
            } else {
                mConfig.setAlignMode(AlignMode.ALIGN_DISABLE);
            }
            mPipeline.start(mConfig);
        } catch (Exception e) {
            Log.e(TAG, "setAlignToColor: " + e.getMessage());
        }
    }

    private boolean checkIfSupportHWD2CAlign(StreamProfile colorProfile, StreamProfile depthProfile) {
        try (StreamProfileList hwD2CSupportedDepthStreamProfiles = mPipeline.getD2CDepthProfileList(colorProfile, AlignMode.ALIGN_D2C_HW_MODE)) {
            if (null == hwD2CSupportedDepthStreamProfiles) {
                return false;
            }
            if (hwD2CSupportedDepthStreamProfiles.getCount() == 0) {
                return false;
            }

            VideoStreamProfile depthVsp = depthProfile.as(StreamType.VIDEO);
            int count = hwD2CSupportedDepthStreamProfiles.getCount();
            for (int i = 0; i < count; i++) {
                StreamProfile sp = hwD2CSupportedDepthStreamProfiles.getProfile(i);
                VideoStreamProfile vsp = sp.as(StreamType.VIDEO);
                if (vsp.getWidth() == depthVsp.getWidth() && vsp.getHeight() == depthVsp.getHeight()
                        && vsp.getFormat() == depthVsp.getFormat() && vsp.getFps() == depthVsp.getFps()) {
                    return true;
                }
                vsp.close();
            }
            depthVsp.close();
            return false;
        } catch (Exception e) {
            Log.e(TAG, "checkIfSupportHWD2CAlign: " + e.getMessage());
            return false;
        }
    }

    private Config createHwD2CAlignConfig() {
        try (StreamProfileList coloStreamProfiles = mPipeline.getStreamProfileList(SensorType.COLOR);
             StreamProfileList depthStreamProfiles = mPipeline.getStreamProfileList(SensorType.DEPTH)) {

            int colorSpCount = coloStreamProfiles.getCount();
            int depthSpCount = depthStreamProfiles.getCount();
            for (int i = 0; i < colorSpCount; i++) {
                StreamProfile colorProfile = coloStreamProfiles.getProfile(i);
                VideoStreamProfile colorVsp = colorProfile.as(StreamType.VIDEO);
                for (int j = 0; j < depthSpCount; j++) {
                    StreamProfile depthProfile = depthStreamProfiles.getProfile(j);
                    VideoStreamProfile depthVsp = depthProfile.as(StreamType.VIDEO);

                    if (colorVsp.getFps() != depthVsp.getFps()) {
                        depthVsp.close();
                        continue;
                    }

                    if (checkIfSupportHWD2CAlign(colorProfile, depthProfile)) {
                        Config hwD2CAlignConfig = new Config();
                        hwD2CAlignConfig.enableStream(colorProfile);
                        hwD2CAlignConfig.enableStream(depthProfile);
                        hwD2CAlignConfig.setAlignMode(mIsHwD2CEnabled ? AlignMode.ALIGN_D2C_HW_MODE : AlignMode.ALIGN_DISABLE);
                        hwD2CAlignConfig.setFrameAggregateOutputMode(FrameAggregateOutputMode.OB_FRAME_AGGREGATE_OUTPUT_ALL_TYPE_FRAME_REQUIRE);
                        runOnUiThread(() -> {
                            mHardwareD2CCb.setChecked(mIsHwD2CEnabled);
                            String colorProfileInfo = "Color: " + colorVsp.getWidth() + "x" + colorVsp.getHeight() + "@" + colorVsp.getFormat()
                                    + " " + colorVsp.getFps() + "fps";
                            mColorProfileInfoTv.setText(colorProfileInfo);
                            String depthProfileInfo = "Depth: " + depthVsp.getWidth() + "x" + depthVsp.getHeight() + "@" + depthVsp.getFormat()
                                    + " " + depthVsp.getFps() + "fps";
                            mDepthProfileInfoTv.setText(depthProfileInfo);
                        });

                        return hwD2CAlignConfig;
                    }
                }
                colorVsp.close();
            }
        } catch (Exception e) {
            Log.e(TAG, "createHwD2CAlignConfig: " + e.getMessage());
        }
        return null;
    }
}