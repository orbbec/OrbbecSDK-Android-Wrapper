package com.orbbec.orbbecsdkexamples.activity;

import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.orbbec.obsensor.Device;
import com.orbbec.obsensor.DeviceChangedCallback;
import com.orbbec.obsensor.DeviceInfo;
import com.orbbec.obsensor.DeviceList;
import com.orbbec.obsensor.Format;
import com.orbbec.obsensor.FrameCallback;
import com.orbbec.obsensor.FrameType;
import com.orbbec.obsensor.OBContext;
import com.orbbec.obsensor.Sensor;
import com.orbbec.obsensor.SensorType;
import com.orbbec.obsensor.StreamProfile;
import com.orbbec.obsensor.StreamProfileList;
import com.orbbec.obsensor.StreamType;
import com.orbbec.obsensor.VideoFrame;
import com.orbbec.obsensor.VideoStreamProfile;
import com.orbbec.orbbecsdkexamples.R;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.ShortBuffer;

/**
 * Hot plugin Viewer
 */
public class HotPluginActivity extends BaseActivity {
    private static final String TAG = "HotPluginActivity";

    private Device mDevice;

    private Sensor mDepthSensor;
    private Sensor mColorSensor;
    private Sensor mIrSensor;

    private TextView mNameTv;
    private TextView mProfileInfoTv;

    private int mDepthFps;
    private int mColorFps;
    private int mIrFps;

    private DeviceChangedCallback mDeviceChangedCallback = new DeviceChangedCallback() {
        @Override
        public void onDeviceAttach(DeviceList deviceList) {
            try {
                if (deviceList == null || deviceList.getDeviceCount() <= 0) {
                    setText(mNameTv, getString(R.string.device_not_connected));
                }

                // 2.Create a device and get the device name
                mDevice = deviceList.getDevice(0);
                DeviceInfo devInfo = mDevice.getInfo();
                String deviceName = devInfo.getName();
                setText(mNameTv, deviceName);
                devInfo.close();

                // 3.Get depth sensor
                mDepthSensor = mDevice.getSensor(SensorType.DEPTH);

                // 4.Open the depth stream, and pass null to profile, which means using the parameters
                // configured in the configuration file to open the stream. If there is no such configuration
                // in the device, or the configuration file does not exist, it means using the
                // first configuration in the Profile list.
                if (null != mDepthSensor) {
                    mDepthSensor.start(null, mDepthFrameCallback);
                } else {
                    Log.w(TAG, "onDeviceAttach: depth sensor is unsupported!");
                }

                // 5.Get color sensor
                mColorSensor = mDevice.getSensor(SensorType.COLOR);

                // 6.Open the color stream, and pass null to profile, which means using the
                // parameters configured in the configuration file to open the stream. If there is
                // no such configuration in the device, or the configuration file does not exist,
                // it means using the first configuration in the Profile list.
                if (null != mColorSensor) {
                    mColorSensor.start(null, mColorFrameCallback);
                } else {
                    Log.w(TAG, "onDeviceAttach: color sensor is unsupported!");
                }

                // 7.Get IR sensor
                mIrSensor = mDevice.getSensor(SensorType.IR);

                // 8.Open the infrared stream, if the profile is passed in null, it means that the
                // parameters configured in the configuration file are used to open the stream,
                // if there is no such configuration in the device, or there is no configuration file,
                // it means that the first configuration in the profile list is used
                if (null != mIrSensor) {
                    mIrSensor.start(null, mIrFrameCallback);
                } else {
                    Log.w(TAG, "onDeviceAttach: ir sensor is unsupported!");
                }
            } catch (Exception e) {
                e.printStackTrace();
            } finally {
                // 9. New open sensor configuration information
                setText(mProfileInfoTv, formatProfileInfo());

                // 10.Release deviceList resources
                deviceList.close();
            }
        }

        @Override
        public void onDeviceDetach(DeviceList deviceList) {
            try {
                setText(mNameTv, "No device connected !");
                setText(mProfileInfoTv, "");

                mDepthFps = 0;
                mColorFps = 0;
                mIrFps = 0;

                // Stop depth sensor
                if (null != mDepthSensor) {
                    mDepthSensor.stop();
                }

                // Stop color sensor
                if (null != mColorSensor) {
                    mColorSensor.stop();
                }

                // Stop IR sensor
                if (null != mIrSensor) {
                    mIrSensor.stop();
                }

                // Release Device
                if (null != mDevice) {
                    mDevice.close();
                    mDevice = null;
                }

                // Release deviceList
                deviceList.close();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    };

    private FrameCallback mDepthFrameCallback = frame -> {
        printFrameInfo(frame.as(FrameType.DEPTH), mDepthFps);

        // Release frame resources
        frame.close();
    };

    private FrameCallback mColorFrameCallback = frame -> {
        printFrameInfo(frame.as(FrameType.COLOR), mColorFps);

        // Release frame resources
        frame.close();
    };

    private FrameCallback mIrFrameCallback = frame -> {
        printFrameInfo(frame.as(FrameType.IR), mIrFps);

        // Release frame resources
        frame.close();
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setTitle("HotPlugin");
        setContentView(R.layout.activity_hot_plugin);
        mNameTv = findViewById(R.id.tv_device_name);
        mProfileInfoTv = findViewById(R.id.tv_profile_info);
    }

    @Override
    protected void onStart() {
        super.onStart();
        initSDK();
    }

    @Override
    protected void onStop() {
        try {
            // Stop depth sensor
            if (null != mDepthSensor) {
                mDepthSensor.stop();
            }

            // Stop color sensor
            if (null != mColorSensor) {
                mColorSensor.stop();
            }

            // Stop Ir sensor
            if (null != mIrSensor) {
                mIrSensor.stop();
            }

            // Release Device
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

    private void setText(TextView tv, String text) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                tv.setText(text);
            }
        });
    }

    private String formatProfileInfo() {
        String profileInfo = "";
        if (null != mDepthSensor) {
            try (StreamProfileList depthList = mDepthSensor.getStreamProfileList();
                 StreamProfile depthProfile = depthList.getStreamProfile(0)) {
                VideoStreamProfile depthVsp = depthProfile.as(StreamType.VIDEO);
                int depthW = depthVsp.getWidth();
                int depthH = depthVsp.getHeight();
                int depthFps = depthVsp.getFps();
                Format depthFormat = depthVsp.getFormat();
                mDepthFps = depthFps;
                profileInfo += "Depth:" + depthW + "x" + depthH + "@" + depthFps + "fps " + depthFormat + "\n";
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        if (null != mColorSensor) {
            try (StreamProfileList colorList = mColorSensor.getStreamProfileList();
                 StreamProfile colorProfile = colorList.getStreamProfile(0)) {
                VideoStreamProfile colorVsp = colorProfile.as(StreamType.VIDEO);
                int colorW = colorVsp.getWidth();
                int colorH = colorVsp.getHeight();
                int colorFps = colorVsp.getFps();
                Format colorFormat = colorVsp.getFormat();
                mColorFps = colorFps;
                profileInfo += "Color:" + colorW + "x" + colorH + "@" + colorFps + "fps " + colorFormat + "\n";
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        if (null != mIrSensor) {
            try (StreamProfileList irList = mIrSensor.getStreamProfileList();
                 StreamProfile irProfile = irList.getStreamProfile(0)) {
                VideoStreamProfile irVsp = irProfile.as(StreamType.VIDEO);
                int irW = irVsp.getWidth();
                int irH = irVsp.getHeight();
                int irFps = irVsp.getFps();
                Format irFormat = irVsp.getFormat();
                mIrFps = irFps;
                profileInfo += "IR:" + irW + "x" + irH + "@" + irFps + "fps " + irFormat;
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        return profileInfo;
    }

    private void printFrameInfo(VideoFrame frame, int fps) {
        try {
            String frameInfo = "FrameType:" + frame.getStreamType()
                    + ", index:" + frame.getFrameIndex()
                    + ", width:" + frame.getWidth()
                    + ", height:" + frame.getHeight()
                    + ", format:" + frame.getFormat()
                    + ", fps:" + fps
                    + ", timeStampUs:" + frame.getTimeStampUs();
            if (frame.getStreamType() == FrameType.DEPTH) {
                frameInfo += ", middlePixelValue:" + getMiddlePixelValue(frame);
            }
            Log.i(TAG, frameInfo);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * Get the depth value of the center point of the depth frame
     *
     * @param depthFrame depth frame
     * @return Returns the depth value of the center point
     */
    private short getMiddlePixelValue(VideoFrame depthFrame) {
        int w = depthFrame.getWidth();
        int h = depthFrame.getHeight();
        byte[] depthBytes = new byte[depthFrame.getDataSize()];
        depthFrame.getData(depthBytes);
        ByteBuffer depthBuffer = ByteBuffer.wrap(depthBytes);
        depthBuffer.order(ByteOrder.nativeOrder());
        ShortBuffer depthShortBuffer = depthBuffer.asShortBuffer();
        return depthShortBuffer.get(w * h / 2 + w / 2);
    }
}