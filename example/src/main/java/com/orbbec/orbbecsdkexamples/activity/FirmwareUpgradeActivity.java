package com.orbbec.orbbecsdkexamples.activity;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.Nullable;

import com.orbbec.obsensor.Device;
import com.orbbec.obsensor.DeviceChangedCallback;
import com.orbbec.obsensor.DeviceInfo;
import com.orbbec.obsensor.DeviceList;
import com.orbbec.obsensor.UpgradeCallback;
import com.orbbec.orbbecsdkexamples.R;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class FirmwareUpgradeActivity extends BaseActivity {
    private static final String TAG = "FirmwareUpgradeActivity";

    private Device mDevice;
    private Button mSelectFile;
    private Button mUpgradeButton;
    private TextView mUpgradeInfo;
    private LinearLayout mProgressBarLL;

    private String firmwarePath = "";
    private String versionRegex = "\\d+\\.\\d+\\.\\d+";
    private boolean isUpgradeSuccess = false;

    private DeviceChangedCallback mDeviceChangedCallback = new DeviceChangedCallback() {

        @Override
        public void onDeviceAttach(DeviceList deviceList) {
            try {
                if (mDevice == null) {
                    mDevice = deviceList.getDevice(0);

                    drawFirmwareInfo(mDevice);
                }
            } catch (Exception e) {
                e.printStackTrace();
            } finally {
                deviceList.close();
            }
        }

        @Override
        public void onDeviceDetach(DeviceList deviceList) {
            try {
                if (mDevice != null) {
                    for (int i = 0, N = deviceList.getDeviceCount(); i < N; i++) {
                        String uid = deviceList.getUid(i);
                        DeviceInfo deviceInfo = mDevice.getInfo();
                        if (deviceInfo != null && TextUtils.equals(uid, deviceInfo.getUid())) {
                            mDevice.close();
                            mDevice = null;
                        }
                    }
                }
            } catch (Exception e) {
                e.printStackTrace();
            } finally {
                deviceList.close();
            }
        }
    };

    @Override
    protected DeviceChangedCallback getDeviceChangedCallback() {
        return mDeviceChangedCallback;
    }

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_firmware_upgrade);
        init();
    }

    @Override
    protected void onStart() {
        super.onStart();
        initSDK();
    }

    @Override
    protected void onStop() {
        super.onStop();
        try {
            if (mDevice != null) {
                mDevice.close();
                mDevice = null;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        releaseSDK();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == 1 && resultCode == RESULT_OK) {
            Uri uri = data.getData();
            if (uri != null) {
                firmwarePath = uri.getPath();
                Toast.makeText(this, "选择的文件路径: " + firmwarePath, Toast.LENGTH_SHORT).show();
            }
        }
    }

    private void init() {
        mSelectFile = findViewById(R.id.btn_select_file);
        mSelectFile.setOnClickListener(v -> {
            Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
            intent.setType("*/*");
            intent.putExtra(Intent.EXTRA_MIME_TYPES, new String[] {"application/octet-stream"});
            startActivityForResult(intent, 1);
        });
        mUpgradeButton = findViewById(R.id.btn_firmware_upgrade);
        mUpgradeButton.setOnClickListener(v -> {
//            String firmwarePath = FileUtils.getExternalSaveDir() + "/firmware";
            if (firmwarePath.equals("")) {
                Log.w(TAG, "onCreate: No found firmware file");
                showToast(getString(R.string.no_file_select));
                return;
            }
            firmwarePath = firmwarePath.replace("/document/primary:", "/storage/emulated/0/");
            Log.d(TAG, "onCreate: " + firmwarePath);

            mProgressBarLL.setVisibility(View.VISIBLE);
            mSelectFile.setEnabled(false);
            mUpgradeButton.setEnabled(false);
            Pattern pattern = Pattern.compile(versionRegex);
            Matcher matcher = pattern.matcher(firmwarePath);
            if (matcher.find()) {
                String newVersion = matcher.group();
                String oldVersion = mDevice.getInfo().getFirmwareVersion();

                if (newVersion.equals(oldVersion)) {
                    mProgressBarLL.setVisibility(View.GONE);
                    mSelectFile.setEnabled(true);
                    mUpgradeButton.setEnabled(true);
                    showToast(getString(R.string.firmware_is_latest_version));
                    Log.i(TAG, "onCreate: The firmware is the latest version！");
                    return;
                }
            }

            new Thread(() -> {
                boolean result = upgradeFirmware(firmwarePath);
                runOnUiThread(() -> updateUIAfterUpgrade(result));
            }).start();
        });
        mUpgradeInfo = findViewById(R.id.tv_firmware_info);
        mProgressBarLL = findViewById(R.id.ll_progress_bar);
    }

    private void drawFirmwareInfo(Device device) {
        DeviceInfo deviceInfo = device.getInfo();
        if (deviceInfo != null) {
            StringBuilder sb = new StringBuilder();
            sb.append("Device name：").append(deviceInfo.getName()).append("\n");
            sb.append("Device pid：").append(deviceInfo.getPid()).append("\n");
            sb.append("Firmware version：").append(deviceInfo.getFirmwareVersion()).append("\n");
            sb.append("Serial number：").append(deviceInfo.getSerialNumber()).append("\n");
            runOnUiThread(() -> {
                mUpgradeInfo.setText(sb.toString());
            });
        }
    }

    private boolean upgradeFirmware(String firmwarePath) {
        int index = firmwarePath.lastIndexOf(".img");
        boolean isImageFile = index != -1;
        index = firmwarePath.lastIndexOf(".bin");
        boolean isBinFile = index != -1;
        if (!(isImageFile || isBinFile)) {
            Log.e(TAG, "upgradeFirmware: Upgrade Fimware failed. invalid firmware file: " + firmwarePath);
            runOnUiThread(() -> showToast(getString(R.string.not_find_firmware_file)));
            return false;
        }

        try {
            mDevice.upgrade(firmwarePath, new UpgradeCallback() {
                @Override
                public void onCallback(short state, short percent, String msg) {
                    Log.d(TAG, "onCallback: state=" + state + ", percent=" + percent + ", msg=" + msg);
                    if (state == STAT_DONE) {
                        isUpgradeSuccess = true;
                    }
                }
            });
        } catch (Exception e) {
            Log.e(TAG, "upgradeFirmware: Upgrade Firmware ob error!");
            e.printStackTrace();
        }

        return isUpgradeSuccess;
    }

    private void updateUIAfterUpgrade(boolean result) {
        mProgressBarLL.setVisibility(View.GONE);
        mSelectFile.setEnabled(true);
        mUpgradeButton.setEnabled(true);
        if (result) {
            showToast(getString(R.string.firmware_upgrade_success));
            mDevice.reboot();
            drawFirmwareInfo(mDevice);
        } else {
            showToast(getString(R.string.firmware_upgrade_failed));
            Log.e(TAG, "onCreate: Upgrade Fimware failed.");
        }
    }

    private void showToast(String msg) {
        runOnUiThread(() -> Toast.makeText(FirmwareUpgradeActivity.this, msg, Toast.LENGTH_SHORT).show());
    }
}
