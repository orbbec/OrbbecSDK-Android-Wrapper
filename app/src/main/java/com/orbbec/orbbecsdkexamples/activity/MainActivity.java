package com.orbbec.orbbecsdkexamples.activity;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.orbbec.orbbecsdkexamples.R;

import org.opencv.android.OpenCVLoader;

public class MainActivity extends AppCompatActivity implements View.OnClickListener {

    static {
        System.loadLibrary("orbbecsdkexamples");
        if (!OpenCVLoader.initLocal()) {
            Log.e("MainActivity", "OpenCV initialization failure!");
        } else {
            Log.d("MainActivity", "OpenCV Initialization Successful!");
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        requestPermission();
        initView();
    }

    private void requestPermission() {
        if (ContextCompat.checkSelfPermission(this,
                Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED ||
                ContextCompat.checkSelfPermission(this,
                        Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED ||
                ContextCompat.checkSelfPermission(this,
                        Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {

            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.CAMERA, Manifest.permission.WRITE_EXTERNAL_STORAGE, Manifest.permission.READ_EXTERNAL_STORAGE},
                    999);
        }
    }

    private void initView() {
        Button enumerate = findViewById(R.id.btn_basic_enumerate);
        enumerate.setOnClickListener(this);

        Button quickStart = findViewById(R.id.btn_basic_quick_start);
        quickStart.setOnClickListener(this);

        Button colorView = findViewById(R.id.btn_stream_color);
        colorView.setOnClickListener(this);

        Button depthView = findViewById(R.id.btn_stream_depth);
        depthView.setOnClickListener(this);

        Button imu = findViewById(R.id.btn_stream_imu);
        imu.setOnClickListener(this);

        Button irView = findViewById(R.id.btn_stream_infrared);
        irView.setOnClickListener(this);

        Button multiStream = findViewById(R.id.btn_stream_multi_streams);
        multiStream.setOnClickListener(this);

        Button deviceControl = findViewById(R.id.btn_device_control);
        deviceControl.setOnClickListener(this);

        Button firmwareUpdate = findViewById(R.id.btn_device_firmware_update);
        firmwareUpdate.setOnClickListener(this);

        Button hotPlugin = findViewById(R.id.btn_device_hot_plugin);
        hotPlugin.setOnClickListener(this);

        Button presetUpdate = findViewById(R.id.btn_device_optional_depth_presets_update);
        presetUpdate.setOnClickListener(this);

        Button hdr = findViewById(R.id.btn_advanced_hdr);
        hdr.setOnClickListener(this);

        Button hwD2CAlignView = findViewById(R.id.btn_advanced_hw_d2c_align);
        hwD2CAlignView.setOnClickListener(this);

        Button multiDevices = findViewById(R.id.btn_advanced_multi_devices);
        multiDevices.setOnClickListener(this);

        Button pointClound = findViewById(R.id.btn_advanced_point_cloud);
        pointClound.setOnClickListener(this);

        Button postProcessing = findViewById(R.id.btn_advanced_post_processing);
        postProcessing.setOnClickListener(this);

        Button syncAlign = findViewById(R.id.btn_advanced_sync_align);
        syncAlign.setOnClickListener(this);

        Button recordPlayback = findViewById(R.id.btn_record_playback);
        recordPlayback.setOnClickListener(this);

        Button saveToDisk = findViewById(R.id.btn_savetodisk);
        saveToDisk.setOnClickListener(this);
    }

    @Override
    public void onClick(View v) {
        int id = v.getId();
        if (id == R.id.btn_basic_enumerate) {
            toActivity(BasicEnumerateActivity.class);
        } else if (id == R.id.btn_basic_quick_start) {
            toActivity(BasicQuickStartActivity.class);
        } else if (id == R.id.btn_stream_color) {
            toActivity(StreamColorActivity.class);
        } else if (id == R.id.btn_stream_depth) {
            toActivity(StreamDepthActivity.class);
        } else if (id == R.id.btn_stream_imu) {
            toActivity(StreamImuActivity.class);
        } else if (id == R.id.btn_stream_infrared) {
            toActivity(StreamInfraredActivity.class);
        } else if (id == R.id.btn_stream_multi_streams) {
            toActivity(StreamMultiStreamsActivity.class);
        } else if (id == R.id.btn_device_control) {
            toActivity(DeviceControlActivity.class);
        } else if (id == R.id.btn_device_firmware_update) {
            toActivity(DeviceFirmwareUpdateActivity.class);
        } else if (id == R.id.btn_device_hot_plugin) {
            toActivity(DeviceHotPluginActivity.class);
        } else if (id == R.id.btn_device_optional_depth_presets_update) {
            toActivity(DeviceOptionalDepthPresetsUpdateActivity.class);
        } else if (id == R.id.btn_advanced_hdr) {
            toActivity(AdvancedHdrActivity.class);
        } else if (id == R.id.btn_advanced_hw_d2c_align) {
            toActivity(AdvancedHwD2CAlignActivity.class);
        } else if (id == R.id.btn_advanced_multi_devices) {
            toActivity(AdvancedMultiDevicesActivity.class);
        } else if (id == R.id.btn_advanced_point_cloud) {
            toActivity(AdvancedPointCloudActivity.class);
        } else if (id == R.id.btn_advanced_post_processing) {
            toActivity(AdvancedPostProcessingActivity.class);
        } else if (id == R.id.btn_advanced_sync_align) {
            toActivity(AdvancedSyncAlignActivity.class);
        }  else if (id == R.id.btn_record_playback) {
            toActivity(RecordPlaybackActivity.class);
        } else if (id == R.id.btn_savetodisk) {
            toActivity(SaveToDiskActivity.class);
        }
    }

    private void toActivity(Class activityClass) {
        startActivity(new Intent(this, activityClass));
    }
}