package com.orbbec.orbbecsdkexamples.activity;

import androidx.appcompat.app.AppCompatActivity;

import android.annotation.SuppressLint;
import android.os.Bundle;
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
import com.orbbec.obsensor.DeviceList;
import com.orbbec.obsensor.Format;
import com.orbbec.obsensor.FrameSet;
import com.orbbec.obsensor.OBContext;
import com.orbbec.obsensor.Pipeline;
import com.orbbec.obsensor.SensorType;
import com.orbbec.obsensor.StreamProfile;
import com.orbbec.obsensor.StreamProfileList;
import com.orbbec.obsensor.StreamType;
import com.orbbec.obsensor.VideoStreamProfile;
import com.orbbec.orbbecsdkexamples.R;
import com.orbbec.orbbecsdkexamples.utils.ImageUtils;
import com.orbbec.orbbecsdkexamples.view.OBGLView;

import java.nio.ByteBuffer;

/**
 * SyncAlignViewer
 *
 * In this example, the depth map and color image mirroring status may be inconsistent because the
 * depth or color sensor does not support mirroring, resulting in the depth map and color map displaying
 * opposite images. If this situation is encountered, set the mirroring interface. Just keep the two
 * mirroring states consistent. In addition, the resolution obtained by some devices may not support
 * the D2C function. Therefore, the D2C function is subject to the actual supported D2C resolution.
 *
 * For example: The resolution of D2C supported by DaBai DCW is 640x360, but the actual resolution obtained
 * by this example may be 640x480. In this case, the user can obtain the corresponding 640x360 resolution
 * according to the actual module situation.
 */
public class SyncAlignViewerActivity extends AppCompatActivity implements SeekBar.OnSeekBarChangeListener, View.OnClickListener {
    private static final String TAG = "SyncAlignViewerActivity";

    private OBContext mOBContext;
    private Pipeline mPipeline;
    private Thread mStreamThread;
    private volatile boolean mIsStreamRunning;
    private OBGLView mColorView;
    private CheckBox mSyncCb;
    private CheckBox mHardwareD2CCb;
    private CheckBox mSoftwareD2CCb;

    private TextView tvTransparency;
    private SeekBar sbTransparency;
    private Device mDevice;
    private Config mConfig;
    private float mAlpha = 0.5f;

    private ByteBuffer mDepthSrcBuffer;
    private ByteBuffer mDepthDstBuffer;

    private ByteBuffer mColorSrcBuffer;
    private ByteBuffer mColorDstBuffer;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setTitle("SyncAlignViewer");
        setContentView(R.layout.activity_sync_align_viewer);
        initView();

        initOBContext();
    }

    private void initOBContext() {
        // 1.Initialize the SDK Context and listen device changes
        mOBContext = new OBContext(this, new DeviceChangedCallback() {
            @Override
            public void onDeviceAttach(DeviceList deviceList) {
                try {
                    if (null == mPipeline) {
                        // 2.Create Device and initialize Pipeline through Device
                        mDevice = deviceList.getDevice(0);

                        if (null == mDevice.getSensor(SensorType.DEPTH)) {
                            showToast(getString(R.string.device_not_support_depth));
                            return;
                        }

                        if (null == mDevice.getSensor(SensorType.COLOR)) {
                            showToast(getString(R.string.device_not_support_color));
                            return;
                        }

                        // 3.Create Device and initialize Pipeline through Device
                        mPipeline = new Pipeline(mDevice);

                        // 4.Create Pipeline configuration
                        mConfig = new Config();

                        // 5.D2C is disabled by default
                        mConfig.setAlignMode(AlignMode.ALIGN_D2C_DISABLE);

                        // 6.Get the color stream configuration in the specified format
                        StreamProfileList colorProfileList = mPipeline.getStreamProfileList(SensorType.COLOR);
                        StreamProfile colorStreamProfile = getVideoStreamProfile(colorProfileList, 640, 0, Format.RGB888, 30);
                        if (null == colorStreamProfile) {
                            colorStreamProfile = getVideoStreamProfile(colorProfileList, 0, 0, Format.RGB888, 30);
                        }
                        if (null != colorProfileList) {
                            colorProfileList.close();
                        }

                        // 8.Enable color Sensor VideoStreamProfile
                        if (null != colorStreamProfile) {
                            printStreamProfile(colorStreamProfile.as(StreamType.VIDEO));
                            mConfig.enableStream(colorStreamProfile);
                            colorStreamProfile.close();
                        } else {
                            Log.w(TAG, "onDeviceAttach: No target color stream profile!");
                        }

                        // 9.Get Deep Sensor VideoStreamProfile
                        StreamProfileList depthProfileList = mPipeline.getStreamProfileList(SensorType.DEPTH);
                        StreamProfile depthStreamProfile = getVideoStreamProfile(depthProfileList, 640, 0, Format.UNKNOWN, 30);
                        if (null == depthStreamProfile) {
                            depthStreamProfile = getVideoStreamProfile(depthProfileList, 0, 0, Format.UNKNOWN, 30);
                        }
                        if (null != depthProfileList) {
                            depthProfileList.close();
                        }

                        // 10.Enable deep sensor VideoStreamProfile
                        if (null != depthStreamProfile) {
                            printStreamProfile(depthStreamProfile.as(StreamType.VIDEO));
                            mConfig.enableStream(depthStreamProfile);
                            depthStreamProfile.close();
                        } else {
                            Log.w(TAG, "onDeviceAttach: No target depth stream profile!");
                        }

                        // 11.Start pipeline
                        mPipeline.start(mConfig);

                        // 12.Create a thread to obtain Pipeline data
                        start();
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                } finally {
                    // 13.Release DeviceList
                    deviceList.close();
                }
            }

            @Override
            public void onDeviceDetach(DeviceList deviceList) {
                try {
                    // Release DeviceList
                    deviceList.close();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        });
    }

    private void showToast(String msg) {
        runOnUiThread(() -> Toast.makeText(SyncAlignViewerActivity.this, msg, Toast.LENGTH_SHORT).show());
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

    private void initView() {
        mColorView = findViewById(R.id.colorview_id);
        sbTransparency = findViewById(R.id.sb_transparency);
        tvTransparency = findViewById(R.id.tv_transparency);
        mSyncCb = findViewById(R.id.cb_sync);
        mHardwareD2CCb = findViewById(R.id.cb_hardware_align_to_color);
        mSoftwareD2CCb = findViewById(R.id.cb_software_align_to_color);

        mSyncCb.setOnClickListener(this);
        mHardwareD2CCb.setOnClickListener(this);
        mSoftwareD2CCb.setOnClickListener(this);
        sbTransparency.setOnSeekBarChangeListener(this);

        tvTransparency.setText(String.format("%.2f", sbTransparency.getProgress() / 100f));
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
            case RGB888:
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
        // Depth frame data to RGB888
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

            // Convert depth data to RGB888 format
            decodeDepthFrame(depthFrame);

            // Convert color data to RGB888 format
            decodeColorFrame(colorFrame);

            if (mDepthDstBuffer != null && mColorDstBuffer != null) {
                byte[] depthColorData = ImageUtils.depthAlignToColor(mColorDstBuffer, mDepthDstBuffer,
                        colorW, colorH, depthW, depthH, mAlpha);
                mColorView.update(colorW, colorH, StreamType.COLOR, Format.RGB888, depthColorData, 1.0f);
            }
        }
    }

    private Runnable mStreamRunnable = () -> {
        while (mIsStreamRunning) {
            try {
                // If it cannot be obtained after waiting for 100ms, it will time out
                FrameSet frameSet = mPipeline.waitForFrameSet(100);

                if (null == frameSet) {
                    continue;
                }

                // Get depth sensor frame
                DepthFrame depthFrame = frameSet.getDepthFrame();

                // Get color sensor frame
                ColorFrame colorFrame = frameSet.getColorFrame();

                // Depth and color overlay rendering
                depthOverlayColorProcess(depthFrame, colorFrame);

                // Release depth frame
                if (null != depthFrame) {
                    depthFrame.close();
                }

                // Release color frame
                if (null != colorFrame) {
                    colorFrame.close();
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

            // Stop the Pipeline and close it
            if (null != mPipeline) {
                mPipeline.stop();
                mPipeline.close();
            }

            // Release Config
            if (null != mConfig) {
                mConfig.close();
            }

            // Release Device resource
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

    @Override
    public void onClick(View v) {
        int id = v.getId();
        if (id == R.id.cb_sync) {
            setSync(mSyncCb.isChecked());
        } else if (id == R.id.cb_hardware_align_to_color) {
            mSoftwareD2CCb.setChecked(false);
            setAlignToColor(mHardwareD2CCb.isChecked(), true);
        } else if (id == R.id.cb_software_align_to_color) {
            mHardwareD2CCb.setChecked(false);
            setAlignToColor(mSoftwareD2CCb.isChecked(), false);
        }
    }

    // set frame sync
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

    // Config D2C Align
    private void setAlignToColor(boolean isChecked, boolean isHardware) {
        try {
            if (mPipeline == null || mDevice == null) {
                return;
            }
            try {
                if (isChecked) {
                    mConfig.setAlignMode((isHardware ? AlignMode.ALIGN_D2C_HW_ENABLE : AlignMode.ALIGN_D2C_SW_ENABLE));
                } else {
                    mConfig.setAlignMode(AlignMode.ALIGN_D2C_DISABLE);
                }
                mPipeline.switchConfig(mConfig);
            } catch (Exception e) {
                Log.w(TAG, "setAlignToColor: " + e.getMessage());
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
        if (seekBar.getId() == R.id.sb_transparency) {
            float progressF = progress / 100f;
            tvTransparency.setText(String.format("%.2f", progressF));
            mAlpha = progressF;
        }
    }

    @Override
    public void onStartTrackingTouch(SeekBar seekBar) {

    }

    @Override
    public void onStopTrackingTouch(SeekBar seekBar) {

    }
}