package com.orbbec.orbbecsdkexamples.activity;

import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
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
import com.orbbec.obsensor.VideoFrame;
import com.orbbec.obsensor.VideoStreamProfile;
import com.orbbec.obsensor.types.ConvertFormat;
import com.orbbec.obsensor.types.DeviceInfo;
import com.orbbec.obsensor.types.FrameType;
import com.orbbec.obsensor.types.SensorType;
import com.orbbec.obsensor.types.StreamType;
import com.orbbec.orbbecsdkexamples.R;
import com.orbbec.orbbecsdkexamples.bean.FrameCopy;
import com.orbbec.orbbecsdkexamples.utils.FileUtils;
import com.orbbec.orbbecsdkexamples.utils.ImageUtils;

import java.io.File;
import java.nio.ByteBuffer;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.TimeUnit;

public class SaveToDiskActivity extends BaseActivity {
    private static final String TAG = "SaveToDiskActivity";

    private Pipeline mPipeline;

    private Thread mStreamThread;
    private Thread mPicSavingThread;
    private volatile boolean mIsStreamRunning;
    private volatile boolean mIsPicSavingRunning;

    private int colorCount = 0;
    private int depthCount = 0;
    private Device mDevice;
    private FormatConvertFilter mFormatConvertFilter;
    private BlockingQueue<FrameCopy> mFrameSaveQueue = new ArrayBlockingQueue<>(10);

    private String mSaveImagePath;

    private DeviceChangedCallback mDeviceChangedCallback = new DeviceChangedCallback() {
        @Override
        public void onDeviceAttach(DeviceList deviceList) {
            try {
                if (null == mPipeline) {
                    // 2.Create Device and initialize Pipeline through Device
                    mDevice = deviceList.getDevice(0);

                    if (null == mDevice.getSensor(SensorType.DEPTH)) {
                        depthCount = 5;
                        showToast(getString(R.string.device_not_support_depth));
                    }
                    if (null == mDevice.getSensor(SensorType.COLOR)) {
                        colorCount = 5;
                        showToast(getString(R.string.device_not_support_color));
                    }

                    mPipeline = new Pipeline(mDevice);

                    // 3.Initialize the format conversion filter
                    if (null != mFormatConvertFilter) {
                        mFormatConvertFilter = new FormatConvertFilter();
                    }

                    // 4.Create Pipeline configuration
                    Config config = new Config();

                    // 5.Get the color Sensor VideoStreamProfile and configure it to Config
                    try {
                        VideoStreamProfile colorStreamProfile = getStreamProfile(mPipeline, SensorType.COLOR);

                        // 6.Enable color sensor through the obtained color sensor configuration
                        if (null != colorStreamProfile) {
                            printStreamProfile(colorStreamProfile.as(StreamType.VIDEO));
                            config.enableStream(colorStreamProfile);
                            colorStreamProfile.close();
                        } else {
                            Log.w(TAG, "onDeviceAttach: No target color stream profile!");
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                    }

                    // 7.Get the depth sensor configuration and configure it to Config
                    try {
                        VideoStreamProfile depthStreamProfile = getStreamProfile(mPipeline, SensorType.DEPTH);

                        // 8.Enable depth sensor through the obtained depth sensor configuration
                        if (null != depthStreamProfile) {
                            printStreamProfile(depthStreamProfile.as(StreamType.VIDEO));
                            config.enableStream(depthStreamProfile);
                            depthStreamProfile.close();
                        } else {
                            Log.w(TAG, "onDeviceAttach: No target depth stream profile!");
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                    }

                    initSaveImageDir();

                    // 9.Open Pipeline with Config
                    mPipeline.start(config);

                    // 10.Release config resources
                    config.close();

                    // 11.Create a pipeline data acquisition thread and a picture saving thread
                    start();
                }
            } catch (Exception e) {
                e.printStackTrace();
            } finally {
                // 12.Release DeviceList
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
                        //deviceInfo.close();
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
        setTitle("SaveToDisk");
        setContentView(R.layout.activity_save_to_disk);
    }

    @Override
    protected void onStart() {
        super.onStart();
        initSDK();
    }

    @Override
    protected void onStop() {
        try {
            // Stop getting Pipeline data
            stop();

            // Release filter resources
            if (null != mFormatConvertFilter) {
                try {
                    mFormatConvertFilter.close();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }

            // Stop the Pipeline and close it
            if (null != mPipeline) {
                mPipeline.stop();
                mPipeline.close();
                mPipeline = null;
            }

            // Release Device resources
            if (null != mDevice) {
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

    private void initSaveImageDir() {
        String rootSaveDir = FileUtils.getExternalSaveDir();
        if (TextUtils.isEmpty(rootSaveDir)) {
            return;
        }

        File file = new File(rootSaveDir + File.separator + "save_images");
        if (!file.exists()) {
            file.mkdirs();
        }
        if (file.exists()) {
            mSaveImagePath = file.getAbsolutePath();
        }
    }

    private void showToast(String msg) {
        runOnUiThread(() -> Toast.makeText(SaveToDiskActivity.this, msg, Toast.LENGTH_SHORT).show());
    }

    private void start() {
        colorCount = 0;
        depthCount = 0;
        mIsStreamRunning = true;
        mIsPicSavingRunning = true;
        if (null == mStreamThread) {
            mStreamThread = new Thread(mStreamRunnable);
            mStreamThread.start();
        }

        if (null == mPicSavingThread) {
            mPicSavingThread = new Thread(mPicSavingRunnable);
            mPicSavingThread.start();
        }
    }

    private void stop() {
        mIsStreamRunning = false;
        mIsPicSavingRunning = false;
        if (null != mStreamThread) {
            try {
                mStreamThread.join(1000);
            } catch (InterruptedException e) {
            }
            mStreamThread = null;
        }

        if (null != mPicSavingThread) {
            try {
                mPicSavingThread.join(1000);
            } catch (InterruptedException e) {
            }
            mPicSavingThread = null;
        }
    }

    private Runnable mStreamRunnable = () -> {
        int count = 0;
        ByteBuffer colorSrcBuf = null;
        ByteBuffer colorDstBuf = null;
        while (mIsStreamRunning) {
            try {
                // If it cannot be obtained after waiting for 100ms, it will time out
                FrameSet frameSet = mPipeline.waitForFrameSet(100);
                if (null == frameSet) {
                    continue;
                }
                if (count < 5) {
                    frameSet.close();
                    count++;
                    continue;
                }

                // Get color sensor frame data
                ColorFrame colorFrame = frameSet.getColorFrame();
                if (null != colorFrame) {
                    Frame rgbFrame = null;
                    switch (colorFrame.getFormat()) {
                        case MJPG:
                            mFormatConvertFilter.setFormatType(ConvertFormat.FORMAT_MJPEG_TO_RGB);
                            rgbFrame = mFormatConvertFilter.process(colorFrame);
                            break;
                        case RGB:
                            rgbFrame = colorFrame;
                            break;
                        case YUYV:
                            mFormatConvertFilter.setFormatType(ConvertFormat.FORMAT_YUYV_TO_RGB);
                            rgbFrame = mFormatConvertFilter.process(colorFrame);
                            break;
                        case UYVY:
                            FrameCopy frameCopy = copyToFrameT(colorFrame);
                            if (null == colorSrcBuf || colorSrcBuf.capacity() != colorFrame.getDataSize()) {
                                colorSrcBuf = ByteBuffer.allocateDirect(colorFrame.getDataSize());
                            }
                            colorSrcBuf.clear();
                            colorFrame.getData(colorSrcBuf);

                            int colorDstSize = colorFrame.getWidth() * colorFrame.getHeight() * 3;
                            if (null == colorDstBuf || colorDstBuf.capacity() != colorDstSize) {
                                colorDstBuf = ByteBuffer.allocateDirect(colorDstSize);
                            }
                            colorDstBuf.clear();
                            ImageUtils.uyvyToRgb(colorSrcBuf, colorDstBuf, colorFrame.getWidth(), colorFrame.getHeight());
                            frameCopy.data = new byte[colorDstSize];
                            colorDstBuf.get(frameCopy.data);
                            mFrameSaveQueue.offer(frameCopy);
                            break;
                        default:
                            Log.w(TAG, "Unsupported color format!");
                            break;
                    }
                    if (null != rgbFrame) {
                        FrameCopy frameCopy = copyToFrameT(rgbFrame.as(FrameType.VIDEO));
                        mFrameSaveQueue.offer(frameCopy);
                        if (rgbFrame != colorFrame) {
                            rgbFrame.close();
                        }
                    }
                    colorFrame.close();
                }

                // Get depth sensor frame data
                DepthFrame depthFrame = frameSet.getDepthFrame();
                if (null != depthFrame) {
                    FrameCopy frameT = copyToFrameT(depthFrame);
                    mFrameSaveQueue.offer(frameT);
                    depthFrame.close();
                }

                // Release FrameSet
                frameSet.close();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    };

    private Runnable mPicSavingRunnable = () -> {
        while (mIsPicSavingRunning) {
            try {
                FrameCopy frameT = mFrameSaveQueue.poll(300, TimeUnit.MILLISECONDS);
                if (null != frameT && null != mSaveImagePath) {
                    Log.d(TAG, "colorCount :" + colorCount);
                    if (frameT.getStreamType() == FrameType.COLOR && colorCount < 5) {
                        FileUtils.saveImage(frameT, mSaveImagePath);
                        colorCount++;
                    }

                    Log.d(TAG, "depthCount :" + depthCount);
                    if (frameT.getStreamType() == FrameType.DEPTH && depthCount < 5) {
                        FileUtils.saveImage(frameT, mSaveImagePath);
                        depthCount++;
                    }
                    runOnUiThread(() -> {
                        TextView msgView = findViewById(R.id.tv_msg);
                        msgView.setText(getString(R.string.save_to_disk_save_path) + FileUtils.convertSDCardPath(mSaveImagePath));
                    });
                }
            } catch (Exception e) {
            }

            if (colorCount == 5 && depthCount == 5) {
                mIsPicSavingRunning = false;
                break;
            }
        }

        mFrameSaveQueue.clear();
    };

    // Frame数据拷贝
    private FrameCopy copyToFrameT(VideoFrame frame) {
        FrameCopy frameT = new FrameCopy();
        frameT.size = frame.getDataSize();
        frameT.data = new byte[frameT.size];
        frame.getData(frameT.data);
        frameT.format = frame.getFormat();
        frameT.width = frame.getWidth();
        frameT.height = frame.getHeight();
        frameT.frameIndex = frame.getIndex();
        frameT.frameType = frame.getType();
        frameT.timeStamp = frame.getTimeStamp();
        frameT.systemTimeStamp = frame.getSystemTimeStamp();
        return frameT;
    }

}