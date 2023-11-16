#include "NetworkConfig.h"

#include "utils/LocalUtils.h"

namespace obandroid {
jobject convert_j_NetworkConfig(JNIEnv *env, const OBNetIpConfig &config) {
    jclass clsNetConfig = env->FindClass("com/orbbec/obsensor/OBNetworkConfig");
    if (!clsNetConfig) {
        ob_throw_error(env, __func__, "find class failed");
    }
    jmethodID constructMethod = env->GetMethodID(clsNetConfig, "<init>", "()V");
    jobject jobjConfig = env->NewObject(clsNetConfig, constructMethod);
    if (!jobjConfig) {
        ob_throw_error(env, __func__, "Create Java OBNetworkConfig failed.");
    }

    jfieldID dhcpEnableField = env->GetFieldID(clsNetConfig, "dhcpEnable", "Z");
    env->SetBooleanField(jobjConfig, dhcpEnableField, (bool)config.dhcp);

    jint values[4];

    jfieldID ipValueField = env->GetFieldID(clsNetConfig, "ipValue", "[I");
    jintArray intArr = (jintArray)env->GetObjectField(jobjConfig, ipValueField);
    for (int i = 0; i < 4; i++) {
        values[i] = (int)config.address[i];
    }
    env->SetIntArrayRegion(intArr, 0, 4, values);

    jfieldID maskValueField = env->GetFieldID(clsNetConfig, "maskValue", "[I");
    intArr = (jintArray)env->GetObjectField(jobjConfig, maskValueField);
    for (int i = 0; i < 4; i++) {
        values[i] = (int)config.mask[i];
    }
    env->SetIntArrayRegion(intArr, 0, 4, values);

    jfieldID gatewayValueField = env->GetFieldID(clsNetConfig, "gatewayValue", "[I");
    intArr = (jintArray)env->GetObjectField(jobjConfig, gatewayValueField);
    for (int i = 0; i < 4; i++) {
        values[i] = (int)config.gateway[i];
    }
    env->SetIntArrayRegion(intArr, 0, 4, values);
    return jobjConfig;
}

OBNetIpConfig convert_c_NetworkConfig(JNIEnv *env, jobject jobjConfig) {
    OBNetIpConfig config;

    jclass clsNetConfig = env->FindClass("com/orbbec/obsensor/OBNetworkConfig");
    if (!clsNetConfig) {
        ob_throw_error(env, __func__, "find class failed");
    }

    jfieldID dhcpEnableField = env->GetFieldID(clsNetConfig, "dhcpEnable", "Z");
    config.dhcp = env->GetBooleanField(jobjConfig, dhcpEnableField);

    jint values[4];

    jfieldID ipValueField = env->GetFieldID(clsNetConfig, "ipValue", "[I");
    jintArray intArr = (jintArray)env->GetObjectField(jobjConfig, ipValueField);
    env->GetIntArrayRegion(intArr, 0, 4, values);
    for (int i = 0; i < 4; i++) {
        config.address[i] = (uint8_t)values[i];
    }

    jfieldID maskValueField = env->GetFieldID(clsNetConfig, "maskValue", "[I");
    intArr = (jintArray)env->GetObjectField(jobjConfig, maskValueField);
    env->GetIntArrayRegion(intArr, 0, 4, values);
    for (int i = 0; i < 4; i++) {
        config.mask[i] = (uint8_t)values[i];
    }

    jfieldID gatewayValueField = env->GetFieldID(clsNetConfig, "gatewayValue", "[I");
    intArr = (jintArray)env->GetObjectField(jobjConfig, gatewayValueField);
    env->GetIntArrayRegion(intArr, 0, 4, values);
    for (int i = 0; i < 4; i++) {
        config.gateway[i] = (uint8_t)values[i];
    }
    return config;
}

} // namespace obandroid
