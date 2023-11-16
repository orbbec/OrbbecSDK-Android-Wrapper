#include "TimestampResetConfig.h"
#include "../utils/LocalUtils.h"


namespace obandroid {

jobject convert_j_TimestampResetConfig(JNIEnv *env, const OBDeviceTimestampResetConfig &config) {
    jclass clsTimestampResetConfig = env->FindClass("com/orbbec/obsensor/TimestampResetConfig");
    if (!clsTimestampResetConfig) {
        ob_throw_error(env, __func__, "find class failed");
    }
    jmethodID constructMethod = env->GetMethodID(clsTimestampResetConfig, "<init>", "()V");
    jobject jobjConfig = env->NewObject(clsTimestampResetConfig, constructMethod);
    if (!jobjConfig) {
        ob_throw_error(env, __func__, "Create Java TimestampResetConfig failed.");
    }

    jfieldID enableField = env->GetFieldID(clsTimestampResetConfig, "enable", "Z");
    env->SetBooleanField(jobjConfig, enableField, config.enable);

    jfieldID timestampResetDelayUsField = env->GetFieldID(clsTimestampResetConfig, "timestampResetDelayUs", "I");
    env->SetIntField(jobjConfig, timestampResetDelayUsField, config.timestamp_reset_delay_us);

    jfieldID timestampResetSignalOutputEnableField = env->GetFieldID(clsTimestampResetConfig, "timestampResetSignalOutputEnable", "Z");
    env->SetBooleanField(jobjConfig, timestampResetSignalOutputEnableField, config.timestamp_reset_signal_output_enable);
    return jobjConfig;
}

OBDeviceTimestampResetConfig convert_c_TimestampResetConfig(JNIEnv *env, jobject jobjConfig) {
    jclass clsTimestampResetConfig = env->FindClass("com/orbbec/obsensor/TimestampResetConfig");
    if (!clsTimestampResetConfig) {
        ob_throw_error(env, __func__, "find class failed");
    }
    OBDeviceTimestampResetConfig config;

    jfieldID enableField = env->GetFieldID(clsTimestampResetConfig, "enable", "Z");
    config.enable = env->GetBooleanField(jobjConfig, enableField);

    jfieldID timestampResetDelayUsField = env->GetFieldID(clsTimestampResetConfig, "timestampResetDelayUs", "I");
    config.timestamp_reset_delay_us = env->GetIntField(jobjConfig, timestampResetDelayUsField);

    jfieldID timestampResetSignalOutputEnableField = env->GetFieldID(clsTimestampResetConfig, "timestampResetSignalOutputEnable", "Z");
    config.timestamp_reset_signal_output_enable = env->GetBooleanField(jobjConfig, timestampResetSignalOutputEnableField);
    return config;
}

} // namespace obandroid
