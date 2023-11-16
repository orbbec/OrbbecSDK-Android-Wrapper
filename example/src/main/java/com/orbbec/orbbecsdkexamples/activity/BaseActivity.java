package com.orbbec.orbbecsdkexamples.activity;

import android.Manifest;
import android.content.pm.PackageManager;
import android.os.Environment;
import android.text.TextUtils;
import android.util.Log;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import com.orbbec.obsensor.AlignMode;
import com.orbbec.obsensor.DeviceChangedCallback;
import com.orbbec.obsensor.Format;
import com.orbbec.obsensor.LogSeverity;
import com.orbbec.obsensor.OBContext;
import com.orbbec.obsensor.OBException;
import com.orbbec.obsensor.Pipeline;
import com.orbbec.obsensor.SensorType;
import com.orbbec.obsensor.StreamProfileList;
import com.orbbec.obsensor.StreamType;
import com.orbbec.obsensor.VideoStreamProfile;
import com.orbbec.orbbecsdkexamples.BuildConfig;
import com.orbbec.orbbecsdkexamples.utils.FileUtils;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

public abstract class BaseActivity extends AppCompatActivity {
    private static final String TAG = "BaseActivity";
    private final String XML_CONFIG_FILE_NAME = "OrbbecSDKConfig_v1.0.xml";

    /**
     * \if English
     * OBContext is entry of OrbbecSDK, and support only one instance.
     * \else
     * OBContext在一个应用中只能有一个对象实例，不允许同时存在多个OBContext实例。
     * \endif
     */
    protected OBContext mOBContext;

    protected abstract DeviceChangedCallback getDeviceChangedCallback();

    protected void initSDK() {
        try {
            if (BuildConfig.DEBUG) {
                // set debug level in code
                OBContext.setLoggerSeverity(LogSeverity.DEBUG);
            }

            DeviceChangedCallback deviceChangedCallback = getDeviceChangedCallback();

            // 1.Initialize the SDK Context and listen device changes
            String configFilePath = getXmlConfigFile();
            if (!TextUtils.isEmpty(configFilePath)) {
                mOBContext = new OBContext(getApplicationContext(), configFilePath, deviceChangedCallback);
            } else {
                mOBContext = new OBContext(getApplicationContext(), deviceChangedCallback);
            }
        } catch (OBException e) {
            e.printStackTrace();
        }
    }

    protected void releaseSDK() {
        try {
            // Release SDK Context
            if (null != mOBContext) {
                mOBContext.close();
            }
        } catch (OBException e) {
            e.printStackTrace();
        }
    }

    protected final void printStreamProfile(VideoStreamProfile vsp) {
        Log.i(TAG, "printStreamProfile: "
                + vsp.getWidth() + "×" + vsp.getHeight()
                + "@" + vsp.getFps() + "fps " + vsp.getFormat());
    }

    protected final String getXmlConfigFile() {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
            return null;
        }

        File file = new File(FileUtils.getExternalSaveDir() + File.separator + XML_CONFIG_FILE_NAME);
        if (file.exists()) {
            return file.getAbsolutePath();
        }
        file = new File(Environment.getExternalStorageDirectory(), "Orbbec" + File.separator + XML_CONFIG_FILE_NAME);
        if (file.exists()) {
            return file.getAbsolutePath();
        }
        return null;
    }

    /**
     * Get best VideoStreamProfile of pipeline support by OrbbecSdkExamples.
     * Note: OrbbecSdkExamples just sample code to render and save frame, it support limit VideoStreamProfile Format.
     * @param pipeline Pipeline
     * @param sensorType Target Sensor Type
     * @return If success return a VideoStreamProfile, otherwise return null.
     */
    protected final VideoStreamProfile getStreamProfile(Pipeline pipeline, SensorType sensorType) {
        // Select prefer Format
        Format format;
        if (sensorType == SensorType.COLOR) {
            format = Format.RGB888;
        } else if (sensorType == SensorType.IR
             || sensorType == SensorType.IR_LEFT
             || sensorType == SensorType.IR_RIGHT) {
            format = Format.Y8;
        } else if (sensorType == SensorType.DEPTH) {
            format = Format.Y16;
        } else {
            Log.w(TAG, "getStreamProfile not support sensorType: " + sensorType);
            return null;
        }

        try {
            // Get StreamProfileList from sensor
            StreamProfileList profileList = pipeline.getStreamProfileList(sensorType);
            List<VideoStreamProfile> profiles = new ArrayList<>();
            for (int i = 0, N = profileList.getStreamProfileCount(); i < N; i++) {
                // Get StreamProfile by index and convert it to VideoStreamProfile
                VideoStreamProfile profile = profileList.getStreamProfile(i).as(StreamType.VIDEO);
                // Match target with and format.
                // Note: width >= 640 && width <= 1280 is consider best render for OrbbecSdkExamples
                if ((profile.getWidth() >= 640 && profile.getWidth() <= 1280) && profile.getFormat() == format) {
                    profiles.add(profile);
                } else {
                    profile.close();
                }
            }
            // If not match StreamProfile with prefer format, Try other.
            // Note: OrbbecSdkExamples not support render Format of MJPEG and RVL
            if (profiles.isEmpty() && profileList.getStreamProfileCount() > 0) {
                for (int i = 0, N = profileList.getStreamProfileCount(); i < N; i++) {
                    VideoStreamProfile profile = profileList.getStreamProfile(i).as(StreamType.VIDEO);
                    if ((profile.getWidth() >= 640 && profile.getWidth() <= 1280)
                            && (profile.getFormat() != Format.MJPG && profile.getFormat() != Format.RVL)) {
                        profiles.add(profile);
                    } else {
                        profile.close();
                    }
                }
            }
            // Release StreamProfileList
            profileList.close();

            // Sort VideoStreamProfile list and let recommend profile at first
            // Priority:
            // 1. high fps at first.
            // 2. large width at first
            // 3. large height at first
            Collections.sort(profiles, new Comparator<VideoStreamProfile>() {
                @Override
                public int compare(VideoStreamProfile o1, VideoStreamProfile o2) {
                    if (o1.getFps() != o2.getFps()) {
                        return o2.getFps() - o1.getFps();
                    }
                    if (o1.getWidth() != o2.getWidth()) {
                        return o2.getWidth() - o1.getWidth();
                    }
                    return o2.getHeight() - o1.getHeight();
                }
            });
            for (VideoStreamProfile p : profiles) {
                Log.d(TAG, "getStreamProfile " + p.getWidth() + "x" + p.getHeight() + "--" + p.getFps());
            }

            if (profiles.isEmpty()) {
                return null;
            }

            // Get first stream profile which is the best for OrbbecSdkExamples.
            VideoStreamProfile retProfile = profiles.get(0);
            // Release other stream profile
            for (int i = 1; i < profiles.size(); i++) {
                profiles.get(i).close();
            }
            return retProfile;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    /**
     * Get D2CStreamProfile which contain color and depth
     * @param pipeline
     * @param alignMode
     * @return Success: D2CStreamProfile which contain color and depth. Failure: null.
     */
    protected D2CStreamProfile genD2CStreamProfile(Pipeline pipeline, AlignMode alignMode) {
        // Config color profile
        VideoStreamProfile colorProfile = null;
        List<VideoStreamProfile> colorProfiles = getAvailableColorProfiles(pipeline, alignMode);
        if (colorProfiles.isEmpty()) {
            Log.w(TAG, "genConfig failed. colorProfiles is empty");
            return null;
        }
        for (VideoStreamProfile profile : colorProfiles) {
            if (profile.getWidth() >= 640 && profile.getWidth() <= 1280 && profile.getFormat() == Format.RGB888) {
                colorProfile = profile;
                break;
            }
        }
        if (null == colorProfile) {
            if (colorProfiles.size() > 0) {
                colorProfile = colorProfiles.get(0);
            } else {
                Log.w(TAG, "genConfig failed. not match color profile width >= 640 and width <= 1280");
                return null;
            }
        }
        // Release colorProfiles resource
        for (VideoStreamProfile profile : colorProfiles) {
            if (profile != colorProfile) {
                profile.close();
            }
        }
        colorProfiles.clear();

        // Config depth profile
        VideoStreamProfile depthProfile = null;
        List<VideoStreamProfile> depthProfiles = getAvailableDepthProfiles(pipeline, colorProfile, alignMode);
        for (VideoStreamProfile profile : depthProfiles) {
            if (profile.getWidth() >= 640 && profile.getWidth() <= 1280 && profile.getFormat() == Format.Y16) {
                depthProfile = profile;
                break;
            }
        }
        if (null == depthProfile) {
            if (depthProfiles.size() > 0) {
                depthProfile = depthProfiles.get(0);
            } else {
                Log.w(TAG, "genConfig failed. not match depth profile width >= 640 and width <= 1280");
                colorProfile.close();
                colorProfile = null;
                return null;
            }
        }
        // Release depthProfiles resource
        for (VideoStreamProfile profile : depthProfiles) {
            if (depthProfile != profile) {
                profile.close();
            }
        }
        depthProfiles.clear();

        D2CStreamProfile d2CStreamProfile = new D2CStreamProfile();
        d2CStreamProfile.colorProfile = colorProfile;
        d2CStreamProfile.depthProfile = depthProfile;
        return d2CStreamProfile;
    }

    /**
     * Get available color profiles with AlignMode. If alignMode is ALIGN_D2C_HW_ENABLE or ALIGN_D2C_SW_ENABLE
     *     Not all color stream profile has match depth stream profile list, This function will filter the color stream profile
     *     when it match any depth stream profile under target alignMode.
     * @param pipeline
     * @param alignMode
     * @return Color stream profile list that has supported depth stream profiles.
     */
    private List<VideoStreamProfile> getAvailableColorProfiles(Pipeline pipeline, AlignMode alignMode) {
        List<VideoStreamProfile> colorProfiles = new ArrayList<>();
        StreamProfileList depthProfileList = null;
        try (StreamProfileList colorProfileList = pipeline.getStreamProfileList(SensorType.COLOR)) {
            final int profileCount = colorProfileList.getStreamProfileCount();
            for (int i = 0; i < profileCount; i++) {
                colorProfiles.add(colorProfileList.getStreamProfile(i).as(StreamType.VIDEO));
            }
            sortVideoStreamProfiles(colorProfiles);

            // All depth profile are available when D2C is disalbe
            if (alignMode == AlignMode.ALIGN_D2C_DISABLE) {
                return colorProfiles;
            }

            // Filter color profile which unsupported depth profile
            for (int i = colorProfiles.size() - 1; i >= 0; i--) {
                VideoStreamProfile colorProfile = colorProfiles.get(i);
                depthProfileList = pipeline.getD2CDepthProfileList(colorProfile, alignMode);
                if (null == depthProfileList || depthProfileList.getStreamProfileCount() == 0) {
                    colorProfiles.remove(i);
                    colorProfile.close();
                }
                // Release depthProfileList
                depthProfileList.close();
                depthProfileList = null;
            }
            return colorProfiles;
        } catch (OBException e) {
            e.printStackTrace();
        } finally {
            // Release depthProfileList when encounter OBException
            if (null != depthProfileList) {
                depthProfileList.close();
                depthProfileList = null;
            }
        }
        return colorProfiles;
    }

    /**
     * Get target depth stream profile list with target color stream profile and alignMode
     * @param pipeline Pipeline
     * @param colorProfile Target color stream profile
     * @param alignMode Target alignMode
     * @return Depth stream profile list associate with target color stream profile.
     *     Success: depth stream profile list has elements. Failure: depth stream profile list is empty.
     */
    private List<VideoStreamProfile> getAvailableDepthProfiles(Pipeline pipeline, VideoStreamProfile colorProfile, AlignMode alignMode) {
        List<VideoStreamProfile> depthProfiles = new ArrayList<>();
        try (StreamProfileList depthProfileList = pipeline.getD2CDepthProfileList(colorProfile, alignMode)) {
            final int profileCount = depthProfileList.getStreamProfileCount();
            for (int i = 0; i < profileCount; i++) {
                depthProfiles.add(depthProfileList.getStreamProfile(i).as(StreamType.VIDEO));
            }
            sortVideoStreamProfiles(depthProfiles);
        } catch (OBException e) {
            e.printStackTrace();
        }
        return depthProfiles;
    }

    private void sortVideoStreamProfiles(List<VideoStreamProfile> profiles) {
        Collections.sort(profiles, new Comparator<VideoStreamProfile>() {
            @Override
            public int compare(VideoStreamProfile o1, VideoStreamProfile o2) {
                if (o1.getFormat() != o2.getFormat()) {
                    return o1.getFormat().value() - o2.getFormat().value();
                }
                if (o1.getWidth() != o2.getWidth()) {
                    // Little first
                    return o1.getWidth() - o2.getWidth();
                }
                if (o1.getHeight() != o2.getHeight()) {
                    // Large first
                    return o2.getHeight() - o1.getHeight();
                }
                // Large first
                return o2.getFps() - o1.getFps();
            }
        });
    }

    /**
     * Data bean bundle VideoStreamProfile of depth and color
     */
    protected static class D2CStreamProfile implements AutoCloseable {
        // color stream profile
        private VideoStreamProfile colorProfile;
        // depth stream profile
        private VideoStreamProfile depthProfile;

        public VideoStreamProfile getColorProfile() {
            return colorProfile;
        }

        public void setColorProfile(VideoStreamProfile colorProfile) {
            this.colorProfile = colorProfile;
        }

        public VideoStreamProfile getDepthProfile() {
            return depthProfile;
        }

        public void setDepthProfile(VideoStreamProfile depthProfile) {
            this.depthProfile = depthProfile;
        }

        @Override
        public void close() {
            if (null != colorProfile) {
                try {
                    colorProfile.close();
                } catch (Exception ignore) {
                }
                colorProfile = null;
            }
            if (null != depthProfile) {
                try {
                    depthProfile.close();
                } catch (Exception ignore) {
                }
                depthProfile = null;
            }
        }
    }
}
