package com.orbbec.orbbecsdkexamples.activity;

import static com.orbbec.obsensor.UpgradeCallback.STAT_DONE;
import static com.orbbec.obsensor.UpgradeCallback.STAT_DONE_WITH_DUPLICATES;

import android.content.ClipData;
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
import com.orbbec.obsensor.PresetList;
import com.orbbec.obsensor.types.DeviceInfo;
import com.orbbec.obsensor.types.UpgradeState;
import com.orbbec.orbbecsdkexamples.R;
import com.orbbec.orbbecsdkexamples.utils.FileUtils;

import java.util.Arrays;

public class DeviceOptionalDepthPresetsUpdateActivity extends BaseActivity {
    private static final String TAG = "DeviceOptionalDepthPresetsUpdateActivity";

    private Device mDevice;
    private Button mSelectFileBtn;
    private Button mUpdateBtn;
    private TextView mDeviceInfoTv;
    private TextView mPresetInfoTv;
    private LinearLayout mProgressBarLL;

    private String[] filePathList;

    private DeviceChangedCallback mDeviceChangedCallback = new DeviceChangedCallback() {
        @Override
        public void onDeviceAttach(DeviceList deviceList) {
            try {
                if (mDevice == null) {
                    mDevice = deviceList.getDevice(0);

                    drawDeviceInfo();
                    drawPresetInfo();
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
                runOnUiThread(() -> {
                    mDeviceInfoTv.setText("");
                    mPresetInfoTv.setText("");
                });
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
        setTitle("Device-Optional Depth Presets Update");
        setContentView(R.layout.activity_device_optional_depth_presets_update);
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

    private void init() {
        mSelectFileBtn = findViewById(R.id.btn_select_file);
        mSelectFileBtn.setOnClickListener(v -> {
            Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
            intent.setType("*/*");
            intent.putExtra(Intent.EXTRA_MIME_TYPES, new String[]{"application/octet-stream"});
            intent.putExtra(Intent.EXTRA_ALLOW_MULTIPLE, true);
            startActivityForResult(intent, 1);
        });
        mUpdateBtn = findViewById(R.id.btn_preset_update);
        mUpdateBtn.setOnClickListener(v -> {
            if (filePathList == null) {
                showToast(getString(R.string.no_file_select));
                return;
            }

            updateUI(false);

            new Thread(() -> {
                updatePreset(filePathList);
                runOnUiThread(() -> updateUI(true));
            }).start();
        });
        mDeviceInfoTv = findViewById(R.id.tv_device_info);
        mPresetInfoTv = findViewById(R.id.tv_preset_info);
        mProgressBarLL = findViewById(R.id.ll_progress_bar);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == 1 && resultCode == RESULT_OK && data != null) {
            ClipData clipData = data.getClipData();
            if (clipData != null) {
                filePathList = new String[clipData.getItemCount()];
                for (int i = 0, N = clipData.getItemCount(); i < N; i++) {
                    Uri uri = clipData.getItemAt(i).getUri();
                    if (uri != null) {
                        filePathList[i] = FileUtils.convertUriToPath(this, uri);
                    }
                }
            } else {
                filePathList = new String[1];
                Uri uri = data.getData();
                if (uri != null) {
                    filePathList[0] = FileUtils.convertUriToPath(this, uri);
                }
            }
            showToast("Selected file path: " + Arrays.toString(filePathList));
        }
    }

    private void drawDeviceInfo() {
        if (mDevice != null) {
            DeviceInfo deviceInfo = mDevice.getInfo();
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
    }

    private void drawPresetInfo() {
        if (mDevice != null) {
            PresetList presetList = mDevice.getAvailablePresetList();
            if (presetList != null) {
                StringBuilder sb = new StringBuilder();
                sb.append("Preset count: ").append(presetList.getCount()).append("\n");
                for (int i = 0, N = presetList.getCount(); i < N; i++) {
                    sb.append(" - ").append(presetList.getName(i)).append("\n");
                }
                sb.append("Current preset: ").append(mDevice.getCurrentPresetName()).append("\n");

                String key = "PresetVer";
                if (mDevice.isExtensionInfoExist(key)) {
                    sb.append("Preset version: ").append(mDevice.getExtensionInfo(key)).append("\n");
                }
                runOnUiThread(() -> {
                    mPresetInfoTv.setText("");
                    mPresetInfoTv.setText(sb.toString());
                });
            }
        }
    }

    private void updatePreset(String[] filePathList) {
        for (String filePath : filePathList) {
            if (!filePath.endsWith(".bin")) {
                Log.w(TAG, "updatePreset: " + filePath + " is not supported.");
                return;
            }
        }

        try {
            mDevice.updateOptionalDepthPresets(filePathList, filePathList.length, (state, percent, msg) -> {
//                Log.d(TAG, "onCallback: state=" + state + ", percent=" + percent + ", msg=" + msg);
                presetUpdateCallback(UpgradeState.get(state), msg, percent);
                if (state == STAT_DONE || state == STAT_DONE_WITH_DUPLICATES) {
                    showToast("Update preset success");
                    mDevice.reboot();
                }
            });
        } catch (Exception e) {
            Log.e(TAG, "The update was interrupted! An error occurred!");
        }
    }

    private void presetUpdateCallback(UpgradeState state, String message, short percent) {
        Log.i(TAG, "Start perset update");
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
            case STAT_DONE_WITH_DUPLICATES:
                Log.i(TAG, "Progress: " + percent + "%; Status: Update completed, duplicated presets have been ignored");
                break;
            case STAT_IN_PROGRESS:
                Log.i(TAG, "Progress: " + percent + "%; Status: Update in progress");
                break;
            case STAT_START:
                Log.i(TAG, "Progress: " + percent + "%; Status: Starting the update");
                break;
            case STAT_VERIFY_IMAGE:
                Log.i(TAG, "Progress: " + percent + "%; Status: Verifying image file");
                break;
            default:
                Log.i(TAG, "Progress: " + percent + "%; Status: Unknown status or error");
                break;
        }
        Log.i(TAG, "Message : " + message);
    }

    private void updateUI(boolean result) {
        mProgressBarLL.setVisibility(result ? View.GONE : View.VISIBLE);
        mSelectFileBtn.setEnabled(result);
        mUpdateBtn.setEnabled(result);
    }

    private void showToast(String msg) {
        runOnUiThread(() -> Toast.makeText(this, msg, Toast.LENGTH_SHORT).show());
    }
}
