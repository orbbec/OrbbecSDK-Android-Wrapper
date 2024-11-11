package com.orbbec.orbbecsdkexamples.activity;

import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.CheckBox;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

import com.libyuv.util.YuvUtil;
import com.orbbec.obsensor.AlignMode;
import com.orbbec.obsensor.ColorFrame;
import com.orbbec.obsensor.Config;
import com.orbbec.obsensor.DepthFrame;
import com.orbbec.obsensor.Device;
import com.orbbec.obsensor.DeviceChangedCallback;
import com.orbbec.obsensor.DeviceInfo;
import com.orbbec.obsensor.DeviceList;
import com.orbbec.obsensor.Format;
import com.orbbec.obsensor.FrameSet;
import com.orbbec.obsensor.Pipeline;
import com.orbbec.obsensor.SensorType;
import com.orbbec.obsensor.StreamType;
import com.orbbec.obsensor.VideoStreamProfile;
import com.orbbec.orbbecsdkexamples.R;
import com.orbbec.orbbecsdkexamples.utils.ImageUtils;
import com.orbbec.orbbecsdkexamples.view.OBGLView;

import java.nio.ByteBuffer;

/**
 * 同步对齐示例
 * <p>
 * 该示例可能会由于深度或者彩色sensor不支持镜像而出现深度图和彩色图镜像状态不一致的情况，
 * 从而导致深度图和彩色图显示的图像是相反的，如遇到该情况，则通过设置镜像接口保持两个镜像状态一致即可
 * 另外可能存在某些设备获取到的分辨率不支持D2C功能，因此D2C功能以实际支持的D2C分辨率为准
 * <p>
 * 例如：DaBai DCW支持的D2C的分辨率为640x360，而实际该示例获取到的分辨率可能为640x480，此时用户根据实际模组情况获取
 * 对应的640x360分辨率即可
 */
public class SyncAlignViewerActivity extends BaseActivity implements SeekBar.OnSeekBarChangeListener, View.OnClickListener {
    private static final String TAG = "SyncAlignViewerActivity";

    private Pipeline mPipeline;
    private Thread mStreamThread;
    private volatile boolean mIsStreamRunning;
    private OBGLView mColorView;
    private CheckBox mSyncCb;
    private CheckBox mHardwareD2CCb;
    private CheckBox mSoftwareD2CCb;

    private TextView tvColorProfileInfo;
    private TextView tvDepthProfileInfo;
    private TextView tvTransparency;
    private SeekBar sbTransparency;
    private Device mDevice;
    private Config mConfig;
    private float mAlpha = 0.5f;

    private ByteBuffer mDepthSrcBuffer;
    private ByteBuffer mDepthDstBuffer;

    private ByteBuffer mColorSrcBuffer;
    private ByteBuffer mColorDstBuffer;

    private DeviceChangedCallback mDeviceChangedCallback = new DeviceChangedCallback() {
        @Override
        public void onDeviceAttach(DeviceList deviceList) {
            try {
                if (null == mPipeline) {
                    // 2.获取Device并通过Device创建Pipeline
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

                    // 3.创建Pipeline配置
                    mConfig = genD2CConfig(mPipeline, AlignMode.ALIGN_D2C_DISABLE);
                    if (null == mConfig) {
                        mPipeline.close();
                        mPipeline = null;
                        mDevice.close();
                        mDevice = null;
                        Log.w(TAG, "onDeviceAttach: No target depth and color stream profile!");
                        showToast(getString(R.string.init_stream_profile_failed));
                        return;
                    }

                    // 4.通过config开流
                    mPipeline.start(mConfig);

                    // 5.创建获取Pipeline数据线程
                    start();
                }
            } catch (Exception e) {
                e.printStackTrace();
            } finally {
                // 6.释放设备列表资源
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
        setTitle("SyncAlignViewer");
        setContentView(R.layout.activity_sync_align_viewer);
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
            // 停止获取Pipeline数据
            stop();

            // 停止Pipeline，并关闭
            if (null != mPipeline) {
                mPipeline.stop();
                mPipeline.close();
            }

            // 释放Config
            if (null != mConfig) {
                mConfig.close();
            }

            // 释放Device
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
        runOnUiThread(() -> Toast.makeText(SyncAlignViewerActivity.this, msg, Toast.LENGTH_SHORT).show());
    }

    private void initView() {
        mColorView = findViewById(R.id.colorview_id);
        sbTransparency = findViewById(R.id.sb_transparency);
        tvTransparency = findViewById(R.id.tv_transparency);
        tvColorProfileInfo = findViewById(R.id.tv_color_profile_info);
        tvDepthProfileInfo = findViewById(R.id.tv_depth_profile_info);
        mSyncCb = findViewById(R.id.cb_sync);
        mHardwareD2CCb = findViewById(R.id.cb_hardware_align_to_color);
        mSoftwareD2CCb = findViewById(R.id.cb_software_align_to_color);

        mSyncCb.setOnClickListener(this);
        mHardwareD2CCb.setOnClickListener(this);
        mSoftwareD2CCb.setOnClickListener(this);
        sbTransparency.setOnSeekBarChangeListener(this);

        tvTransparency.setText(String.format("%.2f", sbTransparency.getProgress() / 100f));
        tvColorProfileInfo.setText("color: null");
        tvDepthProfileInfo.setText("depth: null");
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
                YuvUtil.yuyv2Rgb888(mColorSrcBuffer, mColorDstBuffer, mColorDstBuffer.capacity());
                break;
            case UYVY:
                ImageUtils.uyvyToRgb(mColorSrcBuffer, mColorDstBuffer, colorW, colorH);
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
        //深度转RGB888
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

            // 将深度数据转换为RGB888格式
            decodeDepthFrame(depthFrame);

            // 将彩色数据转换为RGB888格式
            decodeColorFrame(colorFrame);

            if (mDepthDstBuffer != null && mColorDstBuffer != null) {
                byte[] depthColorData = ImageUtils.depthAlignToColor(mColorDstBuffer, mDepthDstBuffer,
                        colorW, colorH, depthW, depthH, mAlpha);
                mColorView.update(colorW, colorH, StreamType.COLOR, Format.RGB, depthColorData, 1.0f);
            }
        }
    }

    private Runnable mStreamRunnable = () -> {
        while (mIsStreamRunning) {
            try {
                // 等待100ms后如果获取不到，则超时
                FrameSet frameSet = mPipeline.waitForFrameSet(100);

                if (null == frameSet) {
                    continue;
                }

                // 获取深度流数据
                DepthFrame depthFrame = frameSet.getDepthFrame();

                // 获取彩色流数据
                ColorFrame colorFrame = frameSet.getColorFrame();

                // 深度和彩色叠加渲染
                depthOverlayColorProcess(depthFrame, colorFrame);

                // 释放深度帧
                if (null != depthFrame) {
                    depthFrame.close();
                }

                // 释放彩色帧
                if (null != colorFrame) {
                    colorFrame.close();
                }

                // 释放数据集
                frameSet.close();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    };

    @Override
    public void onClick(View v) {
        if (v.getId() == R.id.cb_sync) {
            setSync(mSyncCb.isChecked());
            return;
        }
        if (v.getId() == R.id.cb_hardware_align_to_color) {
            mSoftwareD2CCb.setChecked(false);
            setAlignToColor(mHardwareD2CCb.isChecked(), true);
            return;
        }
        if (v.getId() == R.id.cb_software_align_to_color) {
            mHardwareD2CCb.setChecked(false);
            setAlignToColor(mSoftwareD2CCb.isChecked(), false);
            return;
        }
    }

    // 设置帧同步
    private void setSync(boolean isChecked) {
        try {
            if (isChecked) {
                mPipeline.enableFrameSync();
            } else {
                mPipeline.disableFrameSync();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    // 设置D2C
    private void setAlignToColor(boolean isChecked, boolean isHardware) {
        try {
            if (mPipeline == null || mDevice == null) {
                return;
            }
            try {
                mPipeline.stop();
                Thread.sleep(100);
                if (null != mConfig) {
                    mConfig.close();
                    mConfig = null;
                    tvColorProfileInfo.setText("Color: null");
                    tvDepthProfileInfo.setText("Depth: null");
                }
                if (isChecked) {
                    AlignMode alignMode = isHardware ? AlignMode.ALIGN_D2C_HW_ENABLE : AlignMode.ALIGN_D2C_SW_ENABLE;
                    mConfig = genD2CConfig(mPipeline, alignMode);
                } else {
                    mConfig = genD2CConfig(mPipeline, AlignMode.ALIGN_D2C_DISABLE);
                }
                mPipeline.start(mConfig);
            } catch (Exception e) {
                e.printStackTrace();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private Config genD2CConfig(Pipeline pipeline, AlignMode alignMode) {
        D2CStreamProfile d2CStreamProfile = genD2CStreamProfile(pipeline, alignMode);
        if (null == d2CStreamProfile) {
            return null;
        }

        // Update color information to UI
        VideoStreamProfile colorProfile = d2CStreamProfile.getColorProfile();
        final String colorProfileInfo = colorProfile.getWidth() + "x" + colorProfile.getHeight() + "@" + colorProfile.getFormat()
                + " " + colorProfile.getFps() + "fps";
        runOnUiThread(() -> {
            tvColorProfileInfo.setText("Color: " + colorProfileInfo);
        });

        // Update depth information to UI
        VideoStreamProfile depthProfile = d2CStreamProfile.getDepthProfile();
        final String depthProfileInfo = depthProfile.getWidth() + "x" + depthProfile.getHeight() + "@" + depthProfile.getFormat()
                + " " + depthProfile.getFps() + "fps";
        runOnUiThread(() -> {
            tvDepthProfileInfo.setText("Depth: " + depthProfileInfo);
        });

        Config config = new Config();
        config.setAlignMode(alignMode);
        config.enableStream(d2CStreamProfile.getColorProfile());
        config.enableStream(d2CStreamProfile.getDepthProfile());
        d2CStreamProfile.close();
        return config;
    }

    @Override
    public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
        if (seekBar.getId() == R.id.sb_transparency) {
            float progressF = progress / 100f;
            tvTransparency.setText(String.format("%.2f", progressF));
            mAlpha = progressF;
            return;
        }
    }

    @Override
    public void onStartTrackingTouch(SeekBar seekBar) {

    }

    @Override
    public void onStopTrackingTouch(SeekBar seekBar) {

    }
}