package com.orbbec.orbbecsdkexamples.activity;

import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.widget.CheckBox;
import android.widget.RadioButton;
import android.widget.Toast;

import androidx.annotation.Nullable;

import com.libyuv.util.YuvUtil;
import com.orbbec.obsensor.AlignFilter;
import com.orbbec.obsensor.ColorFrame;
import com.orbbec.obsensor.Config;
import com.orbbec.obsensor.DepthFrame;
import com.orbbec.obsensor.Device;
import com.orbbec.obsensor.DeviceChangedCallback;
import com.orbbec.obsensor.DeviceList;
import com.orbbec.obsensor.Filter;
import com.orbbec.obsensor.Frame;
import com.orbbec.obsensor.FrameSet;
import com.orbbec.obsensor.Pipeline;
import com.orbbec.obsensor.Sensor;
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

public class AdvancedSyncAlignActivity extends BaseActivity {
    private static final String TAG = "AlignFilterViewerActivity";

    private Pipeline mPipeline;
    private Thread mStreamThread;
    private volatile boolean mIsStreamRunning;
    private OBGLView mAlignFilterView;
    private CheckBox mAlignSyncCb;
    private RadioButton mD2CAlignCb;
    private RadioButton mC2DAlignCb;
    private Device mDevice;
    private AlignFilter mDepth2ColorAlign;
    private AlignFilter mColor2DepthAlign;
    private volatile boolean isD2C = true;

    private float mAlpha = 0.5f;

    private ByteBuffer mDepthSrcBuffer;
    private ByteBuffer mDepthDstBuffer;

    private ByteBuffer mColorSrcBuffer;
    private ByteBuffer mColorDstBuffer;

    private DeviceChangedCallback mDeviceChangedCallback = new DeviceChangedCallback() {

        @Override
        public void onDeviceAttach(DeviceList deviceList) {
            try {
                if (mPipeline == null) {
                    // 2.Create Device and initialize Pipeline through Device
                    mDevice = deviceList.getDevice(0);

                    // 3.Get depth sensor
                    Sensor depthSensor = mDevice.getSensor(SensorType.DEPTH);
                    if (depthSensor == null) {
                        showToast(getString(R.string.device_not_support_depth));
                        mDevice.close();
                        mDevice = null;
                        return;
                    }

                    // 4.Get color sensor
                    Sensor colorSensor = mDevice.getSensor(SensorType.COLOR);
                    if (colorSensor == null) {
                        showToast(getString(R.string.device_not_support_color));
                        mDevice.close();
                        mDevice = null;
                        return;
                    }

                    // 5.Create Device and initialize Pipeline through Device
                    mPipeline = new Pipeline(mDevice);

                    // 6.Create Pipeline configuration
                    Config config = new Config();
                    // 7.Enable color stream
                    config.enableVideoStream(StreamType.COLOR, 0, 0, 0, Format.RGB);
                    // 8.Enable depth stream
                    config.enableVideoStream(StreamType.DEPTH, 0, 0, 0, Format.Y16);
                    config.setFrameAggregateOutputMode(FrameAggregateOutputMode.OB_FRAME_AGGREGATE_OUTPUT_ALL_TYPE_FRAME_REQUIRE);

                    // 9.Config depth align to color or color align to depth.
                    mDepth2ColorAlign = new AlignFilter(StreamType.COLOR);
                    mColor2DepthAlign = new AlignFilter(StreamType.DEPTH);

                    // 10.Start sensor stream
                    mPipeline.start(config);

                    // 11.Release config
                    config.close();

                    // 12.Create a thread to obtain Pipeline data
                    start();
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
                    for (int i = 0; i < deviceList.getDeviceCount(); i++) {
                        String uid = deviceList.getUid(i);
                        DeviceInfo deviceInfo = mDevice.getInfo();
                        if (deviceInfo != null && TextUtils.equals(uid, deviceInfo.getUid())) {
                            stop();
                            mPipeline.close();
                            mPipeline = null;
                            mDepth2ColorAlign.close();
                            mColor2DepthAlign.close();
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
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setTitle("Advanced-Sync Align");
        setContentView(R.layout.activity_advanced_sync_align);
        mAlignFilterView = findViewById(R.id.alignfilterview_id);
        mAlignSyncCb = findViewById(R.id.cb_align_sync);
        mAlignSyncCb.setOnClickListener(v -> {
            try {
                if (mAlignSyncCb.isChecked()) {
                    mPipeline.enableFrameSync();
                } else {
                    mPipeline.disableFrameSync();
                }
            } catch (Exception e) {
                Log.e(TAG, "onCreate: " + e.getMessage());
            }
        });
        mD2CAlignCb = findViewById(R.id.cb_d2c_align);
        mD2CAlignCb.setOnClickListener(v -> {
            isD2C = true;
        });
        mC2DAlignCb = findViewById(R.id.cb_c2d_align);
        mC2DAlignCb.setOnClickListener(v -> {
            isD2C = false;
        });
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

            if (mDepth2ColorAlign != null) {
                mDepth2ColorAlign.close();
            }

            if (mColor2DepthAlign != null) {
                mColor2DepthAlign.close();
            }

            if (mPipeline != null) {
                mPipeline.stop();
                mPipeline.close();
            }

            if (mDevice != null) {
                mDevice.close();
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

    private void showToast(String msg) {
        runOnUiThread(() -> Toast.makeText(AdvancedSyncAlignActivity.this, msg, Toast.LENGTH_SHORT).show());
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
        if (mStreamThread != null) {
            try {
                mStreamThread.join(300);
            } catch (InterruptedException e) {
                Log.e(TAG, "stop: " + e.getMessage());
            }
            mStreamThread = null;
        }
    }

    private void decodeColorFrame(ColorFrame colorFrame) {
        if (null == colorFrame) return;

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
        if (null == depthFrame) return;

        int depthW = depthFrame.getWidth();
        int depthH = depthFrame.getHeight();

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

            decodeDepthFrame(depthFrame);

            decodeColorFrame(colorFrame);

            if (mDepthDstBuffer != null && mColorDstBuffer != null) {
                byte[] depthColorData = ImageUtils.depthAlignToColor(mColorDstBuffer, mDepthDstBuffer,
                        colorW, colorH, depthW, depthH, mAlpha);
                mAlignFilterView.update(colorW, colorH, StreamType.COLOR, Format.RGB, depthColorData, 1.0f);
            }
        }
    }

    private Runnable mStreamRunnable = () -> {
        while (mIsStreamRunning) {
            try (FrameSet frameSet = mPipeline.waitForFrameSet(100)) {
                if (frameSet == null) {
                    continue;
                }

                Filter alignFilter = isD2C ? mDepth2ColorAlign : mColor2DepthAlign;
                Frame frame = alignFilter.process(frameSet);
                if (frame != null) {
                    FrameSet newFrameSet = frame.as(FrameType.FRAME_SET);
                    DepthFrame depthFrame = newFrameSet.getDepthFrame();
                    ColorFrame colorFrame = newFrameSet.getColorFrame();

                    // 深度和彩色叠加渲染
                    depthOverlayColorProcess(depthFrame, colorFrame);

                    depthFrame.close();
                    colorFrame.close();
                    newFrameSet.close();
                }
            } catch (Exception e) {
                Log.e(TAG, "run: " + e.getMessage());
            }
        }
    };
}
