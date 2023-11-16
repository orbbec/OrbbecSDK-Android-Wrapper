#include "MultiDeviceSyncConfig.h"
#include "../utils/LocalUtils.h"

namespace obandroid {
jobject convert_j_MultiDeviceSyncConfig(JNIEnv *env,
                                   const OBMultiDeviceSyncConfig &syncConfig) {
  jclass clsDeviceSyncConfig =
      env->FindClass("com/orbbec/obsensor/MultiDeviceSyncConfig");
  if (!clsDeviceSyncConfig) {
    ob_throw_error(env, __func__,
                           "find class failed.");
  }
  jmethodID constructMethod =
      env->GetMethodID(clsDeviceSyncConfig, "<init>", "()V");
  jobject jobjDeviceSyncConfig =
      env->NewObject(clsDeviceSyncConfig, constructMethod);
  if (!jobjDeviceSyncConfig) {
    ob_throw_error(env, __func__,
                           "Create Java DeviceSyncConfig failed.");
  }
  jfieldID syncModeValueField =
      env->GetFieldID(clsDeviceSyncConfig, "syncModeValue", "I");
  env->SetIntField(jobjDeviceSyncConfig, syncModeValueField,
                   (int)syncConfig.syncMode);

  jfieldID depthDelayUsField =
      env->GetFieldID(clsDeviceSyncConfig, "depthDelayUs", "I");
  env->SetIntField(jobjDeviceSyncConfig, depthDelayUsField,
                   syncConfig.depthDelayUs);

  jfieldID colorDelayUsField =
      env->GetFieldID(clsDeviceSyncConfig, "colorDelayUs", "I");
  env->SetIntField(jobjDeviceSyncConfig, colorDelayUsField,
                   syncConfig.colorDelayUs);

  jfieldID trigger2ImageDelayUsField =
      env->GetFieldID(clsDeviceSyncConfig, "trigger2ImageDelayUs", "I");
  env->SetIntField(jobjDeviceSyncConfig, trigger2ImageDelayUsField,
                   syncConfig.trigger2ImageDelayUs);

  jfieldID triggerOutEnableField = env->GetFieldID(
      clsDeviceSyncConfig, "triggerOutEnable", "Z");
  env->SetBooleanField(jobjDeviceSyncConfig, triggerOutEnableField,
                   syncConfig.triggerOutEnable);

  jfieldID triggerOutDelayUsField =
      env->GetFieldID(clsDeviceSyncConfig, "triggerOutDelayUs", "I");
  env->SetIntField(jobjDeviceSyncConfig, triggerOutDelayUsField,
                   syncConfig.triggerOutDelayUs);

  jfieldID framesPerTriggerField =
      env->GetFieldID(clsDeviceSyncConfig, "framesPerTrigger", "I");
  env->SetIntField(jobjDeviceSyncConfig, framesPerTriggerField,
                   syncConfig.framesPerTrigger);

  return jobjDeviceSyncConfig;
}

OBMultiDeviceSyncConfig convert_c_MultiDeviceSyncConfig(JNIEnv *env,
                                              jobject jDeviceConfig) {
  jclass clsDeviceSyncConfig =
      env->FindClass("com/orbbec/obsensor/MultiDeviceSyncConfig");
  OBMultiDeviceSyncConfig syncConfig;
  if (!clsDeviceSyncConfig) {
    ob_throw_error(env, "convert_c_DeviceSyncConfig",
                           "find class failed.");
  }

  jfieldID syncModeValueField =
      env->GetFieldID(clsDeviceSyncConfig, "syncModeValue", "I");
  syncConfig.syncMode =
      (OBMultiDeviceSyncMode)env->GetIntField(jDeviceConfig, syncModeValueField);

  jfieldID depthDelayUsField =
      env->GetFieldID(clsDeviceSyncConfig, "depthDelayUs", "I");
  syncConfig.depthDelayUs =
      env->GetIntField(jDeviceConfig, depthDelayUsField);

  jfieldID colorDelayUsField =
      env->GetFieldID(clsDeviceSyncConfig, "colorDelayUs", "I");
  syncConfig.colorDelayUs =
      env->GetIntField(jDeviceConfig, colorDelayUsField);

  jfieldID trigger2ImageDelayUsField =
      env->GetFieldID(clsDeviceSyncConfig, "trigger2ImageDelayUs", "I");
  syncConfig.trigger2ImageDelayUs =
      env->GetIntField(jDeviceConfig, trigger2ImageDelayUsField);

  jfieldID triggerOutEnableField = env->GetFieldID(
      clsDeviceSyncConfig, "triggerOutEnable", "Z");
  syncConfig.triggerOutEnable =
      env->GetBooleanField(jDeviceConfig, triggerOutEnableField);

  jfieldID triggerOutDelayUsField =
      env->GetFieldID(clsDeviceSyncConfig, "triggerOutDelayUs", "I");
  syncConfig.triggerOutDelayUs =
      env->GetIntField(jDeviceConfig, triggerOutDelayUsField);

  jfieldID framesPerTriggerField =
      env->GetFieldID(clsDeviceSyncConfig, "framesPerTrigger", "I");
  syncConfig.framesPerTrigger = env->GetIntField(jDeviceConfig, framesPerTriggerField);

  return syncConfig;
}
} // namespace obandroid
