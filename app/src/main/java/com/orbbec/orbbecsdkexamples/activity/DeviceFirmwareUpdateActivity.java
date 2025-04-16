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
import com.orbbec.obsensor.DeviceList;
import com.orbbec.obsensor.UpgradeCallback;
import com.orbbec.obsensor.types.DeviceInfo;
import com.orbbec.obsensor.types.UpgradeState;
import com.orbbec.orbbecsdkexamples.R;
import com.orbbec.orbbecsdkexamples.utils.FileUtils;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class DeviceFirmwareUpdateActivity extends BaseActivity {
    private static final String TAG = "DeviceFirmwareUpdateActivity";

    private Device mDevice;
    private Button mSelectFileBtn;
    private Button mUpdateBtn;
    private TextView mDeviceInfoTv;
    private LinearLayout mProgressBarLL;

    private String firmwarePath = "";
    private String versionRegex = "\\d+\\.\\d+\\.\\d+";
    private volatile boolean isUpdateSuccess = false;

    private DeviceChangedCallback mDeviceChangedCallback = new DeviceChangedCallback() {

        @Override
        public void onDeviceAttach(DeviceList deviceList) {
            try {
                if (mDevice == null) {
                    mDevice = deviceList.getDevice(0);

                    drawDeviceInfo(mDevice);
                }
            } catch (Exception e) {
                Log.e(TAG, "onDeviceAttach: " + e.getMessage());
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
                mDeviceInfoTv.post(() -> mDeviceInfoTv.setText(""));
            } catch (Exception e) {
                Log.e(TAG, "onDeviceDetach: " + e.getMessage());
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
        setTitle("Device-Firmware Update");
        setContentView(R.layout.activity_device_firmware_update);
        init();
    }

    @Override
    protected void onStart() {
        super.onStart();
        initSDK();
    }

    @Override
    protected void onStop() {
        try {
            if (mDevice != null) {
                mDevice.close();
                mDevice = null;
            }
        } catch (Exception e) {
            Log.e(TAG, "onStop: " + e.getMessage());
        }
        releaseSDK();
        super.onStop();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == 1 && resultCode == RESULT_OK && data != null) {
            Uri uri = data.getData();
            if (uri != null) {
                firmwarePath = FileUtils.convertUriToPath(this, uri);
                showToast("Selected file path: " + firmwarePath);
            }
        }
    }

    private void init() {
        mSelectFileBtn = findViewById(R.id.btn_select_file);
        mSelectFileBtn.setOnClickListener(v -> {
            Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
            intent.setType("*/*");
            intent.putExtra(Intent.EXTRA_MIME_TYPES, new String[]{"application/octet-stream"});
            startActivityForResult(intent, 1);
        });
        mUpdateBtn = findViewById(R.id.btn_firmware_update);
        mUpdateBtn.setOnClickListener(v -> {
            if (mDevice == null) {
                showToast(getString(R.string.device_not_connected));
                return;
            }
//            String firmwarePath = FileUtils.getExternalSaveDir() + "/firmware";
            if (firmwarePath.isEmpty()) {
                Log.w(TAG, "onCreate: No found firmware file");
                showToast(getString(R.string.no_file_select));
                return;
            }

            mProgressBarLL.setVisibility(View.VISIBLE);
            mSelectFileBtn.setEnabled(false);
            mUpdateBtn.setEnabled(false);
            Pattern pattern = Pattern.compile(versionRegex);
            Matcher matcher = pattern.matcher(firmwarePath);
            if (matcher.find()) {
                String newVersion = matcher.group();
                String oldVersion = mDevice.getInfo().getFirmwareVersion();

                if (newVersion.equals(oldVersion)) {
                    mProgressBarLL.setVisibility(View.GONE);
                    mSelectFileBtn.setEnabled(true);
                    mUpdateBtn.setEnabled(true);
                    showToast(getString(R.string.firmware_is_latest_version));
                    Log.i(TAG, "onCreate: The firmware is the latest version！");
                    return;
                }
            }

            new Thread(() -> {
                updateFirmware(firmwarePath);
                runOnUiThread(this::updateUI);
            }).start();
        });
        mDeviceInfoTv = findViewById(R.id.tv_device_info);
        mProgressBarLL = findViewById(R.id.ll_progress_bar);
    }

    private void drawDeviceInfo(Device device) {
        DeviceInfo deviceInfo = device.getInfo();
        if (deviceInfo != null) {
            StringBuilder sb = new StringBuilder();
            sb.append("Device name：").append(deviceInfo.getName()).append("\n");
            sb.append("Device pid：").append(String.format("0x%04X", deviceInfo.getPid())).append("\n");
            sb.append("Firmware version：").append(deviceInfo.getFirmwareVersion()).append("\n");
            sb.append("Serial number：").append(deviceInfo.getSerialNumber()).append("\n");
            runOnUiThread(() -> {
                mDeviceInfoTv.setText(sb.toString());
            });
        }
    }

    private void updateFirmware(String firmwarePath) {
        int index = firmwarePath.lastIndexOf(".img");
        boolean isImageFile = index != -1;
        index = firmwarePath.lastIndexOf(".bin");
        boolean isBinFile = index != -1;
        if (!(isImageFile || isBinFile)) {
            Log.e(TAG, "Firmware update failed. invalid firmware file: " + firmwarePath);
            runOnUiThread(() -> showToast(getString(R.string.not_find_firmware_file)));
            return;
        }

        try {
            mDevice.upgrade(firmwarePath, (state, percent, msg) -> {
//                Log.d(TAG, "onCallback: state=" + state + ", percent=" + percent + ", msg=" + msg);
                firmwareUpdateCallback(UpgradeState.get(state), msg, percent);
                if (state == UpgradeCallback.STAT_DONE) {
                    isUpdateSuccess = true;
                    mDevice.reboot();
                }
            });
        } catch (Exception e) {
            Log.e(TAG, "The update was interrupted! An error occurred!");
        }

    }

    private void firmwareUpdateCallback(UpgradeState state, String message, short percent) {
        Log.i(TAG, "Start firmware update");
        switch (state) {
            case STAT_VERIFY_SUCCESS:
                Log.i(TAG, "Progress: " + percent + "%; Status: Image file verification success");
                break;
            case STAT_FILE_TRANSFER:
                Log.i(TAG, "Progress: " + percent + "%; Status: File transfer in progress");
                break;
            case STAT_DONE:
                Log.i(TAG, "Progress: " + percent + "%; Status: Update completed");
                break;
            case STAT_IN_PROGRESS:
                Log.i(TAG, "Progress: " + percent + "%; Status: Upgrade in progress");
                break;
            case STAT_START:
                Log.i(TAG, "Progress: " + percent + "%; Status: Starting the upgrade");
                break;
            case STAT_VERIFY_IMAGE:
                Log.i(TAG, "Progress: " + percent + "%; Status: Verifying image file");
                break;
            default:
                Log.i(TAG, "Progress: " + percent + "%; Status: Unknown status or error");
                break;
        }
        if (state == UpgradeState.STAT_DONE) {
            Log.i(TAG, "Message : " + message);
        }
    }

    private void updateUI() {
        mProgressBarLL.setVisibility(View.GONE);
        mSelectFileBtn.setEnabled(true);
        mUpdateBtn.setEnabled(true);
        if (isUpdateSuccess) {
            showToast(getString(R.string.firmware_upgrade_success));
            isUpdateSuccess = false;
        } else {
            Log.e(TAG, "onCreate: Upgrade Firmware failed.");
            showToast(getString(R.string.firmware_upgrade_failed));
        }
    }

    private void showToast(String msg) {
        runOnUiThread(() -> Toast.makeText(DeviceFirmwareUpdateActivity.this, msg, Toast.LENGTH_SHORT).show());
    }
}
