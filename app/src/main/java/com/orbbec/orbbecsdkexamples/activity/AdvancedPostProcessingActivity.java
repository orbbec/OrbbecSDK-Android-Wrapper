package com.orbbec.orbbecsdkexamples.activity;

import android.content.res.Resources;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.DisplayMetrics;
import android.util.Log;
import android.util.TypedValue;
import android.view.View;
import android.view.Window;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.Nullable;

import com.orbbec.obsensor.Config;
import com.orbbec.obsensor.DepthFrame;
import com.orbbec.obsensor.Device;
import com.orbbec.obsensor.DeviceChangedCallback;
import com.orbbec.obsensor.DeviceList;
import com.orbbec.obsensor.Filter;
import com.orbbec.obsensor.Frame;
import com.orbbec.obsensor.FrameSet;
import com.orbbec.obsensor.Pipeline;
import com.orbbec.obsensor.RecommendedFilterList;
import com.orbbec.obsensor.Sensor;
import com.orbbec.obsensor.types.DeviceInfo;
import com.orbbec.obsensor.types.FilterConfigSchemaItem;
import com.orbbec.obsensor.types.Format;
import com.orbbec.obsensor.types.FrameType;
import com.orbbec.obsensor.types.SensorType;
import com.orbbec.obsensor.types.StreamType;
import com.orbbec.orbbecsdkexamples.R;
import com.orbbec.orbbecsdkexamples.adapter.SpinnerContentAdapter;
import com.orbbec.orbbecsdkexamples.view.OBGLView;

import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public class AdvancedPostProcessingActivity extends BaseActivity implements AdapterView.OnItemSelectedListener, View.OnClickListener {
    private static final String TAG = "AdvancedPostProcessingActivity";

    private Device mDevice;
    private Pipeline mPipeline;
    private Thread mStreamThread;
    private volatile boolean mIsStreamRunning;

    private OBGLView mDepthView;
    private OBGLView mPostProcessingView;
    private Spinner mFilterSelectSp;
    private Spinner mConfigSelectSp;
    private Button mConfigSetBtn;
    private EditText mConfigSetEt;
    private Button mConfigGetBtn;
    private TextView mConfigGetTv;
    private LinearLayout mFilterControlLL;

    private SpinnerContentAdapter<String> mFilterSelectAdapter;
    private SpinnerContentAdapter<String> mConfigSelectAdapter;

    private RecommendedFilterList mRecommendedFilterList;
    private Map<String, Filter> filters;
    private List<FilterConfigSchemaItem> mConfigSchemaList;
    private final Map<String, CheckBox> mFilterCheckBoxMap = new HashMap<>();
    private float textSize = 0;

    private DeviceChangedCallback mDeviceChangedCallback = new DeviceChangedCallback() {

        @Override
        public void onDeviceAttach(DeviceList deviceList) {
            try {
                if (mPipeline == null) {
                    // 2.Create Device and initialize Pipeline through Device
                    mDevice = deviceList.getDevice(0);
                    Sensor depthSensor = mDevice.getSensor(SensorType.DEPTH);
                    if (null == depthSensor) {
                        showToast(getString(R.string.device_not_support_post_process));
                        mDevice.close();
                        mDevice = null;
                        return;
                    }
                    // 3.Create Device and initialize Pipeline through Device
                    mPipeline = new Pipeline(mDevice);

                    // 4.Get recommended post processor filter list and get the filter count
                    mRecommendedFilterList = depthSensor.getRecommendedFilterList();
                    int count = mRecommendedFilterList.getFilterListCount();

                    // 使用 LinkedHashMap 保持从 SDK 获取的原始顺序
                    filters = new LinkedHashMap<>();
                    for (int i = 0; i < count; i++) {
                        Filter filter = mRecommendedFilterList.getFilter(i);
                        String name = mRecommendedFilterList.getFilterName(filter);

                        if (name.equals("HDRMerge")) {
                            continue;
                        }
                        String newName = name.contains("Filter") ? name.replace("Filter", "") : name;
                        filters.put(newName, filter);
                        createFilterCheckbox(newName, filter.isEnabled());
                    }
                    updateFilterSelect();

                    // 7.Start sensor stream and create a thread to obtain Pipeline data
                    startStream();
                }
            } catch (Exception e) {
                Log.e(TAG, "onDeviceAttach: " + e.getMessage());
            } finally {
                // 10.Release device list resources
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
        setTitle("Advanced-Post Processing");
        setContentView(R.layout.activity_advanced_post_processing);
        initView();
        // SDK 只在 Activity 创建时初始化一次
        initSDK();
    }

    @Override
    protected void onResume() {
        super.onResume();
        // 从后台回到前台：pipeline 已存在则直接恢复出流，filters/UI 均保留
        if (mPipeline != null && !mIsStreamRunning) {
            startStream();
        }
    }

    @Override
    protected void onPause() {
        // 退到后台：只停流线程和 pipeline 出流，不销毁任何资源
        stopStreamOnly();
        if (mPipeline != null) {
            try {
                mPipeline.stop();
            } catch (Exception e) {
                Log.e(TAG, "onPause pipeline stop: " + e.getMessage());
            }
        }
        super.onPause();
    }

    @Override
    protected void onDestroy() {
        // Activity 退出时统一释放所有资源
        try {
            stop();
            if (null != mPipeline) {
                mPipeline.close();
                mPipeline = null;
            }
            if (null != mDevice) {
                mDevice.close();
                mDevice = null;
            }
        } catch (Exception e) {
            Log.e(TAG, "onDestroy: " + e.getMessage());
        }
        releaseSDK();
        super.onDestroy();
    }

    private void initView() {
        mDepthView = findViewById(R.id.post_processing_depth_view);
        mPostProcessingView = findViewById(R.id.post_processing_view);
        mFilterControlLL = findViewById(R.id.ll_post_processing_switch_control);

        mFilterSelectSp = findViewById(R.id.sp_filter_select);
        mFilterSelectAdapter = new SpinnerContentAdapter<>(this, android.R.layout.simple_spinner_item);
        mFilterSelectAdapter.setDropDownViewResource(R.layout.spinner_dropdown_item);
        mFilterSelectSp.setAdapter(mFilterSelectAdapter);
        mFilterSelectSp.setOnItemSelectedListener(this);

        mConfigSelectSp = findViewById(R.id.sp_filter_config_select);
        mConfigSelectAdapter = new SpinnerContentAdapter<>(this, android.R.layout.simple_spinner_item);
        mConfigSelectAdapter.setDropDownViewResource(R.layout.spinner_dropdown_item);
        mConfigSelectSp.setAdapter(mConfigSelectAdapter);
        mConfigSelectSp.setOnItemSelectedListener(this);

        mConfigSetBtn = findViewById(R.id.btn_filter_config_set);
        mConfigSetBtn.setOnClickListener(this);
        mConfigSetEt = findViewById(R.id.et_filter_config_set);
        mConfigGetBtn = findViewById(R.id.btn_filter_config_get);
        mConfigGetBtn.setOnClickListener(this);
        mConfigGetTv = findViewById(R.id.tv_filter_config_get);

        DisplayMetrics displayMetrics = Resources.getSystem().getDisplayMetrics();
        float density = displayMetrics.density;
        int screenWidth = displayMetrics.widthPixels;
        textSize = screenWidth * 0.01f / density;
    }

    private void startStream() {
        try {
            Config config = new Config();
            config.enableStream(StreamType.DEPTH);
            mPipeline.start(config);
            config.close();
            start();
        } catch (Exception e) {
            Log.e(TAG, "startStream: " + e.getMessage());
        }
    }

    // Only stops the stream thread, does NOT close filters or UI (used for background pause)
    private void stopStreamOnly() {
        mIsStreamRunning = false;
        if (null != mStreamThread) {
            try {
                mStreamThread.join(300);
            } catch (InterruptedException e) {
                Log.e(TAG, "stopStreamOnly: " + e.getMessage());
            }
            mStreamThread = null;
        }
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
        if (null != mStreamThread) {
            try {
                mStreamThread.join(300);
            } catch (InterruptedException e) {
                Log.e(TAG, "stop: " + e.getMessage());
            }
            mStreamThread = null;
        }

        if (null != mRecommendedFilterList) {
            mRecommendedFilterList.close();
            mRecommendedFilterList = null;
        }

        if (null != filters) {
            filters.forEach((k, v) -> v.close());
            filters.clear();
        }

        mFilterCheckBoxMap.clear();

        if (null != mConfigSchemaList) {
            mConfigSchemaList.clear();
            mConfigSchemaList = null;
        }

        updateFilterSelect();
        runOnUiThread(() -> {
            updateConfigUI();
            mFilterControlLL.removeAllViews();
        });
    }

    private Runnable mStreamRunnable = () -> {
        while (mIsStreamRunning) {
            try (FrameSet frameSet = mPipeline.waitForFrameSet(100)) {
                if (frameSet == null) {
                    continue;
                }

                DepthFrame depthFrame = frameSet.getDepthFrame();
                if (depthFrame != null) {
                    byte[] depthData = new byte[depthFrame.getDataSize()];
                    depthFrame.getData(depthData);
                    mDepthView.update(depthFrame.getWidth(), depthFrame.getHeight(), StreamType.DEPTH, depthFrame.getFormat(), depthData, depthFrame.getValueScale());

                    DepthFrame processedFrame = depthFrame;
                    for (Filter filter : filters.values()) {
                        if (filter.isEnabled()) {
                            Frame f = filter.process(processedFrame);
                            processedFrame.close();
                            processedFrame = f.as(FrameType.DEPTH);
                        }
                    }

                    byte[] data = new byte[processedFrame.getDataSize()];
                    processedFrame.getData(data);

                    int width = processedFrame.getWidth();
                    int height = processedFrame.getHeight();

                    if (processedFrame.getIndex() % 30 == 0 && processedFrame.getFormat() == Format.Y16) {
                        float scale = processedFrame.getValueScale();
                        float centerDistance = data[width * height / 2 + width / 2] * scale;

                        Log.i(TAG, "Facing an object: " + centerDistance + "mm away.");
                    }

                    mPostProcessingView.update(width, height, StreamType.DEPTH, processedFrame.getFormat(), data, processedFrame.getValueScale());
                    processedFrame.close();
                }
            } catch (Exception e) {
                Log.e(TAG, "run: " + e.getMessage());
            }
        }
    };

    @Override
    protected DeviceChangedCallback getDeviceChangedCallback() {
        return mDeviceChangedCallback;
    }

    private void updateFilterSelect() {
        runOnUiThread(() -> {
            if (filters != null && !filters.isEmpty()) {
                mFilterSelectAdapter.clear();
                mFilterSelectAdapter.addAll(filters.keySet());
                mFilterSelectAdapter.notifyDataSetChanged();
            } else {
                mFilterSelectAdapter.clear();
                mFilterSelectAdapter.notifyDataSetChanged();
            }
        });
    }

    private void updateConfigUI() {
        if (filters != null && !filters.isEmpty()) {
            Filter filter = filters.get(mFilterSelectSp.getSelectedItem().toString());
            if (filter != null) {
                mConfigSchemaList = filter.getConfigSchemaList();

                if (mConfigSchemaList == null || mConfigSchemaList.isEmpty()) {
                    mConfigSelectAdapter.clear();
                    mConfigSelectAdapter.notifyDataSetChanged();
                    mConfigSetEt.setHint("");
                    return;
                }

                mConfigSelectAdapter.clear();
                mConfigSchemaList.forEach(item -> mConfigSelectAdapter.add(item.getName()));
                mConfigSelectAdapter.notifyDataSetChanged();

                FilterConfigSchemaItem item = mConfigSchemaList.get(0);
                mConfigSetEt.setHint("[" + item.getMin() + "-" + item.getMax() + "]");
            }
        } else {
            mConfigSelectAdapter.clear();
            mConfigSelectAdapter.notifyDataSetChanged();
            mConfigSetEt.setHint("");
        }
    }

    private int getContrastColor(int backgroundColor) {
        // Calculate relative luminance.
        double darkness = 1 - (0.299 * Color.red(backgroundColor) + 0.587 * Color.green(backgroundColor) + 0.114 * Color.blue(backgroundColor)) / 255;
        if (darkness < 0.5) {
            return Color.BLACK; // Light background, use black text
        } else {
            return Color.WHITE; // Dark background, use white text
        }
    }

    private void createFilterCheckbox(String filterName, boolean isEnabled) {
        runOnUiThread(() -> {
            CheckBox cb = new CheckBox(this);
            cb.setId(View.generateViewId());
            LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT);
            cb.setLayoutParams(params);
            cb.setBackground(null);
            cb.setButtonDrawable(null);
            cb.setCompoundDrawablesWithIntrinsicBounds(R.drawable.checkbox, 0, 0, 0);
            cb.setCompoundDrawablePadding(6);
            cb.setPadding(6, 0, 6, 6);
            cb.setText(filterName);
            // Get background color of activity to determine text color
            int bgColor = Color.BLACK; // Default
            TypedValue a = new TypedValue();
            getTheme().resolveAttribute(android.R.attr.windowBackground, a, true);
            if (a.type >= TypedValue.TYPE_FIRST_COLOR_INT && a.type <= TypedValue.TYPE_LAST_COLOR_INT) {
                bgColor = a.data;
            } else {
                try {
                    Drawable background = getWindow().getDecorView().getBackground();
                    if (background instanceof ColorDrawable) {
                        bgColor = ((ColorDrawable) background).getColor();
                    }
                } catch (Exception ignore) {}
            }
            cb.setTextColor(getContrastColor(bgColor));

            cb.post(() -> {
                textSize = Math.max(12, Math.min(textSize, 14));
                cb.setTextSize(TypedValue.COMPLEX_UNIT_SP, textSize);
            });
            cb.setOnClickListener(v -> {
                String name = null;
                for (Map.Entry<String, CheckBox> entry : mFilterCheckBoxMap.entrySet()) {
                    if (entry.getValue().getId() == v.getId()) {
                        name = entry.getKey();
                    }
                }
                if (name != null) {
                    Filter filter = filters.get(name);
                    if (filter != null) filter.enable(((CheckBox) v).isChecked());
                }
            });
            cb.setChecked(isEnabled);
            mFilterCheckBoxMap.put(filterName, cb);

            mFilterControlLL.addView(cb);
        });
    }

    @Override
    public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
        int parentId = parent.getId();
        if (parentId == R.id.sp_filter_select) {
            updateConfigUI();
        } else if (parentId == R.id.sp_filter_config_select) {
            FilterConfigSchemaItem item = mConfigSchemaList.get(position);
            mConfigSetEt.setHint("[" + item.getMin() + "-" + item.getMax() + "]");
        }
    }

    @Override
    public void onNothingSelected(AdapterView<?> parent) {

    }

    @Override
    public void onClick(View v) {
        String selectedConfig = mConfigSelectSp.getSelectedItem().toString();
        Filter filter = filters.get(mFilterSelectSp.getSelectedItem().toString());

        if (filter == null) return;

        try {
            int viewId = v.getId();
            if (viewId == R.id.btn_filter_config_set) {
                String value = mConfigSetEt.getText().toString();
                filter.setConfigValue(selectedConfig, Float.parseFloat(value));
            } else if (viewId == R.id.btn_filter_config_get) {
                double value = filter.getConfigValue(selectedConfig);
                mConfigGetTv.setText(String.format(Locale.getDefault(), "%.3f", value));
            }
        } catch (Exception e) {
            Log.w(TAG, "onClick: " + e.getMessage());
        }
    }

    private void showToast(String msg) {
        runOnUiThread(() -> Toast.makeText(AdvancedPostProcessingActivity.this, msg, Toast.LENGTH_SHORT).show());
    }
}