#include "DeviceSyncConfig.h"
#include "../utils/LocalUtils.h"

namespace obandroid {
jobject convert_j_DeviceSyncConfig(JNIEnv *env,
                                   const OBDeviceSyncConfig &syncConfig) {
  jclass clsDeviceSyncConfig =
      env->FindClass("com/orbbec/obsensor/MultiDeviceSyncConfig");
  if (!clsDeviceSyncConfig) {
    obandroid::throw_error(env, "convert_j_DeviceSyncConfig",
                           "find class failed.");
  }
  jmethodID constructMethod =
      env->GetMethodID(clsDeviceSyncConfig, "<init>", "()V");
  jobject jobjDeviceSyncConfig =
      env->NewObject(clsDeviceSyncConfig, constructMethod);
  if (!jobjDeviceSyncConfig) {
    obandroid::throw_error(env, "convert_j_DeviceSyncConfig",
                           "Create Java DeviceSyncConfig failed.");
  }
  jfieldID syncModeValueField =
      env->GetFieldID(clsDeviceSyncConfig, "syncModeValue", "I");
  env->SetIntField(jobjDeviceSyncConfig, syncModeValueField,
                   (int)syncConfig.syncMode);

  jfieldID irTriggerSignalInDelayField =
      env->GetFieldID(clsDeviceSyncConfig, "irTriggerSignalInDelay", "I");
  env->SetIntField(jobjDeviceSyncConfig, irTriggerSignalInDelayField,
                   (int)syncConfig.irTriggerSignalInDelay);

  jfieldID rgbTriggerSignalInDelayField =
      env->GetFieldID(clsDeviceSyncConfig, "rgbTriggerSignalInDelay", "I");
  env->SetIntField(jobjDeviceSyncConfig, rgbTriggerSignalInDelayField,
                   (int)syncConfig.rgbTriggerSignalInDelay);

  jfieldID deviceTriggerSignalOutDelayField =
      env->GetFieldID(clsDeviceSyncConfig, "deviceTriggerSignalOutDelay", "I");
  env->SetIntField(jobjDeviceSyncConfig, deviceTriggerSignalOutDelayField,
                   (int)syncConfig.deviceTriggerSignalOutDelay);

  jfieldID deviceTriggerSignalOutPolarityField = env->GetFieldID(
      clsDeviceSyncConfig, "deviceTriggerSignalOutPolarity", "I");
  env->SetIntField(jobjDeviceSyncConfig, deviceTriggerSignalOutPolarityField,
                   (int)syncConfig.deviceTriggerSignalOutPolarity);

  jfieldID mcuTriggerFrequencyField =
      env->GetFieldID(clsDeviceSyncConfig, "mcuTriggerFrequency", "I");
  env->SetIntField(jobjDeviceSyncConfig, mcuTriggerFrequencyField,
                   (int)syncConfig.mcuTriggerFrequency);

  jfieldID deviceIdField =
      env->GetFieldID(clsDeviceSyncConfig, "deviceId", "I");
  env->SetIntField(jobjDeviceSyncConfig, deviceIdField,
                   (int)syncConfig.deviceId);

  return jobjDeviceSyncConfig;
}

OBDeviceSyncConfig convert_c_DeviceSyncConfig(JNIEnv *env,
                                              jobject jDeviceConfig) {
  jclass clsDeviceSyncConfig =
      env->FindClass("com/orbbec/obsensor/MultiDeviceSyncConfig");
  OBDeviceSyncConfig syncConfig;
  if (!clsDeviceSyncConfig) {
    obandroid::throw_error(env, "convert_c_DeviceSyncConfig",
                           "find class failed.");
  }

  jfieldID syncModeValueField =
      env->GetFieldID(clsDeviceSyncConfig, "syncModeValue", "I");
  syncConfig.syncMode =
      (OBSyncMode)env->GetIntField(jDeviceConfig, syncModeValueField);

  jfieldID irTriggerSignalInDelayField =
      env->GetFieldID(clsDeviceSyncConfig, "irTriggerSignalInDelay", "I");
  syncConfig.irTriggerSignalInDelay =
      env->GetIntField(jDeviceConfig, irTriggerSignalInDelayField);

  jfieldID rgbTriggerSignalInDelayField =
      env->GetFieldID(clsDeviceSyncConfig, "rgbTriggerSignalInDelay", "I");
  syncConfig.rgbTriggerSignalInDelay =
      env->GetIntField(jDeviceConfig, rgbTriggerSignalInDelayField);

  jfieldID deviceTriggerSignalOutDelayField =
      env->GetFieldID(clsDeviceSyncConfig, "deviceTriggerSignalOutDelay", "I");
  syncConfig.deviceTriggerSignalOutDelay =
      env->GetIntField(jDeviceConfig, deviceTriggerSignalOutDelayField);

  jfieldID deviceTriggerSignalOutPolarityField = env->GetFieldID(
      clsDeviceSyncConfig, "deviceTriggerSignalOutPolarity", "I");
  syncConfig.deviceTriggerSignalOutPolarity =
      env->GetIntField(jDeviceConfig, deviceTriggerSignalOutPolarityField);

  jfieldID mcuTriggerFrequencyField =
      env->GetFieldID(clsDeviceSyncConfig, "mcuTriggerFrequency", "I");
  syncConfig.mcuTriggerFrequency =
      env->GetIntField(jDeviceConfig, mcuTriggerFrequencyField);

  jfieldID deviceIdField =
      env->GetFieldID(clsDeviceSyncConfig, "deviceId", "I");
  syncConfig.deviceId = env->GetIntField(jDeviceConfig, deviceIdField);

  return syncConfig;
}
} // namespace obandroid
