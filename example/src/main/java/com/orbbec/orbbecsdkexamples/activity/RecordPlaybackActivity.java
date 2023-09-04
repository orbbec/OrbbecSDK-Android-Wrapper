package com.orbbec.orbbecsdkexamples.activity;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.orbbec.obsensor.Config;
import com.orbbec.obsensor.DepthFrame;
import com.orbbec.obsensor.Device;
import com.orbbec.obsensor.DeviceChangedCallback;
import com.orbbec.obsensor.DeviceInfo;
import com.orbbec.obsensor.DeviceList;
import com.orbbec.obsensor.DeviceProperty;
import com.orbbec.obsensor.Format;
import com.orbbec.obsensor.FrameSet;
import com.orbbec.obsensor.MediaState;
import com.orbbec.obsensor.MediaStateCallback;
import com.orbbec.obsensor.OBContext;
import com.orbbec.obsensor.PermissionType;
import com.orbbec.obsensor.Pipeline;
import com.orbbec.obsensor.Playback;
import com.orbbec.obsensor.SensorType;
import com.orbbec.obsensor.StreamProfile;
import com.orbbec.obsensor.StreamProfileList;
import com.orbbec.obsensor.StreamType;
import com.orbbec.obsensor.VideoStreamProfile;
import com.orbbec.orbbecsdkexamples.R;
import com.orbbec.orbbecsdkexamples.utils.FileUtils;
import com.orbbec.orbbecsdkexamples.utils.LocalUtils;
import com.orbbec.orbbecsdkexamples.view.OBGLView;

import java.io.File;

/**
 * Record and Playback example
 */
public class RecordPlaybackActivity extends AppCompatActivity implements View.OnClickListener {
    private static final String TAG = "RecordPlaybackActivity";

    private static final String BAG_FILE_PATH = "/sdcard/Orbbec/recorder.bag";

    private LinearLayout mRecordCtlPanelLL;
    private LinearLayout mPlaybackCtlPanelLL;
    private OBGLView mDepthGLView;
    private TextView mDeviceInfoTv;
    private Button mStartRecordBtn;
    private Button mStopRecordBtn;
    private Button mStartPlaybackBtn;
    private Button mStopPlaybackBtn;

    private Pipeline mPipeline;
    private Pipeline mPlaybackPipe;
    private Playback mPlayback;
    private Config mConfig;
    private Device mDevice;
    private OBContext mOBContext;
    private Thread mStreamThread;
    private Thread mPlaybackThread;
    private volatile boolean mIsStreamRunning;

    private volatile boolean mIsPlaying;
    private volatile boolean mIsRecording;

    private final Object mSync = new Object();

    // Playback status callback
    private MediaStateCallback mMediaStateCallback = new MediaStateCallback() {
        @Override
        public void onState(MediaState state) {
            if (state == MediaState.OB_MEDIA_END) {
                stopPlayback();
            }
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setTitle("RecordPlayback");
        setContentView(R.layout.activity_record_playback);
        initViews();
        initSdk();
    }

    private void initViews() {
        mRecordCtlPanelLL = findViewById(R.id.ll_record_control_panel);
        mPlaybackCtlPanelLL = findViewById(R.id.ll_playback_control_panel);
        mDepthGLView = findViewById(R.id.glv_depth);
        mDeviceInfoTv = findViewById(R.id.tv_device_info);
        mDeviceInfoTv.setText(getString(R.string.device_info, "DepthStreamPreview", "", "", "", ""));
        mStartRecordBtn = findViewById(R.id.btn_start_record);
        mStartRecordBtn.setOnClickListener(this);
        mStopRecordBtn = findViewById(R.id.btn_stop_record);
        mStopRecordBtn.setOnClickListener(this);

        mStartPlaybackBtn = findViewById(R.id.btn_start_playback);
        mStartPlaybackBtn.setOnClickListener(this);
        mStopPlaybackBtn = findViewById(R.id.btn_stop_playback);
        mStopPlaybackBtn.setOnClickListener(this);

        mStartRecordBtn.setEnabled(false);
        mStopRecordBtn.setEnabled(false);
        mStartPlaybackBtn.setEnabled(isPlayFileValid());
        mStopPlaybackBtn.setEnabled(false);
    }

    private void showToast(String msg) {
        runOnUiThread(() -> Toast.makeText(RecordPlaybackActivity.this, msg, Toast.LENGTH_SHORT).show());
    }

    private void initSdk() {
        // 1.Initialize the SDK Context and listen device changes
        mOBContext = new OBContext(getApplicationContext(), new DeviceChangedCallback() {
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

                        mPipeline = new Pipeline(mDevice);

                        // 3.Update device information view
                        updateDeviceInfoView(false);

                        // 4.Create Pipeline configuration
                        mConfig = new Config();

                        // 5.Get the depth flow configuration and configure it to Config
                        StreamProfileList depthProfileList = mPipeline.getStreamProfileList(SensorType.DEPTH);
                        StreamProfile streamProfile = getVideoStreamProfile(depthProfileList, 640, 0, Format.UNKNOWN, 30);
                        if (null == streamProfile) {
                            streamProfile = getVideoStreamProfile(depthProfileList, 0, 0, Format.UNKNOWN, 30);
                        }
                        if (null != depthProfileList) {
                            depthProfileList.close();
                        }

                        // 6.Enable deep sensor configuration
                        if (null != streamProfile) {
                            printStreamProfile(streamProfile.as(StreamType.VIDEO));
                            mConfig.enableStream(streamProfile);
                            streamProfile.close();
                        } else {
                            Log.w(TAG, "onDeviceAttach: No target stream profile!");
                        }

                        // 7.Start pipeline
                        mPipeline.start(mConfig);

                        // 8.Create a thread to obtain Pipeline data
                        start();

                        runOnUiThread(() -> {
                            mStartRecordBtn.setEnabled(true);
                        });
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                } finally {
                    // 9.Release DeviceList
                    deviceList.close();
                }
            }

            @Override
            public void onDeviceDetach(DeviceList deviceList) {
                try {
                    release();

                    runOnUiThread(() -> {
                        mStartRecordBtn.setEnabled(false);
                        mStopRecordBtn.setEnabled(false);
                        mStartPlaybackBtn.setEnabled(isPlayFileValid());
                        mStopPlaybackBtn.setEnabled(false);
                    });
                } catch (Exception e) {
                    e.printStackTrace();
                } finally {
                    deviceList.close();
                }
            }
        });
    }

    private void updateDeviceInfoView(boolean isPlayback) {
        try {
            DeviceInfo deviceInfo = null;
            if (isPlayback) {
                if (null != mPlayback) {
                    // Get the device information of the playback device
                    deviceInfo = mPlayback.getDeviceInfo();
                }
            } else {
                // Get the device information of the record device
                if (null != mPipeline) {
                    deviceInfo = mDevice.getInfo();
                }
            }

            // Refresh device information view
            if (null != deviceInfo) {
                String name = deviceInfo.getName();
                String sn = deviceInfo.getSerialNumber();
                String pid = LocalUtils.formatHex04(deviceInfo.getPid());
                String vid = LocalUtils.formatHex04(deviceInfo.getVid());
                runOnUiThread(() -> {
                    mDeviceInfoTv.setText(getString(R.string.device_info, (isPlayback ? "DepthStreamPlayback" : "DepthStreamPreview"),
                            name, sn, pid, vid));
                });
                deviceInfo.close();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * Start the data stream preview thread
     */
    private void start() {
        mIsStreamRunning = true;
        if (null == mStreamThread) {
            mStreamThread = new Thread(mStreamPrevRunnable);
            mStreamThread.start();
        }
    }

    /**
     * Stop stream preview thread
     */
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

    /**
     * Start playback
     */
    private void startPlayback() {
        try {
            if (!FileUtils.isFileExists(BAG_FILE_PATH)) {
                Toast.makeText(RecordPlaybackActivity.this, "File not found!", Toast.LENGTH_LONG).show();
                return;
            }
            if (!mIsPlaying) {
                mIsPlaying = true;

                // Release Playback resources
                if (null != mPlayback) {
                    mPlayback.close();
                    mPlayback = null;
                }

                // Create a playback pipeline
                if (null != mPlaybackPipe) {
                    mPlaybackPipe.close();
                    mPlaybackPipe = null;
                }
                mPlaybackPipe = new Pipeline(BAG_FILE_PATH);

                // Get the Playback from Pipeline
                mPlayback = mPlaybackPipe.getPlayback();

                // Set playback status callback
                mPlayback.setMediaStateCallback(mMediaStateCallback);

                // start playback
                mPlaybackPipe.start(null);

                // Create a playback thread
                if (null == mPlaybackThread) {
                    mPlaybackThread = new Thread(mPlaybackRunnable);
                    mPlaybackThread.start();
                }

                updateDeviceInfoView(true);
                mStartRecordBtn.setEnabled(false);
                mStopRecordBtn.setEnabled(false);
                mStartPlaybackBtn.setEnabled(false);
                mStopPlaybackBtn.setEnabled(true);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
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

    /**
     * stop playback
     */
    private void stopPlayback() {
        try {
            if (mIsPlaying) {
                mIsPlaying = false;
                // stop playback thread
                if (null != mPlaybackThread) {
                    try {
                        mPlaybackThread.join(300);
                    } catch (InterruptedException e) {
                    }
                    mPlaybackThread = null;
                }

                // stop playback
                if (null != mPlaybackPipe) {
                    mPlaybackPipe.stop();
                }
                runOnUiThread(() -> {
                    updateDeviceInfoView(false);
                });
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            runOnUiThread(() -> {
                mStartRecordBtn.setEnabled(null != mDevice);
                mStopRecordBtn.setEnabled(false);
                mStartPlaybackBtn.setEnabled(true);
                mStopPlaybackBtn.setEnabled(false);
            });
        }
    }

    /**
     * Start recording
     */
    private void startRecord() {
        try {
            if (!mIsRecording) {
                if (null != mPipeline) {
                    // Start recording
                    mPipeline.startRecord(BAG_FILE_PATH);
                    mIsRecording = true;

                    mStartRecordBtn.setEnabled(false);
                    mStopRecordBtn.setEnabled(true);
                    mStartPlaybackBtn.setEnabled(false);
                } else {
                    Log.w(TAG, "mPipeline is null !");
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
            mStartRecordBtn.setEnabled(true);
            mStopRecordBtn.setEnabled(false);
            mStartPlaybackBtn.setEnabled(isPlayFileValid());
        }
    }

    /**
     * stop recording
     */
    private void stopRecord() {
        try {
            if (mIsRecording) {
                mIsRecording = false;
                if (null != mPipeline) {
                    // stop recording
                    mPipeline.stopRecord();
                }

                mStartPlaybackBtn.setEnabled(true);
                mStopPlaybackBtn.setEnabled(false);
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            mStartRecordBtn.setEnabled(null != mDevice);
            mStopRecordBtn.setEnabled(false);
        }
    }

    // playback thread
    private Runnable mPlaybackRunnable = () -> {
        while (mIsPlaying) {
            try {
                // If it cannot be obtained after waiting for 100ms, it will time out
                FrameSet frameSet = mPlaybackPipe.waitForFrameSet(100);
                if (null == frameSet) {
                    continue;
                }

                // Get depth stream data
                DepthFrame depthFrame = frameSet.getDepthFrame();
                if (null != depthFrame) {
                    // Get depth data and render
                    byte[] frameData = new byte[depthFrame.getDataSize()];
                    depthFrame.getData(frameData);
                    synchronized (mSync) {
                        mDepthGLView.update(depthFrame.getWidth(), depthFrame.getHeight(), StreamType.DEPTH, depthFrame.getFormat(), frameData, depthFrame.getValueScale());
                    }

                    // Release the depth data frame
                    depthFrame.close();
                }

                // Release FrameSet
                frameSet.close();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    };

    // Render thread
    private Runnable mStreamPrevRunnable = () -> {
        while (mIsStreamRunning) {
            try {
                // If it cannot be obtained after waiting for 100ms, it will time out
                FrameSet frameSet = mPipeline.waitForFrameSet(100);

                if (null == frameSet) {
                    continue;
                }

                // Get depth stream data
                if (!mIsPlaying) {
                    DepthFrame frame = frameSet.getDepthFrame();

                    if (frame != null) {
                        // Get depth data and render
                        byte[] frameData = new byte[frame.getDataSize()];
                        frame.getData(frameData);
                        synchronized (mSync) {
                            mDepthGLView.update(frame.getWidth(), frame.getHeight(), StreamType.DEPTH, frame.getFormat(), frameData, frame.getValueScale());
                        }

                        // Release the depth data frame
                        frame.close();
                    }
                }

                // Release FrameSet
                frameSet.close();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    };

    @Override
    public void onClick(View v) {
        try {
            int id = v.getId();
            if (id == R.id.btn_start_record) {
                startRecord();
            } else if (id == R.id.btn_stop_record) {
                stopRecord();
            } else if (id == R.id.btn_start_playback) {
                startPlayback();
            } else if (id == R.id.btn_stop_playback) {
                stopPlayback();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void release() {
        try {
            // Stop getting Pipeline data
            stop();

            // stop playback thread
            if (mIsPlaying) {
                mIsPlaying = false;
                if (null != mPlaybackThread) {
                    try {
                        mPlaybackThread.join(300);
                    } catch (InterruptedException e) {
                    }
                    mPlaybackThread = null;
                }
            }

            // release playback
            if (null != mPlayback) {
                mPlayback.close();
                mPlayback = null;
            }

            // Release playback pipeline
            if (null != mPlaybackPipe) {
                mPlaybackPipe.close();
                mPlaybackPipe = null;
            }

            // release config
            if (null != mConfig) {
                mConfig.close();
                mConfig = null;
            }

            // Release preview pipeline
            if (null != mPipeline) {
                mPipeline.close();
                mPipeline = null;
            }

            // Release Device
            if (null != mDevice) {
                mDevice.close();
                mDevice = null;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        try {
            release();

            // Release SDK Context
            if (null != mOBContext) {
                mOBContext.close();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private boolean isPlayFileValid() {
        File file = new File(BAG_FILE_PATH);
        return file.exists() && file.isFile() && file.canRead() && file.length() > 0;
    }
}