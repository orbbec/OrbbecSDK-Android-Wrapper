package com.orbbec.orbbecsdkexamples.activity;

import android.os.Bundle;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.Spinner;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.constraintlayout.widget.ConstraintLayout;

import com.orbbec.obsensor.ColorFrame;
import com.orbbec.obsensor.Config;
import com.orbbec.obsensor.DepthFrame;
import com.orbbec.obsensor.Device;
import com.orbbec.obsensor.DeviceChangedCallback;
import com.orbbec.obsensor.DeviceInfo;
import com.orbbec.obsensor.DeviceList;
import com.orbbec.obsensor.DeviceProperty;
import com.orbbec.obsensor.Format;
import com.orbbec.obsensor.FrameSet;
import com.orbbec.obsensor.FrameType;
import com.orbbec.obsensor.IRFrame;
import com.orbbec.obsensor.OBContext;
import com.orbbec.obsensor.OBException;
import com.orbbec.obsensor.PermissionType;
import com.orbbec.obsensor.Pipeline;
import com.orbbec.obsensor.Sensor;
import com.orbbec.obsensor.SensorType;
import com.orbbec.obsensor.StreamProfileList;
import com.orbbec.obsensor.StreamType;
import com.orbbec.obsensor.VideoStreamProfile;
import com.orbbec.obsensor.DepthWorkMode;
import com.orbbec.orbbecsdkexamples.R;
import com.orbbec.orbbecsdkexamples.view.OBGLView;

import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.List;

/**
 * DepthWorkMode example
 * Support product: Gemini 2, Gemini 2 L, Astra 2
 */
public class DepthModeActivity extends AppCompatActivity {
    private static final String TAG = DepthModeActivity.class.getSimpleName();
    private OBGLView mGlView;
    private Spinner mDepthModeSpinner;
    private Spinner mSensorSpinner;
    private Button mStartPlayBtn;
    private Button mStopPlayBtn;

    private OBContext mOBContext;
    private Device   mDevice;
    private Object mDeviceLock = new Object();
    private List<DepthWorkMode> mDepthModeList = new ArrayList<DepthWorkMode>();
    private List<Sensor> mSensorList = new ArrayList<Sensor>();
    private DepthWorkMode mCurDepthMode;
    private SensorType    mCurSensorType;
    private PlayThread    mPlayThread;

    private ArrayAdapter<String> mDepthModeAdapter = null;
    private ArrayAdapter<String> mSensorAdapter = null;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setTitle("DepthWorkMode");
        setContentView(R.layout.activity_depth_mode);
        initView();
    }

    @Override
    protected void onStart() {
        super.onStart();
        refreshUIState();
        initOBContext();
    }

    @Override
    protected void onStop() {
        super.onStop();
        stopPlayThread();
        synchronized (mDeviceLock) {
            mCurDepthMode = null;
            mDepthModeList.clear();
            mSensorList.clear();
            if (null != mDevice) {
                mDevice.close();
                mDevice = null;
            }
        }
        if (null != mOBContext) {
            mOBContext.close();
        }
    }

    private void initView() {
        mGlView = findViewById(R.id.glview_sensor);
        mDepthModeSpinner = findViewById(R.id.spinner_depth_mode);
        mSensorSpinner = findViewById(R.id.spinner_sensor);
        mStartPlayBtn = findViewById(R.id.btn_start_play);
        mStopPlayBtn = findViewById(R.id.btn_stop_play);

        mDepthModeSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                if (mDepthModeList.size() > 0 && position < mDepthModeList.size()) {
                    DepthWorkMode mode = mDepthModeList.get(position);
                    if (null != mDevice && null != mCurDepthMode && !mode.getName().equals(mCurDepthMode.getName())) {
                        switchDepthMode(mode);
                    }
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });

        mSensorSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                if (mSensorList.size() > 0 && position < mSensorList.size()) {
                    SensorType oldType = mCurSensorType;
                    mCurSensorType = mSensorList.get(position).getType();
                    if (oldType != mCurSensorType) {
                        stopPlayThread();
                    }
                    mStartPlayBtn.setEnabled(true);
                    mStopPlayBtn.setEnabled(false);
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });

        mStartPlayBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                stopPlayThread();
                if (SensorType.COLOR == mCurSensorType) {
                    mPlayThread = new ColorSensorThread();
                } else if (SensorType.DEPTH == mCurSensorType) {
                    mPlayThread = new DepthSensorThread();
                } else if (SensorType.IR == mCurSensorType
                    || SensorType.IR_LEFT == mCurSensorType
                    || SensorType.IR_RIGHT == mCurSensorType) {
                    mPlayThread = new IRSensorThread(mCurSensorType);
                }
                if (null != mPlayThread) {
                    mPlayThread.start();
                } else {
                    Log.d(TAG, "Not match sensorType: " + mCurSensorType);
                }
            }
        });
        mStopPlayBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                stopPlayThread();
            }
        });
    }

    private void stopPlayThread() {
        PlayThread playThread = mPlayThread;
        mPlayThread = null;
        if (null != playThread) {
            playThread.terminate();
            try {
                playThread.join();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }

    private void switchDepthMode(DepthWorkMode mode) {
        try {
            stopPlayThread();
            mDevice.switchDepthWorkMode(mode.getName());
            mCurDepthMode = mode;
            mSensorList.clear();
            for (Sensor sensor : mDevice.querySensors()) {
                switch (sensor.getType()) {
                    case COLOR: // follow
                    case IR_LEFT: // follow
                    case IR_RIGHT: // follow
                    case IR: // follow
                    case DEPTH:
                        mSensorList.add(sensor);
                    default:
                        break;
                }
            }
            mCurSensorType = mSensorList.get(0).getType();
            refreshUIState();
        } catch (OBException e) {
            e.printStackTrace();
            showToast(e.getMessage());

            refreshUIState();
        }
    }

    private void refreshUIState() {
        Device device;
        synchronized (mDeviceLock) {
            device = mDevice;
        }

        if (null == mDepthModeSpinner) {
            mDepthModeSpinner = findViewById(R.id.spinner_depth_mode);
        }
        if (null == mSensorSpinner) {
            mSensorSpinner = findViewById(R.id.spinner_sensor);
        }

        if (null != device) {
            int curModeIndex = -1;
            String[] depthModeNames = new String[mDepthModeList.size()];
            for (int i = 0; i < depthModeNames.length; i++) {
                DepthWorkMode mode = mDepthModeList.get(i);
                depthModeNames[i] = mode.getName() + "(" + mode.getChecksumHex().substring(0, 6) + ")";
                if (mode.getName().equals(mCurDepthMode.getName())) {
                    curModeIndex = i;
                }
            }
            mDepthModeAdapter = new ArrayAdapter<String>(this, android.R.layout.simple_spinner_item, depthModeNames);
            mDepthModeAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
            mDepthModeSpinner.setAdapter(mDepthModeAdapter);
            mDepthModeSpinner.setSelection(curModeIndex);

            Log.d(TAG, "sensor list size: " + mSensorList.size());
            String[] sensorNames = new String[mSensorList.size()];
            for (int i = 0; i < sensorNames.length; i++) {
                SensorType sensorType = mSensorList.get(i).getType();
                if (sensorType == SensorType.COLOR) {
                    sensorNames[i] = "SENSOR_COLOR";
                } else if (sensorType == SensorType.IR_LEFT) {
                    sensorNames[i] = "SENSOR_IR_LEFT";
                } else if (sensorType == SensorType.IR_RIGHT) {
                    sensorNames[i] = "SENSOR_IR_RIGHT";
                } else if (sensorType == SensorType.IR) {
                    sensorNames[i] = "SENSOR_IR";
                } else if (sensorType == SensorType.DEPTH) {
                    sensorNames[i] = "SENSOR_DEPTH";
                } else {
                    sensorNames[i] = "UNKNOWN_" + sensorType.value();
                }
            }
            mSensorAdapter = new ArrayAdapter<String>(this, android.R.layout.simple_spinner_item, sensorNames);
            mSensorAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
            mSensorSpinner.setAdapter(mSensorAdapter);
            mStartPlayBtn.setEnabled(true);
            mStopPlayBtn.setEnabled(false);
        } else {
            String[] emptyTexts = {""};
            mDepthModeAdapter = new ArrayAdapter<String>(this, android.R.layout.simple_spinner_item, emptyTexts);
            mDepthModeAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
            mDepthModeSpinner.setAdapter(mDepthModeAdapter);

            mSensorAdapter = new ArrayAdapter<String>(this, android.R.layout.simple_spinner_item, emptyTexts);
            mSensorAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
            mSensorSpinner.setAdapter(mSensorAdapter);

            mStartPlayBtn.setEnabled(false);
            mStopPlayBtn.setEnabled(false);
        }
    }

    private void showToast(String msg) {
        runOnUiThread(() -> Toast.makeText(this, msg, Toast.LENGTH_SHORT).show());
    }

    private void initOBContext() {
        // 1.Initialize the SDK Context and listen device changes
        mOBContext = new OBContext(getApplicationContext(), new DeviceChangedCallback() {
            @Override
            public void onDeviceAttach(DeviceList deviceList) {
                try {
                    // 2.Create Device and initialize Pipeline through Device
                    Device device = deviceList.getDevice(0);
                    onDeviceConnected(device);
                } catch (OBException e) {
                    e.printStackTrace();
                } finally {
                    deviceList.close();
                }
            }

            @Override
            public void onDeviceDetach(DeviceList deviceList) {
                try {
                    String curSn = "";
                    synchronized (mDeviceLock) {
                        if (null != mDevice) {
                            DeviceInfo deviceInfo = mDevice.getInfo();
                            curSn = deviceInfo.getSerialNumber();
                            deviceInfo.close();
                        }
                    }

                    int count = deviceList.getDeviceCount();
                    for (int i = 0; i < count; i++) {
                        String sn = deviceList.getDeviceSerialNumber(i);
                        if (null != sn && sn.length() > 0 && sn.equals(curSn)) {
                            onDeviceDisconnected(mDevice);
                        }
                    }
                } catch (OBException e) {
                    e.printStackTrace();
                } finally {
                    deviceList.close();
                }
            }
        });
    }

    private void onDeviceConnected(Device device) {
        synchronized (mDeviceLock) {
            Device oldDevice = mDevice;
            if (null != oldDevice) {
                oldDevice.close();
            }

            mDevice = device;
        }

        mDepthModeList.clear();
        mSensorList.clear();

        if (!mDevice.isPropertySupported(DeviceProperty.OB_STRUCT_CURRENT_DEPTH_ALG_MODE, PermissionType.OB_PERMISSION_READ_WRITE)) {
            runOnUiThread(() -> {
                Toast t = Toast.makeText(DepthModeActivity.this, getString(R.string.device_not_support_depth_mode), Toast.LENGTH_SHORT);
                t.setGravity(Gravity.CENTER, 0, 0);
                t.show();
                DepthModeActivity.this.finish();
            });
            return;
        }

        mCurDepthMode = mDevice.getCurrentDepthWorkMode();
        mDepthModeList.addAll(mDevice.getDepthWorkModeList());
        for (Sensor sensor : mDevice.querySensors()) {
            switch (sensor.getType()) {
                case COLOR: // follow
                case IR_LEFT: // follow
                case IR_RIGHT: // follow
                case IR: // follow
                case DEPTH:
                    mSensorList.add(sensor);
                default:
                    break;
            }
        }
        mCurSensorType = mSensorList.get(0).getType();
        runOnUiThread(() -> {
            refreshUIState();
        });
    }

    private void onDeviceDisconnected(Device device) {
        synchronized (mDeviceLock) {
            if (null != mDevice) {
                mDevice.close();
                mDevice = null;
            }
        }

        mCurDepthMode = null;
        mDepthModeList.clear();
        mSensorList.clear();
        runOnUiThread(() -> {
            refreshUIState();
        });
    }

    private abstract class PlayThread extends Thread {
        public abstract void terminate();
    }

    private class ColorSensorThread extends PlayThread {
        private Pipeline pipeline;
        private volatile boolean running = true;
        ColorSensorThread() {

        }

        @Override
        public void terminate() {
            running = false;
        }

        @Override
        public void run() {
            Config config = new Config();
            try {
                // Create Device and initialize Pipeline through Device
                this.pipeline = new Pipeline(mDevice);
                // Get a list of VideoStreamProfile supported by the Color sensor
                StreamProfileList profileList = pipeline.getStreamProfileList(SensorType.COLOR);
                // Get a list of target VideoStreamProfile
                VideoStreamProfile profile = getVideoStreamProfile(profileList, 640, 0, Format.RGB888, 30);
                if (null == profile) {
                    profile = getVideoStreamProfile(profileList, 0, 0, Format.RGB888, 30);
                }
                profileList.close();
                if (null == profile) {
                    Log.e(TAG, "ColorSensorThread not profile");
                    return;
                }

                // Setting the waiting time is very important and should be set according to the actual
                // frame rate
                long waitTimeMs = 1000 / profile.getFps() + 15;
                // Put the play configuration list into the pipeline configuration
                config.enableStream(profile);
                // pipeline starts play through configuration
                this.pipeline.start(config);
                final int profileWidth = profile.getWidth();
                final int profileHeight = profile.getHeight();
                profile.close();

                runOnUiThread(() -> {
                    ConstraintLayout.LayoutParams params = (ConstraintLayout.LayoutParams)mGlView.getLayoutParams();
                    params.dimensionRatio = "W, " + profileWidth + ":" + profileHeight;
                    mGlView.setLayoutParams(params);

                    mStartPlayBtn.setEnabled(false);
                    mStopPlayBtn.setEnabled(true);
                });

                // Loop to get data from pipeline and render
                ByteBuffer buffer = null;
                while (running) {
                    try {
                        // Obtain the data set in blocking mode. If it cannot be obtained after waiting for 100ms,
                        // it will time out.
                        FrameSet frameSet = pipeline.waitForFrameSet(waitTimeMs);
                        if (!running) {
                            if (null != frameSet) {
                                frameSet.close();
                            }
                            break;
                        }
                        if (null == frameSet) {
                            continue;
                        }

                        ColorFrame colorFrame = frameSet.getColorFrame();
                        if (null != colorFrame) {
                            if (null != buffer) {
                                buffer.clear();
                            }
                            if (null == buffer || buffer.capacity() != colorFrame.getDataSize()) {
                                buffer = ByteBuffer.allocateDirect(colorFrame.getDataSize());
                            }
                            // Get data and render
                            colorFrame.getData(buffer);
                            mGlView.update(colorFrame.getWidth(), colorFrame.getHeight(), StreamType.COLOR, colorFrame.getFormat(), buffer, 1.0f);

                            // Release color frame
                            colorFrame.close();
                        } else {
                            Log.d(TAG, "Get ColorFrame frame frameSet failed. return null");
                        }
                        // Release FrameSet
                        frameSet.close();
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }

            } catch (OBException e) {
                e.printStackTrace();
            } catch (Exception e) {
                e.printStackTrace();
            } finally {
                config.close();
                if (null != pipeline) {
                    pipeline.stop();
                    pipeline.close();
                    pipeline = null;
                }
                runOnUiThread(() -> {
                    mStartPlayBtn.setEnabled(null != mDevice);
                    mStopPlayBtn.setEnabled(false);
                });
            }
        }
    };

    private class DepthSensorThread extends PlayThread {
        private Pipeline pipeline;
        private volatile boolean running = true;
        DepthSensorThread() {

        }

        @Override
        public void terminate() {
            running = false;
        }

        @Override
        public void run() {
            Config config = new Config();
            try {
                // Build a pipeline through the device object
                this.pipeline = new Pipeline(mDevice);
                // Get a list of VideoStreamProfile supported by the Depth sensor.
                StreamProfileList profileList = pipeline.getStreamProfileList(SensorType.DEPTH);
                // Get a list of target VideoStreamProfile
                VideoStreamProfile profile = getVideoStreamProfile(profileList, 640, 0, Format.UNKNOWN, 30);
                if (null == profile) {
                    profile = getVideoStreamProfile(profileList, 0, 0, Format.UNKNOWN, 30);
                }
                profileList.close();
                if (null == profile) {
                    Log.e(TAG, "DepthSensorThread not profile");
                    return;
                }

                // Setting the waiting time is very important and should be set according to the
                // actual frame rate
                long waitTimeMs = 1000 / profile.getFps() + 15;
                // Put the play configuration list into the pipeline configuration
                config.enableStream(profile);
                // pipeline starts play through configuration
                this.pipeline.start(config);
                final int profileWidth = profile.getWidth();
                final int profileHeight = profile.getHeight();
                profile.close();

                runOnUiThread(() -> {
                    ConstraintLayout.LayoutParams params = (ConstraintLayout.LayoutParams)mGlView.getLayoutParams();
                    params.dimensionRatio = "W, " + profileWidth + ":" + profileHeight;
                    mGlView.setLayoutParams(params);

                    mStartPlayBtn.setEnabled(false);
                    mStopPlayBtn.setEnabled(true);
                });

                // Loop to get data from pipeline and render
                ByteBuffer buffer = null;
                while (running) {
                    try {
                        // Obtain the data set in blocking mode. If it cannot be obtained after waiting for 100ms,
                        // it will time out.
                        FrameSet frameSet = pipeline.waitForFrameSet(waitTimeMs);
                        if (!running) {
                            if (null != frameSet) {
                                frameSet.close();
                            }
                            break;
                        }
                        if (null == frameSet) {
                            continue;
                        }

                        DepthFrame depthFrame = frameSet.getDepthFrame();
                        if (null != depthFrame) {
                            if (null != buffer) {
                                buffer.clear();
                            }
                            if (null == buffer || buffer.capacity() != depthFrame.getDataSize()) {
                                buffer = ByteBuffer.allocateDirect(depthFrame.getDataSize());
                            }
                            // Get data and render
                            depthFrame.getData(buffer);
                            mGlView.update(depthFrame.getWidth(), depthFrame.getHeight(), StreamType.DEPTH, depthFrame.getFormat(), buffer, depthFrame.getValueScale());

                            // Release depth frame
                            depthFrame.close();
                        } else {
                            Log.d(TAG, "Get ColorFrame frame frameSet failed. return null");
                        }
                        // Release FrameSet
                        frameSet.close();
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }

            } catch (OBException e) {
                e.printStackTrace();
            } catch (Exception e) {
                e.printStackTrace();
            } finally {
                config.close();
                if (null != pipeline) {
                    pipeline.stop();
                    pipeline.close();
                    pipeline = null;
                }
                runOnUiThread(() -> {
                    mStartPlayBtn.setEnabled(null != mDevice);
                    mStopPlayBtn.setEnabled(false);
                });
            }
        }
    }

    private class IRSensorThread extends PlayThread {
        private Pipeline pipeline;
        private volatile boolean running = true;
        private SensorType sensorType;
        private FrameType frameType;
        IRSensorThread(SensorType type) {
            this.sensorType = type;
            if (type == SensorType.IR) {
                frameType = FrameType.IR;
            } else if (type == SensorType.IR_LEFT) {
                frameType = FrameType.IR_LEFT;
            } else if (type == SensorType.IR_RIGHT) {
                frameType = FrameType.IR_RIGHT;
            }
        }

        @Override
        public void terminate() {
            running = false;
        }

        @Override
        public void run() {
            Config config = new Config();
            try {
                // Build a pipeline through the device object
                this.pipeline = new Pipeline(mDevice);
                // Get a list of VideoStreamProfile supported by the current sensor type
                StreamProfileList profileList = pipeline.getStreamProfileList(sensorType);
                // Get a list of target VideoStreamProfile
                VideoStreamProfile profile = getVideoStreamProfile(profileList, 640, 0, Format.UNKNOWN, 30);
                if (null == profile) {
                    profile = getVideoStreamProfile(profileList, 0, 0, Format.UNKNOWN, 30);
                }
                profileList.close();
                if (null == profile) {
                    Log.e(TAG, "IRSensorThread not profile");
                    return;
                }

                Log.d(TAG, "format: " + profile.getFormat().value());
                // Setting the waiting time is very important and should be set according to the
                // actual frame rate
                long waitTimeMs = 1000 / profile.getFps() + 15;
                // Put the play configuration list into the pipeline configuration
                config.enableStream(profile);
                // pipeline starts play through configuration
                this.pipeline.start(config);
                final int profileWidth = profile.getWidth();
                final int profileHeight = profile.getHeight();
                profile.close();

                runOnUiThread(() -> {
                    ConstraintLayout.LayoutParams params = (ConstraintLayout.LayoutParams)mGlView.getLayoutParams();
                    params.dimensionRatio = "W, " + profileWidth + ":" + profileHeight;
                    mGlView.setLayoutParams(params);

                    mStartPlayBtn.setEnabled(false);
                    mStopPlayBtn.setEnabled(true);
                });

                // Loop to get data from pipeline and render
                ByteBuffer buffer = null;
                while (running) {
                    try {
                        // Obtain the data set in blocking mode. If it cannot be obtained after waiting for 100ms,
                        // it will time out.
                        FrameSet frameSet = pipeline.waitForFrameSet(waitTimeMs);
                        if (!running) {
                            if (null != frameSet) {
                                frameSet.close();
                            }
                            break;
                        }
                        if (null == frameSet) {
                            continue;
                        }

                        IRFrame irFrame = frameSet.getFrame(frameType);
                        if (null != irFrame) {
                            if (null != buffer) {
                                buffer.clear();
                            }
                            if (null == buffer || buffer.capacity() != irFrame.getDataSize()) {
                                buffer = ByteBuffer.allocateDirect(irFrame.getDataSize());
                            }
                            // Get data and render
                            irFrame.getData(buffer);
                            mGlView.update(irFrame.getWidth(), irFrame.getHeight(), StreamType.IR, irFrame.getFormat(), buffer, 1.0f);

                            // Release frame
                            irFrame.close();
                        } else {
                            Log.d(TAG, "Get ColorFrame frame frameSet failed. return null");
                        }
                        // Release FrameSet
                        frameSet.close();
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }

            } catch (OBException e) {
                e.printStackTrace();
            } catch (Exception e) {
                e.printStackTrace();
            } finally {
                config.close();
                if (null != pipeline) {
                    pipeline.stop();
                    pipeline.close();
                    pipeline = null;
                }
                runOnUiThread(() -> {
                    mStartPlayBtn.setEnabled(null != mDevice);
                    mStopPlayBtn.setEnabled(false);
                });
            }
        }
    };

    private VideoStreamProfile getVideoStreamProfile(StreamProfileList profileList,
                                                     int width, int height, Format format, int fps) {
        VideoStreamProfile vsp = null;
        try {
            vsp = profileList.getVideoStreamProfile(width, height, format, fps);
        } catch (Exception e) {
            Log.w(TAG, "getVideoStreamProfile: " + e.getMessage() + ", width: " + width
                    + ", height: " + height + ", format: " + format + ", fps: " + fps);
        }
        return vsp;
    }
}
