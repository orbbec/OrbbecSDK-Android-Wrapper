package com.orbbec.orbbecsdkexamples.activity;

import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.widget.CheckBox;
import android.widget.Toast;

import androidx.annotation.Nullable;

import com.libyuv.util.YuvUtil;
import com.orbbec.obsensor.AlignFilter;
import com.orbbec.obsensor.ColorFrame;
import com.orbbec.obsensor.Config;
import com.orbbec.obsensor.DepthFrame;
import com.orbbec.obsensor.Device;
import com.orbbec.obsensor.DeviceChangedCallback;
import com.orbbec.obsensor.DeviceInfo;
import com.orbbec.obsensor.DeviceList;
import com.orbbec.obsensor.Format;
import com.orbbec.obsensor.Frame;
import com.orbbec.obsensor.FrameSet;
import com.orbbec.obsensor.FrameType;
import com.orbbec.obsensor.Pipeline;
import com.orbbec.obsensor.Sensor;
import com.orbbec.obsensor.SensorType;
import com.orbbec.obsensor.StreamType;
import com.orbbec.obsensor.VideoStreamProfile;
import com.orbbec.orbbecsdkexamples.R;
import com.orbbec.orbbecsdkexamples.utils.ImageUtils;
import com.orbbec.orbbecsdkexamples.view.OBGLView;

import java.nio.ByteBuffer;

public class AlignFilterViewerActivity extends BaseActivity {
    private static final String TAG = "AlignFilterViewerActivity";

    private Pipeline mPipeline;
    private Thread mStreamThread;
    private volatile boolean mIsStreamRunning;
    private OBGLView mAlignFilterView;
    private CheckBox mAlignSyncCb;
    private Device mDevice;
    private AlignFilter mAlignFilter;

    private float mAlpha = 0.5f;

    private ByteBuffer mDepthSrcBuffer;
    private ByteBuffer mDepthDstBuffer;

    private ByteBuffer mColorSrcBuffer;
    private ByteBuffer mColorDstBuffer;

    private final int[] gemini330List = {0x0801, 0x0805, 0x0800, 0x0804, 0x0803, 0x0807};

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

                    // 5.check if the device supports align filter
                    int pid = mDevice.getInfo().getPid();
                    if (!isSupported(pid)) {
                        Log.d(TAG, "onDeviceAttach: " + pid);
                        showToast(getString(R.string.device_not_support_align_filter));
                        mDevice.close();
                        mDevice = null;
                        return;
                    }

                    // 6.Create Device and initialize Pipeline through Device
                    mPipeline = new Pipeline(mDevice);

                    // 7.Create Pipeline configuration
                    Config config = new Config();

                    // 8.Get the color sensor configuration and configure it to Config
                    VideoStreamProfile colorProfile = getStreamProfile(mPipeline, SensorType.COLOR);
                    if (colorProfile != null) {
                        printStreamProfile(colorProfile.as(StreamType.VIDEO));
                        config.enableStream(colorProfile);
                        colorProfile.close();
                    } else {
                        mPipeline.close();
                        mPipeline = null;
                        mDevice.close();
                        mDevice = null;

                        config.close();
                        Log.w(TAG, "No target stream profile!");
                        showToast(getString(R.string.init_stream_profile_failed));
                        return;
                    }
                    // 9.Get the depth sensor configuration and configure it to Config
                    VideoStreamProfile depthProfile = getStreamProfile(mPipeline, SensorType.DEPTH);
                    if (depthProfile != null) {
                        printStreamProfile(depthProfile.as(StreamType.VIDEO));
                        config.enableStream(depthProfile);
                        depthProfile.close();
                    } else {
                        mPipeline.close();
                        mPipeline = null;
                        mDevice.close();
                        mDevice = null;

                        config.close();
                        Log.w(TAG, "No target stream profile!");
                        showToast(getString(R.string.init_stream_profile_failed));
                        return;
                    }

                    // 10.Config depth align to color or color align to depth.
                    mAlignFilter = new AlignFilter(StreamType.COLOR);

                    // 11.Start sensor stream
                    mPipeline.start(config);

                    // 12.Release config
                    config.close();

                    // 13.Create a thread to obtain Pipeline data
                    start();
                }
            } catch (Exception e) {
                e.printStackTrace();
            } finally {
                // 14.Release device list resources
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
                            mAlignFilter.close();
                            mAlignFilter = null;
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

    private boolean isSupported(int pid) {
        for (int supportedPid : gemini330List) {
            if (supportedPid == pid) {
                return true;
            }
        }
        return false;
    }

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setTitle("AlignFilterViewer");
        setContentView(R.layout.activity_align_filter_viewer);
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
                e.printStackTrace();
            }
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

            if (mPipeline != null) {
                mPipeline.stop();
                mPipeline.close();
            }

            if (mAlignFilter != null) {
                mAlignFilter.close();
            }

            if (mDevice != null) {
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
        runOnUiThread(() -> Toast.makeText(AlignFilterViewerActivity.this, msg, Toast.LENGTH_SHORT).show());
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
                e.printStackTrace();
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
            try {
                FrameSet frameSet = mPipeline.waitForFrameSet(100);

                if (frameSet == null) {
                    continue;
                }

                DepthFrame depthFrame = frameSet.getDepthFrame();
                ColorFrame colorFrame = frameSet.getColorFrame();

                if (depthFrame != null && colorFrame != null) {

                    Frame frame = mAlignFilter.process(frameSet);
                    FrameSet newFrameSet = frame.as(FrameType.FRAME_SET);

                    depthFrame.close();
                    colorFrame.close();

                    depthFrame = newFrameSet.getDepthFrame();
                    colorFrame = newFrameSet.getColorFrame();

                    // 深度和彩色叠加渲染
                    depthOverlayColorProcess(depthFrame, colorFrame);

                    newFrameSet.close();
                }

                // 无论是否经过mAlignFilter处理，都统一释放资源
                if (depthFrame != null) {
                    depthFrame.close();
                }
                if (colorFrame != null) {
                    colorFrame.close();
                }
                frameSet.close();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    };
}
