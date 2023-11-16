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
import com.orbbec.obsensor.Format;
import com.orbbec.obsensor.FrameType;
import com.orbbec.obsensor.IRFrame;
import com.orbbec.obsensor.Pipeline;
import com.orbbec.obsensor.Sensor;
import com.orbbec.obsensor.SensorType;
import com.orbbec.obsensor.StreamProfile;
import com.orbbec.obsensor.StreamProfileList;
import com.orbbec.obsensor.StreamType;
import com.orbbec.obsensor.VideoStreamProfile;
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
                vH.irGlView.clearWindow();
                vH.irGlView.setVisibility(View.GONE);
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
            viewHolder.deviceNameTv = convertView.findViewById(R.id.tv_device_name);
            viewHolder.depthCtlLayout = convertView.findViewById(R.id.ll_depth_view);
            viewHolder.depthCtlBtn = convertView.findViewById(R.id.btn_depth_control);
            viewHolder.depthGlView = convertView.findViewById(R.id.glv_depth);
            viewHolder.colorCtlLayout = convertView.findViewById(R.id.ll_color_view);
            viewHolder.colorCtlBtn = convertView.findViewById(R.id.btn_color_control);
            viewHolder.colorGlView = convertView.findViewById(R.id.glv_color);
            viewHolder.irCtlLayout = convertView.findViewById(R.id.ll_ir_view);
            viewHolder.irCtlBtn = convertView.findViewById(R.id.btn_ir_control);
            viewHolder.irGlView = convertView.findViewById(R.id.glv_ir);
            mViewHolderMap.put(position, viewHolder);
            convertView.setTag(viewHolder);
        } else {
            viewHolder = (ViewHolder) convertView.getTag();
            viewHolder.depthGlView.clearWindow();
            viewHolder.colorGlView.clearWindow();
            viewHolder.irGlView.clearWindow();
        }
        viewHolder.depthGlView.setVisibility(View.VISIBLE);
        viewHolder.colorGlView.setVisibility(View.VISIBLE);
        viewHolder.irGlView.setVisibility(View.VISIBLE);

        DeviceBean deviceBean = getItem(position);
        // Get device
        Device device = deviceBean.getDevice();

        // Get device name
        String devName = deviceBean.getDeviceName();
        // Get device uid
        String uid = deviceBean.getDeviceUid();
        // Get device connection type
        String connectionType = deviceBean.getDeviceConnectionType();
        viewHolder.deviceNameTv.setText(devName + "#" + uid + " " + connectionType);
        final Sensor depthSensor = device.getSensor(SensorType.DEPTH);
        final Sensor colorSensor = device.getSensor(SensorType.COLOR);
        final Sensor irSensor;
        if (null != device.getSensor(SensorType.IR)) {
            viewHolder.irCtlBtn.setText("IR");
            irSensor = device.getSensor(SensorType.IR);
        } else {
            viewHolder.irCtlBtn.setText("IR_LEFT");
            irSensor = device.getSensor(SensorType.IR_LEFT);
        }

        viewHolder.depthCtlLayout.setVisibility(null != depthSensor ? View.VISIBLE : View.INVISIBLE);
        viewHolder.colorCtlLayout.setVisibility(null != colorSensor ? View.VISIBLE : View.INVISIBLE);
        viewHolder.irCtlLayout.setVisibility(null != irSensor ? View.VISIBLE : View.INVISIBLE);

        if (deviceBean.isDepthRunning) {
            startStream(depthSensor, viewHolder.depthGlView);
        }
        setColor(deviceBean.isDepthRunning, viewHolder.depthCtlBtn);
        viewHolder.depthCtlBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Control depth sensor stream
                if (deviceBean.isDepthRunning) {
                    stopStream(depthSensor);
                    deviceBean.isDepthRunning = false;
                } else {
                    startStream(depthSensor, viewHolder.depthGlView);
                    deviceBean.isDepthRunning = true;
                }

                setColor(deviceBean.isDepthRunning, viewHolder.depthCtlBtn);
            }
        });

        if (deviceBean.isColorRunning) {
            startStream(colorSensor, viewHolder.colorGlView);
        }
        setColor(deviceBean.isColorRunning, viewHolder.colorCtlBtn);
        viewHolder.colorCtlBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Control color sensor stream
                if (deviceBean.isColorRunning) {
                    stopStream(colorSensor);
                    deviceBean.isColorRunning = false;
                } else {
                    startStream(colorSensor, viewHolder.colorGlView);
                    deviceBean.isColorRunning = true;
                }

                setColor(deviceBean.isColorRunning, viewHolder.colorCtlBtn);
            }
        });

        if (deviceBean.isIrRunning) {
            startStream(irSensor, viewHolder.irGlView);
        }
        setColor(deviceBean.isIrRunning, viewHolder.irCtlBtn);
        viewHolder.irCtlBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Control IR sensor stream
                if (deviceBean.isIrRunning) {
                    stopStream(irSensor);
                    deviceBean.isIrRunning = false;
                } else {
                    startStream(irSensor, viewHolder.irGlView);
                    deviceBean.isIrRunning = true;
                }

                setColor(deviceBean.isIrRunning, viewHolder.irCtlBtn);
            }
        });

        return convertView;
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
     * @param sensor Target sensor
     * @return If success return a VideoStreamProfile, otherwise return null.
     */
    protected final VideoStreamProfile getStreamProfile(Sensor sensor) {
        // Select prefer Format
        Format format;
        SensorType sensorType = sensor.getType();
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
            StreamProfileList profileList = sensor.getStreamProfileList();
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

    private void stopStream(Sensor sensor) {
        if (null == sensor) {
            return;
        }
        try {
            sensor.stop();
        } catch (Exception e) {
            e.printStackTrace();
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
                    OBGLView depthGLView = glView;
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
                            depthGLView.update(depthFrame.getWidth(), depthFrame.getHeight(),
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
                    OBGLView colorGLView = glView;
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
                            colorGLView.update(colorFrame.getWidth(), colorFrame.getHeight(), StreamType.COLOR, colorFrame.getFormat(), bytes, 1.0f);
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
                case IR: {
                    OBGLView irGLView = glView;
                    // Obtain open stream configuration through StreamProfileList
                    VideoStreamProfile irProfile = getStreamProfile(sensor);
                    if (null != irProfile) {
                        printStreamProfile(irProfile.as(StreamType.VIDEO));
                        // Start sensor through specified VideoStreamVideoProfile
                        sensor.start(irProfile, frame -> {
                            IRFrame irFrame = frame.as(FrameType.IR);
                            // Get Frame data
                            byte[] bytes = new byte[irFrame.getDataSize()];
                            irFrame.getData(bytes);
                            // Render data
                            irGLView.update(irFrame.getWidth(), irFrame.getHeight(),
                                    StreamType.IR, irFrame.getFormat(), bytes, 1.0f);
                            // Release frame
                            frame.close();
                        });
                        // Release IR profile
                        irProfile.close();
                    } else {
                        Log.w(TAG, "start ir stream failed, irProfile is null!");
                    }
                    break;
                }
                case IR_LEFT: {
                    OBGLView irGLView = glView;
                    // Obtain open stream configuration through StreamProfileList
                    VideoStreamProfile irProfile = getStreamProfile(sensor);
                    if (null != irProfile) {
                        printStreamProfile(irProfile.as(StreamType.VIDEO));
                        // Start sensor through specified VideoStreamVideoProfile
                        sensor.start(irProfile, frame -> {
                            IRFrame irFrame = frame.as(FrameType.IR_LEFT);
                            // Get Frame data
                            byte[] bytes = new byte[irFrame.getDataSize()];
                            irFrame.getData(bytes);
                            // Render data
                            irGLView.update(irFrame.getWidth(), irFrame.getHeight(),
                                    StreamType.IR, irFrame.getFormat(), bytes, 1.0f);
                            // Release frame
                            frame.close();
                        });
                        // Release IR profile
                        irProfile.close();
                    } else {
                        Log.w(TAG, "start ir stream failed, irProfile is null!");
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

    class ViewHolder {
        TextView deviceNameTv;
        LinearLayout depthCtlLayout;
        Button depthCtlBtn;
        OBGLView depthGlView;
        LinearLayout colorCtlLayout;
        Button colorCtlBtn;
        OBGLView colorGlView;
        LinearLayout irCtlLayout;
        Button irCtlBtn;
        OBGLView irGlView;
    }
}
