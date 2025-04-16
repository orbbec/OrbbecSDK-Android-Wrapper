package com.orbbec.orbbecsdkexamples.adapter;

import android.content.Context;
import android.graphics.Color;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.orbbec.obsensor.ColorFrame;
import com.orbbec.obsensor.DepthFrame;
import com.orbbec.obsensor.Device;
import com.orbbec.obsensor.Sensor;
import com.orbbec.obsensor.StreamProfileList;
import com.orbbec.obsensor.VideoStreamProfile;
import com.orbbec.obsensor.types.Format;
import com.orbbec.obsensor.types.FrameType;
import com.orbbec.obsensor.types.SensorType;
import com.orbbec.obsensor.types.StreamType;
import com.orbbec.orbbecsdkexamples.R;
import com.orbbec.orbbecsdkexamples.bean.DeviceBean;
import com.orbbec.orbbecsdkexamples.view.OBGLView;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;

public class DeviceControllerAdapter extends BaseAdapter {
    private static final String TAG = "DeviceControllerAdapter";

    private List<DeviceBean> mDeviceBeanList;
    private LayoutInflater mInflater;
    private HashMap<Integer, ViewHolder> mViewHolderMap = new LinkedHashMap<>();

    public DeviceControllerAdapter(List<DeviceBean> deviceBean, Context context) {
        this.mDeviceBeanList = deviceBean;
        this.mInflater = LayoutInflater.from(context);
    }

    @Override
    public int getCount() {
        return mDeviceBeanList == null ? 0 : mDeviceBeanList.size();
    }

    @Override
    public DeviceBean getItem(int position) {
        return mDeviceBeanList.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    public void addItem(DeviceBean deviceBean) {
        mDeviceBeanList.add(deviceBean);
        this.notifyDataSetChanged();
    }

    public void deleteItem(DeviceBean deviceBean) {
        mDeviceBeanList.remove(deviceBean);
        this.notifyDataSetChanged();
    }

    private void refreshViewHolderMap() {
        for (int i = mDeviceBeanList.size(), c = mViewHolderMap.size(); i < c; i++) {
            ViewHolder vH = mViewHolderMap.get(i);
            if (null != vH) {
                vH.depthGlView.clearWindow();
                vH.depthGlView.setVisibility(View.GONE);
                vH.colorGlView.clearWindow();
                vH.colorGlView.setVisibility(View.GONE);
            }
        }
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        ViewHolder viewHolder;
        refreshViewHolderMap();
        // 初始化item ui
        if (null == convertView) {
            viewHolder = new ViewHolder();
            convertView = mInflater.inflate(R.layout.layout_device_item, parent, false);
            initViewHolder(viewHolder, convertView);
            mViewHolderMap.put(position, viewHolder);
            convertView.setTag(viewHolder);
        } else {
            viewHolder = (ViewHolder) convertView.getTag();
            viewHolder.depthGlView.clearWindow();
            viewHolder.colorGlView.clearWindow();
        }

        DeviceBean deviceBean = getItem(position);
        // Get device
        Device device = deviceBean.getDevice();

        // Get device name
        String devName = deviceBean.getDeviceName();
        // Get device uid
        String uid = deviceBean.getDeviceUid();
        // Get device connection type
        String connectionType = deviceBean.getDeviceConnectionType();

        String deviceInfo = devName + "\nuid: " + uid + "\nconnection type: " + connectionType;
        viewHolder.deviceNameTv.setText(deviceInfo);
        final Sensor depthSensor = device.getSensor(SensorType.DEPTH);
        final Sensor colorSensor = device.getSensor(SensorType.COLOR);

        viewHolder.depthCtlLayout.setVisibility(null != depthSensor ? View.VISIBLE : View.GONE);
        viewHolder.colorCtlLayout.setVisibility(null != colorSensor ? View.VISIBLE : View.GONE);

        if (deviceBean.isDepthRunning) {
            startStream(depthSensor, viewHolder.depthGlView);
        }
        setColor(deviceBean.isDepthRunning, viewHolder.depthCtlBtn);
        viewHolder.depthCtlBtn.setOnClickListener(v -> {
            // Control depth sensor stream
            if (deviceBean.isDepthRunning) {
                stopStream(depthSensor);
                deviceBean.isDepthRunning = false;
            } else {
                startStream(depthSensor, viewHolder.depthGlView);
                deviceBean.isDepthRunning = true;
            }

            setColor(deviceBean.isDepthRunning, viewHolder.depthCtlBtn);
        });

        if (deviceBean.isColorRunning) {
            startStream(colorSensor, viewHolder.colorGlView);
        }
        setColor(deviceBean.isColorRunning, viewHolder.colorCtlBtn);
        viewHolder.colorCtlBtn.setOnClickListener(v -> {
            // Control color sensor stream
            if (deviceBean.isColorRunning) {
                stopStream(colorSensor);
                deviceBean.isColorRunning = false;
            } else {
                startStream(colorSensor, viewHolder.colorGlView);
                deviceBean.isColorRunning = true;
            }

            setColor(deviceBean.isColorRunning, viewHolder.colorCtlBtn);
        });

        return convertView;
    }

    private void initViewHolder(ViewHolder viewHolder, View convertView) {
        viewHolder.deviceNameTv = convertView.findViewById(R.id.tv_device_name);
        viewHolder.depthCtlLayout = convertView.findViewById(R.id.ll_depth_view);
        viewHolder.depthCtlBtn = convertView.findViewById(R.id.btn_depth_control);
        viewHolder.depthGlView = convertView.findViewById(R.id.glv_depth);
        viewHolder.colorCtlLayout = convertView.findViewById(R.id.ll_color_view);
        viewHolder.colorCtlBtn = convertView.findViewById(R.id.btn_color_control);
        viewHolder.colorGlView = convertView.findViewById(R.id.glv_color);
    }

    private void setColor(boolean isStart, Button btn) {
        if (isStart) {
            btn.setBackgroundColor(Color.parseColor("#FF00FF00"));
            btn.setTextColor(Color.parseColor("#FF000000"));
        } else {
            btn.setBackgroundColor(Color.parseColor("#FF3700B3"));
            btn.setTextColor(Color.parseColor("#FFFFFFFF"));
        }
    }

    private void printStreamProfile(VideoStreamProfile vsp) {
        Log.i(TAG, "printStreamProfile: "
                + vsp.getWidth() + "×" + vsp.getHeight()
                + "@" + vsp.getFps() + "fps " + vsp.getFormat());
    }

    /**
     * Get best VideoStreamProfile of sensor support by OrbbecSdkExamples.
     * Note: OrbbecSdkExamples just sample code to render and save frame, it support limit VideoStreamProfile Format.
     *
     * @param sensor Target sensor
     * @return If success return a VideoStreamProfile, otherwise return null.
     */
    protected final VideoStreamProfile getStreamProfile(Sensor sensor) {
        // Select prefer Format
        Format format;
        SensorType sensorType = sensor.getType();
        if (sensorType == SensorType.COLOR) {
            format = Format.RGB;
        } else if (sensorType == SensorType.DEPTH) {
            format = Format.Y16;
        } else {
            Log.w(TAG, "getStreamProfile not support sensorType: " + sensorType);
            return null;
        }

        try {
            // Get StreamProfileList from sensor
            StreamProfileList profileList = sensor.getStreamProfileList();
            List<VideoStreamProfile> profiles = new ArrayList<>();
            for (int i = 0, N = profileList.getCount(); i < N; i++) {
                // Get StreamProfile by index and convert it to VideoStreamProfile
                VideoStreamProfile profile = profileList.getProfile(i).as(StreamType.VIDEO);
                // Match target with and format.
                // Note: width >= 640 && width <= 1280 is consider best render for OrbbecSdkExamples
                if ((profile.getWidth() >= 640 && profile.getWidth() <= 1280)
                        && profile.getHeight() >= 360
                        && profile.getFormat() == format) {
                    profiles.add(profile);
                } else {
                    profile.close();
                }
            }
            // If not match StreamProfile with prefer format, Try other.
            // Note: OrbbecSdkExamples not support render Format of MJPEG and RVL
            if (profiles.isEmpty() && profileList.getCount() > 0) {
                for (int i = 0, N = profileList.getCount(); i < N; i++) {
                    VideoStreamProfile profile = profileList.getProfile(i).as(StreamType.VIDEO);
                    if ((profile.getWidth() >= 640 && profile.getWidth() <= 1280)
                            && profile.getHeight() >= 360
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
            Log.w(TAG, "getStreamProfile: " + e.getMessage());
        }
        return null;
    }

    private void stopStream(Sensor sensor) {
        if (null == sensor) {
            return;
        }
        try {
            sensor.stop();
        } catch (Exception e) {
            Log.w(TAG, "stopStream: " + e.getMessage());
        }
    }

    private void startStream(Sensor sensor, OBGLView glView) {
        if (null == sensor) {
            return;
        }
        try {
            // Get VideoStreamProfile List for a sensor
            StreamProfileList profileList = sensor.getStreamProfileList();
            if (null == profileList) {
                Log.w(TAG, "start stream failed, profileList is null !");
                return;
            }
            switch (sensor.getType()) {
                case DEPTH: {
                    // Obtain open stream configuration through StreamProfileList
                    VideoStreamProfile depthProfile = getStreamProfile(sensor);
                    if (null != depthProfile) {
                        printStreamProfile(depthProfile.as(StreamType.VIDEO));
                        // Start sensor through specified VideoStreamVideoProfile
                        sensor.start(depthProfile, frame -> {
                            DepthFrame depthFrame = frame.as(FrameType.DEPTH);
                            // Get frame data
                            byte[] bytes = new byte[depthFrame.getDataSize()];
                            depthFrame.getData(bytes);
                            // Render frame
                            glView.update(depthFrame.getWidth(), depthFrame.getHeight(),
                                    StreamType.DEPTH, depthFrame.getFormat(), bytes, depthFrame.getValueScale());
                            // Release Frame
                            frame.close();
                        });
                        // Release depth profile
                        depthProfile.close();
                    } else {
                        Log.w(TAG, "start depth stream failed, depthProfile is null!");
                    }
                    break;
                }
                case COLOR: {
                    // Obtain open stream configuration through StreamProfileList
                    VideoStreamProfile colorProfile = getStreamProfile(sensor);
                    if (null != colorProfile) {
                        printStreamProfile(colorProfile.as(StreamType.VIDEO));
                        // Start sensor through specified VideoStreamVideoProfile
                        sensor.start(colorProfile, frame -> {
                            ColorFrame colorFrame = frame.as(FrameType.COLOR);
                            // Get frame data
                            byte[] bytes = new byte[colorFrame.getDataSize()];
                            colorFrame.getData(bytes);
                            // Render frame
                            glView.update(colorFrame.getWidth(), colorFrame.getHeight(), StreamType.COLOR, colorFrame.getFormat(), bytes, 1.0f);
                            // Release Frame
                            frame.close();
                        });
                        // Release color profile
                        colorProfile.close();
                    } else {
                        Log.w(TAG, "start color stream failed, colorProfile is null!");
                    }
                    break;
                }
            }

            // Release the profileList resource
            profileList.close();
        } catch (Exception e) {
            Log.w(TAG, "startStream: " + e.getMessage());
        }
    }

    static class ViewHolder {
        TextView deviceNameTv;
        LinearLayout depthCtlLayout;
        Button depthCtlBtn;
        OBGLView depthGlView;
        LinearLayout colorCtlLayout;
        Button colorCtlBtn;
        OBGLView colorGlView;
    }
}
