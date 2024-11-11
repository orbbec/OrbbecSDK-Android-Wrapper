package com.orbbec.orbbecsdkexamples.activity;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.orbbec.orbbecsdkexamples.R;

public class MainActivity extends AppCompatActivity implements View.OnClickListener {

    static {
        System.loadLibrary("orbbecsdkexamples");
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
        Button colorView = findViewById(R.id.btn_colorview);
        colorView.setOnClickListener(this);

        Button irView = findViewById(R.id.btn_irview);
        irView.setOnClickListener(this);

        Button doubleIRView = findViewById(R.id.btn_double_irview);
        doubleIRView.setOnClickListener(this);

        Button depthView = findViewById(R.id.btn_depthview);
        depthView.setOnClickListener(this);

        Button recordPlayback = findViewById(R.id.btn_record_playback);
        recordPlayback.setOnClickListener(this);

        Button helloOrbbec = findViewById(R.id.btn_hello_orbbec);
        helloOrbbec.setOnClickListener(this);

        Button pointClound = findViewById(R.id.btn_pointcloud);
        pointClound.setOnClickListener(this);

        Button syncAlignView = findViewById(R.id.btn_sync_align_view);
        syncAlignView.setOnClickListener(this);

        Button sensorControl = findViewById(R.id.btn_sensor_control);
        sensorControl.setOnClickListener(this);

        Button multiDevice = findViewById(R.id.btn_multi_device);
        multiDevice.setOnClickListener(this);

        Button imu = findViewById(R.id.btn_imu);
        imu.setOnClickListener(this);

        Button depthMode = findViewById(R.id.btn_depth_work_mode);
        depthMode.setOnClickListener(this);

        Button saveToDisk = findViewById(R.id.btn_savetodisk);
        saveToDisk.setOnClickListener(this);

        Button hotPlugin = findViewById(R.id.btn_hot_plugin);
        hotPlugin.setOnClickListener(this);

        Button hdrMerge = findViewById(R.id.btn_hdr_merge);
        hdrMerge.setOnClickListener(this);

        Button alignFilter = findViewById(R.id.btn_align_filter_view);
        alignFilter.setOnClickListener(this);

        Button postProcessing = findViewById(R.id.btn_post_processing);
        postProcessing.setOnClickListener(this);

        Button multiStream = findViewById(R.id.btn_multi_stream);
        multiStream.setOnClickListener(this);

        Button firmwareUpgrade = findViewById(R.id.btn_firmware_upgrade);
        firmwareUpgrade.setOnClickListener(this);
    }

    @Override
    public void onClick(View v) {
        int id = v.getId();
        if (id == R.id.btn_colorview) {
            toActivity(ColorViewerActivity.class);
        } else if (id == R.id.btn_irview) {
            toActivity(InfraredViewerActivity.class);
        } else if (id == R.id.btn_double_irview) {
            toActivity(DoubleIRViewerActivity.class);
        } else if (id == R.id.btn_depthview) {
            toActivity(DepthViewerActivity.class);
        } else if (id == R.id.btn_record_playback) {
            toActivity(RecordPlaybackActivity.class);
        } else if (id == R.id.btn_hello_orbbec) {
            toActivity(HelloOrbbecActivity.class);
        } else if (id == R.id.btn_pointcloud) {
            toActivity(PointCloudActivity.class);
        } else if (id == R.id.btn_sync_align_view) {
            toActivity(SyncAlignViewerActivity.class);
        } else if (id == R.id.btn_sensor_control) {
            toActivity(SensorControlActivity.class);
        } else if (id == R.id.btn_multi_device) {
            toActivity(MultiDeviceActivity.class);
        } else if (id == R.id.btn_imu) {
            toActivity(ImuActivity.class);
        } else if (id == R.id.btn_depth_work_mode) {
            toActivity(DepthModeActivity.class);
        } else if (id == R.id.btn_savetodisk) {
            toActivity(SaveToDiskActivity.class);
        } else if (id == R.id.btn_hot_plugin) {
            toActivity(HotPluginActivity.class);
        } else if (id == R.id.btn_hdr_merge) {
            toActivity(HdrMergeActivity.class);
        } else if (id == R.id.btn_align_filter_view) {
            toActivity(AlignFilterViewerActivity.class);
        } else if (id == R.id.btn_post_processing) {
            toActivity(PostProcessingActivity.class);
        } else if (id == R.id.btn_multi_stream) {
            toActivity(MultiStreamActivity.class);
        } else if (id == R.id.btn_firmware_upgrade) {
            toActivity(FirmwareUpgradeActivity.class);
        }
    }

    private void toActivity(Class activityClass) {
        startActivity(new Intent(this, activityClass));
    }
}