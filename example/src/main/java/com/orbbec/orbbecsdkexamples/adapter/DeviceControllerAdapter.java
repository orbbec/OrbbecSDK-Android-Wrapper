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
import com.orbbec.obsensor.Sensor;
import com.orbbec.obsensor.SensorType;
import com.orbbec.obsensor.StreamProfile;
import com.orbbec.obsensor.StreamProfileList;
import com.orbbec.obsensor.StreamType;
import com.orbbec.obsensor.VideoStreamProfile;
import com.orbbec.orbbecsdkexamples.R;
import com.orbbec.orbbecsdkexamples.bean.DeviceBean;
import com.orbbec.orbbecsdkexamples.view.OBGLView;

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
        Sensor depthSensor = device.getSensor(SensorType.DEPTH);
        Sensor colorSensor = device.getSensor(SensorType.COLOR);
        Sensor irSensor = device.getSensor(SensorType.IR);

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
                case DEPTH:
                    OBGLView depthGLView = glView;
                    // Obtain open stream configuration through StreamProfileList
                    StreamProfile depthProfile = getVideoStreamProfile(profileList, 640, 0, Format.UNKNOWN, 30);
                    if (null == depthProfile) {
                        depthProfile = getVideoStreamProfile(profileList, 0, 0, Format.UNKNOWN, 30);
                    }
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
                case COLOR:
                    OBGLView colorGLView = glView;
                    // Obtain open stream configuration through StreamProfileList
                    StreamProfile colorProfile = getVideoStreamProfile(profileList, 640, 0, Format.RGB888, 30);
                    if (null == colorProfile) {
                        colorProfile = getVideoStreamProfile(profileList, 0, 0, Format.RGB888, 30);
                    }

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
                case IR:
                    OBGLView irGLView = glView;
                    // Obtain open stream configuration through StreamProfileList
                    StreamProfile irProfile = getVideoStreamProfile(profileList, 640, 0, Format.UNKNOWN, 30);
                    if (null == irProfile) {
                        irProfile = getVideoStreamProfile(profileList, 0, 0, Format.UNKNOWN, 30);
                    }
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
